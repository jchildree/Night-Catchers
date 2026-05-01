Scaffold a new Night Catchers Android Gradle module end-to-end.

## Usage

/scaffold-module <group>/<name> [type]

- `<group>/<name>` — module path relative to repo root, e.g. `feature/notifications` or `core/analytics`
- `[type]` — optional: `feature`, `library`, `application`. Inferred from group if omitted (group=`feature` → `feature`, group=`core` → `library`)

## What this command does

Given `$ARGUMENTS` (e.g. `feature/notifications`):

### 1. Parse arguments

Split `$ARGUMENTS` on whitespace. First token is the module path (e.g. `feature/notifications`), second optional token is the explicit type override.

Derive:
- `GROUP` = path prefix before `/` (e.g. `feature`)
- `NAME` = path suffix after last `/` (e.g. `notifications`)
- `MODULE_PATH` = full path (e.g. `feature/notifications`)
- `NAMESPACE` = `com.nightcatchers.${GROUP}.${NAME}` (e.g. `com.nightcatchers.feature.notifications`)
- `GRADLE_PATH` = `:${GROUP}:${NAME}` (e.g. `:feature:notifications`)
- `PLUGIN` = determined by type:
  - `feature` → `nightcatchers.android.feature`
  - `library` → `nightcatchers.android.library`
  - `application` → `nightcatchers.android.application`
  - If group is `feature` and no type given → `feature`
  - If group is `core` and no type given → `library`
  - Otherwise default to `library`

### 2. Verify the module doesn't already exist

Check that `$MODULE_PATH/` does not exist. If it does, stop and tell the user.

### 3. Create directory structure

```
$MODULE_PATH/
  build.gradle.kts
  src/main/kotlin/com/nightcatchers/$GROUP/$NAME/
  src/test/kotlin/com/nightcatchers/$GROUP/$NAME/
```

For `feature` type modules also create:
```
  src/main/res/values/strings.xml   (placeholder with module name string)
```

### 4. Generate build.gradle.kts

For **feature** modules:
```kotlin
plugins {
    alias(libs.plugins.nightcatchers.android.feature)
    alias(libs.plugins.nightcatchers.compose)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "$NAMESPACE"
}
```

For **library** modules:
```kotlin
plugins {
    alias(libs.plugins.nightcatchers.android.library)
}

android {
    namespace = "$NAMESPACE"
}
```

For **library** modules that need Hilt (ask the user — default no for core/testing, yes for core/data, core/security, core/network):
Add `alias(libs.plugins.nightcatchers.hilt)` to the plugins block.

Use your judgement: if the module name suggests data access (`data`, `network`, `repository`, `sync`) add Hilt automatically. If it's `testing`, `common`, or `domain` do not add Hilt.

### 5. Register module in settings.gradle.kts

Read the current `settings.gradle.kts`. Find the block that includes modules (lines with `include(`). Add the new module in alphabetical order within its group. For example if adding `:feature:notifications`, insert it after `:feature:onboarding` but before `:feature:parental` (alphabetically).

Do NOT add it as a raw string append — place it in the correct alphabetical position within the existing include list.

### 6. Create a placeholder stub source file

Create a minimal Kotlin file so the module has at least one source file:

For **feature** modules create `$MODULE_PATH/src/main/kotlin/com/nightcatchers/$GROUP/$NAME/${NAME_PASCAL}Screen.kt`:
```kotlin
package com.nightcatchers.$GROUP.$NAME

import androidx.compose.runtime.Composable

@Composable
fun ${NAME_PASCAL}Screen() {
    // TODO: implement $NAME screen
}
```

Where `NAME_PASCAL` is the name in PascalCase (e.g. `notifications` → `Notifications`).

For **library** modules create a placeholder object file named after the module.

### 7. Commit

Stage all new files and the modified `settings.gradle.kts`, then commit with message:
```
feat($GRADLE_PATH): scaffold $MODULE_PATH module
```

### 8. Report

Print a summary:
```
✓ Created $MODULE_PATH
  Plugin:    $PLUGIN
  Namespace: $NAMESPACE
  Gradle:    $GRADLE_PATH
  Files:     [list of created files]
  Registered in settings.gradle.kts
  Committed: [short SHA]
```

## Notes

- Never hardcode dependency versions in the new `build.gradle.kts` — all dependencies use `libs.*` aliases from the version catalog
- The `nightcatchers.android.feature` convention plugin automatically adds `:core:ui`, `:core:domain`, and `:core:common` dependencies — do not re-add them manually
- If the user provides a name with slashes deeper than one level (e.g. `feature/pet/minigames`), treat the last segment as `NAME` and the rest as `GROUP` path
- Push to `main` after committing unless the user says otherwise
