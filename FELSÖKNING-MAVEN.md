# FELSÖKNING: Hittar inte JAR-filer

## Steg 1: Kör diagnostic script

```bash
chmod +x find-jars.sh
./find-jars.sh
```

Detta script kommer att:
- Visa var du är i filsystemet
- Leta efter alla 'target' kataloger
- Hitta alla JAR-filer
- Kontrollera projektstrukturen

## Steg 2: Vanliga problem och lösningar

### Problem 1: Maven kompilerade inte alla moduler

**Symptom:** `mvn clean package` körde klart men du ser inte alla JAR-filer

**Lösning:**
```bash
# Kontrollera Maven output - letade efter "BUILD SUCCESS" för varje modul
mvn clean package -DskipTests

# Letar efter rader som:
# [INFO] admin-service ...................................... SUCCESS
# [INFO] blog-service ....................................... SUCCESS
# osv.
```

**Om en modul säger "BUILD FAILURE":**
1. Läs felmeddelandet noggrant
2. Vanliga fel:
   - Saknade dependencies i pom.xml
   - Kompileringsfel i Java-koden
   - Test-fel (men vi skippar tester)

### Problem 2: Multi-module projekt utan parent POM

**Symptom:** Du måste kompilera varje service separat

**Kontrollera:**
```bash
# Finns det en parent pom.xml i rotkatalogen?
cat pom.xml | grep "<modules>"
```

**Om det INTE finns parent POM:**
```bash
# Kompilera varje service individuellt
cd admin-service && mvn clean package -DskipTests && cd ..
cd blog-service && mvn clean package -DskipTests && cd ..
cd email-service && mvn clean package -DskipTests && cd ..
cd image-service && mvn clean package -DskipTests && cd ..
cd shop-service && mvn clean package -DskipTests && cd ..
```

**Eller använd vårt script:**
```bash
./build-all.sh
```

### Problem 3: JAR-filerna har andra namn

**Symptom:** JAR-filer finns men har andra namn än förväntat

**Kontrollera:**
```bash
# Hitta alla JAR-filer och visa deras namn
find . -name "*.jar" -type f

# Vanliga namnmönster:
# - admin-service-1.0.jar
# - admin-service-1.0-SNAPSHOT.jar
# - admin-service.jar
# - perfect8-admin-service-1.0.jar
```

**Lösning:** Uppdatera Dockerfiles med rätt JAR-namn

### Problem 4: Fel katalog

**Symptom:** Du är inte i projektets rotkatalog

**Kontrollera:**
```bash
pwd
ls -la

# Du borde se:
# - admin-service/
# - blog-service/
# - email-service/
# - image-service/
# - shop-service/
# - pom.xml (kanske)
```

**Lösning:**
```bash
cd /path/to/Perfect8/backend
```

### Problem 5: Maven är inte konfigurerat korrekt

**Kontrollera Maven version:**
```bash
mvn --version

# Förväntat output:
# Apache Maven 3.x.x
# Java version: 17 eller 21
```

**Om Maven saknas:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install maven

# Alpine
apk add maven

# MacOS
brew install maven
```

## Steg 3: Manuell kontroll

### Kontrollera en specifik service:

```bash
# Gå till service-katalogen
cd admin-service

# Kontrollera att pom.xml finns
ls -la pom.xml

# Kompilera denna service
mvn clean package -DskipTests

# Kontrollera target-katalogen
ls -lah target/

# Du borde se något liknande:
# -rw-r--r-- 1 user group  45M Oct  2 12:00 admin-service-1.0.jar
# eller
# -rw-r--r-- 1 user group  45M Oct  2 12:00 admin-service-1.0-SNAPSHOT.jar
```

## Steg 4: Vanliga JAR-namn patterns

Baserat på din pom.xml kan JAR-filen heta:

**Om du har:**
```xml
<artifactId>admin-service</artifactId>
<version>1.0</version>
```
**JAR-namn:** `admin-service-1.0.jar`

**Om du har:**
```xml
<artifactId>admin-service</artifactId>
<version>1.0-SNAPSHOT</version>
```
**JAR-namn:** `admin-service-1.0-SNAPSHOT.jar`

**Om du har:**
```xml
<build>
    <finalName>admin-service</finalName>
</build>
```
**JAR-namn:** `admin-service.jar`

## Steg 5: Uppdatera Dockerfiles om JAR-namn är annorlunda

Om dina JAR-filer heter något annat, uppdatera Dockerfiles:

```bash
# Exempel: Om JAR heter admin-service-1.0-SNAPSHOT.jar
cd admin-service
nano Dockerfile

# Ändra raden:
# COPY target/admin-service-1.0.jar app.jar
# Till:
COPY target/admin-service-1.0-SNAPSHOT.jar app.jar
```

## Steg 6: Komplett build från scratch

Om inget annat fungerar, försök detta:

```bash
# 1. Rensa allt
mvn clean
rm -rf */target

# 2. Verifiera Java version
java -version
# Borde vara Java 17 eller 21

# 3. Kompilera med verbose output
mvn clean package -DskipTests -X > build.log 2>&1

# 4. Granska loggen
less build.log
# Sök efter "ERROR" eller "FAILURE"

# 5. Leta efter JAR-filer
find . -name "*.jar" -type f
```

## Behöver du mer hjälp?

Kör följande kommandon och skicka output till mig:

```bash
# 1. Visa projektstruktur
ls -la

# 2. Visa service-kataloger
ls -la */

# 3. Kör diagnostic script
./find-jars.sh

# 4. Visa target-kataloger (om de finns)
find . -name "target" -type d -exec ls -la {} \;

# 5. Visa Maven output (sista 50 rader)
mvn clean package -DskipTests 2>&1 | tail -50
```

Skicka mig output från dessa kommandon så kan jag hjälpa dig vidare!
