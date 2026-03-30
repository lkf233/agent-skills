"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { login } from "@/lib/services/auth-service";

export default function LoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const onLogin = async () => {
    const nextUsername = username.trim();
    const nextPassword = password.trim();
    if (!nextUsername || !nextPassword) {
      toast.error("请输入用户名和密码");
      return;
    }
    setLoading(true);
    try {
      const response = await login({ username: nextUsername, password: nextPassword });
      localStorage.setItem("accessToken", response.data.accessToken);
      toast.success("登录成功");
      router.push("/explore");
    } catch (error) {
      const message = error instanceof Error ? error.message : "登录失败，请检查用户名或密码";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-md items-center px-6">
      <Card className="w-full">
        <CardHeader>
          <CardTitle>登录</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">用户名</Label>
              <Input
                id="username"
                name="username"
                placeholder="请输入用户名"
                autoComplete="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">密码</Label>
              <Input
                id="password"
                name="password"
                placeholder="请输入密码"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </div>
            <Button type="button" className="w-full" disabled={loading} onClick={onLogin}>
              {loading ? "登录中..." : "登录"}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              还没有账号？{" "}
              <Link href="/register" className="text-primary hover:underline">
                去注册
              </Link>
            </p>
          </div>
        </CardContent>
      </Card>
    </main>
  );
}
