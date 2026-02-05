'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { authService, getSessionId, removeAuthToken } from '@/app/api';

export default function Logout() {
  const router = useRouter();
  const [dots, setDots] = useState('');
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);

    // Animated dots for loading text
    const dotsInterval = setInterval(() => {
      setDots(prev => prev.length >= 3 ? '' : prev + '.');
    }, 500);

    const checkAuth = async () => {
      // Wait 3 seconds before making request
      await new Promise(resolve => setTimeout(resolve, 3000));

      const sessionId = getSessionId();
      if(sessionId == null){
        router.push('/auth/login');
        return;
      }
      const response = await authService.logout(sessionId);
      if (response?.status === 'success') {
        removeAuthToken();
        router.push('/auth/login');
        window.location.reload();
      } else {
        throw new Error(response?.data?.message || 'Logout failed');
      }
    };
    
    checkAuth();

    return () => clearInterval(dotsInterval);
  }, [router]);

  if (!mounted) {
    return null;
  }

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      background: '#0f0f0f',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
    }}>
      <div style={{
        textAlign: 'center',
        position: 'relative'
      }}>
        {/* Glowing circle loader */}
        <div style={{
          position: 'relative',
          width: '120px',
          height: '120px',
          margin: '0 auto 40px'
        }}>
          <div style={{
            position: 'absolute',
            width: '100%',
            height: '100%',
            border: '3px solid rgba(159, 29, 9, 0.2)',
            borderRadius: '50%',
            borderTopColor: '#c3290b',
            animation: 'rotate 1.5s linear infinite'
          }} />
          <div style={{
            position: 'absolute',
            width: '100%',
            height: '100%',
            border: '3px solid transparent',
            borderRadius: '50%',
            borderRightColor: '#c3290b',
            animation: 'rotate 2s linear infinite reverse'
          }} />
          <div style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            width: '60px',
            height: '60px',
            background: 'linear-gradient(135deg, #c3290b 0%, #c3290b 100%)',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '24px'
          }}>
            👋
          </div>
        </div>

        <style jsx>{`
          @keyframes rotate {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
          @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
          }
        `}</style>

        <h2 style={{
          color: '#fff',
          fontSize: '32px',
          marginBottom: '12px',
          fontWeight: '700',
          letterSpacing: '-0.5px'
        }}>
          Logging you out{dots}
        </h2>

        <p style={{
          color: '#9ca3af',
          fontSize: '16px',
          margin: '0',
          animation: 'pulse 2s ease-in-out infinite'
        }}>
          Securing your session
        </p>
      </div>
    </div>
  );
}