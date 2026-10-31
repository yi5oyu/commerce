import Link from "next/link";

export default function ShopLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <header className="border-b border-black/10 dark:border-white/15">
        <div className="mx-auto flex h-14 w-full max-w-6xl items-center justify-between px-4">
          <Link href="/" className="font-semibold">
            Commerce
          </Link>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4">{children}</main>
      <footer className="border-t border-black/10 dark:border-white/15">
        <div className="mx-auto w-full max-w-6xl px-4 py-6 text-sm text-zinc-500">© Commerce</div>
      </footer>
    </>
  );
}
