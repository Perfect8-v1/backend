#!/bin/bash
# diagnose-project.sh - Komplett diagnos av Perfect8 projektet

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}"
echo "╔════════════════════════════════════════════════╗"
echo "║   PERFECT8 - Komplett Projekt Diagnos         ║"
echo "╚════════════════════════════════════════════════╝"
echo -e "${NC}"
echo ""

# 1. Var är vi?
echo -e "${BLUE}1. Nuvarande katalog:${NC}"
pwd
echo ""

# 2. Vad finns här?
echo -e "${BLUE}2. Innehåll i nuvarande katalog:${NC}"
ls -la
echo ""

# 3. Leta efter pom.xml filer
echo -e "${BLUE}3. Söker efter pom.xml filer:${NC}"
POM_FILES=$(find . -maxdepth 3 -name "pom.xml" 2>/dev/null)
if [ -z "$POM_FILES" ]; then
    echo -e "${RED}✗ Inga pom.xml filer hittades!${NC}"
    echo ""
    echo -e "${YELLOW}Detta är INTE ett Maven-projekt eller så är du i fel katalog.${NC}"
    echo ""
    echo "Förväntad struktur:"
    echo "  Perfect8/backend/"
    echo "  ├── admin-service/"
    echo "  │   └── pom.xml"
    echo "  ├── blog-service/"
    echo "  │   └── pom.xml"
    echo "  └── ..."
    echo ""
    exit 1
else
    echo -e "${GREEN}✓ Hittade följande pom.xml filer:${NC}"
    echo "$POM_FILES" | while read pom; do
        echo "  - $pom"
    done
    echo ""
fi

# 4. Kontrollera service-kataloger
echo -e "${BLUE}4. Kontrollerar förväntade service-kataloger:${NC}"
SERVICES=("admin-service" "blog-service" "email-service" "image-service" "shop-service")
FOUND_SERVICES=0

for service in "${SERVICES[@]}"; do
    if [ -d "$service" ]; then
        echo -e "  ${GREEN}✓${NC} $service/"
        FOUND_SERVICES=$((FOUND_SERVICES + 1))
        
        # Kontrollera om pom.xml finns
        if [ -f "$service/pom.xml" ]; then
            echo "    └─ pom.xml finns"
        else
            echo -e "    └─ ${RED}pom.xml saknas!${NC}"
        fi
        
        # Kontrollera om src finns
        if [ -d "$service/src" ]; then
            echo "    └─ src/ finns"
        else
            echo -e "    └─ ${RED}src/ saknas!${NC}"
        fi
    else
        echo -e "  ${RED}✗${NC} $service/ saknas"
    fi
done
echo ""

if [ $FOUND_SERVICES -eq 0 ]; then
    echo -e "${RED}✗ Inga förväntade service-kataloger hittades!${NC}"
    echo ""
    echo "Du är troligen i fel katalog."
    echo ""
fi

# 5. Kolla Maven
echo -e "${BLUE}5. Maven installation:${NC}"
if command -v mvn &> /dev/null; then
    mvn --version | head -3
    echo -e "${GREEN}✓ Maven är installerat${NC}"
else
    echo -e "${RED}✗ Maven är INTE installerat!${NC}"
    echo ""
    echo "Installera Maven:"
    echo "  Ubuntu/Debian: sudo apt install maven"
    echo "  Alpine: apk add maven"
    echo "  MacOS: brew install maven"
    echo ""
fi
echo ""

# 6. Kolla Java
echo -e "${BLUE}6. Java installation:${NC}"
if command -v java &> /dev/null; then
    java -version 2>&1 | head -1
    echo -e "${GREEN}✓ Java är installerat${NC}"
else
    echo -e "${RED}✗ Java är INTE installerat!${NC}"
fi
echo ""

# 7. Leta efter target-kataloger
echo -e "${BLUE}7. Söker efter target-kataloger:${NC}"
TARGET_DIRS=$(find . -maxdepth 3 -type d -name "target" 2>/dev/null)
if [ -z "$TARGET_DIRS" ]; then
    echo -e "${YELLOW}⚠ Inga target-kataloger hittades${NC}"
    echo "Detta betyder att Maven aldrig har körts eller misslyckades."
else
    echo -e "${GREEN}✓ Hittade target-kataloger:${NC}"
    echo "$TARGET_DIRS"
fi
echo ""

# 8. Leta efter .java filer
echo -e "${BLUE}8. Söker efter Java källkod:${NC}"
JAVA_FILES=$(find . -name "*.java" 2>/dev/null | head -5)
if [ -z "$JAVA_FILES" ]; then
    echo -e "${RED}✗ Inga .java filer hittades!${NC}"
    echo "Detta är kanske inte rätt katalog?"
else
    echo -e "${GREEN}✓ Hittade Java-filer (visar 5 första):${NC}"
    echo "$JAVA_FILES" | while read jfile; do
        echo "  - $jfile"
    done
fi
echo ""

# 9. Kontrollera Git
echo -e "${BLUE}9. Git repository:${NC}"
if [ -d ".git" ]; then
    echo -e "${GREEN}✓ Detta är ett Git repository${NC}"
    REMOTE=$(git remote get-url origin 2>/dev/null)
    if [ -n "$REMOTE" ]; then
        echo "  Remote: $REMOTE"
    fi
else
    echo -e "${YELLOW}⚠ Inte ett Git repository${NC}"
fi
echo ""

# 10. Sammanfattning och rekommendationer
echo -e "${BLUE}════════════════════════════════════════════════${NC}"
echo -e "${BLUE}SAMMANFATTNING OCH REKOMMENDATIONER:${NC}"
echo -e "${BLUE}════════════════════════════════════════════════${NC}"
echo ""

if [ $FOUND_SERVICES -eq 0 ]; then
    echo -e "${RED}❌ PROBLEM: Inga service-kataloger hittades!${NC}"
    echo ""
    echo "Du är troligen inte i rätt katalog."
    echo ""
    echo "Lösning:"
    echo "  1. Hitta din Perfect8 backend katalog"
    echo "  2. cd till rätt plats"
    echo "  3. Kör detta script igen"
    echo ""
    echo "Tips för att hitta projektet:"
    echo "  find ~ -name 'admin-service' -type d 2>/dev/null"
    echo "  find ~ -name 'Perfect8' -type d 2>/dev/null"
    echo ""
elif [ -z "$POM_FILES" ]; then
    echo -e "${RED}❌ PROBLEM: Inga pom.xml filer!${NC}"
    echo ""
    echo "Detta är inte ett Maven-projekt."
    echo ""
elif ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ PROBLEM: Maven är inte installerat!${NC}"
    echo ""
    echo "Installera Maven först:"
    echo "  Ubuntu/Debian: sudo apt install maven"
    echo "  Alpine: apk add maven"
    echo "  MacOS: brew install maven"
    echo ""
elif [ -z "$TARGET_DIRS" ]; then
    echo -e "${YELLOW}⚠️  Maven har aldrig körts eller misslyckades${NC}"
    echo ""
    echo -e "${GREEN}LÖSNING:${NC}"
    echo ""
    
    # Kolla om det finns parent POM
    if [ -f "pom.xml" ] && grep -q "<modules>" pom.xml 2>/dev/null; then
        echo "1. Du har en parent POM. Kompilera alla services:"
        echo -e "   ${BLUE}mvn clean package -DskipTests${NC}"
    else
        echo "1. Kompilera varje service individuellt:"
        echo ""
        for service in "${SERVICES[@]}"; do
            if [ -d "$service" ]; then
                echo -e "   ${BLUE}cd $service && mvn clean package -DskipTests && cd ..${NC}"
            fi
        done
        echo ""
        echo "   Eller använd vårt script:"
        echo -e "   ${BLUE}./build-all.sh${NC}"
    fi
    echo ""
    echo "2. Efter kompilering, kör detta script igen för att verifiera"
    echo ""
else
    echo -e "${GREEN}✅ Projektet ser bra ut!${NC}"
    echo ""
    echo "Nästa steg:"
    echo "  1. Kompilera: ./build-all.sh"
    echo "  2. Skapa Dockerfiles: ./setup-dockerfiles.sh"
    echo "  3. Bygg images: ./build-images.sh"
    echo "  4. Starta: ./start-perfect8.sh"
fi

echo ""
