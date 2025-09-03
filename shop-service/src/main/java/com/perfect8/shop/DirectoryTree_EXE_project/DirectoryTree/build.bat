@echo off
REM Builds a single-file self-contained EXE for Windows x64
setlocal
where dotnet >nul 2>nul
if errorlevel 1 (
  echo .NET SDK (dotnet) not found. Please install from https://dotnet.microsoft.com/download
  exit /b 1
)
dotnet publish -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true -p:DebugType=none -p:EnableCompressionInSingleFile=true
echo.
echo Build done. The EXE is here:
for /f "delims=" %%i in ('dir /s /b bin\Release\net8.0\win-x64\publish\DirectoryTree.exe') do set EXE=%%i
echo %EXE%
pause