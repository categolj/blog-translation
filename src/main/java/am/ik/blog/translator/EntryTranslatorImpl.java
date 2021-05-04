package am.ik.blog.translator;

import am.ik.blog.translator.text.TextTranslator;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
public class EntryTranslatorImpl implements EntryTranslator {
	private final RestTemplate restTemplate;

	private final TextTranslator textTranslator;

	private final TranslatorProps props;

	public EntryTranslatorImpl(RestTemplateBuilder restTemplateBuilder, TextTranslator textTranslator, TranslatorProps props) {
		this.restTemplate = restTemplateBuilder
				.build();
		this.textTranslator = textTranslator;
		this.props = props;
	}

	@Override
	public Translated translate(Long entryId, String language) {
		try {
			final JsonNode entry = this.restTemplate.getForObject("%s/entries/{entryId}".formatted(this.props.getBlogApiUrl()), JsonNode.class, entryId);
			final String title = entry.get("frontMatter").get("title").asText();
			final String content = entry.get("content").asText();
			final String translatedTitle = this.textTranslator.translate(title, "ja", language).trim();
			final String translatedContent = this.textTranslator.translate("""
					> ⚠️ **注意**: この記事は自動的に翻訳されました。 <br>最終的には編集される可能性がありますが、現時点では誤った情報が含まれている可能性があることに注意してください。		
					
					<br>
					
					"""
					+ content, "ja", language);
			return new Translated(entryId, language, translatedTitle, translatedContent);
		}
		catch (RestClientResponseException e) {
			throw new ResponseStatusException(HttpStatus.valueOf(e.getRawStatusCode()), e.getMessage(), e);
		}
	}
}
