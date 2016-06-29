%drv%
cd %CD%

call cd .\api.parent
call mvn install -Dmaven.test.skip=true

call cd ..\api.init\trunk
call mvn install -Dmaven.test.skip=true

call cd ..\..\api.fcm\trunk
call mvn install -Dmaven.test.skip=true

explorer %CD%