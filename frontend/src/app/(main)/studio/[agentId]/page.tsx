type StudioPageProps = {
  params: Promise<{ agentId: string }>;
};

export default async function StudioPage({ params }: StudioPageProps) {
  const { agentId } = await params;
  return (
    <section className="space-y-2">
      <h1 className="text-2xl font-semibold">Studio</h1>
      <p className="text-sm text-zinc-600">当前 Agent：{agentId}</p>
    </section>
  );
}
