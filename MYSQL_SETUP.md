1. Download the driver into lib

Open PowerShell in the project folder (the terminal in your screenshot) and run:

powershell
mkdir lib -Force
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.6.0/mysql-connector-j-9.6.0.jar" -OutFile "lib\mysql-connector-j-9.6.0.jar"
dir lib

The file should be about 2 to 3 MB. If the link gives a 404, take the current version number from https://central.sonatype.com/artifact/com.mysql/mysql-connector-j and replace 9.6.0 in both places.

2. Make the VS Code Run button see the driver

Create or edit .vscode\settings.json in the project folder:

json
{
    "java.project.referencedLibraries": ["lib/**/*.jar"]
}

Then press Ctrl+Shift+P, type Java: Clean Java Language Server Workspace, and choose Restart and delete. After it reloads, press Run again.

3. Or run it from the terminal

On Windows the separator in -cp is ;, not ::

powershell
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp "lib/*" -d bin $files
java -cp "bin;lib/*;resources" ui.Main

The error dialog shows the Linux version of this command, with :. On Windows, use the one above.

4. After the driver, you may get a different error on this laptop

config/db.properties is missing. It isn’t on GitHub on purpose. Copy config/db.properties.example to config/db.properties and set db.user and db.password.
MySQL may not be installed on this laptop. The Ubuntu steps don’t apply here, and your data lives in the MySQL on your Ubuntu computer. On Windows, install MySQL Server from https://dev.mysql.com/downloads/installer/. Then, in PowerShell opened in the project folder, create the tables and the program’s user. Replace YOUR_ROOT_PASSWORD with the root password you set during the install:
powershell
Get-Content databases\schema.sql | mysql -u root -p
mysql -u root -p -e "CREATE USER IF NOT EXISTS 'library_user'@'localhost' IDENTIFIED BY 'Jeremiah29:12'; GRANT SELECT, INSERT, UPDATE, DELETE ON library_db.* TO 'library_user'@'localhost'; FLUSH PRIVILEGES;"

If mysql isn’t recognized, add C:\Program Files\MySQL\MySQL Server 8.0\bin to your PATH, or open “MySQL 8.0 Command Line Client” from the Start menu.

Check it works with java -cp "bin;lib/*" util.DbCheck. You want All checks passed. MySQL is ready. If the next message is different, send it to me.