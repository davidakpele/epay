import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactCompiler: true,
  devIndicators: false, 
  experimental: {
    appNavFailHandling:false,
    turbopackFileSystemCacheForDev: true,
    turbopackClientSideNestedAsyncChunking: true
  },
  images: {
    remotePatterns: [
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '8292',
        pathname: '/api/uploads/images/**',
      },
    ],
  },
};

export default nextConfig;