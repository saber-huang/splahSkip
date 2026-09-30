# 发布新版本

给别人下载的安装包必须用**自己的密钥签名**。下面是第一次发布和以后每次发布的步骤。

## 怎么打开“生成签名安装包”的窗口

不同版本的 Android Studio，这个菜单的位置和名字略有不同。**最省事的办法，所有版本都能用**：

按 **⇧⌘A**（Shift + Command + A），输入 `Generate Signed`，选中 **Generate Signed App Bundle or APK…**，回车。

也可以用菜单：屏幕**最顶端**的菜单栏（macOS 的菜单栏在屏幕顶部，不在 Android Studio 窗口里面）→ **Build** → 新版本里在 **Generate App Bundles or APKs** 子菜单里，旧版本直接在 Build 下面。

## 一次性准备：创建签名密钥

1. 窗口里选 **APK**（不是 Android App Bundle），点 **Next**。
2. **Module** 选 `app`。**Key store path** 那一行的右边有两个按钮：**Choose existing…** 和 **Create new…**，点 **Create new…**。
3. 在弹出的窗口里填：
   - **Key store path**：选一个**项目文件夹以外**的位置，比如 `~/Documents/splashskip-release.jks`。
   - **Password / Confirm**：Key store 的密码。
   - **Alias**：比如 `splashskip`。**Password / Confirm**：Key 的密码（可以和上面一样）。
   - **Validity (years)**：填 `30`。
   - **Certificate**：**First and Last Name** 写你的名字或昵称，其余可以不填。
4. 点 **OK**，回到上一个窗口，路径和密码已经自动填好了，点 **Next**。
5. 选 **release**，点 **Create**（新版本里是 **Finish**）。
6. 完成后右下角会弹出提示，点 **locate**，就能找到 `app-release.apk`。

> **⚠️ 密钥文件和密码一定要备份到至少两个地方（比如网盘 + 密码管理器）。**
> 弄丢了，以后就没法给已经安装的用户更新，他们只能卸载重装，并且丢失设置。
> **绝对不要把密钥文件传到 GitHub。** 仓库的 `.gitignore` 已经挡住了 `*.jks`，但也不要故意绕开。

## 每次发布

1. **改版本号**：打开 `app/build.gradle.kts`，`versionCode` 加 1（必须比上一个大，否则手机不让更新），`versionName` 改成新的，比如 `1.0.1`。
2. **提交并推送**代码，等 GitHub 页面的 **Actions** 显示这次提交编译通过（绿色对勾）。
3. **生成安装包**：按上面的方法打开窗口 → **APK** → **Next** → 选**已有的密钥**（点 **Choose existing…**，输入密码）→ 选 **release** → **Create**。完成后右下角点 **locate**，文件是 `app-release.apk`。
4. **改名**成 `SplashSkip-1.0.1.apk`（和 `versionName` 一致）。
5. **发布**：GitHub 仓库页面 → **Releases → Draft a new release**：
   - **Choose a tag** 里输入 `v1.0.1`，选 **Create new tag**
   - 标题写 `SplashSkip 1.0.1`，说明里写这一版改了什么
   - 把安装包拖进 **Attach binaries**
   - 点 **Publish release**

## 注意

- 用自己的密钥签名的安装包，和 Android Studio 里点 ▶ 装到手机上的“调试版”**签名不同**，不能互相覆盖。第一次装发布版之前，要先卸载手机上的调试版（会清空设置和累计次数）。
- 每次都用**同一个密钥**签名，用户才能直接更新。
