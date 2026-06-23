# 解压键盘（GongDe）

一款面向学生和上班族、以“功德 +1”为核心反馈的解压类 Android 应用。首页强调即时按键反馈，并通过每日目标、成就进度和近期记录形成轻量养成。

## 当前功能

- **机械键盘点击**：三态键帽图片、按压动画、机械轴合成音效、功德飘字。
- **计数与历史**：累计功德、今日功德、近 30 天时间线、本周/本月统计。
- **成就系统**：按累计、单日和连续使用天数解锁成就。
- **专注模式**：倒计时专注，每 3 秒自动增加 1 功德。
- **ASMR 模式**：增强点击音效，支持雨声白噪音开关。
- **分享卡片**：生成今日功德分享图并通过系统分享。
- **个性化设置**：触觉反馈开关、青/红/茶轴音效、主题切换。
- **匿名体验指标**：通过统一埋点接口记录启动、按键里程碑、模式进入和分享事件；没有 Firebase 配置时自动使用本地日志。

## 技术栈

| 项目 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 导航 | Compose Navigation |
| 状态 | ViewModel + Compose state |
| 数据 | SharedPreferences（计数/成就）+ DataStore（设置/连续使用）+ Room（历史） |
| 音效 | AudioTrack 实时合成 |
| 最低 API | 24 |
| 构建 | Gradle Kotlin DSL + Version Catalog |

## 项目结构

```text
app/src/main/java/com/gongde/app/
├── MainActivity.kt              # Compose 页面组装与导航
├── GongDeApplication.kt
├── navigation/
│   └── Screen.kt                # 页面路由
├── viewmodel/
│   └── GongDeViewModel.kt       # UI 状态、业务动作、仓库协调
├── data/
│   ├── AppDatabase.kt           # Room 数据库
│   ├── HistoryDao.kt            # 每日历史 DAO
│   ├── GongDeRepository.kt      # 计数、历史、设置、成就协调
│   ├── MeritStore.kt            # 累计/今日计数
│   ├── PreferencesStore.kt      # DataStore 设置与连续使用
│   └── AchievementStore.kt      # 成就定义与解锁状态
└── ui/
    ├── MechanicalButton.kt
    ├── MeritCounter.kt
    ├── MeditationScreen.kt
    ├── AsmrScreen.kt
    ├── AchievementScreen.kt
    ├── TimelineScreen.kt
    ├── ShareCard.kt
    ├── SettingsScreen.kt
    ├── SoundEngine.kt
    └── theme/
```

## 构建与测试

本机如果没有全局配置 `JAVA_HOME`，可以临时使用 Android Studio 自带 JBR：

```powershell
$env:JAVA_HOME='D:\Programs\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

Release 构建需要本地 `keystore.properties` 和签名文件。它们被 `.gitignore` 排除，不应提交到仓库。

## 资源说明

- `app/src/main/res/drawable/keycap_off.png`：未按下状态
- `app/src/main/res/drawable/keycap_mid.png`：按下中状态
- `app/src/main/res/drawable/keycap_on.png`：按下到底状态

## 维护备注

- README 描述的是当前 v2.5.x 架构。
- Room 历史记录使用 `gongde.db`，DataStore 设置文件为 `datastore/settings.preferences_pb`。
- 当前测试覆盖数据存储、成就、音效参数和历史统计；UI 仍主要依赖人工/设备验证。
