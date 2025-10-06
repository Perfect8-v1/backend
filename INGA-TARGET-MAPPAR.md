# INGA TARGET-MAPPAR? - Steg-för-steg lösning

## Problem
Du körde `mvn clean package -DskipTests` men det finns inga target-mappar.

## Lösning - Steg för Steg

### STEG 1: Hitta din projektkatalog

Kör detta för att hitta ditt Perfect8 projekt:
```bash
# Försök hitta admin-service katalogen
find ~ -name "admin-service" -type d 2>/dev/null

# Eller sök efter Perfect8
find ~ -name "Perfect8" -type d 2>/dev/null

# Eller om du klonade från GitHub nyligen
ls -la ~/git/
ls -la ~/projects/
ls -la ~/Documents/
```

### STEG 2: Navigera till rätt katalog

När du hittat projektet:
```bash
# Exempel (byt ut med din faktiska sökväg):
cd ~/Perfect8/backend

# ELLER
cd ~/git/backend

# ELLER dit du klonades projektet
```

### STEG 3: Verifiera att du är på rätt plats

Kör vårt diagnos-script:
```bash
./diagnose-project.sh
```

Du borde se:
- ✓ admin-service/
- ✓ blog-service/
- ✓ email-service/
- ✓ image-service/
- ✓ shop-service/
- ✓ pom.xml filer

**Om du INTE ser detta:** Du är fortfarande i fel katalog! Gå tillbaka till STEG 1.

### STEG 4: Kontrollera Maven

```bash
# Är Maven installerat?
mvn --version
```

**Om kommandot inte hittas:**

Ubuntu/Debian:
```bash
sudo apt update
sudo apt install maven
```

Alpine:
```bash
apk add maven openjdk17
```

MacOS:
```bash
brew install maven
```

### STEG 5: Kontrollera Java

```bash
# Är Java installerat?
java -version
```

Du behöver Java 17 eller 21.

**Om Java saknas:**

Ubuntu/Debian:
```bash
sudo apt install openjdk-21-jdk
```

Alpine:
```bash
apk add openjdk21
```

MacOS:
```bash
brew install openjdk@21
```

### STEG 6: Testa kompilera EN service först

```bash
# Gå till en service
cd admin-service

# Kontrollera att pom.xml finns
ls -la pom.xml

# Försök kompilera
mvn clean package -DskipTests
```

**Vad ska hända:**
- Du kommer se massa text som scrollar förbi
- Det borde ta 30-60 sekunder
- Till slut ska det stå: `[INFO] BUILD SUCCESS`
- En `target/` mapp ska skapas

**Titta efter output som:**
```
[INFO] Building admin-service 1.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.2.0:clean (default-clean) @ admin-service ---
[INFO] 
[INFO] --- maven-resources-plugin:3.3.0:resources (default-resources) @ admin-service ---
...
[INFO] BUILD SUCCESS
```

### STEG 7A: OM KOMPILERINGEN LYCKADES

```bash
# Kolla target-mappen
ls -lah target/

# Du borde se något liknande:
# admin-service-1.0.jar (eller admin-service-1.0-SNAPSHOT.jar)
```

✅ **SUCCESS!** Gå tillbaka till roten:
```bash
cd ..

# Nu kompilera alla services
./build-all.sh
```

### STEG 7B: OM KOMPILERINGEN MISSLYCKADES

Leta efter felmeddelanden:

**Vanligt fel 1: "Cannot resolve dependencies"**
```
[ERROR] Failed to execute goal on project admin-service: 
Could not resolve dependencies...
```

**Lösning:** Maven kan inte ladda ner dependencies.
```bash
# Testa internet-anslutningen
ping google.com

# Tvinga Maven att uppdatera
mvn clean package -U -DskipTests
```

**Vanligt fel 2: "Source option 5 is no longer supported"**
```
[ERROR] Source option 5 is no longer supported. Use 7 or later.
```

**Lösning:** Din Java-version är för gammal.
```bash
# Kontrollera Java-version
java -version

# Borde vara minst version 17 eller 21
```

**Vanligt fel 3: Kompileringsfel i koden**
```
[ERROR] /path/to/SomeFile.java:[125,8] cannot find symbol
```

**Lösning:** Det finns fel i Java-koden. Skicka mig felmeddelandet!

**Vanligt fel 4: "pom.xml not found" eller "Not a valid Maven project"**

**Lösning:** Du är i fel katalog! Gå tillbaka till STEG 1.

### STEG 8: Kompilera ALLA services

När en service fungerar, kompilera alla:

```bash
# Gå tillbaka till roten
cd /path/to/Perfect8/backend

# Kör build-scriptet
./build-all.sh
```

Detta kommer att:
1. Detektera om du har parent POM eller ej
2. Kompilera alla services
3. Visa alla skapade JAR-filer

### STEG 9: Verifiera

```bash
# Hitta alla target-mappar
find . -name "target" -type d

# Du borde se:
# ./admin-service/target
# ./blog-service/target
# ./email-service/target
# ./image-service/target
# ./shop-service/target

# Hitta alla JAR-filer
find . -name "*.jar" -type f
```

## Fortfarande problem?

Kör detta och skicka mig output:

```bash
# 1. Diagnos
./diagnose-project.sh > diagnosis.txt 2>&1

# 2. Försök kompilera EN service med verbose
cd admin-service
mvn clean package -DskipTests -X > maven-output.txt 2>&1

# Skicka mig innehållet i dessa filer:
cat diagnosis.txt
cat admin-service/maven-output.txt | tail -100
```

## Snabb-checklista

- [ ] Jag är i rätt katalog (kan se admin-service, blog-service, etc.)
- [ ] Maven är installerat (`mvn --version` fungerar)
- [ ] Java är installerat (`java -version` visar version 17 eller 21)
- [ ] pom.xml finns i varje service-katalog
- [ ] Internet-anslutning fungerar
- [ ] Jag har läs/skriv-rättigheter i katalogen

## När allt fungerar

När du har target-mappar och JAR-filer:

```bash
# 1. Skapa Dockerfiles
./setup-dockerfiles.sh

# 2. Bygg Docker images
./build-images.sh

# 3. Starta systemet
./start-perfect8.sh
```

🎉 **Lycka till!**
