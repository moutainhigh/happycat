package cbss.api.nciic;

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
import com.woniu.sncp.cbss.api.nciic.controller.NciicRequestParam;

public class Test {

	public static void main(final String[] args) {
		try {
			// TSocket transport = new TSocket("10.103.4.137", 13001);
			TSocket transport = new TSocket("172.18.70.180", 8080);
			transport.open();
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			TMultiplexedProtocol protocol1 = new TMultiplexedProtocol(protocol, "api.nciicniftyservice");
			Api.Client client = new Api.Client(protocol1);
			long time = System.currentTimeMillis();
			for (int i = 0; i < 1000; i++) {
				Access access = new Access(203, 9, "l47Bz5vboMtfwa5", new HashMap<String, String>(0));
				NciicRequestParam nciicRequestParam = new NciicRequestParam();
				nciicRequestParam.setIdentityNo("320504198301242753");
				nciicRequestParam.setRealName("毛从长");
				Data data = new Data("1.0.0", new ClientRequest(System.currentTimeMillis(), "172.18.70.180", "172.18.70.180", new HashMap<String, String>(0)), new Param(
						NciicRequestParam.class.getName(), ApiConstants.PARAM_RESOLVE_TYPE_DEFAULT, JSONObject.toJSONString(nciicRequestParam), new ArrayList<String>(0)),
						ApiConstants.TRACE_OPEN_CLIENT_STATE, "1");
				Signature signature = new Signature(1, "229C2110CC31DF561CDCADF0A8436E03");
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
