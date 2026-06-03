# Compose - 仅保留必要规则（Compose 编译器自动生成的规则已足够）
-dontwarn androidx.compose.**

# SharedPreferences JSON 序列化的数据类
-keep class com.gongde.app.data.Achievement { *; }

# Room Entity
-keep class com.gongde.app.data.DailyHistory { *; }

# 枚举（SP 存储了枚举 name）
-keepclassmembers enum com.gongde.app.ui.SwitchType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Android Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
