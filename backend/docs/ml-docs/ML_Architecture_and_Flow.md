# SwiftTrack ML Service Architecture & Workflow

## 1. Executive Summary

The **SwiftTrack ML Assignment Service** is a specialized, stateless microservice responsible for predicting the "Success Probability" of assigning a delivery order to a specific provider. 

Unlike traditional "monolithic ML" where models are loaded inside the main backend, this service runs as a standalone **FastAPI** container, ensuring:
*   **Separation of Concerns**: Java services handle business logic; Python handles inference.
*   **Independent Scaling**: The ML service can be scaled horizontally on GPU/CPU nodes independently of the core backend.
*   **Automated Lifecycle**: Models are retrained, versioned, and deployed automatically without manual intervention.

---

## 2. System Architecture

```mermaid
graph LR
    A[Order Service] -- POST /predict --> B(ML Microservice)
    B -- Load --> C[Model Artifact (.pkl)]
    D[PostgreSQL DB] -- Training Data --> E[GitHub Actions CI]
    E -- 1. Train & Save --> C
    E -- 2. Build Docker --> F{GHCR}
    E -- 3. Push Image --> F
    B -- Pull Image --> F
```

### Core Components
| Component | Technology | Responsibility |
| :--- | :--- | :--- |
| **Inference Engine** | FastAPI (Python) | Serving real-time predictions via REST API. |
| **Training Pipeline** | Scikit-Learn | Fetching data, preprocessing, and training Random Forest models. |
| **Model Registry** | GitHub Container Registry (GHCR) | Storing the *entire Docker image* as the versioned model artifact. |
| **Orchestrator** | GitHub Actions | Managing the daily retraining schedule and build process. |

---

## 3. The "Immutable Model" Lifecycle

We utilize an **Immutable Artifact** pattern. We do *not* download models at runtime from S3/Blob Storage. Instead, the model is "baked" into the Docker image.

### 🔄 The Daily Workflow

1.  **Trigger (12:00 AM)**: GitHub Actions wakes up via cron schedule.
2.  **Data Ingestion**:
    *   The pipeline connects to the Production Database (`Order` & `OrderAiFeatures` tables).
    *   It executes a JOIN query to fetch real-world performance data.
    *   *Fallback*: If the database is unreachable or has insufficient data (<100 rows), it generates **Synthetic Data** based on realistic statistical distributions.
3.  **Training**:
    *   A **Random Forest Classifier** is trained on the data.
    *   The model predicts `success_probability` based on: `distance_km`, `traffic_level`, `is_peak_hour`, `provider_load`.
    *   Output: `assignment_model.pkl`.
4.  **Packaging**:
    *   A `docker build` command is executed.
    *   The `Dockerfile` performs `COPY model/assignment_model.pkl .`, permanently freezing the model version inside the container.
5.  **Publishing**:
    *   The image is tagged with the build ID (e.g., `1.0.125`) and `latest`.
    *   Pushed to **GitHub Container Registry (GHCR)**.

---

## 4. Deployment & Updates

### 🌍 Cloud / Production Container
The production environment uses the **Pull-to-Update** strategy.
*   **Startup**: The container pulls the `latest` image from GHCR.
*   **Model Loading**: It loads the `.pkl` file located locally *inside* the container (`/app/model/`).
*   **Zero External Deps**: It does *not* need to connect to a model store or training DB at runtime.

### 💻 Local Development
*   **Hybrid Mode**: Developers can run `python -m training.train` locally to generate a dev model.
*   **Volume Mounts**: In `docker-compose.yml`, the code directory is mounted, but the `model/` directory is **excluded** from the mount in production configurations. This ensures the container uses the official nightly model, not a stale local file.

---

## 5. Technical Specifications

### Model Inputs (Features)
| Feature | Type | Description |
| :--- | :--- | :--- |
| `provider` | Categorical | The provider ID (e.g., UBER, RAPIDO). One-Hot Encoded. |
| `distance_km` | Float | Trip distance. Standard Scaled. |
| `traffic_level` | Int (1-5) | Real-time traffic congestion metric. |
| `is_peak_hour` | Boolean | Whether the order is during peak demand windows. |
| `provider_load` | Int (1-3) | Current load factor of the provider network. |

### API Contract
**Endpoint**: `POST /predict/assignment`

**Request**:
```json
{
  "providers": [
    {
      "provider": "UBER",
      "distance_km": 5.5,
      "traffic_level": 4,
      "is_peak_hour": true,
      "provider_load": 2
    }
  ]
}
```

**Response**:
```json
{
  "predictions": [
    {
      "provider": "UBER",
      "success_probability": 0.875
    }
  ],
  "model_version": "1.0.45"
}
```

---

## 6. How to Update

To force a manual update of the model outside the daily schedule:

1.  Go to the **Actions** tab in GitHub.
2.  Select the **Train and Publish** workflow.
3.  Click **Run workflow**.
4.  Once finished, run the following on your server:
    ```bash
    docker-compose pull ml-assignment
    docker-compose up -d ml-assignment
    ```
