import pandas as pd
import numpy as np

def generate_synthetic_data(n_samples=10000):
    """
    Generates synthetic training data for provider assignment.
    Simulates realistic patterns:
    - High traffic -> Lower success
    - Long distance -> Lower success
    - Peak hour -> Lower success
    """
    np.random.seed(42)

    providers = ['UBER', 'LYFT', 'RAPIDO', 'SHADOWFAX', 'DUNZO']
    
    data = {
        'provider': np.random.choice(providers, n_samples),
        'distance_km': np.random.exponential(scale=5.0, size=n_samples), # Most trips short
        'traffic_level': np.random.randint(1, 6, n_samples), # 1-5 scale
        'is_peak_hour': np.random.choice([0, 1], n_samples, p=[0.7, 0.3]),
        'provider_load': np.random.randint(1, 4, n_samples) # 1-3 (Low, Med, High)
    }

    df = pd.DataFrame(data)

    # Logic for ground truth (target variable: 'success')
    # Start with base probability
    success_prob = 0.8 

    # Penalties
    success_prob -= (df['distance_km'] / 20.0) * 0.3  # Longer distance = harder
    success_prob -= (df['traffic_level'] / 5.0) * 0.2 # More traffic = harder
    success_prob -= df['is_peak_hour'] * 0.15         # Peak hour = harder
    success_prob -= (df['provider_load'] / 3.0) * 0.1 # High load = harder

    # Clip probabilities
    success_prob = np.clip(success_prob, 0.1, 0.95)

    # Generate labels
    df['success'] = np.random.binomial(1, success_prob)

    return df

if __name__ == '__main__':
    df = generate_synthetic_data()
    print(df.head())
    df.to_csv('training/synthetic_data.csv', index=False)
