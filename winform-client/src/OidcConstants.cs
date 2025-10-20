namespace winform_client
{
    public static class OidcConstants
    {
        public const string Authority = "http://auth-server:9000";
        public const string ClientId = "winforms-client";
        public const string RedirectUri = "http://127.0.0.1:7890";
        public const string Scope = "openid profile";
        public const string ResourceServerUrl = "http://localhost:8082/api/";
    }
}
