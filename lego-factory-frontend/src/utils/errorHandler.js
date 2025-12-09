/**
 * Utility functions for handling API errors and extracting error information.
 */

/**
 * Extract error message from API response
 * Handles both ErrorResponse format and generic error responses
 */
export function getErrorMessage(error) {
  // Custom error response format from backend
  if (error.response?.data) {
    const data = error.response.data;
    
    // Prefer user-friendly message
    if (data.message && data.message !== 'Internal server error') {
      return data.message;
    }
    
    // Fall back to error field
    if (data.error) {
      return data.error;
    }
    
    // Fall back to details
    if (data.details) {
      return data.details;
    }
  }
  
  // Network errors
  if (!error.response) {
    if (error.message === 'Network Error') {
      return 'Network error: Unable to connect to server';
    }
    return error.message || 'An unexpected error occurred';
  }
  
  // HTTP status code based messages
  const status = error.response?.status;
  switch (status) {
    case 400:
      return 'Invalid request. Please check your input.';
    case 401:
      return 'Unauthorized. Please login again.';
    case 403:
      return 'Forbidden. You do not have permission to perform this action.';
    case 404:
      return 'Resource not found.';
    case 409:
      return 'Conflict. The resource may already exist or be in use.';
    case 500:
      return 'Server error. Please try again later.';
    case 503:
      return 'Service unavailable. The server is temporarily down.';
    default:
      return error.response?.data?.message || 'An error occurred. Please try again.';
  }
}

/**
 * Extract detailed error information for logging/debugging
 */
export function getErrorDetails(error) {
  return {
    status: error.response?.status,
    message: error.response?.data?.message,
    error: error.response?.data?.error,
    details: error.response?.data?.details,
    path: error.response?.data?.path,
    timestamp: error.response?.data?.timestamp,
    cause: error.message
  };
}

/**
 * Check if error is due to validation failure
 */
export function isValidationError(error) {
  return error.response?.status === 400 && 
         error.response?.data?.message?.includes('Validation');
}

/**
 * Check if error is due to not found
 */
export function isNotFoundError(error) {
  return error.response?.status === 404;
}

/**
 * Check if error is due to unauthorized access
 */
export function isUnauthorizedError(error) {
  return error.response?.status === 401 || error.response?.status === 403;
}

/**
 * Check if error is a network error
 */
export function isNetworkError(error) {
  return !error.response || error.message === 'Network Error';
}

/**
 * Check if error is a server error
 */
export function isServerError(error) {
  const status = error.response?.status;
  return status >= 500;
}

export default {
  getErrorMessage,
  getErrorDetails,
  isValidationError,
  isNotFoundError,
  isUnauthorizedError,
  isNetworkError,
  isServerError
};
