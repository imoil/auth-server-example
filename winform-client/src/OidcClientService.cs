using IdentityModel.OidcClient;
using System;
using System.Threading.Tasks;

namespace winform_client
{
    public class OidcClientService
    {
        private readonly OidcClient _oidcClient;

        public OidcClientService()
        {
            var options = new OidcClientOptions
            {
                Authority = OidcConstants.Authority,
                ClientId = OidcConstants.ClientId,
                RedirectUri = OidcConstants.RedirectUri,
                Scope = OidcConstants.Scope,
                Browser = new SystemBrowser(), // This will use the system's default web browser
                Policy = new Policy
                {
                    // For development, we might not have HTTPS on the auth server.
                    // In a production environment, this should be true.
                    RequireHttps = false
                }
            };

            _oidcClient = new OidcClient(options);
        }

        public async Task<(bool IsSuccess, string AccessToken, string ErrorMessage)> LoginAsync()
        {
            try
            {
                var loginResult = await _oidcClient.LoginAsync(new LoginRequest());

                if (loginResult.IsError)
                {
                    // The user might have closed the browser or there was a protocol error.
                    return (false, null, $"Login failed: {loginResult.Error}");
                }

                // Successfully authenticated, now save the token.
                SecureTokenStorage.SaveToken(loginResult.AccessToken);

                return (true, loginResult.AccessToken, null);
            }
            catch (Exception ex)
            {
                return (false, null, $"An unexpected error occurred during login: {ex.Message}");
            }
        }

        public async Task LogoutAsync()
        {
            // The OidcClient doesn't have a built-in logout for this flow that communicates
            // with the server in a standard way without more complex session management.
            // For this client, "logout" will simply mean deleting the local token.
            SecureTokenStorage.DeleteToken();
            await Task.CompletedTask; // To make the method async as per the plan
        }
    }
}
