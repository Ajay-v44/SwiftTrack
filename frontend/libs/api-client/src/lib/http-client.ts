import axios from "axios"
import type { AxiosError, InternalAxiosRequestConfig } from "axios"
import { gatewayBaseUrl } from "./endpoints"

export const httpClient = axios.create({
  baseURL: gatewayBaseUrl,
  headers: {
    "Content-Type": "application/json",
  },
})

const AUTH_STORAGE_KEY = "swifttrack-auth"
const LOGIN_PATH = "/login"

function isBrowser() {
  return typeof window !== "undefined"
}

function getCookieValue(name: string) {
  if (!isBrowser()) {
    return undefined
  }

  const prefix = `${name}=`
  const cookie = document.cookie
    .split(";")
    .map((entry) => entry.trim())
    .find((entry) => entry.startsWith(prefix))

  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : undefined
}

export function getAuthTokenFromBrowser() {
  return getCookieValue("auth_token")
}

function removeCookie(name: string) {
  if (!isBrowser()) {
    return
  }

  document.cookie = `${name}=; Max-Age=0; path=/`
}

function isLoginRequest(config?: InternalAxiosRequestConfig) {
  const url = config?.url ?? ""
  return (
    url.includes("/v1/login/emailAndPassword") ||
    url.includes("/v1/login/mobileNumAndOtp")
  )
}

function isProtectedRequest(config?: InternalAxiosRequestConfig) {
  return Boolean(config) && !isLoginRequest(config)
}

function clearPersistedAuth() {
  removeCookie("auth_token")

  if (!isBrowser()) {
    return
  }

  window.localStorage.removeItem(AUTH_STORAGE_KEY)
}

function redirectToLogin() {
  if (!isBrowser() || window.location.pathname === LOGIN_PATH) {
    return
  }

  window.location.replace(LOGIN_PATH)
}

function logoutUser() {
  clearPersistedAuth()
  redirectToLogin()
}

httpClient.interceptors.request.use(
  (config) => {
    const token = getCookieValue("auth_token")

    if (!token && isProtectedRequest(config)) {
      logoutUser()
      return Promise.reject(new Error("Missing auth token"))
    }

    if (token) {
      config.headers = config.headers ?? {}
      config.headers["token"] = token
    }

    return config
  },
  (error) => Promise.reject(error)
)

httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status

    if ((status === 401 || status === 403) && isProtectedRequest(error.config)) {
      logoutUser()
    }

    // Safety net: catch auth failures that downstream services accidentally return as 500
    if (status === 500 && isProtectedRequest(error.config)) {
      const data = error.response?.data as Record<string, unknown> | undefined
      const message =
        typeof data?.["message"] === "string" ? (data["message"] as string).toLowerCase() : ""
      if (
        message.includes("expired token") ||
        message.includes("invalid token") ||
        message.includes("missing auth token") ||
        message.includes("[401]") ||
        message.includes("unauthorized") ||
        message.includes("getuserdetails")
      ) {
        logoutUser()
      }
    }

    return Promise.reject(error)
  }
)
