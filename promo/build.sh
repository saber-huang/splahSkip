#!/bin/bash
# 生成 SplashSkip 抖音宣传视频（1080x1920，竖屏）。素材：a.mp4=没装，b.mp4=装了
set -e
F=/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc
BG=0x0f172a
ENC="-c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p -r 30 -an"
mkdir -p out

# 1) 钩子 2.5s
ffmpeg -v error -y -f lavfi -i "color=c=$BG:s=1080x1920:r=30:d=2.5" -vf "
drawtext=fontfile=$F:text='打开 App 之前':fontcolor=white:fontsize=84:x=(w-text_w)/2:y=700,
drawtext=fontfile=$F:text='你每天要看多久开屏广告？':fontcolor=0xfbbf24:fontsize=84:x=(w-text_w)/2:y=830,
drawtext=fontfile=$F:text='3 … 2 … 1 …':fontcolor=0x94a3b8:fontsize=64:x=(w-text_w)/2:y=1050:enable='gte(t,0.8)'
" $ENC out/s1.mp4

# 2) 分屏对比 7s
ffmpeg -v error -y -i a.mp4 -i b.mp4 -filter_complex "
color=c=$BG:s=1080x1920:r=30:d=7,
drawbox=x=10:y=335:w=530:h=1166:color=white@0.12:t=fill,
drawbox=x=540:y=335:w=530:h=1166:color=white@0.12:t=fill,
drawtext=fontfile=$F:text='同一台手机 · 同一个 App':fontcolor=white:fontsize=64:x=(w-text_w)/2:y=100,
drawtext=fontfile=$F:text='打开网易有道词典，看开屏广告':fontcolor=0x94a3b8:fontsize=40:x=(w-text_w)/2:y=200,
drawtext=fontfile=$F:text='没装':fontcolor=0xf87171:fontsize=56:x=275-text_w/2:y=1530,
drawtext=fontfile=$F:text='装了 SplashSkip':fontcolor=0x4ade80:fontsize=56:x=805-text_w/2:y=1530,
drawtext=fontfile=$F:text=' 干等广告 约4秒 ':fontcolor=white:fontsize=48:box=1:boxcolor=0xdc2626@0.9:boxborderw=14:x=275-text_w/2:y=1650:enable='gte(t,6.3)',
drawtext=fontfile=$F:text=' 约1秒直达首页 ':fontcolor=white:fontsize=48:box=1:boxcolor=0x16a34a@0.9:boxborderw=14:x=805-text_w/2:y=1650:enable='gte(t,4.8)'
[bg];
[0:v]trim=start=1:end=8,setpts=PTS-STARTPTS,fps=30,scale=520:-2[a];
[1:v]trim=start=1.25:end=7,setpts=PTS-STARTPTS,fps=30,scale=520:-2,tpad=stop_mode=clone:stop_duration=1.25[b];
[bg][a]overlay=15:340[t];[t][b]overlay=545:340
" $ENC out/s2.mp4

# 3) 结尾 4.5s
ffmpeg -v error -y -f lavfi -i "color=c=$BG:s=1080x1920:r=30:d=4.5" -vf "
drawtext=fontfile=$F:text='SplashSkip':fontcolor=white:fontsize=120:x=(w-text_w)/2:y=520,
drawtext=fontfile=$F:text='自动点掉开屏广告的“跳过”':fontcolor=0xfbbf24:fontsize=58:x=(w-text_w)/2:y=700,
drawtext=fontfile=$F:text='不联网  ·  不上传  ·  只对你勾选的 App 生效':fontcolor=white:fontsize=42:x=(w-text_w)/2:y=880,
drawtext=fontfile=$F:text='免费开源  ·  下载方式见主页':fontcolor=0x4ade80:fontsize=48:x=(w-text_w)/2:y=1010,
drawtext=fontfile=$F:text='目前测试：华为手机 + 有道词典，其他机型 App 不保证':fontcolor=0x94a3b8:fontsize=32:x=(w-text_w)/2:y=1650,
drawtext=fontfile=$F:text='仅供个人使用':fontcolor=0x94a3b8:fontsize=32:x=(w-text_w)/2:y=1710
" $ENC out/s3.mp4

printf "file 's1.mp4'\nfile 's2.mp4'\nfile 's3.mp4'\n" > out/list.txt
ffmpeg -v error -y -f concat -i out/list.txt -c copy splashskip_promo.mp4
ffmpeg -v error -y -ss 5.5 -i splashskip_promo.mp4 -frames:v 1 cover_preview.png
ffprobe -v error -show_entries format=duration,size -of default=nw=1 splashskip_promo.mp4
