package cbss.core.errorcode;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@ConditionalOnMissingBean(MessageSource.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MessageSourceAutoConfiguration {

	@Value("${cbss.api.errorcode.bis.basename}")
	private List<String> basenames;

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames(basenames.toArray(new String[]{}));
		return messageSource;
	}

}
