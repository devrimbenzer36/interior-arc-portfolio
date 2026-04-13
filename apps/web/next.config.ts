import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  turbopack: {
    // Turbopack'e doğru proje kökünü söyle.
    // C:\Users\Asus içindeki yabancı package-lock.json yüzünden yanlış root
    // seçiliyordu — bu ayar sorunu düzeltir.
    root: path.resolve(__dirname),
  },
};

export default nextConfig;
