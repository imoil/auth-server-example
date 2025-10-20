using System;
using System.Net.Http;
using System.Threading.Tasks;

namespace winform_client
{
    public class ApiClient
    {
        private readonly HttpClient _httpClient;

        public ApiClient()
        {
            // The ApiClient uses an HttpClient that has our custom
            // AuthenticationHandler attached to its pipeline.
            _httpClient = new HttpClient(new AuthenticationHandler())
            {
                BaseAddress = new Uri(OidcConstants.ResourceServerUrl)
            };
        }

        public async Task<string> GetPublicResourceAsync()
        {
            return await CallApiAsync("public");
        }

        public async Task<string> GetUserResourceAsync()
        {
            return await CallApiAsync("user");
        }

        public async Task<string> GetAdminResourceAsync()
        {
            return await CallApiAsync("admin");
        }

        private async Task<string> CallApiAsync(string endpoint)
        {
            try
            {
                var response = await _httpClient.GetAsync(endpoint);

                if (response.IsSuccessStatusCode)
                {
                    return await response.Content.ReadAsStringAsync();
                }

                // Handle specific error codes with user-friendly messages.
                switch (response.StatusCode)
                {
                    case System.Net.HttpStatusCode.Unauthorized:
                        return "Access Denied. Your session may have expired. Please log in again.";
                    case System.Net.HttpStatusCode.Forbidden:
                        return "Permission Denied. You do not have the required role to access this resource.";
                    default:
                        return $"Error: {response.StatusCode} - {await response.Content.ReadAsStringAsync()}";
                }
            }
            catch (Exception ex)
            {
                // This could be a network error, DNS issue, etc.
                return $"An unexpected error occurred: {ex.Message}";
            }
        }
    }
}
