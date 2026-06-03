import type { Metadata } from "next";
import { Manrope, Noto_Serif, Playfair_Display } from "next/font/google";
import { AuthProvider } from "@/contexts/AuthContext";
import "./globals.css";

const manrope = Manrope({
  subsets: ["latin"],
  variable: "--font-manrope",
  display: "swap",
});

const notoSerif = Noto_Serif({
  subsets: ["latin"],
  variable: "--font-noto-serif",
  weight: ["700"],
  display: "swap",
});

const playfair = Playfair_Display({
  subsets: ["latin"],
  variable: "--font-playfair",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Alexandria — Biblioteca Digital",
  description: "Sua biblioteca digital pessoal",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html
      lang="pt-BR"
      suppressHydrationWarning
      className={`${manrope.variable} ${notoSerif.variable} ${playfair.variable} h-full`}
    >
      <body className="h-full">
          <AuthProvider>{children}</AuthProvider>
        </body>
    </html>
  );
}
