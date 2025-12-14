import joblib
import os
import sys
from app.config import settings

def load_model():
    """
    Loads the trained ML model from the disk.
    If the model is missing, it raises a critical error, preventing startup.
    This ensures we never run an 'empty' inference service.
    """
    path = settings.MODEL_PATH
    if not os.path.exists(path):
        print(f"CRITICAL: Model not found at {path}. startup aborted.")
        # In a real scenario, we might want to fail hard or fallback. 
        # For this design, we fail hard to ensure correctness.
        # However, for local dev without a model, we might handle gracefully or provide a dummy.
        # We will assume build process places model there.
        raise FileNotFoundError(f"Model not found at {path}")
    
    print(f"Loading model from {path}...")
    
    # Debug: Print model timestamp to verify freshness
    try:
        mod_time = os.path.getmtime(path)
        from datetime import datetime
        print(f"Model build timestamp: {datetime.fromtimestamp(mod_time)}")
    except:
        pass

    model = joblib.load(path)
    print("Model loaded successfully.")
    return model

# Global model instance
model = None
try:
    model = load_model()
except FileNotFoundError:
    print("Model file missing. Service will start but inference will fail until model is present.")
