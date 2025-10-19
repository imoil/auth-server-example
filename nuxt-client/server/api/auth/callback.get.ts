import { ofetch } from 'ofetch'

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event).oauth
  const { code } = getQuery(event)

  const codeVerifier = getCookie(event, 'code_verifier')
  // Clear the cookie after retrieving it
  deleteCookie(event, 'code_verifier')

  if (!codeVerifier) {
    console.error("Code verifier not found in cookie")
    return sendRedirect(event, '/login-failed?error=missing_verifier')
  }

  const body = new URLSearchParams()
  body.append('grant_type', 'authorization_code')
  body.append('code', code as string)
  body.append('redirect_uri', config.redirectUri)
  body.append('client_id', config.clientId)
  body.append('code_verifier', codeVerifier)

  try {
    const tokens = await ofetch(config.tokenEndpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body
    })

    setCookie(event, 'auth_token', tokens.access_token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: tokens.expires_in
    })

    return sendRedirect(event, '/')
  } catch (error) {
    console.error("Token exchange failed:", error)
    return sendRedirect(event, '/login-failed')
  }
})