# Dhunora 1.0

Dhunora now uses the actual SimpMusic Android interface and feature implementation, based on the pinned SimpMusic 2.1.0 release. Original SimpMusic authors: maxrave-dev and contributors. Dhunora adaptation: Alok. Licensed under GPL-3.0; see LICENSE and the upstream notices.

## Source layout

- `upstream/SimpMusic`: exact upstream release `f26622bdf48be734cbfb65c360124cfeed8907c5`, with its pinned core submodule `454213897e020e8b52097acde25fed3c43a12c73`.
- `patches`: reviewable changes for the Dhunora package/name, startup safety, sign-in handling, existing-library import, and correct update links.
- `app`: preserved legacy Dhunora implementation. The new release is built from `upstream/SimpMusic/androidApp`.

## Build

Requires JDK 21 and the Android SDK (platform 37.0 and current build tools).

```sh
git clone --recurse-submodules https://github.com/AlokGond/Dhunora.git
cd Dhunora
python3 scripts/prepare-upstream.py
bash scripts/build-release.sh
```

The preparation script verifies both pinned commits and refuses to overwrite incompatible local changes. Gradle wrapper checksums are retained. All upstream UI, playback, search, lyrics, equalizer, crossfade, offline library, widgets, Android Auto, Cast, and Listen Together implementation are included. Last.fm needs your own `LASTFM_API_KEY` and `LASTFM_SECRET` in upstream/SimpMusic/local.properties; AI features need user-supplied keys in app settings. Streaming quality and account-specific features retain upstream service requirements. Upstream crash reporting is disabled in this adaptation.

## Signed release

Set `DHUNORA_KEYSTORE`, `DHUNORA_STORE_PASSWORD`, `DHUNORA_KEY_ALIAS`, and `DHUNORA_KEY_PASSWORD` to reuse your private release key. The build script signs and verifies APKs into `dist/`. Keys must never be committed. If the old installed APK used a different key, Android will not accept an in-place update. Do not uninstall before preserving any data you need. Automatic legacy playlist/recent/search import runs only when the existing app data is accessible (same signing identity).

This release keeps package `com.alok.dhunora`, uses versionCode 100, and requires Android 8.0+. The original legacy preferences are never deleted. Last.fm credentials, private signing keys, and service sessions are not part of the public source.

Smoothness and third-party service availability require device testing; source parity does not guarantee zero lag or uninterrupted playback.
