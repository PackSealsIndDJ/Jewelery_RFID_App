# Android Release Readiness

**Assessment:** Conditionally ready. The optimized API 35 release builds successfully, but it is unsigned and still requires validation on the Nation G2 hardware before distribution.

| Check | Result | Changes and remaining action |
|---|---|---|
| Version | Ready | `versionCode 3`, `versionName 1.2.0`. |
| Release build | Ready | Updated to AGP 8.6.1 and Gradle 8.7; compile/target SDK 35. Release is non-debuggable by default. |
| R8 / ProGuard | Ready | R8 enabled with vendor/JNI keep rules. Release optimization removes Android log calls and `printStackTrace` output. |
| Resource shrinking | Ready | `shrinkResources` enabled with minification. |
| Icon / splash | Ready | Added square and round adaptive launcher icons with the branded foreground, refreshed density-specific square icons, and configured the AndroidX system splash to use a dedicated 432 px logo instead of a low-resolution launcher asset. The duplicate in-app launch screen was removed. The 822 KiB product image remains in use in the QR view. |
| Permissions / security | Ready | Removed all-files, legacy storage, and privileged phone-state permissions; disabled backup; restricted internal activities; kept only camera, NFC, location, and vibration permissions. |
| Debug / test code | Ready for packaging | Release R8 strips logging; sample tests remain only under test source sets and are not packaged in the APK. |
| APK size | Measured | Unsigned optimized APK is 4,400,804 bytes (about 4.20 MiB). Native libraries for four ABIs remain bundled; consider ABI splits or an Android App Bundle if the distribution channel supports them. |
| Crash-prone paths | Improved | Firmware selection and export use the Storage Access Framework; firmware streams close reliably, partial packets no longer reuse stale bytes, invalid protocol preferences fall back safely, and device-info handles an empty ABI list. |
| Signing / release verification | **Blocked** | No production keystore was present, so the generated APK is unsigned. Supply all four `RELEASE_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD` values outside source control, rebuild, and verify the resulting signature. |

## Validation performed

- `:app:assembleRelease` — successful with R8 and resource shrinking, including the updated launcher and splash resources.
- `:app:testReleaseUnitTest` — successful; the existing unit test is only a placeholder and does not exercise app behavior.
- `:app:lintRelease` — successful with 847 warnings and no errors. Most remaining warnings are pre-existing hardcoded UI strings, accessibility/autofill hints, and legacy Butter Knife annotation use.
- APK inspection confirmed application ID `com.pda.uhfdemo`, version 3 / 1.2.0, target SDK 35, minimum SDK 21, and no broad storage or privileged phone-state permissions.
- Signature inspection confirmed the generated APK is unsigned.
- Hardware-dependent reader, firmware-update, barcode, NFC, GPS, export, and Android 15 UI flows were not exercised in this environment. Verify these on the exact Nation G2 firmware before distribution.

The pre-existing modified `app/release/app-release.apk` was left untouched. The optimized build output is at `app/build/outputs/apk/release/app-release-unsigned.apk`.

The barcode SDK requires an exported broadcast receiver to receive results from the separate Nation G2 scanner service. The receiver accepts results only while decoding and displays them; apply a vendor signature-level broadcast permission if the device firmware provides one.
