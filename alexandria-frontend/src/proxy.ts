import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const PRIVATE_PATHS = ['/biblioteca', '/dashboard', '/configuracoes'];
const AUTH_PATHS = ['/login', '/registrar'];

export function proxy(request: NextRequest) {
  const token = request.cookies.get('auth-token')?.value;
  const { pathname } = request.nextUrl;

  const isPrivate = PRIVATE_PATHS.some((p) => pathname === p || pathname.startsWith(p + '/'));
  const isAuth = AUTH_PATHS.some((p) => pathname === p);

  if (isPrivate && !token) {
    return NextResponse.redirect(new URL('/explorar?auth=1', request.url));
  }

  if (isAuth && token) {
    return NextResponse.redirect(new URL('/explorar', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!_next/static|_next/image|favicon.ico|api).*)'],
};
