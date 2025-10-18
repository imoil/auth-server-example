### `auth-server`: 페더레이션 ID 및 중앙 집중식 권한 부여 허브

이 프로젝트는 외부 ID 공급자(IdP)와 내부 마이크로서비스 생태계 사이의 브릿지 역할을 하는 **중앙 집중식 인증 및 권한 부여 허브(ID Hub)**를 구현합니다. 핵심 아키텍처 패턴은 **인증(Authentication) 행위**와 **권한 부여 토큰 발급(Authorization Token Issuance) 행위**를 전략적으로 분리하는 것입니다.

---

#### 주요 아키텍처 및 데이터 흐름

본 인증 서버는 직접 사용자를 인증하지 않습니다. 대신, 신뢰할 수 있는 외부 IdP(예: Azure AD)에 인증을 위임하고, 인증된 사용자에 대한 내부 컨텍스트(역할, 권한 등)를 결합하여 자체 서명된 JWT를 내부 서비스에 제공합니다.

1.  **인증 요청 (Client → ID Hub)**: 클라이언트 애플리케이션(Nuxt.js, WinForms 등)이 ID Hub의 `/oauth2/authorize` 엔드포인트를 호출하여 OAuth 2.0 인증 코드 플로우를 시작합니다.
2.  **인증 위임 (ID Hub → External IdP)**: ID Hub는 OIDC 프로토콜을 통해 사용자를 외부 IdP(Azure AD)의 로그인 페이지로 리디렉션합니다.
3.  **외부 인증 및 콜백 (User ↔ External IdP → ID Hub)**: 사용자가 외부 IdP에서 성공적으로 인증하면, IdP는 `authorization_code`와 함께 ID Hub에 등록된 콜백 URI(`/login/oauth2/code/azure`)로 사용자를 리디렉션합니다.
4.  **토큰 교환 (ID Hub ↔ External IdP)**: ID Hub는 백그라운드에서 수신한 `code`를 사용하여 외부 IdP의 토큰 엔드포인트를 호출하고, IdP로부터 `ID Token`과 `Access Token`을 수신합니다.
5.  **토큰 강화 (Token Enrichment)**:
    *   ID Hub는 수신한 `ID Token`에서 사용자의 고유 식별자(예: `email`)를 추출합니다.
    *   이 식별자를 키로 내부 데이터베이스(H2/Oracle)를 조회하여 해당 사용자의 역할 목록(예: `['ROLE_USER', 'ROLE_ADMIN']`)을 가져옵니다.
6.  **커스텀 JWT 발급 (ID Hub → Client)**:
    *   ID Hub는 외부 IdP의 기본 사용자 정보와 내부 DB에서 조회한 역할 정보를 결합하여 **새로운 페이로드(Payload)**를 구성합니다.
    *   이 페이로드를 기반으로, ID Hub가 자체 개인키로 서명한 **새로운 커스텀 JWT**를 생성합니다.
    *   생성된 JWT를 클라이언트 애플리케이션에 전달합니다.
7.  **리소스 접근 (Client → Resource Server)**: 클라이언트는 발급받은 커스텀 JWT를 `Authorization: Bearer` 헤더에 담아 내부 리소스 서버의 보호된 API를 호출합니다.
8.  **토큰 검증 및 권한 부여 (Resource Server)**:
    *   리소스 서버는 오직 **ID Hub의 `issuer`**만을 신뢰하도록 설정됩니다. (외부 IdP의 존재를 알지 못함)
    *   ID Hub의 공개키(JWKS)를 사용하여 JWT 서명을 검증합니다.
    *   토큰 내의 `roles` 클레임을 기반으로 요청된 작업에 대한 접근을 허용하거나 거부합니다.

---

#### 핵심 기술 구현

*   **Build Tool**: Maven
*   **Framework**: Spring Boot 3.x, Spring Security 6.x, Java 17
*   **Test Framework**: JUnit 5 (TDD 방식 개발)

*   **OAuth 2.1 / OIDC 구현**: `spring-security-oauth2-authorization-server`를 사용하여 인증 서버의 핵심 기능을 구축합니다.
*   **클라이언트 등록**: `RegisteredClientRepository` Bean을 통해 OAuth2 클라이언트(`nuxt-client`, `winforms-client`) 정보를 메모리에 등록합니다. Public 클라이언트이므로 `PKCE (Proof Key for Code Exchange)`를 필수로 사용하도록 설정합니다.
*   **Azure AD 연동**: `application.yml`에 `spring.security.oauth2.client.registration.azure` 속성을 정의하여 OIDC 연동을 구성합니다.
*   **토큰 강화 로직**:
    *   `AuthenticationSuccessHandler`를 구현하여 OIDC 로그인 성공 이벤트를 가로챕니다.
    *   `OAuth2TokenCustomizer<JwtEncodingContext>` Bean을 등록하여, JWT가 생성되기 직전 클레임을 커스터마이징합니다. 이 단계에서 DB 조회를 통해 `roles` 클레임을 추가합니다.
*   **데이터 모델**: `User`와 `Role` 엔티티를 `@ManyToMany` 관계로 매핑하고, `UserRepository`를 통해 사용자 정보를 조회합니다.
*   **보안 필터 체인**: `SecurityFilterChain`을 구성하여 `oauth2Login()`을 활성화하고, 위에서 구현한 커스텀 핸들러와 로직을 통합합니다.

---

#### 실행 및 테스트 방법

1.  **사전 준비**:
    *   `JDK 17` 및 `Maven` 설치가 필요합니다.
    *   **Azure Portal**: App Registration을 생성하고, `애플리케이션 (클라이언트) ID`, `디렉터리 (테넌트) ID`, 그리고 `클라이언트 암호`를 발급받아야 합니다.
    *   리디렉션 URI는 `웹` 플랫폼으로 `http://localhost:9000/login/oauth2/code/azure`를 등록합니다.

2.  **환경 설정**:
    *   `src/main/resources/application.yml` 파일을 열고 `YOUR_..._ID_FROM_AZURE` 플레이스홀더들을 위에서 발급받은 값으로 교체합니다.

3.  **애플리케이션 실행**:
    ```bash
    mvn spring-boot:run
    ```

4.  **데이터베이스 확인**:
    *   애플리케이션은 `dev` 프로파일이 활성화되어 H2 인메모리 데이터베이스를 사용합니다.
    *   웹 브라우저에서 `http://localhost:9000/h2-console`로 접속하여 DB 상태를 확인할 수 있습니다.
    *   **JDBC URL**: `jdbc:h2:mem:authdb`
    *   **Username**: `sa`, **Password**: (없음)
