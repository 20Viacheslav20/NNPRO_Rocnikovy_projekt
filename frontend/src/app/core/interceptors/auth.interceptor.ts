import { HttpInterceptorFn } from '@angular/common/http';

const AUTH_SKIP = /\/api\/auth\/(login|register|refresh)(\/)?$/i;
const JWT_RE = /^[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+$/;

function cleanToken(raw: string | null): string | null {
  if (!raw) return null;
  let t = raw.trim().replace(/^"+|"+$/g, '').replace(/^Bearer\s+/i, '').trim();
  if (!JWT_RE.test(t)) return null;
  return t;
}

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  // не трогаем авторизационные эндпоинты
  if (AUTH_SKIP.test(req.url)) return next(req);

  const token = cleanToken(localStorage.getItem('token'));
  if (!token) return next(req);

  const authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  return next(authReq);
};
