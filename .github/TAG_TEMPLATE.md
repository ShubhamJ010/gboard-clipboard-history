# Release & Tag Template

Use this template when tagging a new release and publishing release notes on GitHub.

---

## Git Tag Command

```bash
VERSION="0.0.X-alpha"
git tag -a "${VERSION}" -m "Release ${VERSION}"
git push origin "${VERSION}"
```

---

## Release Notes Template (`gh release create` / GitHub UI)

```markdown
### Summary
<!-- 2-3 lines summarizing the main highlights of this release -->

### What's Changed
- **PixelBoard Material 3 UI**: <!-- UI improvements, palette, layout updates -->
- **Settings Hierarchy**: <!-- Placement of preference entry points -->
- **Sync & Backfill**: <!-- Clipboard capture hooks and database sync -->
- **Bug Fixes**: <!-- Fixes for crashes, permissions, or lifecycle -->

### Release Assets (Mandatory)
Every release must include exactly these two assets:
1. **`Gboard-com.sj010.gboard.apk`**: Standalone patched and signed Gboard keyboard package with clipboard history pre-integrated.
2. **`patches-<version>.mpp`**: Standalone Morphe patch bundle for use with Morphe Manager or CLI patcher.

### Morphe Manager Import
- Direct Import Link: `https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History`
- Remote Source: `github.com/ShubhamJ010/gboard-clipboard-history`

**Full Changelog**: https://github.com/ShubhamJ010/gboard-clipboard-history/compare/<previous-tag>...<new-tag>
```

---

## Release Creation CLI Command

```bash
VERSION="0.0.X-alpha"
PREV_VERSION="0.0.Y-alpha"

gh release create "${VERSION}" \
  output/Gboard-com.sj010.gboard.apk \
  output/patches-${VERSION}.mpp \
  --title "${VERSION}" \
  --notes-file .github/release_notes.md \
  --prerelease
```

---

## Pre-Release Verification Checklist

- [ ] `./build-local.sh --rebuild-extension` builds `patches/GboardClipboardHistory.mpp` without errors.
- [ ] `./build-local.sh --patch <stock.apk>` builds functional `output/Gboard-com.sj010.gboard.apk`.
- [ ] Tested on target device:
  - [ ] App installs and enables as primary IME.
  - [ ] Setting entry is strictly inside `Gboard Settings -> Clipboard`.
  - [ ] Copying in third-party apps (e.g. Google Keep) captures clips in foreground.
  - [ ] History activity opens with Material 3 Monet dynamic theming.
- [ ] Exactly two release assets prepared:
  - `Gboard-com.sj010.gboard.apk`
  - `patches-<version>.mpp`
- [ ] `patches-bundle.json` updated with new version number and download URL.
- [ ] `README.md` download links and badges point to the new release tag.
