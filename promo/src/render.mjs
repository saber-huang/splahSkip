// 用 Chromium 打开 index.html，逐帧调用 renderFrame(t) 并截图，交给 ffmpeg 编码。
// 用法：
//   node src/render.mjs video <输出.mp4>
//   node src/render.mjs stills <输出目录> <t1> [t2 ...]   单独渲染某几个时刻，检查画面用
import { chromium } from 'playwright';
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import fs from 'node:fs';
import path from 'node:path';

const FPS = 30;
const here = path.dirname(fileURLToPath(import.meta.url));
const [mode, out, ...times] = process.argv.slice(2);
if (!['video', 'stills'].includes(mode) || !out) {
  console.error('用法：node src/render.mjs video <out.mp4> | stills <dir> <t...>');
  process.exit(1);
}

const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1080, height: 1920 }, deviceScaleFactor: 1 });
await page.goto('file://' + path.join(here, 'index.html'));
await page.evaluate(() => document.fonts.ready);
for (const t of [0.8, 5, 11]) await page.evaluate(t => window.renderFrame(t), t); // 先把字体和图片都加载一遍

async function frame(t) {
  await page.evaluate(t => window.renderFrame(t), t);
  return page.screenshot({ type: 'png' });
}

if (mode === 'stills') {
  fs.mkdirSync(out, { recursive: true });
  for (const t of times) fs.writeFileSync(path.join(out, `t${t}.png`), await frame(Number(t)));
} else {
  const total = Math.round(await page.evaluate(() => window.DURATION) * FPS);
  const ff = spawn('ffmpeg', [
    '-v', 'error', '-y', '-f', 'image2pipe', '-framerate', String(FPS), '-c:v', 'png', '-i', '-',
    '-vf', 'scale=out_color_matrix=bt709:out_range=tv,format=yuv420p',
    '-c:v', 'libx264', '-preset', 'slow', '-crf', '17', '-profile:v', 'high',
    '-colorspace', 'bt709', '-color_primaries', 'bt709', '-color_trc', 'bt709',
    '-movflags', '+faststart', out,
  ], { stdio: ['pipe', 'inherit', 'inherit'] });
  const done = new Promise(r => ff.on('close', r));
  const start = Date.now();
  for (let i = 0; i < total; i++) {
    const buf = await frame(i / FPS);
    if (!ff.stdin.write(buf)) await new Promise(r => ff.stdin.once('drain', r));
    if (i % 60 === 0) process.stdout.write(`\r${i}/${total}`);
  }
  ff.stdin.end();
  const code = await done;
  console.log(`\r${total} 帧，用时 ${((Date.now() - start) / 1000).toFixed(0)} 秒`);
  if (code !== 0) process.exitCode = 1;
}
await browser.close();
