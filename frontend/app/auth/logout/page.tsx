"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { authService, getUserId, removeAuthToken } from "@/app/api";

export default function Logout() {
  const router = useRouter();

  useEffect(() => {
    const doLogout = async () => {
      const userId = getUserId();
      try {
        if (userId) await authService.logout(userId);
      } catch (_) {
        // ignore — clear session regardless
      } finally {
        removeAuthToken();
        router.replace("/default");
      }
    };

    doLogout();
  }, [router]);

  return (
    <>
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
        .logout-spinner {
          width: 36px;
          height: 36px;
          border: 3px solid rgba(255, 255, 255, 0.15);
          border-top-color: rgba(255, 255, 255, 0.75);
          border-radius: 50%;
          animation: spin 0.9s linear infinite;
        }
      `}</style>

      <div
        style={{
          position: "fixed",
          inset: 0,
          background: "#2b0f56",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: "18px",
          zIndex: 9999,
        }}
      >
        <div className="logout-spinner" />
        <p
          style={{
            color: "rgba(255, 255, 255, 0.55)",
            fontSize: "15px",
            fontFamily: "system-ui, -apple-system, sans-serif",
            margin: 0,
            letterSpacing: "0.01em",
          }}
        >
          Logging out...
        </p>
      </div>
    </>
  );
}
