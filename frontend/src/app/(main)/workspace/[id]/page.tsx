type WorkspacePageProps = {
  params: Promise<{ id: string }>;
};

export default async function WorkspacePage({ params }: WorkspacePageProps) {
  const { id } = await params;
  return (
    <section className="space-y-2">
      <h1 className="text-2xl font-semibold">Workspace</h1>
      <p className="text-sm text-zinc-600">当前工作区：{id}</p>
    </section>
  );
}
