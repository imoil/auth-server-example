using System;
using System.Windows.Forms;

namespace winform_client
{
    public partial class MainForm : Form
    {
        private readonly OidcClientService _oidcClientService;
        private readonly ApiClient _apiClient;

        public MainForm()
        {
            InitializeComponent();
            _oidcClientService = new OidcClientService();
            _apiClient = new ApiClient();
        }

        private async void btnLogin_Click(object sender, EventArgs e)
        {
            var result = await _oidcClientService.LoginAsync();
            if (result.IsError)
            {
                txtLog.Text = $"Login failed: {result.Error}";
                return;
            }

            // Store the token securely
            SecureTokenStorage.SaveToken(result.AccessToken);

            txtLog.Text = "Login successful!" + Environment.NewLine;
            txtLog.Text += $"AccessToken: {result.AccessToken}" + Environment.NewLine;
        }

        private async void btnCallApi_Click(object sender, EventArgs e)
        {
            txtLog.Text = "Calling API...";
            var response = await _apiClient.GetUserResourceAsync();
            txtLog.Text = $"API Response: {response}";
        }
    }
}
