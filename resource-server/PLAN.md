# 리소스 서버 개발 계획 (TDD)

이 문서는 `resource-server`의 개발 계획을 추적합니다.

- [x] **Task 1: Maven 프로젝트 설정**
    - [x] 필요한 의존성(Spring Web, Security, OAuth2 Resource Server, Thymeleaf)으로 `pom.xml` 생성.
    - [x] 디렉토리 구조 생성.
    - [x] 메인 애플리케이션 클래스 생성.
    - [x] `issuer-uri`를 포함한 `application.yml` 생성.

- [x] **Task 2: 보안 구성 (TDD)**
    - [x] 보호된 엔드포인트에 대한 미인증 요청이 401을 반환하는지 확인하는 테스트 작성.
    - [x] 유효한 JWT를 사용한 보호된 엔드포인트 요청이 성공하는지 확인하는 테스트 작성.
    - [x] JWT 유효성 검사를 활성화하는 `SecurityFilterChain` 구현.

- [x] **Task 3: API 컨트롤러 (TDD)**
    - [x] public, user, admin 엔드포인트에 대한 테스트 작성.
    - [x] `/api/public`, `/api/user`, `/api/admin` 엔드포인트를 가진 `ApiController` 구현.

- [x] **Task 4: 역할 기반 접근 제어(RBAC) (TDD)**
    - [x] 역할에 따라 엔드포인트가 보호되는지 확인하는 테스트 작성.
    - [x] 메서드 수준 보안 활성화.
    - [x] 컨트롤러 메서드에 `@PreAuthorize` 어노테이션을 사용하여 RBAC 적용.

- [x] **Task 5: Thymeleaf를 이용한 UI (TDD)**
    - [x] 웹 UI에 대한 테스트 작성.
    - [x] 메인 페이지를 제공하는 `WebController` 생성.
    - [x] 사용자 정보 및 역할 기반 콘텐츠를 표시하는 Thymeleaf 템플릿 생성.
