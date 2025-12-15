from fastapi import APIRouter, HTTPException
from app.schemas import PredictionRequest, PredictionResponse
from app.predictor import predict_assignments
from app.config import settings

router = APIRouter()

@router.post("/predict/assignment", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """
    Predicts the success probability for a list of providers 
    based on current context (traffic, distance, etc).
    """
    try:
        return predict_assignments(request)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health")
def health_check():
    return {"status": "ok", "version": settings.PROJECT_VERSION}
