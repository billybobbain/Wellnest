# Java Environment Configuration

## JAVA_HOME Path

```
C:\Program Files\Android\Android Studio\jbr
```

## Usage

When running Gradle commands from the command line, set JAVA_HOME:

### Windows Command Prompt
```cmd
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
./gradlew assembleDebug
```

### Git Bash / PowerShell
```bash
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
./gradlew assembleDebug
```

### One-liner for Git Bash
```bash
JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew assembleDebug
```

## Notes

- This is the JBR (JetBrains Runtime) that comes bundled with Android Studio
- All Android projects in this workspace require JDK 17 or later
- The path includes spaces, so always quote it when setting the environment variable
