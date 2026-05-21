# GitHub repository setup

Follow these steps once to publish the project and legal pages.

## 1. Create the repository on GitHub

1. Go to [github.com/new](https://github.com/new)
2. **Repository name:** `fake-emergency-escape-call`
3. **Visibility:** Public (required for free GitHub Pages on personal accounts)
4. Do **not** add README, .gitignore, or license (this project already has them)
5. Click **Create repository**

## 2. Push from your machine

In Terminal, from this project folder:

```bash
cd "/Users/moustafaothman/Desktop/Fake Emergency Escape Call"

git init
git add .
git commit -m "Initial commit: Fake Emergency Escape Call Android app"

git branch -M main
git remote add origin https://github.com/Moustafaplusplus/fake-emergency-escape-call.git
git push -u origin main
```

If `origin` already exists (wrong URL), fix it first:

```bash
git remote set-url origin https://github.com/Moustafaplusplus/fake-emergency-escape-call.git
git push -u origin main
```

If you use SSH:

```bash
git remote set-url origin git@github.com:Moustafaplusplus/fake-emergency-escape-call.git
git push -u origin main
```

## 3. Enable GitHub Pages

1. Repo → **Settings** → **Pages**
2. **Source:** Deploy from a branch
3. **Branch:** `main` / **`/docs`**
4. Save

Your URLs (after deploy):

- About: `https://moustafaplusplus.github.io/fake-emergency-escape-call/about.html`
- Privacy: `https://moustafaplusplus.github.io/fake-emergency-escape-call/privacy-policy.html`

## 4. Play Console

Paste the **privacy policy** URL into Play Console → App content → Privacy policy.

## 5. Different username or repo name?

Edit `app/src/main/java/com/fakeemergencyescape/call/legal/LegalUrls.kt` and the links in `docs/*.html`.
