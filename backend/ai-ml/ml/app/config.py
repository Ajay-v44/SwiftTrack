import os

class Settings:
    PROJECT_NAME: str = "SwiftTrack ML Assignment Service"
    PROJECT_VERSION: str = os.getenv("MODEL_VERSION", "1.0.0-dev")
    MODEL_PATH: str = os.getenv("MODEL_PATH", "model/assignment_model.pkl")
    CONFIDENCE_THRESHOLD: float = 0.6
    
    # Database Config
    DB_USER: str = os.getenv("DB_USERNAME", "postgres")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "postgres")
    DB_HOST: str = os.getenv("DB_HOST", "localhost")
    DB_PORT: str = os.getenv("DB_PORT", "5432")
    DB_NAME: str = os.getenv("DB_NAME", "Order")
    
    @property
    def DATABASE_URL(self) -> str:
        return f"postgresql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

settings = Settings()
