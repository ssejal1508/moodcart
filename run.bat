@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java:
java -version

echo.
echo Starting MoodCart application...
echo.

cd /d "%~dp0"

if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run
) else (
    echo Maven wrapper not found!
    pause
)
