%drv%
cd %CD%
mvn clean install -Dmaven.test.skip=true
explorer %CD%