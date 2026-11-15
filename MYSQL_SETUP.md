# Library System: connect to MySQL (Ubuntu)

Run every command from the **project root** (the folder that contains `bin`, `lib`, `src`, `databases`, `config`).
Commands in `sudo mysql` use the system administrator login, so no MySQL password is needed for them.

## What changes

| Before | After |
|---|---|
| Books saved in `data/Book.txt` | Books saved in MySQL table `books` |
| Students saved in `data/Users.txt` | Students saved in MySQL table `users` |
| The window opens directly | The program checks MySQL first and shows a clear message if it can't connect |

The admin account (`admin` / `admin123`) is still built into the program. The screens look and work the same.

---

## Step 1. Install MySQL and start it

```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl enable --now mysql
sudo systemctl status mysql --no-pager
```

Look for `active (running)` in green. Then:

```bash
mysql --version
```

> `enable --now` also makes MySQL start by itself after every reboot.

## Step 2. Check that you can log in

```bash
sudo mysql
```

You should see the `mysql>` prompt. Try:

```sql
SELECT VERSION();
SHOW DATABASES;
EXIT;
```

## Step 3. Create the database and tables

```bash
sudo mysql < databases/schema.sql
```

Check the result:

```bash
sudo mysql -e "USE library_db; SHOW TABLES; DESCRIBE users; DESCRIBE books;"
```

You should see the tables `books` and `users`.

## Step 4. Create a MySQL user for the program

The program should not log in as the administrator. This creates a user that can only touch `library_db`:

```bash
sudo mysql <<'SQL'
CREATE USER IF NOT EXISTS 'library_user'@'localhost' IDENTIFIED BY 'ChangeMe123!';
GRANT SELECT, INSERT, UPDATE, DELETE ON library_db.* TO 'library_user'@'localhost';
FLUSH PRIVILEGES;
SQL
```

Test the new login (it asks for the password):

```bash
mysql -u library_user -p library_db -e "SHOW TABLES;"
```

> **Want your own password?** Change `ChangeMe123!` above, then put the same password in `config/db.properties` (Step 6).
> If MySQL says the password does not satisfy the policy, use a longer one with upper case, lower case, a number and a symbol.

## Step 5. Download the MySQL driver for Java

The file in `lib` named `mysql-connector-j-x.x.x.jar` is an empty placeholder. Replace it with the real driver:

```bash
rm -f lib/mysql-connector-j-x.x.x.jar
wget -P lib https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.6.0/mysql-connector-j-9.6.0.jar
ls -lh lib
```

The `.jar` should be about 2 to 3 MB. If `wget` says 404, open https://central.sonatype.com/artifact/com.mysql/mysql-connector-j to see the current version and use it in the link (it appears twice). If `wget` is missing: `sudo apt install wget`.

## Step 6. Add the new code and the settings

Copy the files from `library-mysql-update.zip` into the project root (they land in the right folders):

```bash
sudo apt install unzip -y
unzip -o library-mysql-update.zip
```

The settings file is `config/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/library_db?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectTimeout=5000
db.user=library_user
db.password=ChangeMe123!
```

Make `db.password` the same as in Step 4. Don't share this file.

## Step 7. Compile

```bash
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
```

No output means success.

## Step 8. Check the connection

```bash
java -cp "bin:lib/*" util.DbCheck
```

It adds a temporary student and book, tests borrowing, returning and deleting, then removes them. You want to see `All checks passed. MySQL is ready.`

## Step 9. Import your old data (only if you have data in `data/*.txt`)

```bash
java -cp "bin:lib/*" util.ImportFromFiles
```

It copies the old students and books into MySQL. You can run it twice safely; entries already in MySQL are skipped. Keep `data/*.txt` as a backup; the program no longer uses them.

## Step 10. Run the program

```bash
java -cp "bin:lib/*:resources" ui.Main
```

(If you start it with the VS Code Run button instead, make sure `.vscode/settings.json` still has `"java.project.referencedLibraries": ["lib/**/*.jar"]`.)

---

## Look at your data in MySQL

```bash
mysql -u library_user -p library_db
```

```sql
SELECT * FROM books;
SELECT id, title, author, borrowed_by FROM books WHERE status = 'Borrowed';
SELECT username, created_at FROM users;
SELECT borrowed_by, COUNT(*) AS books FROM books WHERE status = 'Borrowed' GROUP BY borrowed_by;
```

## Useful maintenance commands

Reset a student's password (the program stores a SHA-256 hash, and MySQL's `SHA2` makes the same one):

```sql
UPDATE users SET password_hash = SHA2('newpass123', 256) WHERE username = 'maria';
```

Delete a student (any books they hold become "borrower unknown"):

```sql
DELETE FROM users WHERE username = 'maria';
```

Back up everything:

```bash
sudo mysqldump --single-transaction library_db > backup_$(date +%F).sql
```

Restore a backup:

```bash
sudo mysql library_db < backup_2026-11-15.sql
```

Wipe everything and start over (**this deletes all books and students**):

```bash
sudo mysql -e "DROP DATABASE library_db;"
sudo mysql < databases/schema.sql
```

Change the program's MySQL password:

```bash
sudo mysql -e "ALTER USER 'library_user'@'localhost' IDENTIFIED BY 'NewStrongPass1!';"
```

then put the same password in `config/db.properties`. Single quotes keep the `!` safe in the terminal.

---

## If something goes wrong

| Message | What it means and the fix |
|---|---|
| `The MySQL driver (connector .jar) was not found` | The jar is missing or not on the classpath. Redo Step 5 and run with `-cp "bin:lib/*:resources"`. |
| `MySQL refused the username or password` | `db.user` or `db.password` in `config/db.properties` doesn't match Step 4. |
| `The database 'library_db' does not exist yet` | Run Step 3. |
| `A table is missing in the database` | Run Step 3 again (safe to repeat). |
| `Could not reach MySQL. Is it running?` | `sudo systemctl start mysql`, then `sudo systemctl status mysql`. |
| `Your password does not satisfy the current policy requirements` | Use a stronger password in Step 4 (and in `db.properties`). |
| `ERROR 1045 (28000): Access denied for user 'root'` | Use `sudo mysql` (no `-u root -p`). On Ubuntu the root login uses the system account. |
| `Public Key Retrieval is not allowed` | The URL in `db.properties` must keep `allowPublicKeyRetrieval=true`. |
| `Data too long for column` | A title or author is over 255 characters (or a username over 50). |
| `Cannot add or update a child row` during import | A borrowed book names a student who isn't in `users`. The import tool handles this itself; if you see it elsewhere, add the student first. |
| Logo is a plain green circle | `resources/logo.jpg` is missing, or `resources` is not on the classpath. |
