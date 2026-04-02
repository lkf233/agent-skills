"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BookOpen, BriefcaseBusiness, Puzzle, Search, Wrench } from "lucide-react";
import { cn } from "@/lib/utils";

const navItems = [
  { href: "/explore", matchPrefix: "/explore", label: "探索", icon: Search },
  { href: "/workspace/demo", matchPrefix: "/workspace", label: "工作台", icon: BriefcaseBusiness },
  { href: "/knowledge/demo-kb", matchPrefix: "/knowledge", label: "知识库", icon: BookOpen },
  { href: "/studio/demo-agent", matchPrefix: "/studio", label: "工具", icon: Wrench },
];

export function TopNav() {
  const pathname = usePathname();

  return (
    <header className="sticky top-0 z-20 border-b border-border/80 bg-background">
      <div className="flex h-14 w-full items-center justify-between px-6">
        <div className="flex items-center gap-4">
          <Link href="/explore" className="text-[20px] font-bold tracking-tight text-[#101828]">
            Dify
          </Link>
          <div className="hidden items-center gap-2 rounded-full border border-border px-2 py-1 text-[11px] text-muted-foreground md:flex">
            <span className="inline-flex size-5 items-center justify-center rounded-full bg-primary/15 text-[10px] text-primary">
              2
            </span>
            <span className="max-w-[180px] truncate">210554741@qq.com</span>
            <button
              type="button"
              className="rounded-full bg-primary px-2 py-0.5 text-[10px] font-medium text-primary-foreground"
            >
              升级
            </button>
          </div>
        </div>

        <nav className="hidden items-center gap-1 md:flex">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = pathname.startsWith(item.matchPrefix);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "inline-flex h-8 items-center gap-1.5 rounded-md px-3 text-[13px] text-muted-foreground transition-colors hover:bg-primary/5 hover:text-foreground",
                  active && "bg-primary/10 text-primary"
                )}
              >
                <Icon className="size-4" />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="flex items-center gap-3 text-sm">
          <Link href="/traces/demo-trace" className="hidden items-center gap-1 text-muted-foreground md:inline-flex">
            <Puzzle className="size-4" />
            插件
          </Link>
          <button
            type="button"
            className="inline-flex size-7 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground"
          >
            2
          </button>
        </div>
      </div>
    </header>
  );
}
