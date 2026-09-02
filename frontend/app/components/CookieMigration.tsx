'use client';

import { useEffect } from 'react';

/**
 * One-time migration: strips any leftover `secure; samesite=None` cookies that
 * the server cannot read over HTTP (localhost), and re-writes them with
 * `samesite=Lax` so the middleware can authenticate correctly.
 */
export default function CookieMigration() {
  useEffect(() => {
    if (typeof document === 'undefined') return;

    const migrated = sessionStorage.getItem('_cookie_migrated');
    if (migrated) return;

    // Helper: read a raw cookie value by name
    const getCookie = (name: string): string | null => {
      const match = document.cookie
        .split('; ')
        .find(row => row.startsWith(name + '='));
      return match ? match.split('=').slice(1).join('=') : null;
    };

    const jwt  = getCookie('jwt');
    const data = getCookie('data');

    if (jwt || data) {
      // Delete old cookies (all possible attribute combos)
      const deletions = [
        `jwt=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT`,
        `data=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT`,
        `jwt=; path=/; secure; samesite=None; expires=Thu, 01 Jan 1970 00:00:00 GMT`,
        `data=; path=/; secure; samesite=None; expires=Thu, 01 Jan 1970 00:00:00 GMT`,
      ];
      deletions.forEach(d => { document.cookie = d; });

      // Re-write with correct flags
      if (jwt)  document.cookie = `jwt=${jwt}; path=/; samesite=Lax`;
      if (data) document.cookie = `data=${data}; path=/; samesite=Lax`;
    }

    sessionStorage.setItem('_cookie_migrated', '1');
  }, []);

  return null;
}
