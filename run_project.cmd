@echo off
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-26.0.2.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
    ) else if exist "C:\Program Files\Java\jdk-21.0.12.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
    )
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Compilando y levantando servidor Tomcat...
call mvnw.cmd tomcat7:run
pause