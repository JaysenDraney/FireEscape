// Serves each built look on its own local port. Listens on 127.0.0.1 only, so nothing is exposed to the network.
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const sites = { ember: 4321, terminal: 4322, aurora: 4323, escape: 4324 };

const types = {
	'.html': 'text/html; charset=utf-8',
	'.css': 'text/css; charset=utf-8',
	'.js': 'text/javascript; charset=utf-8',
	'.mjs': 'text/javascript; charset=utf-8',
	'.json': 'application/json; charset=utf-8',
	'.svg': 'image/svg+xml',
	'.png': 'image/png',
	'.webp': 'image/webp',
	'.ico': 'image/x-icon',
	'.txt': 'text/plain; charset=utf-8',
	'.xml': 'application/xml',
	'.woff2': 'font/woff2',
	'.wasm': 'application/wasm',
};

for (const [name, port] of Object.entries(sites)) {
	const dir = path.join(root, `dist-${name}`);
	if (!fs.existsSync(dir)) {
		console.error(`missing ${dir}. Run: npm run themes:build`);
		continue;
	}
	http
		.createServer((req, res) => {
			const pathname = decodeURIComponent(new URL(req.url ?? '/', 'http://localhost').pathname);
			let file = path.join(dir, pathname);
			if (!file.startsWith(dir)) {
				res.writeHead(403);
				return res.end('Forbidden');
			}
			if (fs.existsSync(file) && fs.statSync(file).isDirectory()) file = path.join(file, 'index.html');
			if (!fs.existsSync(file)) {
				const notFound = path.join(dir, '404.html');
				res.writeHead(404, { 'content-type': 'text/html; charset=utf-8' });
				return res.end(fs.existsSync(notFound) ? fs.readFileSync(notFound) : 'Not found');
			}
			res.writeHead(200, { 'content-type': types[path.extname(file)] ?? 'application/octet-stream' });
			fs.createReadStream(file).pipe(res);
		})
		.listen(port, '127.0.0.1', () => console.log(`${name}: http://localhost:${port}/`));
}
