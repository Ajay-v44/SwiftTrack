import requests
from typing import Any, Dict, Optional

class APIClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip('/')

    def get(self, endpoint: str, params: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> Any:
        url = f"{self.base_url}/{endpoint.lstrip('/')}"
        try:
            response = requests.get(url, params=params, headers=headers)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            return {"error": str(e), "details": str(e.response.text) if e.response else "No response"}

    def post(self, endpoint: str, data: Optional[Dict[str, Any]] = None, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> Any:
        url = f"{self.base_url}/{endpoint.lstrip('/')}"
        try:
            response = requests.post(url, data=data, json=json, headers=headers)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            return {"error": str(e), "details": str(e.response.text) if e.response else "No response"}

    def put(self, endpoint: str, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None) -> Any:
        url = f"{self.base_url}/{endpoint.lstrip('/')}"
        try:
            response = requests.put(url, json=json, headers=headers)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            return {"error": str(e), "details": str(e.response.text) if e.response else "No response"}

    def delete(self, endpoint: str, headers: Optional[Dict[str, str]] = None) -> Any:
        url = f"{self.base_url}/{endpoint.lstrip('/')}"
        try:
            response = requests.delete(url, headers=headers)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            return {"error": str(e), "details": str(e.response.text) if e.response else "No response"}
