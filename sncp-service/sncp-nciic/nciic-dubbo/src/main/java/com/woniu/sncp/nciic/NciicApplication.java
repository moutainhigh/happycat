package com.woniu.sncp.nciic;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.woniu.sncp.nciic.dto.NciicMessageIn;
import com.woniu.sncp.nciic.dto.NciicMessageOut;
import com.woniu.sncp.nciic.service.NciicMessageService;

@SpringBootApplication
@ImportResource({ "classpath*:META-INF/spring/dubbo-nciic-provider.xml" })
public class NciicApplication {

	public static void main(String[] args) {
		SpringApplication.run(NciicApplication.class, args);
	}

	@Bean
	CommandLineRunner lookup(NciicMessageService nciicMessageService) {
		return args -> {
			String userName = "毛从长";
			String identityNo = "320504198301242753";

			if (args.length > 0) {
				userName = args[0];
				identityNo = args[0];
			}

			NciicMessageIn nciicMessageIn = new NciicMessageIn(userName, identityNo);

			NciicMessageOut nciicMessageOut = nciicMessageService.checkRealNameIdentityNo(nciicMessageIn);
			
			System.out.println(String.format(
					"%s,errorInfo:%s,xm:%s-%s,sfzhm:%s-%s",
					new String[] { String.valueOf(nciicMessageOut.actualResult()), nciicMessageOut.getErrorInfo(), nciicMessageOut.getUserName(), nciicMessageOut.getUserNameResult(),
							nciicMessageOut.getIdentityNo(), nciicMessageOut.getIdentityNoResult() }));
		};
	}
}
