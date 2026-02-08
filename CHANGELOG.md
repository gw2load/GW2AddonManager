# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- The manager now supports migrations to smoothen the on-boarding experience for users with existing set-ups by
  inspecting and cleaning up their game directories.
  - The legacy addonloader can now be removed automatically if it is no longer required.
  - Legacy addonloader add-ons can now be renamed to ensure that they are loaded via GW2Load.
  - d3d9_wrapper can now be removed automatically if it is no longer required.

### Changed

- Users are now told explicitly when no add-ons are installed rather than being presented an empty list.
- The circular progress indicator in the add-on lists has been replaced with a more fitting customized linear progress
  indicator.
- Directories that become empty after uninstalling add-ons are now automatically deleted.
  - Directories that still contain files are not deleted automatically to preserve add-on configurations.

### Fixed

- The "install" button in the add-on details screen now correctly installs the add-on. (#174)
- The "update" button in the add-on details screen now correctly updates the add-on.
- The manager now longer fails to load logging-related classes leading to error popups and incorrect behavior. (#171)
- The manager is now automatically started after completing the installation when using the installer. (#170)
- The manager now correctly declares DPI awareness and should no longer scale poorly on high-resolution monitors. (#29)
- The manager is now more resilient against unexpected add-on structures.
