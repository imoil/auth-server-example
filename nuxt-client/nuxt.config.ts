// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  modules: [
    '@nuxt/fonts',
    '@nuxt/icon',
    '@nuxt/image',
    '@nuxt/scripts',
    '@nuxt/test-utils',
    'vuetify-nuxt-module',
    '@pinia/nuxt'
    // '/@nuxtjs/storybook'
  ],

  vuetify: {
    treeshaking: true,
    autoImport: true,
  },

  runtimeConfig: {
    oauth: {
      authServerUrl: 'http://localhost:9000',
      clientId: 'nuxt-client',
      clientSecret: 'secret', // Public client이므로 실제로는 사용되지 않음
      tokenEndpoint: 'http://localhost:9000/oauth2/token',
      authorizationEndpoint: 'http://localhost:9000/oauth2/authorize',
      redirectUri: 'http://localhost:3000/api/auth/callback'
    },
    public: {
      apiBase: 'http://localhost:8082/api'
    }
  }
})