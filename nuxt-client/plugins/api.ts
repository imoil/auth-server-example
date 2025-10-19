import { ofetch } from 'ofetch'

export default defineNuxtPlugin((_nuxtApp) => {
  const config = useRuntimeConfig()

  globalThis.$api = ofetch.create({
    baseURL: config.public.apiBase,

    onRequest({ options }) {
      if (process.server) {
        const event = useRequestEvent()
        if (event) {
          const token = getCookie(event, 'auth_token')
          if (token) {
            const headers = (options.headers ||= {}) as Record<string, string>
            headers.Authorization = `Bearer ${token}`
          }
        }
      }
    },

    onResponseError({ response }) {
      if (response.status === 401) {
        if (process.client) {
          window.location.href = '/api/auth/login'
        }
      }
    }
  })
})
