# 解压键盘（GongDe）

一款面向学生和上班族的每日解压任务机。首页围绕透明红键帽、30 秒解压轮次、情绪签、连击和每日奖励建立即时反馈与轻量养成。

## 当前发布

- 最新正式版：`v2.6.0`
- Android 版本：`versionName 2.6.0`，`versionCode 17`
- 发布渠道：GitHub Releases
- 发布类型：正式 release，替代 `v2.6.0-rc1` 预发布验证版

## 当前功能

- **每日解压任务**：固定 100 次每日目标，首页展示进度、奖励状态和跨日重置。
- **30 秒解压轮次**：点击键帽启动一轮，记录本轮压力、当前连击和最高连击。
- **概念图键帽**：不再使用旧键盘图片，透明红键帽、轴体、底座和阴影使用概念图切出的透明资产。
- **情绪签玩法**：随机切换会议、作业、通勤、Deadline 等压力场景，强化每轮目标感。
- **图鉴与成就**：情绪签、键帽收藏、累计/单日/连续成就集中在图鉴页。
- **记录与分享**：记录页展示今日复盘、最近 5 天默认记录和新版解压日报分享图。
- **个性化设置**：触感反馈、声音主题、外观主题、隐私与数据管理。
- **匿名体验指标**：通过统一埋点接口记录启动、按键里程碑、每日目标和分享事件；没有 Firebase 配置时自动使用本地日志。

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
    ├── FloatingText.kt
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

Release 构建需要本地 `keystore.properties` 和签名文件。它们被 `.gitignore` 排除，不应提交到仓库。当前正式包使用 `assembleRelease` 生成签名 APK，并通过 `apksigner verify` 验证签名。

## 资源说明

首页背景由 Compose 绘制，键帽、礼物、票券、时钟、宝箱和收藏图标来自概念图切出的 `drawable-nodpi/concept_*` 资产。旧版键帽图片和自定义背景图已从当前工程移除。

## 维护备注

- README 描述的是当前 v3 体验改造后的架构。
- Room 历史记录使用 `gongde.db`，DataStore 设置文件为 `datastore/settings.preferences_pb`。
- 当前测试覆盖数据存储、成就、音效参数、历史统计和首页/导航 Compose UI。震动体感仍需要真机回归。
