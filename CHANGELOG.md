# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Users are now told explicitly when no add-ons are installed rather than being presented an empty list.

### Fixed

- The "install" button in the add-on details screen now correctly installs the add-on. (#174)
- The "update" button in the add-on details screen now correctly updates the add-on.
- The manager now longer fails to load logging-related classes leading to error popups and incorrect behavior. (#171)
- The manager is now automatically started after completing the installation when using the installer. (#170)
- The manager now correctly declares DPI awareness and should no longer scale poorly on high-resolution monitors. (#29)
