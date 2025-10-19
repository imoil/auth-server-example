import { defineStore } from 'pinia'

// Define the user type
interface User {
  name: string;
  roles: string[];
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.user,
  },
  actions: {
    setUser(user: User | null) {
        this.user = user
    },
    async fetchUser() {
      if (this.user) return

      try {
        // The $api helper is configured in plugins/api.ts
        // It automatically adds the auth token for server-side requests.
        const userData = await globalThis.$api<User>('/me')
        this.user = userData
      } catch (error) {
        // If the request fails (e.g., 401), the user is not authenticated
        this.user = null
      }
    },
  },
})
