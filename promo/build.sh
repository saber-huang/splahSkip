#!/bin/bash
# 生成 SplashSkip 抖音宣传视频（1080x1920 竖屏，30fps，13.6 秒）和封面预览图。
# 素材：a.mp4 = 没装，b.mp4 = 装了。
# 画面写在 src/index.html 里，src/render.mjs 用 Chromium 逐帧截图，再交给 ffmpeg 编码。
# 需要：ffmpeg、Node.js、字体 Noto Sans CJK SC 和 Inter（Ubuntu：apt install fonts-noto-cjk fonts-noto-cjk-extra fonts-inter）。
# 第一次在自己电脑上运行，还要执行一次 npx playwright install chromium。
set -e
cd "$(dirname "$0")"
[ -d node_modules/playwright ] || npm install --no-audit --no-fund

# 1) 录屏拆成逐帧图片，去掉顶部 56px 状态栏（那里有录屏计时的红点）
for f in a b; do
  if [ ! -f out/frames/$f/0001.jpg ]; then
    mkdir -p out/frames/$f
    ffmpeg -v error -y -i $f.mp4 -vf "fps=30,crop=576:1224:0:56" -q:v 2 out/frames/$f/%04d.jpg
  fi
done

# 2) 逐帧渲染并编码（约 5 分钟）
node src/render.mjs video splashskip_promo.mp4

# 3) 封面预览：两边计时都停下之后的一帧
node src/render.mjs stills out/stills 8.4
cp out/stills/t8.4.png cover_preview.png
