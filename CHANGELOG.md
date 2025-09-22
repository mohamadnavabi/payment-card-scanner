# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.1] - 2024-12-19

### Fixed

- Fixed immediate activity closure with RESULT_CANCELED on Android
- Added comprehensive debug logging for camera initialization issues
- Implemented retry mechanism for camera opening (3 attempts with 500ms delay)
- Added camera hardware availability checks
- Improved permission validation before camera access
- Fixed timing issues in onResume() with proper UI thread handling

### Added

- Enhanced error logging in CameraThread and ScanBaseActivity
- Better error messages for camera initialization failures
- Retry logic for camera opening failures

## [1.2.0] - 2024-12-18

### Added

- Initial release with payment card scanning functionality
- TensorFlow Lite integration for card recognition
- Support for Android API 21+
- OCR-based card number and expiry date detection

### Features

- Real-time card scanning
- Automatic card number formatting
- Expiry date recognition
- Flashlight support
- Orientation handling
