import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  distDir: process.env.E2E_ISOLATED_BUILD === "1" ? ".next/e2e" : ".next",
};

export default nextConfig;
