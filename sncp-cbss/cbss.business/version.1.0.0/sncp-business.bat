%drv%
cd %CD%

call cd %CD%\api.parent
call mvn install -Dmaven.test.skip=true

call cd ..\api.init\trunk
call mvn install -Dmaven.test.skip=true

call cd ..\..\api.nciic\trunk
call mvn install -Dmaven.test.skip=true
