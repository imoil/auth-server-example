# auth-server 개발 계획 (TDD)

이 문서는 중앙 인증 허브(`auth-server`) 개발을 위한 작업 계획 및 진행 상황을 추적합니다. 모든 기능 구현은 TDD(Test-Driven Development)를 따릅니다.

- [x] **Task 1: Maven 프로젝트 구조 설정**
    - [x] `pom.xml` 파일 생성 및 의존성 추가
    - [x] `src/main/java`, `src/main/resources`, `src/test/java` 디렉토리 구조 생성
    - [x] `application.yml` 및 `application-dev.yml` 기본 설정 파일 생성

- [x] **Task 2: JPA 엔티티 및 리포지토리 구현 (TDD)**
    - [x] `UserRepository` CRUD 테스트 케이스 작성 (`src/test/java`)
    - [x] `User`, `Role` 엔티티 및 `UserRepository` 인터페이스 구현 (`src/main/java`)
    - [x] 테스트 통과 확인

- [x] **Task 3: OAuth2 클라이언트 등록 기능 구현 (TDD)**
    - [x] `RegisteredClientRepository` Bean 설정 및 검증 테스트 작성
    - [x] `SecurityConfig`에 `RegisteredClientRepository` Bean 구현
    - [x] 테스트 통과 확인

- [x] **Task 4: 토큰 커스터마이징 기능 구현 (TDD)**
    - [x] `OAuth2TokenCustomizer` 클레임 추가 검증 테스트 작성 (Mockito 사용)
    - [x] `SecurityConfig`에 `tokenCustomizer` Bean 구현
    - [x] 테스트 통과 확인

- [x] **Task 5: 전체 보안 필터 체인 통합 (TDD)**
    - [x] 보호된 리소스 접근 시 OIDC 리디렉션 검증 통합 테스트 작성
    - [x] `SecurityFilterChain` Bean 최종 구현 및 통합
    - [x] 테스트 통과 확인

- [x] **Task 6: 초기 데이터 생성**
    - [x] 애플리케이션 실행 시 테스트 데이터 생성을 위한 `CommandLineRunner` 구현