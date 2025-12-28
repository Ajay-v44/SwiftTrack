import pandas as pd
from app.model_loader import model
from app.schemas import PredictionRequest, PredictionResponse, ProviderPrediction
from app.config import settings

def predict_assignments(request: PredictionRequest) -> PredictionResponse:
    if model is None:
        raise RuntimeError("Model is not loaded")

    # Convert request to DataFrame
    data = [p.dict() for p in request.providers]
    df = pd.DataFrame(data)

    # Preprocessing (ensure columns match training)
    # Note: In a real system, we'd share a FeatureTransformer between training and inference.
    # Here we assume the model pipeline handles transformation or we do basic prep.
    # The pipeline expected features: ['distance_km', 'traffic_level', 'is_peak_hour', 'provider_load', 'provider']
    
    # Predict probabilities
    # model.predict_proba returns [prob_fail, prob_success]
    try:
        probabilities = model.predict_proba(df)[:, 1]
    except Exception as e:
        # Fallback or error handling
        print(f"Prediction error: {e}")
        raise e

    predictions = []
    for i, prob in enumerate(probabilities):
        # Round for cleaner output
        score = float(round(prob, 4))
        
        predictions.append(ProviderPrediction(
            provider=request.providers[i].provider,
            success_probability=score
        ))

    return PredictionResponse(
        predictions=predictions,
        model_version=settings.PROJECT_VERSION
    )
