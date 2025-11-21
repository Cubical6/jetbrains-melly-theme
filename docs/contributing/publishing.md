# Publishing

This theme is distributed manually through GitHub Releases. The following instructions are a reference for maintainers.

## Publishing a Release

Follow the steps below to publish a new release:

1. Update the CHANGELOG.md file with the new version number and the change notes.
1. Push all changes to master.
1. Go to the [release page](https://github.com/one-dark/jetbrains-one-dark-theme/releases).
1. Click the "Draft a new release" button.
1. Enter the new version number for the tag and release name (e.g. `v1.0.0`).
1. Add the change notes from the changelog to the description.
1. Build the plugin using `./gradlew buildPlugin`
1. Attach the generated plugin ZIP file from `build/distributions/` to the release.
1. Click "Publish release".
