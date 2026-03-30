"use client";

import Link from "next/link";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export default function ExplorePage() {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">Explore</h1>
      <p className="text-sm text-zinc-600">这里是前端初始化后的主入口页面。</p>
      <div className="flex gap-3">
        <Link href="/workspace/demo" className={cn(buttonVariants({ variant: "default" }))}>
          进入 Workspace
        </Link>
        <Link href="/studio/demo-agent" className={cn(buttonVariants({ variant: "outline" }))}>
          进入 Studio
        </Link>
      </div>
    </section>
  );
}
