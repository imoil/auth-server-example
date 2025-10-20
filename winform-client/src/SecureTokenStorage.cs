using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;

namespace winform_client
{
    public static class SecureTokenStorage
    {
        // A static, unchanging entropy value adds an extra layer of security.
        // It's like a "salt" for the DPAPI encryption.
        private static readonly byte[] s_entropy = Encoding.UTF8.GetBytes("WinformClientSecretEntropy");

        private static readonly string s_tokenFilePath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "WinformClient", // Application-specific folder
            "token.dat");

        public static void SaveToken(string token)
        {
            if (string.IsNullOrEmpty(token))
            {
                DeleteToken();
                return;
            }

            byte[] tokenBytes = Encoding.UTF8.GetBytes(token);

            // Use the DPAPI to encrypt the token with the current user's credentials.
            byte[] encryptedToken = ProtectedData.Protect(tokenBytes, s_entropy, DataProtectionScope.CurrentUser);

            // Ensure the directory exists.
            Directory.CreateDirectory(Path.GetDirectoryName(s_tokenFilePath));

            // Save the encrypted token to a file.
            File.WriteAllBytes(s_tokenFilePath, encryptedToken);
        }

        public static string LoadToken()
        {
            if (!File.Exists(s_tokenFilePath))
            {
                return null;
            }

            byte[] encryptedToken = File.ReadAllBytes(s_tokenFilePath);

            try
            {
                // Decrypt the token using the same entropy and user scope.
                // This will fail if a different user is logged in.
                byte[] tokenBytes = ProtectedData.Unprotect(encryptedToken, s_entropy, DataProtectionScope.CurrentUser);
                return Encoding.UTF8.GetString(tokenBytes);
            }
            catch (CryptographicException)
            {
                // Failed to decrypt, likely because it was encrypted by another user.
                // Delete the invalid file.
                DeleteToken();
                return null;
            }
        }

        public static void DeleteToken()
        {
            if (File.Exists(s_tokenFilePath))
            {
                File.Delete(s_tokenFilePath);
            }
        }
    }
}
