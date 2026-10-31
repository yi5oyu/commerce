import Link from "next/link";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex flex-1">
      <aside className="w-56 shrink-0 border-r border-black/10 p-4 dark:border-white/15">
        <Link href="/admin" className="font-semibold">
          Commerce Admin
        </Link>
        {/* 메뉴는 도메인 Phase마다 추가 (상품 · 주문 …) */}
        <nav className="mt-6 flex flex-col gap-2 text-sm" />
      </aside>
      <main className="flex-1 p-6">{children}</main>
    </div>
  );
}
