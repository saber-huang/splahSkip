# 发布新版本

给别人下载的安装包必须用**自己的密钥签名**。下面是第一次发布和以后每次发布的步骤。

## 一次性准备：创建签名密钥

在 Android Studio 里：**Build → Generate Signed App Bundle / APK → APK → Next → Create new…**

- **Key store path**：选一个**项目文件夹以外**的位置，比如 `~/Documents/splashskip-release.jks`。
- 设置 **Key store 密码**、**Key alias**（比如 `splashskip`）、**Key 密码**，有效期（Validity）填 25 年以上，Certificate 里至少填一个名字。

> **⚠️ 密钥文件和密码一定要备份到至少两个地方（比如网盘 + 密码管理器）。**
> 弄丢了，以后就没法给已经安装的用户更新，他们只能卸载重装，并且丢失设置。
> **绝对不要把密钥文件传到 GitHub。** 仓库的 `.gitignore` 已经挡住了 `*.jks`，但也不要故意绕开。

## 每次发布

1. **改版本号**：打开 `app/build.gradle.kts`，`versionCode` 加 1（必须比上一个大，否则手机不让更新），`versionName` 改成新的，比如 `1.0.1`。
2. **提交并推送**代码，等 GitHub 页面的 **Actions** 显示这次提交编译通过（绿色对勾）。
3. **生成安装包**：Build → Generate Signed App Bundle / APK → APK → 选刚才的密钥 → 选 **release** → Finish。完成后右下角点 **locate**，文件是 `app-release.apk`。
4. **改名**成 `SplashSkip-1.0.1.apk`（和 `versionName` 一致）。
5. **发布**：GitHub 仓库页面 → **Releases → Draft a new release**：
   - **Choose a tag** 里输入 `v1.0.1`，选 **Create new tag**
   - 标题写 `SplashSkip 1.0.1`，说明里写这一版改了什么
   - 把安装包拖进 **Attach binaries**
   - 点 **Publish release**

## 注意

- 用自己的密钥签名的安装包，和 Android Studio 里点 ▶ 装到手机上的“调试版”**签名不同**，不能互相覆盖。第一次装发布版之前，要先卸载手机上的调试版（会清空设置和累计次数）。
- 每次都用**同一个密钥**签名，用户才能直接更新。
