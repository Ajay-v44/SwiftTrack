from pydantic import BaseModel
from typing import List, Optional

class ProviderFeatures(BaseModel):
    provider: str
    distance_km: float
    traffic_level: int
    is_peak_hour: bool
    provider_load: int

class PredictionRequest(BaseModel):
    providers: List[ProviderFeatures]

class ProviderPrediction(BaseModel):
    provider: str
    success_probability: float

class PredictionResponse(BaseModel):
    predictions: List[ProviderPrediction]
    model_version: str
