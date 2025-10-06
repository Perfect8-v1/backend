# 🆘 SNABBHJÄLP - Inga target-mappar

## Problem
Du körde `mvn clean package -DskipTests` men det finns inga target-mappar.

## 🚀 Snabbaste lösningen (1 kommando)

Kopiera alla filer till din Perfect8 backend-katalog och kör:

```bash
./first-time-build.sh
```

Detta script kommer att:
- ✅ Kontrollera att du är i rätt katalog
- ✅ Verifiera att Maven och Java är installerade
- ✅ Kompilera alla services automatiskt
- ✅ Visa alla skapade JAR-filer
- ✅ Ge dig nästa steg

## 🔍 Alternativ: Diagnos först

Om du vill förstå problemet först:

```bash
# Kör detta för komplett diagnos
./diagnose-project.sh
```

Detta visar:
- Var du är i filsystemet
- Vilka services som finns
- Om Maven och Java är installerat
- Om pom.xml filer finns
- Konkreta rekommendationer

## 📚 Detaljerade guider

Vi har skapat flera guider för olika situationer:

### 1. [INGA-TARGET-MAPPAR.md](INGA-TARGET-MAPPAR.md)
Steg-för-steg guide från grunden:
- Hur du hittar din projektkatalog
- Hur du installerar Maven och Java
- Hur du felsöker Maven-fel
- Vad du gör om kompileringen misslyckas

### 2. [FELSÖKNING-MAVEN.md](FELSÖKNING-MAVEN.md)
Djupdykning i Maven-problem:
- Vanliga Maven-fel och lösningar
- Multi-module vs single module projekt
- JAR-namngivning problem
- Dependency problem

## 🛠️ Tillgängliga verktyg

Alla script är körbara (`chmod +x` redan gjort):

| Script | Användning |
|--------|-----------|
| `first-time-build.sh` | ⭐ **Start här!** Första kompileringen med full diagnos |
| `diagnose-project.sh` | Komplett projekt-analys |
| `quick-diagnosis.sh` | Snabb överblick (30 sekunder) |
| `find-jars.sh` | Hitta alla JAR-filer i projektet |
| `build-all.sh` | Kompilera alla services (efter första gången) |
| `setup-dockerfiles.sh` | Skapa Dockerfiles för alla services |
| `build-images.sh` | Bygg Podman/Docker images |
| `start-perfect8.sh` | Master-script för att starta systemet |

## 🎯 Rekommenderad ordning

```bash
# 1. Första kompilering (gör allt åt dig)
./first-time-build.sh

# 2. Om det fungerade - skapa Dockerfiles
./setup-dockerfiles.sh

# 3. Bygg Docker images
./build-images.sh

# 4. Starta systemet
./start-perfect8.sh
```

## ❓ Fortfarande fastnat?

### Scenario 1: "Hittar inte services"
Du är troligen i fel katalog.

**Lösning:**
```bash
# Hitta projektet
find ~ -name "admin-service" -type d 2>/dev/null

# Navigera dit
cd /path/to/Perfect8/backend

# Försök igen
./first-time-build.sh
```

### Scenario 2: "Maven är inte installerat"
**Lösning:**
```bash
# Ubuntu/Debian
sudo apt update && sudo apt install maven

# Alpine
apk add maven openjdk21

# MacOS
brew install maven
```

### Scenario 3: "BUILD FAILURE"
Läs felmeddelandet noggrant.

**Vanliga fel:**
- **"Cannot resolve dependencies"** → Kör: `mvn clean package -U -DskipTests`
- **"Source option X is no longer supported"** → Uppgradera Java
- **"Cannot find symbol"** → Kod-fel, skicka felmeddelandet till mig

### Scenario 4: "Script fungerar inte"
**Lösning:**
```bash
# Gör script körbart
chmod +x *.sh

# Kör igen
./first-time-build.sh
```

## 📞 Behöver du mer hjälp?

Kör detta och skicka mig output:

```bash
# Full diagnos
./diagnose-project.sh > diagnos.txt 2>&1

# Försök kompilera en service
cd admin-service
mvn clean package -DskipTests > maven.log 2>&1
cd ..

# Skicka mig:
cat diagnos.txt
cat admin-service/maven.log | tail -50
```

## 🎓 Lär dig mer

- [bootstrap.docx](bootstrap.docx) - Komplett dokumentation
- [README.md](README.md) - Full deployment guide
- [DEPLOYMENT-CHECKLIST.md](DEPLOYMENT-CHECKLIST.md) - Steg-för-steg checklista

---

**TL;DR:** Kör `./first-time-build.sh` och följ instruktionerna! 🚀
