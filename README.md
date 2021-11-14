# Ace Explorer
Ace Explorer brings you a simple and easy way to browse your files, media library like Images, Videos, Music, PDF and many more right there at your home screen.

• Multi operations - Perform all the operations you need - Cut, Copy, Rename, Info and many more

• Themes - Tweak the app to choose your style - Light or Dark Theme.

• Drag and Drop - Move or copy files from one folder to another

• App shortcuts - Enjoy new features of App shortcuts and split window in Android 7.1

• Battery friendly - No battery draining services running in background

• More user control - Choice of toggling home screen in Settings

• Voice search - Use Voice search to search your files

• Root mode - For advanced users

• Peek and pop images and videos

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
