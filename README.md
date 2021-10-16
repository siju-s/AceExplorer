# FileManager
Simple File Manager

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
