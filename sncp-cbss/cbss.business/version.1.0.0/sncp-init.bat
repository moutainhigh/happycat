%drv%
cd %CD%

call cd %CD%\api.parent
call mvn clean install -Dmaven.test.skip=true

call cd ..\api.init\trunk
call mvn clean install -Dmaven.test.skip=true

call cd ..\
