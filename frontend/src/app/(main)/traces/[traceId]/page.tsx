type TracesPageProps = {
  params: Promise<{ traceId: string }>;
};

export default async function TracesPage({ params }: TracesPageProps) {
  const { traceId } = await params;
  return (
    <section className="space-y-2">
      <h1 className="text-2xl font-semibold">Traces</h1>
      <p className="text-sm text-zinc-600">当前追踪链路：{traceId}</p>
    </section>
  );
}
