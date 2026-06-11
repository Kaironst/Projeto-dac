import jwt, { JwtPayload } from 'jsonwebtoken'
import { jwtDecode } from 'jwt-decode';

const GetPublicKey = () => process.env.JWT_PUBLIC_KEY!
  .replace("-----BEGIN PUBLIC KEY-----", "")
  .replace("-----END PUBLIC KEY-----", "")
  .replace(/\s/g, "");
export const publicKey = GetPublicKey();

export function validateJwt(token: string) {
  let result: string | JwtPayload | null;
  try {
    result = jwt.verify(token, publicKey, {
      algorithms: ["RS256"]
    })
  } catch (err) {
    console.error(err);
    result = null;
  }
  return result !== null ? true : false
}

export function getRoles(token: string) {
  let roles: string[] = []
  try {
    let decoded = jwtDecode(token);
    roles = (decoded as any).roles as string[]
  }
  catch (err) {
    console.error(err);
    roles = []
  }
  return roles
}
