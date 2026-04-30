"use client";

import Link from "next/link";
import { Search, BookMarked } from "lucide-react";

export default function MobileHeader() {
  return (
    <header className="sticky top-0 z-30 backdrop-blur-sm bg-cream/95 border-b border-cream-active flex items-center justify-between px-6 py-5 md:hidden">
      <Link href="/explorar" className="flex items-center gap-3">
        <BookMarked size={16} className="text-brown" />
        <div>
          <p className="font-serif font-bold text-brown text-xl leading-none">Alexandria</p>
          <p className="text-slate text-[8px] tracking-widest uppercase opacity-70 mt-0.5">
            BIBLIOTECA DIGITAL
          </p>
        </div>
      </Link>

      <div className="flex items-center gap-4">
        <button className="text-slate">
          <Search size={15} />
        </button>
        <div className="size-8 rounded-xl bg-cream-border overflow-hidden border border-white/10">
          <div className="size-full bg-gradient-to-br from-slate to-brown opacity-60" />
        </div>
      </div>
    </header>
  );
}
