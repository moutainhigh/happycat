
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TSocket;

import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.cbss.api.core.thrift.Access;
import com.woniu.sncp.cbss.api.core.thrift.Api;
import com.woniu.sncp.cbss.api.core.thrift.ApiConstants;
import com.woniu.sncp.cbss.api.core.thrift.ClientRequest;
import com.woniu.sncp.cbss.api.core.thrift.Data;
import com.woniu.sncp.cbss.api.core.thrift.Echo;
import com.woniu.sncp.cbss.api.core.thrift.Param;
import com.woniu.sncp.cbss.api.core.thrift.Signature;
import com.woniu.sncp.cbss.api.fcm.controller.FcmConfRequestParam;

public class Test {

	public static void main(final String[] args) {
		try {
			 TSocket transport = new TSocket("10.103.4.137", 12001);
//			TSocket transport = new TSocket("172.18.70.180", 8080);
			transport.open();
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			TMultiplexedProtocol protocol1 = new TMultiplexedProtocol(protocol, "api.fcmapiservice");
			Api.Client client = new Api.Client(protocol1);
			long time = System.currentTimeMillis();
			for (int i = 0; i < 100; i++) {
				Access access = new Access(203, 9, "l47Bz5vboMtfwa5", new HashMap<String, String>(0));
				FcmConfRequestParam nciicRequestParam = new FcmConfRequestParam();
				nciicRequestParam.setGameIds("1,2,3,4");
				nciicRequestParam.setIssuerId(7L);
				Data data = new Data(ApiConstants.VERSION, new ClientRequest(System.currentTimeMillis(), "172.18.70.180", "172.18.70.180", new HashMap<String, String>(0)), new Param(
						FcmConfRequestParam.class.getName(), ApiConstants.PARAM_RESOLVE_TYPE_DEFAULT, JSONObject.toJSONString(nciicRequestParam), new ArrayList<String>(0)),
						ApiConstants.TRACE_OPEN_CLIENT_STATE, "1");
				Signature signature = new Signature(1, "83FD67BBB5535E30E558B605747A8E7D");
				long time1 = System.currentTimeMillis();
				Echo rep = client.invoke(access, data, signature);
				System.out.println("Message: " + (System.currentTimeMillis() - time1) + JSONObject.toJSONString(rep));
			}
			System.out.println("end:" + (System.currentTimeMillis() - time));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
