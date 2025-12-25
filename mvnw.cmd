@echo off
setlocal

rem Ensure JAVA_HOME is set (fallback to JDK 21 if not)
if not defined JAVA_HOME (
	set "JAVA_HOME=C:\Program Files\Java\jdk-21"
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0"

set "MAVEN_PROJECTBASEDIR=%CD%"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

echo Running Maven Wrapper with JAVA_HOME=%JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*

endlocal
