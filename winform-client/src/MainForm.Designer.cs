namespace winform_client
{
    partial class MainForm
    {
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.Button btnLogin;
        private System.Windows.Forms.Button btnLogout;
        private System.Windows.Forms.Button btnCallPublic;
        private System.Windows.Forms.Button btnCallUser;
        private System.Windows.Forms.Button btnCallAdmin;
        private System.Windows.Forms.TextBox txtLog;
        private System.Windows.Forms.GroupBox grpAuth;
        private System.Windows.Forms.GroupBox grpApi;

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            this.btnLogin = new System.Windows.Forms.Button();
            this.btnLogout = new System.Windows.Forms.Button();
            this.btnCallPublic = new System.Windows.Forms.Button();
            this.btnCallUser = new System.Windows.Forms.Button();
            this.btnCallAdmin = new System.Windows.Forms.Button();
            this.txtLog = new System.Windows.Forms.TextBox();
            this.grpAuth = new System.Windows.Forms.GroupBox();
            this.grpApi = new System.Windows.Forms.GroupBox();
            this.grpAuth.SuspendLayout();
            this.grpApi.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnLogin
            // 
            this.btnLogin.Location = new System.Drawing.Point(15, 25);
            this.btnLogin.Name = "btnLogin";
            this.btnLogin.Size = new System.Drawing.Size(100, 30);
            this.btnLogin.TabIndex = 0;
            this.btnLogin.Text = "Login";
            this.btnLogin.UseVisualStyleBackColor = true;
            this.btnLogin.Click += new System.EventHandler(this.btnLogin_Click);
            // 
            // btnLogout
            //
            this.btnLogout.Location = new System.Drawing.Point(121, 25);
            this.btnLogout.Name = "btnLogout";
            this.btnLogout.Size = new System.Drawing.Size(100, 30);
            this.btnLogout.TabIndex = 1;
            this.btnLogout.Text = "Logout";
            this.btnLogout.UseVisualStyleBackColor = true;
            this.btnLogout.Click += new System.EventHandler(this.btnLogout_Click);
            //
            // btnCallPublic
            //
            this.btnCallPublic.Location = new System.Drawing.Point(15, 25);
            this.btnCallPublic.Name = "btnCallPublic";
            this.btnCallPublic.Size = new System.Drawing.Size(120, 30);
            this.btnCallPublic.TabIndex = 2;
            this.btnCallPublic.Text = "Call /api/public";
            this.btnCallPublic.UseVisualStyleBackColor = true;
            this.btnCallPublic.Click += new System.EventHandler(this.btnCallPublic_Click);
            //
            // btnCallUser
            //
            this.btnCallUser.Location = new System.Drawing.Point(141, 25);
            this.btnCallUser.Name = "btnCallUser";
            this.btnCallUser.Size = new System.Drawing.Size(120, 30);
            this.btnCallUser.TabIndex = 3;
            this.btnCallUser.Text = "Call /api/user";
            this.btnCallUser.UseVisualStyleBackColor = true;
            this.btnCallUser.Click += new System.EventHandler(this.btnCallUser_Click);
            //
            // btnCallAdmin
            // 
            this.btnCallAdmin.Location = new System.Drawing.Point(267, 25);
            this.btnCallAdmin.Name = "btnCallAdmin";
            this.btnCallAdmin.Size = new System.Drawing.Size(120, 30);
            this.btnCallAdmin.TabIndex = 4;
            this.btnCallAdmin.Text = "Call /api/admin";
            this.btnCallAdmin.UseVisualStyleBackColor = true;
            this.btnCallAdmin.Click += new System.EventHandler(this.btnCallAdmin_Click);
            // 
            // txtLog
            // 
            this.txtLog.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.txtLog.Location = new System.Drawing.Point(12, 105);
            this.txtLog.Multiline = true;
            this.txtLog.Name = "txtLog";
            this.txtLog.ReadOnly = true;
            this.txtLog.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.txtLog.Size = new System.Drawing.Size(776, 333);
            this.txtLog.TabIndex = 5;
            //
            // grpAuth
            //
            this.grpAuth.Controls.Add(this.btnLogin);
            this.grpAuth.Controls.Add(this.btnLogout);
            this.grpAuth.Location = new System.Drawing.Point(12, 12);
            this.grpAuth.Name = "grpAuth";
            this.grpAuth.Size = new System.Drawing.Size(240, 75);
            this.grpAuth.TabIndex = 6;
            this.grpAuth.TabStop = false;
            this.grpAuth.Text = "Authentication";
            //
            // grpApi
            //
            this.grpApi.Controls.Add(this.btnCallPublic);
            this.grpApi.Controls.Add(this.btnCallUser);
            this.grpApi.Controls.Add(this.btnCallAdmin);
            this.grpApi.Location = new System.Drawing.Point(258, 12);
            this.grpApi.Name = "grpApi";
            this.grpApi.Size = new System.Drawing.Size(400, 75);
            this.grpApi.TabIndex = 7;
            this.grpApi.TabStop = false;
            this.grpApi.Text = "API Calls";
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.grpApi);
            this.Controls.Add(this.grpAuth);
            this.Controls.Add(this.txtLog);
            this.Name = "MainForm";
            this.Text = "WinForms OIDC Client";
            this.grpAuth.ResumeLayout(false);
            this.grpApi.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();
        }
    }
}
