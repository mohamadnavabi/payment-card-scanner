# Release Notes - Version 1.2.2

## 🐛 Bug Fixes

### Camera Initialization Issues Fixed

This release addresses the critical issue where the payment card scanner activity would immediately close with `RESULT_CANCELED` upon opening.

#### What was fixed:

- **Immediate Activity Closure**: Fixed the issue where the activity would close immediately after opening
- **Camera Opening Failures**: Added retry mechanism with 3 attempts and 500ms delay between attempts
- **Permission Validation**: Improved camera permission checks before attempting to open the camera
- **Hardware Availability**: Added checks to ensure camera hardware is available on the device
- **Timing Issues**: Fixed timing problems in `onResume()` with proper UI thread handling

#### Technical Improvements:

- Added comprehensive debug logging for better troubleshooting
- Implemented retry mechanism for camera opening failures
- Enhanced error handling and user feedback
- Improved permission validation flow

## 📦 Installation

### Option 1: Local Maven Repository (Recommended for testing)

The package has been published to your local Maven repository. To use it in your project:

```gradle
repositories {
    mavenLocal()
    // ... other repositories
}

dependencies {
    implementation 'ir.mohammadnavabi:payment-card-scanner:1.2.2'
}
```

### Option 2: GitHub Packages (Requires credentials)

To publish to GitHub Packages, you need to set up credentials:

1. Create a GitHub Personal Access Token with `write:packages` permission
2. Set the credentials in your `gradle.properties` file:

```properties
gpr.user=your_github_username
gpr.key=your_github_token
```

3. Then run:

```bash
./gradlew :PaymentCardScanner:publishReleasePublicationToGitHubPackagesRepository
```

### Option 3: Manual AAR Installation

You can find the built AAR file at:

```
PaymentCardScanner/build/outputs/aar/PaymentCardScanner-release.aar
```

## 🚀 Usage

The API remains the same as previous versions:

```java
// Start the scanner activity
ScanActivity.start(this);

// Handle the result
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (ScanActivity.isScanResult(requestCode)) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            DebitCard scanResult = ScanActivity.debitCardFromResult(data);
            // Use the scanned card data
            String cardNumber = scanResult.number;
            String expiryMonth = scanResult.expiryMonth;
            String expiryYear = scanResult.expiryYear;
        } else if (resultCode == ScanActivity.RESULT_CANCELED) {
            // Check if it was a fatal error
            boolean fatalError = data.getBooleanExtra(ScanActivity.RESULT_FATAL_ERROR, false);
            boolean cameraError = data.getBooleanExtra(ScanActivity.RESULT_CAMERA_OPEN_ERROR, false);

            if (fatalError) {
                // Handle fatal error
            } else if (cameraError) {
                // Handle camera opening error
            } else {
                // User pressed back button
            }
        }
    }
}
```

## 🔧 Debugging

If you still encounter issues, check the Android logs for these tags:

- `ScanBaseActivity`: Main activity lifecycle and camera setup
- `CameraThread`: Camera opening attempts and retries

## 📋 Requirements

- Android API 21+ (Android 5.0)
- Camera permission
- Camera hardware support

## 🏷️ Version Information

- **Version**: 1.2.2
- **Version Code**: 2
- **Release Date**: December 19, 2024
- **Git Tag**: v1.2.2

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed changelog information.
