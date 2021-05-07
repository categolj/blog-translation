package am.ik.blog.config;

import java.util.function.Supplier;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class OtelConfig {
	@Bean
	Supplier<Resource> otelResourceProvider(Environment environment) {
		return () -> {
			Attributes attributes;
			// for the compatibility with Brave
			String zipkinServiceName = environment.getProperty("spring.zipkin.service.name");
			if (zipkinServiceName == null) {
				attributes = Attributes.empty();
			}
			else {
				attributes = Attributes.of(ResourceAttributes.SERVICE_NAME, zipkinServiceName);
			}
			return Resource.create(attributes);
		};
	}
}