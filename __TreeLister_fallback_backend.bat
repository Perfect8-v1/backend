@echo off
REM Fallback: drop this file in any folder and double-click to export structure to struktur.txt
chcp 65001 >nul
tree /f > "backend_struktur.txt"
echo Skrev filstruktur till backend_struktur.txt
pause