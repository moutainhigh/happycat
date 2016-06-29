%drv%
cd %CD%

cd %CD%\api.parent
mvn install -Dmaven.test.skip=true

cd %CD%\api.fcm\trunk
mvn install -Dmaven.test.skip=true

cd %CD%\api.appMembers\trunk
mvn install -Dmaven.test.skip=true

explorer %CD%