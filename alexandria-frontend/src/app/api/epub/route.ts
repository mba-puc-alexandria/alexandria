import { NextRequest, NextResponse } from "next/server";

const API_URL = process.env.NEXT_PUBLIC_API_URL!;

export async function GET(request: NextRequest) {
  const bookId = request.nextUrl.searchParams.get("bookId");
  if (!bookId) {
    return new NextResponse("Missing bookId parameter", { status: 400 });
  }

  // O proxy roda no servidor e só enxerga o cookie, não o localStorage.
  const token = request.cookies.get("auth-token")?.value;
  if (!token) {
    return new NextResponse("Unauthorized", { status: 401 });
  }

  const res = await fetch(`${API_URL}/books/${bookId}/epub`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "User-Agent": "Mozilla/5.0 (compatible; Alexandria/1.0)",
    },
  });

  if (res.status === 402 || res.status === 403) {
    return new NextResponse("Subscription required", { status: 402 });
  }

  if (!res.ok) {
    return new NextResponse("Falha ao buscar arquivo EPUB", { status: res.status });
  }

  const data = await res.arrayBuffer();
  return new NextResponse(data, {
    headers: {
      "Content-Type": "application/epub+zip",
      "Cache-Control": "private, max-age=3600",
    },
  });
}
