'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    const checkAuth = () => {
      try {
        const token = localStorage.getItem('authToken');
        const userData = localStorage.getItem('userData');
        if (token && userData) {
          router.push('/dashboard');
        } else {
          router.push('/auth/login');
        }
      } catch (error) {
        console.error('Error checking authentication:', error);
        router.push('/auth/login');
      }
    };

    checkAuth();
  }, [router]);

  return (
   <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      background: '#f8fafc'
    }}>
      <div style={{
        width: '40px',
        height: '40px',
        border: '4px solid #f1f5f9',
        borderTopColor: '#dc2626',
        borderRadius: '50%',
        animation: 'spin 0.8s linear infinite'
      }} />
      <style jsx>{`
        @keyframes spin {
          to {
            transform: rotate(360deg);
          }
        }
      `}</style>
    </div>
  );
}