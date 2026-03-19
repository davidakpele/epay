import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "ePay Admin Dashboard",
  description: "ePay payment platform admin dashboard",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}