# 통합 테스트 가이드: auth-server와 resource-server 연동

이 문서는 `auth-server`와 `resource-server`를 함께 실행하고, OAuth 2.0 인증 코드 플로우(PKCE 포함)를 통해 발급받은 Access Token으로 보호된 리소스에 접근하는 통합 테스트 방법을 안내합니다.

## 1단계: auth-server 실행

먼저, 인증 서버를 시작합니다. 이 서버는 클라이언트 등록 정보를 관리하고 토큰을 발급합니다.

```powershell
# 1. auth-server 디렉터리로 이동
cd .\auth-server

# 2. Maven을 사용하여 애플리케이션 실행 (포트 9000)
mvn spring-boot:run
```

서버가 성공적으로 시작되면 `Started AuthServerApplication` 로그를 확인할 수 있습니다.

## 2단계: resource-server 실행

다음으로, 리소스 서버를 시작합니다. 이 서버는 `auth-server`에서 발급한 JWT를 검증하여 API 접근을 제어합니다.

**새로운 터미널**을 열고 다음 명령을 실행하세요.

```powershell
# 1. resource-server 디렉터리로 이동
cd .\resource-server

# 2. Maven을 사용하여 애플리케이션 실행 (포트 8082)
mvn spring-boot:run
```

서버가 성공적으로 시작되면 `Started ResourceServerApplication` 로그를 확인할 수 있습니다.

## 3단계: PowerShell을 이용한 통합 테스트 스크립트

아래 스크립트는 전체 인증 및 API 호출 과정을 자동화합니다. 스크립트를 실행하기 전에 `auth-server`와 `resource-server`가 모두 실행 중인지 확인하세요.

스크립트는 다음 작업을 수행합니다.

1.  **PKCE 코드 생성**: `code_verifier`와 `code_challenge`를 생성합니다.
2.  **인증 요청 및 로그인**: `auth-server`의 인가 엔드포인트(`/oauth2/authorize`)로 인증을 요청하고, 하드코딩된 사용자 정보(`user@example.com` / `password`)로 로그인하여 `authorization_code`를 받습니다.
3.  **토큰 교환**: 발급받은 `authorization_code`와 `code_verifier`를 사용하여 `auth-server`의 토큰 엔드포인트(`/oauth2/token`)에서 `access_token`을 요청합니다.
4.  **API 호출**: 발급받은 `access_token`을 `Authorization` 헤더에 담아 `resource-server`의 보호된 API (`/api/user`, `/api/admin`)를 호출하고 결과를 출력합니다.

### PowerShell 스크립트

```powershell
# ====================================================== 
# 1. PKCE 파라미터 생성
# ====================================================== 
$codeVerifier = -join ([char[]](65..90) + ([char[]](97..122)) + (0..9) | Get-Random -Count 43)

$sha256 = [System.Security.Cryptography.SHA256]::Create()
$challengeBytes = $sha256.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($codeVerifier))
$codeChallenge = [System.Convert]::ToBase64String($challengeBytes) -replace '\+', '-' -replace '/', '_' -replace '=', ''

Write-Host "- Code Verifier: $codeVerifier"
Write-Host "- Code Challenge: $codeChallenge"

# ====================================================== 
# 2. 인증 및 Authorization Code 발급
# ====================================================== 
$clientId = "nuxt-client"
$redirectUri = "http://127.0.0.1:3000/login/oauth2/code/auth-server"
$authServerUrl = "http://localhost:9000"

$authorizeUrl = "$authServerUrl/oauth2/authorize?response_type=code&client_id=$clientId&scope=openid read.user&redirect_uri=$redirectUri&code_challenge=$codeChallenge&code_challenge_method=S256"

Write-Host "
[Step 1] Authorize URL: $authorizeUrl"

# 세션 유지를 위한 웹 세션 객체 생성
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# 로그인 페이지에서 CSRF 토큰 가져오기
$loginPageResponse = Invoke-WebRequest -Uri "$authServerUrl/login" -SessionVariable session
$csrfToken = $loginPageResponse.Forms[0].Fields['_csrf']

# 로그인 요청
$loginResponse = Invoke-WebRequest -Uri "$authServerUrl/login" -Method Post -WebSession $session -Body @{ "username" = "user@example.com"; "password" = "password"; "_csrf" = $csrfToken }

# 동의(consent) 페이지에서 CSRF 토큰 및 scope 가져오기
$consentPageResponse = Invoke-WebRequest -Uri $authorizeUrl -WebSession $session
$consentCsrfToken = $consentPageResponse.Forms[0].Fields['_csrf']
$scopesToConsent = $consentPageResponse.Forms[0].Fields.Keys | Where-Object { $_ -like 'scope.*' }

$consentBody = @{ "client_id" = $clientId; "_csrf" = $consentCsrfToken }
$scopesToConsent | ForEach-Object { $consentBody[$_] = "on" }

# 동의 제출 후 리디렉션 URL에서 code 획득
$consentSubmitResponse = Invoke-WebRequest -Uri "$authServerUrl/oauth2/authorize" -Method Post -WebSession $session -Body $consentBody -MaximumRedirection 0 -ErrorAction SilentlyContinue

$location = $consentSubmitResponse.Headers["Location"]
$authorizationCode = ($location -split 'code=')[-1] -split '&' | Select-Object -First 1

Write-Host "
[Step 2] Authorization Code: $authorizationCode"

# ====================================================== 
# 3. Access Token 교환
# ====================================================== 
$tokenUrl = "$authServerUrl/oauth2/token"

$tokenBody = @{
    "grant_type"    = "authorization_code";
    "code"          = $authorizationCode;
    "redirect_uri"  = $redirectUri;
    "client_id"     = $clientId;
    "code_verifier" = $codeVerifier;
}

$tokenResponse = Invoke-RestMethod -Uri $tokenUrl -Method Post -Body $tokenBody
$accessToken = $tokenResponse.access_token

Write-Host "
[Step 3] Access Token: $accessToken"

# ====================================================== 
# 4. 리소스 서버 API 호출
# ====================================================== 
$resourceUrl = "http://localhost:8082"
$headers = @{ "Authorization" = "Bearer $accessToken" }

Write-Host "
[Step 4] Calling Resource Server APIs..."

# /api/public 호출 (인증 불필요)
$responsePublic = Invoke-RestMethod -Uri "$resourceUrl/api/public" -Method Get
Write-Host "- /api/public response: $responsePublic"

# /api/user 호출 (USER 역할 필요)
$responseUser = Invoke-RestMethod -Uri "$resourceUrl/api/user" -Method Get -Headers $headers
Write-Host "- /api/user response: $responseUser"

# /api/admin 호출 (ADMIN 역할 필요 - user@example.com은 ADMIN 역할이 없으므로 403 예상)
try {
    Invoke-RestMethod -Uri "$resourceUrl/api/admin" -Method Get -Headers $headers -ErrorAction Stop
} catch {
    Write-Host "- /api/admin response: $_"
}
```