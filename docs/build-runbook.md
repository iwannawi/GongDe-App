# Build Runbook

This project should be built with the Android Studio bundled JBR and the locally
installed Gradle 8.14.4 distribution.

PowerShell:

```powershell
$env:JAVA_HOME='D:\Programs\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\zhangjingyao\.gradle\wrapper\dists\gradle-8.14.4-bin\92wwslzcyst3phie3o264zltu\gradle-8.14.4\bin\gradle.bat' testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain
```

Verified successful on 2026-06-24 from `D:\Dev Projects\GongDe`.

Debug APK output:

```text
D:\Dev Projects\GongDe\app\build\outputs\apk\debug\GongDe-v2.5.0-20260624.apk
```

Release build:

```powershell
$env:JAVA_HOME='D:\Programs\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\zhangjingyao\.gradle\wrapper\dists\gradle-8.14.4-bin\92wwslzcyst3phie3o264zltu\gradle-8.14.4\bin\gradle.bat' assembleRelease --console=plain
```

Do not rely on wrapper download in restricted environments. The wrapper URL is
still valid, but a sandbox without network access may fail before Gradle starts.

Codex execution note:

- The command above is the canonical build command for this repo.
- Gradle also needs local daemon/loopback support. If the tool runtime is in a
  restricted sandbox and reports `Unable to establish loopback connection`, the
  build must be rerun in a non-restricted/local shell with the same command.
- Do not spend time changing Gradle versions or wrapper URLs for that error; it
  is an execution-permission issue, not a project build configuration issue.
