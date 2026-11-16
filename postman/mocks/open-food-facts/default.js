// Read-only fixture based on OFF v2 envelope/schema; data below is synthetic.
// The real adapter is not a simulation member.
const http = require("node:http");

http.createServer((req, res) => {
  const url = new URL(req.url, "http://localhost");
  const match = /^\/api\/v2\/product\/(\d+)\.json$/.exec(url.pathname);
  if (req.method !== "GET" || !match) {
    res.writeHead(501).end("Unmodelled endpoint");
    return;
  }
  if (!req.headers["user-agent"] || !url.searchParams.has("fields")) {
    res.writeHead(400, { "content-type": "application/json" });
    res.end(JSON.stringify({ error: "User-Agent and fields expected by this fixture" }));
    return;
  }
  const code = match[1];
  const reply = (status, body) => {
    res.writeHead(status, { "content-type": "application/json" });
    res.end(JSON.stringify(body));
  };
  if (code === "96385074") return reply(404, { code, status: 0, status_verbose: "product not found" });
  if (code === "012345678905") return reply(503, { error: "provider unavailable" });
  if (code === "4006381333931") {
    res.writeHead(500, { "content-type": "text/html" }).end("<html>upstream error</html>");
    return;
  }
  if (code === "5901234123457") {
    setTimeout(() => reply(200, { code, status: 1, product: { product_name: "Slow fixture" } }), 500);
    return;
  }
  if (code !== "3017624010701") {
    res.writeHead(501).end("Unmodelled barcode");
    return;
  }
  reply(200, { code, status: 1, product: {
    product_name: "Produto de teste", brands: "Marca de teste",
    categories: "Categoria externa", product_quantity_unit: "g",
  } });
}).listen(process.env.PORT);
