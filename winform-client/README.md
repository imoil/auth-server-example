# WinForms 클라이언트 (.NET) 분석

이 문서는 `PROJECTS.md`에 기술된 아키텍처에 따라 .NET WinForms 데스크톱 클라이언트를 구축하기 위한 상세 분석 내용을 담고 있습니다. 이 클라이언트는 중앙 ID 허브(Auth Server)와 연동하여 OIDC(OpenID Connect) 프로토콜로 사용자를 인증하고, 발급받은 JWT를 사용하여 리소스 서버의 보호된 API를 안전하게 호출하는 것을 목표로 합니다.

## 1. 핵심 아키텍처 및 기술 스택

- **플랫폼:** .NET WinForms
- **.NET Framework 버전:** 4.5
- **인증 프로토콜:** OAuth 2.1 / OpenID Connect (Authorization Code Flow + PKCE)
- **핵심 라이브러리:**
  - `IdentityModel.OidcClient`: 네이티브 애플리케이션을 위한 OIDC/OAuth 2.0 클라이언트 라이브러리. 인증 흐름을 간소화합니다.
  - `System.Net.Http.HttpClient`: 리소스 서버와의 API 통신을 담당합니다.
  - `System.Security.Cryptography.ProtectedData`: Windows DPAPI를 사용하여 JWT를 로컬 시스템에 안전하게 저장하기 위해 사용됩니다.

## 2. 주요 기능 및 구현 전략

### 2.1. OIDC 인증 흐름 (PKCE 사용)

데스크톱 애플리케이션은 `client_secret`을 안전하게 저장할 수 없는 Public 클라이언트이므로, **PKCE(Proof Key for Code Exchange)**를 사용한 Authorization Code Flow가 필수적입니다.

1.  **인증 요청:** 사용자가 '로그인' 버튼을 클릭하면, `OidcClient`가 `code_verifier`와 `code_challenge`를 생성합니다.
2.  **브라우저 리디렉션:** 시스템의 기본 웹 브라우저를 열어 ID 허브의 인증 엔드포인트(`/oauth2/authorize`)로 사용자를 리디렉션합니다. 이때 `code_challenge`와 `code_challenge_method=S256` 파라미터를 함께 전송합니다.
3.  **사용자 인증:** 사용자는 ID 허브(및 Azure AD)를 통해 인증을 완료합니다.
4.  **콜백 처리:** 인증 성공 후, ID 허브는 미리 등록된 루프백 주소(예: `http://127.0.0.1:7890`)로 `authorization_code`와 함께 리디렉션합니다.
5.  **로컬 리스너:** 애플리케이션은 해당 루프백 주소에서 수신 대기하는 간단한 HTTP 리스너를 내장하여 콜백을 가로챕니다.
6.  **토큰 교환:** `OidcClient`는 수신한 `authorization_code`와 초기에 생성한 `code_verifier`를 ID 허브의 토큰 엔드포인트(`/oauth2/token`)로 보내 Access Token(JWT)을 요청하고 수신합니다.

### 2.2. JWT의 안전한 저장 (Windows DPAPI)

수신한 JWT는 리소스 서버 API 호출에 지속적으로 사용되므로 안전하게 저장해야 합니다. 일반 텍스트 파일이나 레지스트리에 저장하는 것은 리버스 엔지니어링에 취약합니다.

- **해결책:** Windows DPAPI(`Data Protection API`)를 사용합니다.
- **구현:** `System.Security.Cryptography.ProtectedData` 클래스의 `Protect` 및 `Unprotect` 메서드를 활용합니다.
- **범위:** `DataProtectionScope.CurrentUser`를 사용하여 암호화된 데이터를 현재 로그인한 Windows 사용자만 해독할 수 있도록 제한합니다.
- **저장 위치:** 암호화된 토큰 데이터는 사용자의 로컬 애플리케이션 데이터 폴더(`%LOCALAPPDATA%`) 내의 애플리케이션별 디렉터리에 파일로 저장합니다.
- **헬퍼 클래스:** `SecureTokenStorage.cs` 라는 정적 헬퍼 클래스를 구현하여 토큰의 저장, 로드, 삭제 로직을 캡슐화합니다.

### 2.3. API 호출 자동화 (DelegatingHandler)

모든 API 호출 시 `Authorization: Bearer <token>` 헤더를 수동으로 추가하는 것은 번거롭고 오류 발생 가능성이 높습니다.

- **해결책:** `HttpClient`의 파이프라인에 사용자 정의 `DelegatingHandler`를 삽입합니다.
- **구현:** `AuthenticationHandler.cs` 라는 이름의 `DelegatingHandler`를 구현합니다.
    1.  `SendAsync` 메서드를 재정의(override)합니다.
    2.  요청을 보내기 전, `SecureTokenStorage`를 통해 저장된 JWT를 로드합니다.
    3.  토큰이 존재하면 `request.Headers.Authorization`에 `Bearer` 토큰을 설정합니다.
    4.  `base.SendAsync`를 호출하여 요청을 파이프라인의 다음 핸들러로 전달합니다.
- **401 응답 처리:** `SendAsync`에서 응답을 받은 후, 상태 코드가 `401 Unauthorized`(토큰 만료 등)이면 `SecureTokenStorage.DeleteToken()`을 호출하여 만료된 토큰을 삭제합니다. 이는 사용자가 다시 로그인하도록 유도하는 역할을 합니다.
- **`ApiClient`:** `HttpClient` 인스턴스를 `AuthenticationHandler`와 함께 생성하고 관리하는 `ApiClient.cs` 클래스를 만들어 비즈니스 로직(UI 코드)와 API 통신 로직을 분리합니다.

## 3. 프로젝트 구조

'''
winform-client/
├── src/
│   ├── winform-client/
│   │   ├── ApiClient.cs              # HttpClient를 사용하여 API 호출을 담당
│   │   ├── AuthenticationHandler.cs  # DelegatingHandler 구현체
│   │   ├── OidcClientService.cs      # OidcClient 로직 캡슐화
│   │   ├── SecureTokenStorage.cs     # DPAPI를 이용한 토큰 저장/로드
│   │   ├── MainForm.cs               # 메인 UI 폼
│   │   ├── MainForm.Designer.cs
│   │   ├── Program.cs                # 애플리케이션 진입점
│   │   └── Properties/
│   │       ├── AssemblyInfo.cs
│   │       └── ...
│   ├── winform-client.csproj         # 프로젝트 파일 (.NET 4.5 타겟)
│   └── winform-client.sln            # 솔루션 파일
├── .gitignore
├── README.md
└── PLAN.md
'''

이러한 설계는 `PROJECTS.md`에서 제시된 가이드라인을 충실히 따르며, 데스크톱 환경의 보안적 제약을 고려하여 OIDC 인증 흐름을 안전하고 효율적으로 구현하는 것을 목표로 합니다.
