<template>
  <v-app>
    <v-app-bar>
      <v-toolbar-title>ID Hub Client</v-toolbar-title>
      <v-spacer></v-spacer>

      <template v-if="auth.isLoggedIn">
        <v-avatar class="mr-4">
          <v-icon>mdi-account-circle</v-icon>
        </v-avatar>
        <span>{{ auth.user?.name }}</span>
        <v-btn href="/api/auth/logout" class="ml-4">Logout</v-btn>
      </template>
      <template v-else>
        <v-btn href="/api/auth/login">Login</v-btn>
      </template>

    </v-app-bar>
    <v-main>
      <v-container>
        <h1>Welcome to the Home Page</h1>
        <p>This is the main content area.</p>
        <NuxtPage />
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

// Fetch user on server-side. This will be called during the server-side rendering.
// `fetchUser` will make an API call to `/api/me` using the cookie, 
// and populate the store with the user information.
if (process.server) {
  await auth.fetchUser()
}
</script>
