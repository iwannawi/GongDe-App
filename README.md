# 解压键盘（GongDe）

一款以"功德+1"为主题的解压类 Android 应用。用户点击屏幕上的机械键盘按键，配合按压动画、音效和飘字特效，获得即时的解压反馈与趣味体验。

## 功能特性

- **机械键盘按键**：真实质感的三态按键图片（未按下 / 按下中 / 按下到底），点击时自动切换
- **机械键盘音效**：每次点击同步播放合成的机械轴咔嗒声
- **功德飘字**：点击后"功德+1"文字从按键上方飘起并逐渐淡出
- **功德计数**：记录累计功德与当日功德，数据持久化存储
- **一键清零**：支持重置所有计数（带确认弹窗）
- **深色界面**：宇宙深空渐变背景 + 法轮暗纹 + 科技感点阵

## 技术栈

| 项目 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose |
| 最低 API | 24 (Android 7.0) |
| 构建工具 | Gradle (Kotlin DSL) + Version Catalog |
| 数据存储 | SharedPreferences |
| 音效生成 | AudioTrack 实时合成 |

## 项目结构

```
app/src/main/java/com/gongde/app/
├── MainActivity.kt          # 主界面：组装所有组件
├── data/
│   └── MeritStore.kt        # 功德数据持久化（SharedPreferences）
└── ui/
    ├── MechanicalButton.kt  # 机械键盘按键（图片切换 + 音效 + 按压动画）
    ├── FloatingText.kt      # 功德+1 飘字动画
    ├── MeritCounter.kt      # 功德计数面板
    └── theme/
        ├── Color.kt         # 颜色定义
        ├── Theme.kt         # Material3 暗色主题
        └── Type.kt          # 字体样式
```

## 构建与运行

1. 使用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 选择模拟器或连接真机，点击 Run

## 资源说明

- `res/drawable/keycap_off.png` — 未按下状态（无光效）
- `res/drawable/keycap_mid.png` — 按下中状态（金色光效）
- `res/drawable/keycap_on.png` — 按下到底状态（金色光效最强）
- 图片均为 768×768 RGBA 透明底 PNG

## License

个人项目，仅供学习交流。
