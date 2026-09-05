@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Compilando y levantando servidor Tomcat...
call mvnw.cmd tomcat7:run
pause
