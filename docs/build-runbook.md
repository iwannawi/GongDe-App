# Build Runbook

This project should be built with the Android Studio bundled JBR and the locally
installed Gradle 8.14.4 distribution.

Current release version: `v2.6.0`.

PowerShell:

```powershell
$env:JAVA_HOME='D:\Programs\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\zhangjingyao\.gradle\wrapper\dists\gradle-8.14.4-bin\92wwslzcyst3phie3o264zltu\gradle-8.14.4\bin\gradle.bat' testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --console=plain
```

Verified successful on 2026-06-24 and reused for the v2.6.0 release build from
`D:\Dev Projects\GongDe`.

Debug APK output:

```text
D:\Dev Projects\GongDe\app\build\outputs\apk\debug\GongDe-v2.6.0-YYYYMMDD.apk
```

Release build:

```powershell
$env:JAVA_HOME='D:\Programs\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\zhangjingyao\.gradle\wrapper\dists\gradle-8.14.4-bin\92wwslzcyst3phie3o264zltu\gradle-8.14.4\bin\gradle.bat' assembleRelease --console=plain
```

Release APK output:

```text
D:\Dev Projects\GongDe\app\build\outputs\apk\release\GongDe-v2.6.0-YYYYMMDD.apk
```

Full release verification command:

```powershell
$env:JAVA_HOME='D:\Programs\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\zhangjingyao\.gradle\wrapper\dists\gradle-8.14.4-bin\92wwslzcyst3phie3o264zltu\gradle-8.14.4\bin\gradle.bat' testDebugUnitTest lintDebug assembleRelease --console=plain
```

Signature verification:

```powershell
$env:JAVA_HOME='D:\Programs\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat" verify --verbose --print-certs "app\build\outputs\apk\release\GongDe-v2.6.0-YYYYMMDD.apk"
```

GitHub release:

```powershell
gh release create v2.6.0 "app\build\outputs\apk\release\GongDe-v2.6.0-YYYYMMDD.apk#GongDe-v2.6.0-YYYYMMDD.apk" --title "v2.6.0" --notes-file RELEASE_NOTES.md
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
