"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { register } from "@/lib/services/auth-service";

export default function RegisterPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const onRegister = async () => {
    const nextUsername = username.trim();
    const nextPassword = password.trim();
    const nextConfirmPassword = confirmPassword.trim();
    if (!nextUsername || !nextPassword || !nextConfirmPassword) {
      toast.error("请完整填写用户、密码、确认密码");
      return;
    }
    if (nextPassword !== nextConfirmPassword) {
      toast.error("两次输入的密码不一致");
      return;
    }
    setLoading(true);
    try {
      await register({ username: nextUsername, password: nextPassword });
      toast.success("注册成功，请登录");
      router.push("/login");
    } catch (error) {
      const message = error instanceof Error ? error.message : "注册失败，请稍后重试";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-md items-center px-6">
      <Card className="w-full">
        <CardHeader>
          <CardTitle>注册</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">用户</Label>
              <Input
                id="username"
                name="username"
                placeholder="请输入用户"
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
                autoComplete="new-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">确认密码</Label>
              <Input
                id="confirmPassword"
                name="confirmPassword"
                placeholder="请再次输入密码"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
              />
            </div>
            <Button type="button" className="w-full" disabled={loading} onClick={onRegister}>
              {loading ? "注册中..." : "注册"}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              已有账号？{" "}
              <Link href="/login" className="text-primary hover:underline">
                去登录
              </Link>
            </p>
          </div>
        </CardContent>
      </Card>
    </main>
  );
}
