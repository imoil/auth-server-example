using System;
using System.Windows.Forms;
using System.Text;

namespace winform_client
{
    public partial class MainForm : Form
    {
        private readonly OidcClientService _oidcClientService;
        private readonly ApiClient _apiClient;
        private readonly StringBuilder _logBuffer = new StringBuilder();

        public MainForm()
        {
            InitializeComponent();
            _oidcClientService = new OidcClientService();
            _apiClient = new ApiClient();
            Log("Application started. Please log in.");
        }

        private void Log(string message)
        {
            // Append new message and ensure it's thread-safe for UI updates.
            if (txtLog.InvokeRequired)
            {
                txtLog.Invoke(new Action(() => Log(message)));
                return;
            }

            _logBuffer.AppendLine($"[{DateTime.Now:HH:mm:ss}] {message}");
            txtLog.Text = _logBuffer.ToString();
            txtLog.SelectionStart = txtLog.Text.Length;
            txtLog.ScrollToCaret();
        }

        private async void btnLogin_Click(object sender, EventArgs e)
        {
            Log("Starting login process...");
            var result = await _oidcClientService.LoginAsync();

            if (result.IsSuccess)
            {
                Log("Login successful!");
                Log($"Access Token: {result.AccessToken}");
            }
            else
            {
                Log($"Login failed: {result.ErrorMessage}");
            }
        }

        private async void btnLogout_Click(object sender, EventArgs e)
        {
            await _oidcClientService.LogoutAsync();
            Log("Logged out. Local token has been deleted.");
        }

        private async void btnCallPublic_Click(object sender, EventArgs e)
        {
            Log("Calling public API endpoint (/api/public)...");
            var response = await _apiClient.GetPublicResourceAsync();
            Log($"API Response: {response}");
        }

        private async void btnCallUser_Click(object sender, EventArgs e)
        {
            Log("Calling user-level API endpoint (/api/user)...");
            var response = await _apiClient.GetUserResourceAsync();
            Log($"API Response: {response}");
        }

        private async void btnCallAdmin_Click(object sender, EventArgs e)
        {
            Log("Calling admin-level API endpoint (/api/admin)...");
            var response = await _apiClient.GetAdminResourceAsync();
            Log($"API Response: {response}");
        }
    }
}
