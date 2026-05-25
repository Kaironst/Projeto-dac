import jwt, { JwtPayload } from 'jsonwebtoken'

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
