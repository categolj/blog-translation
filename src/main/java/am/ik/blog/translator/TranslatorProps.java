package am.ik.blog.translator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "translator")
@ConstructorBinding
public class TranslatorProps {
	private final String blogApiUrl;

	private final String googleApiUrl;

	private final String googleApiKey;

	public TranslatorProps(String blogApiUrl, String googleApiUrl, String googleApiKey) {
		this.blogApiUrl = blogApiUrl;
		this.googleApiUrl = googleApiUrl;
		this.googleApiKey = googleApiKey;
	}

	public String getBlogApiUrl() {
		return blogApiUrl;
	}

	public String getGoogleApiUrl() {
		return googleApiUrl;
	}

	public String getGoogleApiKey() {
		return googleApiKey;
	}
}
