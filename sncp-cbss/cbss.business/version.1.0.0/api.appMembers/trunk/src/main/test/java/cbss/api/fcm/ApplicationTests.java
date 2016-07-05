package java.cbss.api.fcm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.woniu.sncp.cbss.api.conf.controller.ConfController;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MockServletContext.class)
@WebAppConfiguration
public class ApplicationTests {

	private MockMvc mvc;

	@Before
	public void setUp()
			throws Exception {
		mvc = MockMvcBuilders.standaloneSetup(new ConfController()).build();
	}

	@Test
	public void testUserController()
			throws Exception {
		// 测试UserController
		// RequestBuilder request = new RequestBuilder();
		// mvc.perform(requestBuilder)
		// request = post("/cbss/api//fcm/onlinetime");
		// mvc.perform(request).andExpect(stateus().isOK()).addExpect()
		// 2、post提交一个user
		// request = post("/users/").param("id", "1").param("name",
		// "测试大师").param("age", "20");
		// mvc.perform(request).andExpect(content().string(equalTo("success")));
	}
}
