using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading;
using System.Threading.Tasks;

namespace winform_client
{
    public class AuthenticationHandler : DelegatingHandler
    {
        public AuthenticationHandler()
        {
            // The actual network request is handled by the InnerHandler.
            // By default, this is HttpClientHandler.
            InnerHandler = new HttpClientHandler();
        }

        protected override async Task<HttpResponseMessage> SendAsync(
            HttpRequestMessage request, CancellationToken cancellationToken)
        {
            // Attempt to load the token from secure storage.
            var token = SecureTokenStorage.LoadToken();
            if (!string.IsNullOrEmpty(token))
            {
                // If a token exists, add it to the Authorization header.
                request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            }

            // Send the request on to the rest of the pipeline.
            var response = await base.SendAsync(request, cancellationToken);

            // If the response is 401 Unauthorized, the token is likely expired or invalid.
            if (response.StatusCode == HttpStatusCode.Unauthorized)
            {
                // Delete the stored token so we don't keep sending an invalid one.
                // The user will need to log in again to get a new token.
                SecureTokenStorage.DeleteToken();
            }

            return response;
        }
    }
}
