import { httpClient } from './http-client';
import { serviceEndpoints } from './endpoints';


export function getProviderOnboardingStatusApi() {
  return httpClient.get(`${serviceEndpoints.providerService}/v1/getProviderOnboardingStatus`);
}

export function getProviderByStatusApi(status: boolean) {
  return httpClient.get(`${serviceEndpoints.providerService}/v1/getProviderByStatus?status=${status}`);
}
