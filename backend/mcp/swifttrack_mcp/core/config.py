import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8080")
    
    # Service specific bases if not going through gateway, currently assuming gateway
    PROVIDER_SERVICE_URL = f"{GATEWAY_URL}/providerservice"
    AUTH_SERVICE_URL = f"{GATEWAY_URL}/authservice"
    MAP_SERVICE_URL = f"{GATEWAY_URL}/mapservice" 
    ORDER_SERVICE_URL = f"{GATEWAY_URL}/orderservice"

config = Config()
