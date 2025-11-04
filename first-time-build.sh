#!/bin/bash
# first-time-build.sh - Första kompileringen med full diagnos

set +e  # Fortsätt även vid fel

echo "╔════════════════════════════════════════════════╗"
echo "║   PERFECT8 - Första Kompilering               ║"
echo "╚════════════════════════════════════════════════╝"
echo ""

# Steg 1: Var är vi?
echo "📍 Nuvarande katalog:"
pwd
echo ""

# Steg 2: Vad finns här?
echo "📁 Innehåll:"
ls -la | head -20
echo ""

# Steg 3: Finns services?
echo "🔍 Letar efter services..."
FOUND=0
for service in admin-service blog-service email-service image-service shop-service; do
    if [ -d "$service" ]; then
        echo "  ✓ $service"
        FOUND=$((FOUND + 1))
    else
        echo "  ✗ $service (saknas)"
    fi
done
echo ""

if [ $FOUND -eq 0 ]; then
    echo "❌ PROBLEM: Hittar inga service-kataloger!"
    echo ""
    echo "Du är förmodligen inte i rätt katalog."
    echo ""
    echo "Försök:"
    echo "  find ~ -name 'admin-service' -type d 2>/dev/null"
    echo ""
    echo "Navigera dit projektet finns och kör detta script igen."
    exit 1
fi

# Steg 4: Maven installerat?
echo "🔧 Kontrollerar Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven är INTE installerat!"
    echo ""
    echo "Installera Maven:"
    echo "  Ubuntu/Debian: sudo apt install maven"
    echo "  Alpine: apk add maven"
    echo "  MacOS: brew install maven"
    exit 1
else
    mvn --version | head -1
    echo "  ✓ Maven är installerat"
fi
echo ""

# Steg 5: Java installerat?
echo "☕ Kontrollerar Java..."
if ! command -v java &> /dev/null; then
    echo "❌ Java är INTE installerat!"
    echo ""
    echo "Installera Java:"
    echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  Alpine: apk add openjdk21"
    echo "  MacOS: brew install openjdk@21"
    exit 1
else
    java -version 2>&1 | head -1
    echo "  ✓ Java är installerat"
fi
echo ""

# Steg 6: Försök kompilera!
echo "🚀 Startar kompilering..."
echo ""
echo "Det här kan ta 1-2 minuter första gången (Maven laddar ner dependencies)..."
echo ""

# Försök hitta parent POM
if [ -f "pom.xml" ] && grep -q "<modules>" pom.xml 2>/dev/null; then
    echo "📦 Kompilerar via parent POM..."
    mvn clean package -DskipTests
    BUILD_RESULT=$?
else
    echo "📦 Kompilerar varje service individuellt..."
    BUILD_RESULT=0
    
    for service in admin-service blog-service email-service image-service shop-service; do
        if [ -d "$service" ]; then
            echo ""
            echo "→ Kompilerar $service..."
            cd "$service"
            mvn clean package -DskipTests
            if [ $? -ne 0 ]; then
                BUILD_RESULT=1
                echo "  ✗ $service misslyckades"
                cd ..
                break
            else
                echo "  ✓ $service lyckades"
            fi
            cd ..
        fi
    done
fi

echo ""
echo "════════════════════════════════════════════════"
echo ""

# Steg 7: Resultat
if [ $BUILD_RESULT -eq 0 ]; then
    echo "🎉 SUCCESS! Kompileringen lyckades!"
    echo ""
    
    # Hitta JAR-filer
    echo "📦 Skapade JAR-filer:"
    find . -name "*.jar" -type f -not -name "*-sources.jar" -not -name "*-javadoc.jar" 2>/dev/null | while read jar; do
        size=$(ls -lh "$jar" | awk '{print $5}')
        echo "  ✓ $jar ($size)"
    done
    echo ""
    
    echo "✅ Nästa steg:"
    echo "  1. ./setup-dockerfiles.sh    (Skapar Dockerfiles)"
    echo "  2. ./build-images.sh         (Bygger Docker images)"
    echo "  3. ./start-perfect8.sh       (Startar systemet)"
    echo ""
else
    echo "❌ MISSLYCKADES! Kompileringen hade fel."
    echo ""
    echo "Vad du kan göra:"
    echo "  1. Läs felmeddelandet ovan noggrant"
    echo "  2. Försök kompilera en service manuellt:"
    echo "     cd admin-service"
    echo "     mvn clean package -DskipTests"
    echo "  3. Eller läs: INGA-TARGET-MAPPAR.md"
    echo ""
    echo "Vanliga problem:"
    echo "  - Saknade dependencies (kör: mvn clean package -U -DskipTests)"
    echo "  - Fel Java-version (behöver Java 17 eller 21)"
    echo "  - Ingen internet-anslutning (Maven kan inte ladda ner libs)"
    echo ""
fi
