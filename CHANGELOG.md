# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.0] - 2026-09-21

### Added

- Add-ons can now be enabled and disabled from the add-on details screen. (#210)
- The manager now correctly updates add-ons behind symbolic links. (#211)

### Changed

- Introduced local add-on information caching to increase discovery performance.

### Fixed

- Legacy add-on discovery now skips over file subtrees if an error is encountered while walking the file tree.
  - Users with protected files in their game directory should now be able to use the manager reliably.
- Fixed a bug that caused the addonloader migration to be available even when addonloader add-ons were present.
- ArcDPS add-ons in bin64 are now correctly discovered.

## [0.2.0] - 2026-02-19

### Added

- The manager now supports migrations to smoothen the on-boarding experience for users with existing set-ups by
  inspecting and cleaning up their game directories.
  - The legacy addonloader can now be removed automatically if it is no longer required.
  - Legacy addonloader add-ons can now be renamed to ensure that they are loaded via GW2Load.
  - d3d9_wrapper can now be removed automatically if it is no longer required.
- The manager now a dedicated screen for displaying errors, useful information, and relevant links to the user.
  - Uncaught exceptions are now reported via this screen instead of a default AWT popup dialog.
  - Issues with fetching manifests (for add-ons and the manager) are now reported via the exception mechanism. (#167)

### Changed

- Users are now told explicitly when no add-ons are installed rather than being presented an empty list.
- The circular progress indicator in the add-on lists has been replaced with a more fitting customized linear progress
  indicator.
- Directories that become empty after uninstalling add-ons are now automatically deleted.
  - Directories that still contain files are not deleted automatically to preserve add-on configurations.
- The manager is now more resilient against network failures when fetching add-on or manager version manifests.
  - Manifests are now considered up-to-date and cached for up to 10 minutes before an attempt to reload them is made.
  - In case of network failures, outdated manifests are now used for up to 6 hours.

### Fixed

- The "install" button in the add-on details screen now correctly installs the add-on. (#174)
- The "update" button in the add-on details screen now correctly updates the add-on.
- The manager now longer fails to load logging-related classes leading to error popups and incorrect behavior. (#171)
- The manager is now automatically started after completing the installation when using the installer. (#170)
- The manager now correctly declares DPI awareness and should no longer scale poorly on high-resolution monitors. (#29)
- The manager is now more resilient against unexpected add-on structures.
