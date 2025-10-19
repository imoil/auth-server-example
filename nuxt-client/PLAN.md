# Nuxt.js 클라이언트 개발 계획 (TDD)

이 문서는 `nuxt-client` 개발을 위한 작업 계획 및 진행 상황을 추적합니다.

- [ ] **Task 1: 프로젝트 초기 설정**
    - [ ] `nuxi`를 사용하여 Nuxt 프로젝트 생성
    - [ ] Vuetify, Pinia, Vitest, Storybook 의존성 추가 및 설정

- [ ] **Task 2: 인증 흐름 구현 (OIDC)**
    - [ ] 로그인 시작을 위한 `/api/auth/login` 서버 라우트 작성 (TDD)
    - [ ] `auth-server`로부터의 콜백을 처리하고 토큰을 교환하는 `/api/auth/callback` 서버 라우트 작성 (TDD)
    - [ ] PKCE(Proof Key for Code Exchange) 로직 구현
    - [ ] 발급받은 토큰을 안전한 `httpOnly` 쿠키에 저장

- [ ] **Task 3: API 연동 플러그인 구현**
    - [ ] 전역 `$api` fetch 헬퍼를 생성하는 Nuxt 플러그인 작성
    - [ ] `onRequest` 인터셉터를 사용하여 서버 사이드에서 API 요청 시 `Authorization` 헤더 자동 추가 (TDD)
    - [ ] `onResponseError` 인터셉터를 사용하여 401 에러 발생 시 로그인 페이지로 리디렉션

- [ ] **Task 4: UI 및 상태 관리**
    - [ ] Vuetify를 사용한 기본 레이아웃 구성
    - [ ] 로그인/로그아웃 버튼 및 상태 표시 UI 구현
    - [ ] 인증된 사용자만 접근 가능한 보호된 페이지 생성
    - [ ] Pinia를 사용하여 사용자 정보 및 인증 상태 관리 (TDD)

- [ ] **Task 5: 테스트 및 문서화**
    - [ ] Vitest를 사용하여 핵심 로직 및 컴포넌트 단위 테스트 작성
    - [ ] Storybook을 사용하여 주요 UI 컴포넌트 스토리 작성
