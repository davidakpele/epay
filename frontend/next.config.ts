import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactCompiler: true,
  devIndicators: {
    position: "bottom-left",
  },
  experimental: {
    appNavFailHandling: false,
    turbopackFileSystemCacheForDev: true,
    turbopackClientSideNestedAsyncChunking: true,
  },
  images: {
    remotePatterns: [
      {
        protocol: "http",
        hostname: "localhost",
        port: "8029",
        pathname: "/api/uploads/images/**",
      },
    ],
  },
};

export default nextConfig;
