//src/config/backend.js

/**
 * The base URL for the backend API.
 * It determines the host based on the current window location.
 * If running on localhost, it defaults to http://localhost:8080.
 * Otherwise, it uses the current hostname with port 8080.
 */
export const BACKEND_API_URL = window.location.hostname === "localhost" ? "http://localhost:8080" : `http://${window.location.hostname}:8080`;
