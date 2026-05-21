# Fake Emergency Escape Call

Android app for scheduling **simulated incoming calls** (local-only, no backend).

## Open in Android Studio

1. **File → Open** → select this folder.
2. Let Gradle sync finish.
3. Run on emulator or device (API 26+).

## Build from terminal

Requires **JDK 17** for command-line Gradle (Java 25 is not supported yet). Android Studio uses its bundled JBR 17 automatically.

If `./gradlew` fails on Java 25, either install [Temurin 17](https://adoptium.net/) or use the project `.jdk/` folder and set in `local.properties`:

`org.gradle.java.home=/full/path/to/.jdk/jdk-17.0.19+10/Contents/Home`

```bash
./gradlew assembleDebug
```

## Docs

- [BUILD_PLAN.md](./BUILD_PLAN.md) — architecture & snippets  
- [IMPLEMENTATION_CHECKLIST.md](./IMPLEMENTATION_CHECKLIST.md) — step-by-step tasks  
- [docs/](./docs/) — **Privacy policy & About** (host on GitHub Pages for Play Console)

**Package:** `com.fakeemergencyescape.call`

## GitHub & legal pages

1. Create repo `fake-emergency-escape-call` on GitHub (see [GITHUB_SETUP.md](./GITHUB_SETUP.md)).
2. Enable **Pages** from the `/docs` folder on `main`.
3. Use the privacy URL in Play Console:  
   `https://moustafaplusplus.github.io/fake-emergency-escape-call/privacy-policy.html`
