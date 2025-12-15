import pandas as pd
import joblib
import os
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import Pipeline
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler, FunctionTransformer
from sklearn.metrics import accuracy_score, classification_report

try:
    from .data_generator import generate_synthetic_data
except ImportError:
    from data_generator import generate_synthetic_data


from sqlalchemy import create_engine, text
from app.config import settings

def fetch_data_from_db():
    """
    Attempts to fetch training data from the PostgreSQL database.
    Returns a DataFrame or None if connection fails or insufficient data.
    """
    try:
        print(f"Connecting to database: {settings.DB_HOST}...")
        engine = create_engine(settings.DATABASE_URL)
        
        # We need to join with 'orders' table to get the 'provider' (selected_provider_code)
        # Assuming order_ai_features.order_id matches orders.id
        query = """
            SELECT 
                o.selected_provider_code as provider,
                f.distance_km,
                f.traffic_level,
                f.is_peak_hour,
                f.provider_load,
                f.success
            FROM order_ai_features f
            JOIN orders o ON f.order_id = o.id
            WHERE f.success IS NOT NULL
              AND o.selected_provider_code IS NOT NULL
        """
        
        df = pd.read_sql(query, engine)
        
        if len(df) < 100:
            print(f"Warning: Only {len(df)} rows found in DB. Insufficient for training.")
            return None
            
        print(f"Successfully loaded {len(df)} rows from database.")
        
        # Ensure data types match synthetic data
        df['distance_km'] = df['distance_km'].astype(float)
        df['traffic_level'] = df['traffic_level'].astype(int)
        df['provider_load'] = df['provider_load'].astype(int)
        # Handle boolean or int for is_peak_hour/success depending on DB driver
        df['is_peak_hour'] = df['is_peak_hour'].astype(int) 
        df['success'] = df['success'].astype(int)
        
        return df
        
    except Exception as e:
        print(f"Database connection failed: {e}")
        return None

def train():
    # 1. Try fetching real data
    print("Attempting to load real training data...")
    df = fetch_data_from_db()

    # 2. Fallback to synthetic data
    if df is None:
        print("Falling back to synthetic data generation...")
        df = generate_synthetic_data(n_samples=20000)
    else:
        print("Using real data for training.")
    
    # 3. Check for specific provider column mapping
    # If the DB returns 'selected_provider_code', rename it to 'provider' to match pipeline
    if 'selected_provider_code' in df.columns:
        df.rename(columns={'selected_provider_code': 'provider'}, inplace=True)

    X = df[['provider', 'distance_km', 'traffic_level', 'is_peak_hour', 'provider_load']]
    y = df['success']

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # Preprocessing Pipeline
    # Categorical features -> OneHot
    # Numerical features -> Scale
    # Boolean features -> Pass through (or act as numeric)

    categorical_features = ['provider']
    numerical_features = ['distance_km', 'traffic_level', 'provider_load']
    boolean_features = ['is_peak_hour']

    preprocessor = ColumnTransformer(
        transformers=[
            ('cat', OneHotEncoder(handle_unknown='ignore'), categorical_features),
            ('num', StandardScaler(), numerical_features),
            ('bool', 'passthrough', boolean_features) 
        ]
    )

    # Model Pipeline
    clf = Pipeline(steps=[
        ('preprocessor', preprocessor),
        ('classifier', RandomForestClassifier(n_estimators=100, random_state=42))
    ])

    print("Training model...")
    clf.fit(X_train, y_train)

    print("Evaluating model...")
    y_pred = clf.predict(X_test)
    acc = accuracy_score(y_test, y_pred)
    print(f"Accuracy: {acc:.4f}")
    print(classification_report(y_test, y_pred))

    # Save Model
    # Ensure model directory exists
    os.makedirs('model', exist_ok=True)
    model_path = 'model/assignment_model.pkl'
    
    print(f"Saving model to {model_path}...")
    joblib.dump(clf, model_path)
    print("Training complete.")

if __name__ == "__main__":
    train()
