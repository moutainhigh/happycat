%drv%
cd %CD%

e:
cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.parent
mvn install -Dmaven.test.skip=true

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.model\trunk
mvn install -Dmaven.test.skip=true

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.signature\trunk
mvn install -Dmaven.test.skip=true

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.validation\trunk
mvn install -Dmaven.test.skip=true

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.repository\trunk
mvn install -Dmaven.test.skip=true

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.call\trunk
mvn install -Dmaven.test.skip=true
cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.authorize\trunk
mvn install -Dmaven.test.skip=true

cd E:\workspace_new\sncp-web\api.cbss.woniu.com\sncp-bss\trunk\cbss.core\cbss.errorcode\trunk
mvn install -Dmaven.test.skip=true

explorer %CD%