# Ace Explorer

Ace Explorer is a powerful and feature-rich file manager for Android. It provides a simple and easy way to browse your files, manage your apps, and scan documents.

## Features

*   **File Management:**
    *   Browse and manage files and folders on your device's internal storage and SD card.
    *   Perform all essential file operations: cut, copy, paste, rename, delete, and view details.
    *   Compress and decompress files with support for various archive formats (e.g., ZIP).
    *   Drag and drop files and folders between different locations.
*   **App Manager:**
    *   View a list of all installed apps on your device.
    *   Open, backup, and uninstall apps.
    *   View detailed information about each app.
*   **Document Scanner:**
    *   Scan documents using your device's camera.
    *   Save scanned documents as PDF files.
*   **Media Library:**
    *   Browse your images, videos, and music files in a dedicated media library.
    *   Built-in image viewer and video player.
*   **Customization:**
    *   Choose between light and dark themes to match your preference.
    *   Customize the home screen to show the information you need.
*   **Advanced Features:**
    *   **Root Mode:** Access and manage files in the root directory of your device (for advanced users).
    *   **Voice Search:** Quickly find files using voice commands.
    *   **App Shortcuts:** Access your favorite features directly from the home screen (on Android 7.1 and above).
    *   **Split Window:** Use Ace Explorer alongside other apps in split-window mode.

## Modules

The Ace Explorer project is divided into several modules:

*   **`app`:** The main application module that brings together all the features of the app.
*   **`common`:** A shared module that contains code and resources used by other modules.
*   **`feature:appmanager`:** The module that implements the App Manager feature.
*   **`feature:documentscanner`:** The module that implements the Document Scanner feature.

## Building the Project

To build the project from source, you will need:

*   Android Studio
*   The Android SDK

1.  Clone the repository:
    ```
    git clone https://github.com/ace-explorer/ace-explorer.git
    ```
2.  Open the project in Android Studio.
3.  Create a `keystore.properties` file in the root of the project with the following content:
    ```
    KEYSTORE_FILE=<path_to_your_keystore_file>
    KEYSTORE_PASSWORD=<your_keystore_password>
    KEY_ALIAS=<your_key_alias>
    KEY_PASSWORD=<your_key_password>
    ```
4.  Build the project using Gradle:
    ```
    ./gradlew build
    ```

## Contributing

Contributions are welcome! If you would like to contribute to Ace Explorer, please follow these steps:

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix.
3.  Make your changes and commit them with a descriptive message.
4.  Push your changes to your fork.
5.  Create a pull request to the `main` branch of the original repository.

### Reporting Bugs

Explain the problem and include additional details to help maintainers reproduce the problem:

1. Use a clear and descriptive title for the issue to identify the problem.
2. Describe the exact steps which reproduce the problem in as many details as possible.
3. Include screenshots or animated GIFs which show you following the described steps and clearly demonstrate the problem.
4. For crashes, make sure to include logs

### How to take logs

Copy the logs from `Android Studio` logcat window to the issue. Make sure the logs contain the crash info.

## License

Ace Explorer is licensed under the Apache License, Version 2.0. See the `LICENSE` file for more details.

### Playstore

https://play.google.com/store/apps/details?id=com.siju.acexplorer

### Sonar Setup
1. Download and install `Docker Desktop` from <https://www.docker.com/products/docker-desktop>
2. Run using `Terminal` to run `sonarqube` server. Installs `Sonarqube` also if not existing already
   ```
   docker run -d --name sonarqube -p 9000:9000 sonarqube
   ```
3. Once sonarqube is started, enter <http://localhost:9000> into your browser and 
    login into sonarqube using “admin” as your username and password. 
4. Create a project on `Sonarqube` and generate the project login
5. From Terminal run,
   `./gradlew sonarqube -Dsonar.host.url=http://localhost:9000/ -Dsonar.login=$PROJECTLOGIN`
6. After it finishes, report can be checked in `Sonarqube` projects
