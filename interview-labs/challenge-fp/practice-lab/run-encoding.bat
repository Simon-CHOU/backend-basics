@echo off
chcp 65001 >nul
set "JAVA_OPTS=-Dfile.encoding=UTF-8"
java %JAVA_OPTS% -jar target\practice-lab-1.0-SNAPSHOT.jar %*
