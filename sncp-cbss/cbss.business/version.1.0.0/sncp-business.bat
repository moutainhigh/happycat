%drv%
cd %CD%

e:
cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.business\version.1.0.0\api.parent
mvn install -Dmaven.test.skip=true

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.business\version.1.0.0\api.fcm\trunk
mvn install -Dmaven.test.skip=true

explorer %CD%