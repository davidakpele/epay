import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// Route configuration classes
class RouteConfig {
  private routes: string[];

  constructor(routes: string[]) {
    this.routes = routes;
  }

  matches(pathname: string): boolean {
    return this.routes.some(route => {
      // Exact match
      if (route === pathname) return true;
      if (route.endsWith('/*')) {
        const baseRoute = route.slice(0, -2);
        return pathname.startsWith(baseRoute);
      }
      
      return false;
    });
  }

  getRoutes(): string[] {
    return this.routes;
  }

  addRoutes(routes: string[]): void {
    this.routes.push(...routes);
  }
}

export class PublicRoutes extends RouteConfig {
  constructor(routes: string[] = []) {
    const defaultPublicRoutes = [
      '/',
      '/auth/logout',
      '/auth/login',
      '/auth/register',
      '/auth/forgot-password',
      '/auth/reset-password',
      '/auth/verify-email',
      '/about',
      '/contact',
      '/complete/profile',
      '/privacy-policy',
      '/terms-of-service',
    ];
    super([...defaultPublicRoutes, ...routes]);
  }
}

// Private routes - require authentication
export class PrivateRoutes extends RouteConfig {
  constructor(routes: string[] = []) {
    const defaultPrivateRoutes = [
      '/dashboard/*',
      '/banks/*',
      '/user/*',
      '/bills/*',
      'kyc/*',
      '/settings/*',
      '/cards/*',
      '/exchange/*',
      '/wallet/*',
      '/payment/*',
      '/refer/*',
      '/statements/*',
      '/beneficiary/*',
      '/support/*',
    ];
    super([...defaultPrivateRoutes, ...routes]);
  }
}

// Auth utility functions
class AuthUtils {
  static isAuthenticated(request: NextRequest): boolean {
    // Check for JWT in cookies
    const jwtCookie = request.cookies.get('jwt');
    const dataCookie = request.cookies.get('data');
    
    if (!jwtCookie || !dataCookie) {
      return false;
    }

    try {
      // Parse the data cookie to verify user data exists
      const dataValue = decodeURIComponent(dataCookie.value);
      const parsedData = JSON.parse(dataValue);
      
      // Verify that user object and JWT exist
      return !!(
        parsedData?.user?._jwt_?.jwt && 
        parsedData.user.userId &&
        jwtCookie.value
      );
    } catch (error) {
      console.error('Error parsing auth data:', error);
      return false;
    }
  }

  static getUserData(request: NextRequest) {
    try {
      const dataCookie = request.cookies.get('data');
      if (!dataCookie) return null;

      const dataValue = decodeURIComponent(dataCookie.value);
      const parsedData = JSON.parse(dataValue);
      return parsedData?.user || null;
    } catch (error) {
      console.error('Error getting user data:', error);
      return null;
    }
  }

  static clearAuthCookies(response: NextResponse): NextResponse {
    response.cookies.delete('jwt');
    response.cookies.delete('data');
    return response;
  }
}

// Initialize route configurations
const publicRoutes = new PublicRoutes();
const privateRoutes = new PrivateRoutes();

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (
    pathname.startsWith('/_next') ||
    pathname.startsWith('/api') ||
    pathname.includes('.') 
  ) {
    return NextResponse.next();
  }

  const isAuthenticated = AuthUtils.isAuthenticated(request);
  const isPublicRoute = publicRoutes.matches(pathname);
  const isPrivateRoute = privateRoutes.matches(pathname);

  if (isAuthenticated && (pathname === '/auth/login' || pathname === '/auth/register')) {
    const url = request.nextUrl.clone();
    url.pathname = '/dashboard';
    return NextResponse.redirect(url);
  }

  // Redirect unauthenticated users from private routes to login
  if (!isAuthenticated && isPrivateRoute) {
    const url = request.nextUrl.clone();
    url.pathname = '/auth/login';
    url.searchParams.set('redirect', pathname);
    return NextResponse.redirect(url);
  }
  if (isPublicRoute) {
    return NextResponse.next();
  }

  // Default: allow the request
  return NextResponse.next();
}

// Configure which paths the middleware should run on
export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder
     */
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
};

// Export classes and utilities for use in other parts of the application
export { AuthUtils, RouteConfig };