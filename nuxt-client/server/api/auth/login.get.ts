import { nanoid } from 'nanoid'
import { createHash } from 'crypto'

function base64URLEncode(str: Buffer) {
  return str.toString('base64')
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
}

function sha256(buffer: string) {
  return createHash('sha256').update(buffer).digest();
}

export default defineEventHandler((event) => {
  const config = useRuntimeConfig(event).oauth

  const codeVerifier = nanoid(32)
  setCookie(event, 'code_verifier', codeVerifier, {
    httpOnly: true,
    maxAge: 60 * 15 // 15 minutes
  })

  const codeChallenge = base64URLEncode(sha256(codeVerifier))

  const params = new URLSearchParams()
  params.append('response_type', 'code')
  params.append('client_id', config.clientId)
  params.append('scope', 'openid profile')
  params.append('redirect_uri', config.redirectUri)
  params.append('code_challenge', codeChallenge)
  params.append('code_challenge_method', 'S256')

  return sendRedirect(event, `${config.authorizationEndpoint}?${params.toString()}`)
})