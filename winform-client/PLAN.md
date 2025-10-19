# WinForms 클라이언트 구현 계획

이 문서는 `README.md`의 분석 내용을 바탕으로 WinForms 클라이언트 애플리케이션을 단계적으로 구현하기 위한 작업 목록을 정의합니다.

## Task 1: 프로젝트 생성 및 기본 설정

- [ ] `winform-client` 디렉터리 생성
- [ ] Visual Studio 또는 `dotnet new` CLI를 사용하여 .NET Framework 4.5를 타겟으로 하는 새로운 WinForms 애플리케이션 프로젝트 생성
- [ ] `IdentityModel.OidcClient` NuGet 패키지 설치
- [ ] `.gitignore` 파일 생성

## Task 2: OIDC 클라이언트 정보 설정

- [ ] `OidcClient`에 필요한 설정 값(Authority, ClientId, RedirectUri 등)을 관리할 클래스 또는 상수를 정의합니다.
    - **Authority:** `http://auth-server:9000`
    - **ClientId:** `winforms-client`
    - **RedirectUri:** `http://127.0.0.1:7890` (루프백 주소)
    - **Scope:** `openid profile`

## Task 3: 보안 토큰 저장소 구현 (`SecureTokenStorage.cs`)

- [ ] `SecureTokenStorage` 정적 클래스 생성
- [ ] `SaveToken(string token)` 메서드 구현
    - `ProtectedData.Protect`를 사용하여 토큰 암호화
    - 암호화된 데이터를 `%LOCALAPPDATA%` 내 파일에 저장
- [ ] `LoadToken()` 메서드 구현
    - 파일에서 암호화된 데이터 로드
    - `ProtectedData.Unprotect`를 사용하여 토큰 복호화
    - 복호화 실패 시(예: 다른 사용자) `null` 반환 및 파일 삭제
- [ ] `DeleteToken()` 메서드 구현
    - 토큰 파일 삭제

## Task 4: OIDC 인증 서비스 구현 (`OidcClientService.cs`)

- [ ] `OidcClientService` 클래스 생성
- [ ] `OidcClient` 인스턴스 초기화 로직 구현
- [ ] `LoginAsync()` 메서드 구현
    - `OidcClient.LoginAsync()`를 호출하여 로그인 프로세스 시작
    - 성공 시, 반환된 `AccessToken`을 `SecureTokenStorage.SaveToken()`을 통해 저장
- [ ] `LogoutAsync()` 메서드 구현
    - `OidcClient.LogoutAsync()`를 호출하여 로그아웃 수행 (필요 시)
    - `SecureTokenStorage.DeleteToken()`을 호출하여 로컬 토큰 삭제

## Task 5: API 호출 핸들러 구현 (`AuthenticationHandler.cs`)

- [ ] `DelegatingHandler`를 상속하는 `AuthenticationHandler` 클래스 생성
- [ ] `SendAsync` 메서드 재정의
    - `SecureTokenStorage.LoadToken()`으로 토큰 로드
    - 요청 헤더에 `Authorization: Bearer <token>` 추가
    - `base.SendAsync()` 호출
    - 응답 코드가 401이면 `SecureTokenStorage.DeleteToken()` 호출

## Task 6: API 클라이언트 구현 (`ApiClient.cs`)

- [ ] `ApiClient` 클래스 생성
- [ ] `HttpClient` 인스턴스를 `AuthenticationHandler`와 함께 초기화
- [ ] 리소스 서버의 기본 주소(`http://localhost:8082/api/`) 설정
- [ ] API 엔드포인트(예: `/user`, `/admin`)를 호출하는 메서드 구현 (예: `GetUserResourceAsync()`)
- [ ] `HttpRequestException` (특히 401, 403 상태 코드) 처리 로직 추가

## Task 7: 메인 UI 구현 (`MainForm.cs`)

- [ ] `MainForm`에 UI 컨트롤 추가
    - 로그인 버튼 (`btnLogin`)
    - API 호출 버튼 (`btnCallApi`)
    - 로그 출력을 위한 텍스트 박스 (`txtLog`)
- [ ] `btnLogin_Click` 이벤트 핸들러 구현
    - `_oidcClientService.LoginAsync()` 호출
    - 결과를 `txtLog`에 표시
- [ ] `btnCallApi_Click` 이벤트 핸들러 구현
    - `_apiClient.GetUserResourceAsync()` 호출
    - 결과를 `txtLog`에 표시

## Task 8: 최종 조립 및 테스트

- [ ] `Program.cs`에서 `MainForm`이 정상적으로 실행되도록 확인
- [ ] 전체 인증 및 API 호출 흐름을 테스트
- [ ] 예외 처리 및 사용자 피드백 로직 검토
