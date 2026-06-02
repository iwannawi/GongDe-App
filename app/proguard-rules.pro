# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# App data classes
-keep class com.gongde.app.data.** { *; }
-keep class com.gongde.app.viewmodel.** { *; }
-keep class com.gongde.app.ui.SwitchType { *; }

# Android Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# FileProvider
-keep class androidx.core.content.FileProvider { *; }
