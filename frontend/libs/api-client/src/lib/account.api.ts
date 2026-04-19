import { httpClient } from './http-client';
import { serviceEndpoints } from './endpoints';

export function getAccountSummaryApi() {
  return httpClient.get(`${serviceEndpoints.billingAccounts}/v1/dashboard/summary`);
}

export function getMyAccountApi() {
  return httpClient.get(`${serviceEndpoints.billingAccounts}/v1/getMyAccount`);
}

export function getTransactionsApi(page = 0, size = 10) {
  return httpClient.get(`${serviceEndpoints.billingAccounts}/v1/getTransactions?page=${page}&size=${size}`);
}

export function getBankDetailsApi() {
  return httpClient.get(`${serviceEndpoints.billingBase}/billing/v1/driver-bank-details`);
}

export function saveBankDetailsApi(data: {
  accountNumber: string;
  ifscCode: string;
  upiId: string;
  accountHolderName: string;
  bankName: string;
}) {
  return httpClient.post(`${serviceEndpoints.billingBase}/billing/v1/driver-bank-details`, data);
}

export function initiateSettlementApi(accountId: string, amount: number) {
  return httpClient.post(`${serviceEndpoints.billingBase}/settlements/initiate?accountId=${accountId}&amount=${amount}`);
}

export function getSettlementsByAccountApi(accountId: string) {
  return httpClient.get(`${serviceEndpoints.billingBase}/settlements/account/${accountId}`);
}

