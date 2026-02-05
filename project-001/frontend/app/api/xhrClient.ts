interface XhrClientOptions {
  maxRetries?: number;
  baseDelay?: number;
  timeout?: number;
  shouldRetry?: (status: number) => boolean;
  onRetry?: (attempt: number, maxRetries: number, delay: number, status?: number) => void;
}

const xhrClient = <T = any>(
  api_url: string,
  method: string,
  headers: Record<string, string> = {},
  body: any = null,
  options: XhrClientOptions = {}
): Promise<T> => {
  const {
    maxRetries = 3,
    baseDelay = 1000,
    timeout = 10000,
    shouldRetry = (status: number) => status === 429 || (status >= 500 && status < 600),
    onRetry = () => {}
  } = options;

  const executeRequest = (retryCount: number = 0): Promise<T> => {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open(method, api_url);

      // Check if body is FormData
      const isFormData = body instanceof FormData;

      // Set headers (skip Content-Type for FormData - browser sets it automatically)
      Object.keys(headers).forEach((key) => {
        // Don't set Content-Type for FormData - browser adds boundary automatically
        if (isFormData && key.toLowerCase() === 'content-type') {
          return;
        }
        xhr.setRequestHeader(key, headers[key]);
      });

      xhr.onreadystatechange = () => {
        if (xhr.readyState === 4) {
          if (xhr.status === 403) {
            window.location.href = '/auth/logout';
            reject('Payment required. Redirecting to logout.');
            return;
          }

          if (xhr.status >= 200 && xhr.status < 300) {
            if (!xhr.responseText || xhr.responseText.trim() === '') {
              resolve({} as T);
              return;
            }
            try {
              const response = JSON.parse(xhr.responseText);
              resolve(response as T);
            } catch (error) {
              resolve(xhr.responseText as unknown as T);
            }
          } else if (xhr.status === 429 && retryCount < maxRetries) {
            const retryAfter = xhr.getResponseHeader('Retry-After');
            let delay = baseDelay * Math.pow(2, retryCount);
            
            if (retryAfter) {
              const retryAfterSeconds = parseInt(retryAfter, 10);
              if (!isNaN(retryAfterSeconds)) {
                delay = retryAfterSeconds * 1000;
              }
            }
            
            onRetry(retryCount + 1, maxRetries, delay, 429);
            
            setTimeout(() => {
              executeRequest(retryCount + 1)
                .then(resolve)
                .catch(reject);
            }, delay);
            return;
          } else if (shouldRetry(xhr.status) && retryCount < maxRetries) {
            const delay = baseDelay * Math.pow(2, retryCount);
            
            onRetry(retryCount + 1, maxRetries, delay, xhr.status);
            
            setTimeout(() => {
              executeRequest(retryCount + 1)
                .then(resolve)
                .catch(reject);
            }, delay);
            return;
          } else if (xhr.status > 0) {
            let errorMessage = "An error occurred";
            
            try {
              const errorResponse = JSON.parse(xhr.responseText);
              errorMessage = errorResponse.message || errorResponse.error || `Error: ${xhr.status}`;
            } catch (e) {
              errorMessage = xhr.responseText || `Error: ${xhr.status}`;
            }
            
            reject(errorMessage);
          }
        }
      };

      xhr.onerror = () => {
        if (retryCount < maxRetries) {
          const delay = baseDelay * Math.pow(2, retryCount);
          
          onRetry(retryCount + 1, maxRetries, delay, 0);
          
          setTimeout(() => {
            executeRequest(retryCount + 1)
              .then(resolve)
              .catch(reject);
          }, delay);
        } else {
          reject('Connection failed. Please check your internet connection.');
        }
      };

      xhr.timeout = timeout;
      xhr.ontimeout = () => {
        if (retryCount < maxRetries) {
          const delay = baseDelay * Math.pow(2, retryCount);
          
          onRetry(retryCount + 1, maxRetries, delay, -1);
          
          setTimeout(() => {
            executeRequest(retryCount + 1)
              .then(resolve)
              .catch(reject);
          }, delay);
        } else {
          reject('The server is taking too long to respond. It might be down.');
        }
      };

      // Send FormData as-is, or JSON.stringify for regular objects
      xhr.send(isFormData ? body : (body ? JSON.stringify(body) : null));
    });
  };

  return executeRequest();
};

export default xhrClient;