import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest) {
  const url = request.nextUrl.searchParams.get("url");
  if (!url) {
    return new NextResponse("Missing url parameter", { status: 400 });
  }

  const res = await fetch(url, {
    headers: { "User-Agent": "Mozilla/5.0 (compatible; Alexandria/1.0)" },
  });

  if (!res.ok) {
    return new NextResponse("Falha ao buscar arquivo EPUB", { status: res.status });
  }

  const data = await res.arrayBuffer();
  return new NextResponse(data, {
    headers: {
      "Content-Type": "application/epub+zip",
      "Cache-Control": "public, max-age=86400",
    },
  });
}
