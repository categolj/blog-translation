package am.ik.blog;

import am.ik.blog.translator.TranslatorProps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ TranslatorProps.class })
public class BlogTranslationApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogTranslationApplication.class, args);
	}

}
