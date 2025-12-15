# SwiftTrack ML Assignment Service

An Enterprise-grade ML microservice for predicting Provider Assignment success probabilities.

## 🏗 Architecture

This service follows a strict **Training-Inference Separation** pattern:

1.  **Training (Offline/Batch)**:
    *   Executed via GitHub Actions (CI/CD).
    *   Generates synthetic data (simulating production logs).
    *   Trains a Random Forest Classifier.
    *   Persists the model artifact (`.pkl`).
    *   Builds an immutable Docker container with the model baked in.
    *   Publishes to GitHub Container Registry (GHCR).

2.  **Inference (Online/Real-time)**:
    *   FastAPI-based REST service.
    *   Stateless and Database-free.
    *   Loads the model into memory at startup.
    *   Provides sub-millisecond predictions.

## 🚀 Getting Started

### Prerequisites
*   Docker
*   Python 3.10+

### Local Development

1.  **Install Dependencies**:
    ```bash
    pip install -r requirements.txt
    ```

2.  **Train the Model Locally**:
    ```bash
    # Run from the root of the service directory
    python -m training.train
    ```
    This will create `model/assignment_model.pkl`.

3.  **Run the Service**:
    ```bash
    uvicorn app.main:app --reload
    ```
    Access API docs at: `http://localhost:8000/docs`

### Docker Usage

1.  **Build Image**:
    (Ensure you have run training first so `model/` exists)
    ```bash
    docker build -t swifttrack-ml-assignment .
    ```

2.  **Run Container**:
    ```bash
    docker run -p 8000:8000 swifttrack-ml-assignment
    ```

## 🤖 API Usage

**Endpoint**: `POST /predict/assignment`

**Request Example**:
```json
{
  "providers": [
    {
      "provider": "UBER",
      "distance_km": 5.2,
      "traffic_level": 3,
      "is_peak_hour": true,
      "provider_load": 1
    }
  ]
}
```

**Response Example**:
```json
{
  "predictions": [
    {
      "provider": "UBER",
      "success_probability": 0.85
    }
  ],
  "model_version": "1.0.0"
}
```

## 🔄 Automation (CI/CD)

The `.github/workflows/train-and-publish.yml` workflow:
1.  Triggers daily at 12:00 AM.
2.  Retrains the model on fresh (synthetic) data.
3.  Builds a new Docker image tag (`1.0.<RUN_NUMBER>`).
4.  Pushes to GHCR.

To rollback, simply deploy a previous image tag.
