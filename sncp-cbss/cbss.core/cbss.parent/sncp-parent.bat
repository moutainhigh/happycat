@echo off
E:

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.parent
ECHO %CD%
mvn clean install -Dmaven.test.skip=true
pause