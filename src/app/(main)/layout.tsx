import Sidebar from "@/components/Sidebar";
import Header from "@/components/Header";
import MobileHeader from "@/components/MobileHeader";
import BottomNav from "@/components/BottomNav";

export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-full min-h-screen bg-cream">
      {/* Desktop sidebar */}
      <div className="hidden md:flex">
        <Sidebar />
      </div>

      <div className="flex flex-col flex-1 min-w-0">
        {/* Desktop header */}
        <div className="hidden md:flex">
          <Header />
        </div>

        {/* Mobile header */}
        <MobileHeader />

        {/* Page content */}
        <main className="flex-1 overflow-y-auto pb-16 md:pb-0">
          {children}
        </main>

        {/* Mobile bottom nav */}
        <BottomNav />
      </div>
    </div>
  );
}
