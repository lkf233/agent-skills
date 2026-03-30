import { TopNav } from "@/components/top-nav";

export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-muted/40">
      <TopNav />
      <main className="w-full px-6 py-4">{children}</main>
    </div>
  );
}
