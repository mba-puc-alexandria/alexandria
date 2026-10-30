import { NextRequest, NextResponse } from "next/server";

// Esta rota roda no servidor: dentro do Docker, "localhost" é o próprio contêiner do frontend.
// API_INTERNAL_URL aponta para o backend pela rede interna; sem ela, usa a URL pública.
const API_URL = process.env.API_INTERNAL_URL ?? process.env.NEXT_PUBLIC_API_URL!;

export async function GET(request: NextRequest) {
  const bookId = request.nextUrl.searchParams.get("bookId");
  if (!bookId) {
    return new NextResponse("Missing bookId parameter", { status: 400, headers: { "Cache-Control": "no-store" } });
  }

  // O proxy roda no servidor e só enxerga o cookie, não o localStorage.
  const token = request.cookies.get("auth-token")?.value;
  if (!token) {
    return new NextResponse("Unauthorized", { status: 401, headers: { "Cache-Control": "no-store" } });
  }

  const res = await fetch(`${API_URL}/books/${bookId}/epub`, {
    cache: "no-store",
    headers: {
      Authorization: `Bearer ${token}`,
      "User-Agent": "Mozilla/5.0 (compatible; Alexandria/1.0)",
    },
  });

  if (res.status === 402 || res.status === 403) {
    return new NextResponse("Subscription required", { status: 402, headers: { "Cache-Control": "no-store" } });
  }

  if (!res.ok) {
    return new NextResponse("Falha ao buscar arquivo EPUB", { status: res.status, headers: { "Cache-Control": "no-store" } });
  }

  const data = await res.arrayBuffer();
  return new NextResponse(data, {
    headers: {
      "Content-Type": "application/epub+zip",
      "Cache-Control": "no-store",
      "X-Authenticated-User-Id": res.headers.get("X-Authenticated-User-Id") ?? "",
    },
  });
}

export async function HEAD(request: NextRequest) {
  const bookId = request.nextUrl.searchParams.get("bookId");
  if (!bookId) return new NextResponse(null, { status: 400, headers: { "Cache-Control": "no-store" } });
  const token = request.cookies.get("auth-token")?.value;
  if (!token) return new NextResponse(null, { status: 401, headers: { "Cache-Control": "no-store" } });

  const res = await fetch(`${API_URL}/books/${bookId}/epub`, {
    method: "HEAD",
    cache: "no-store",
    headers: { Authorization: `Bearer ${token}` },
  });
  return new NextResponse(null, {
    status: res.status === 402 || res.status === 403 ? 402 : res.status,
    headers: {
      "Cache-Control": "no-store",
      "X-Authenticated-User-Id": res.headers.get("X-Authenticated-User-Id") ?? "",
    },
  });
}
