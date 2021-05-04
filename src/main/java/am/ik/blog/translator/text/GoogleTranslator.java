package am.ik.blog.translator.text;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import am.ik.blog.translator.TranslatorProps;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.formatter.RenderPurpose;
import com.vladsch.flexmark.formatter.TranslationHandler;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoogleTranslator implements TextTranslator {
	private final RestTemplate restTemplate;

	private final TranslatorProps props;

	private final MutableDataSet options = new MutableDataSet()
			.set(Parser.EXTENSIONS, Arrays.asList(
					StrikethroughExtension.create(),
					TablesExtension.create()
			))
			// set GitHub table parsing options
			.set(TablesExtension.WITH_CAPTION, false)
			.set(TablesExtension.COLUMN_SPANS, false)
			.set(TablesExtension.MIN_HEADER_ROWS, 1)
			.set(TablesExtension.MAX_HEADER_ROWS, 1)
			.set(TablesExtension.APPEND_MISSING_COLUMNS, true)
			.set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
			.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);

	private final Parser parser = Parser.builder(options).build();

	private final Formatter formatter = Formatter.builder(options).build();

	private final HtmlRenderer htmlRenderer = HtmlRenderer.builder(options).build();

	private final FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder(options).build();

	private static final String URL_PATTERN = "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	public GoogleTranslator(RestTemplateBuilder restTemplateBuilder, TranslatorProps props) {
		this.restTemplate = restTemplateBuilder
				.build();
		this.props = props;
	}

	@Override
	public String translate(String text, String source, String target) {
		if (Objects.equals(source, target)) {
			return text;
		}
		final String preparedText = text
				.replace("[http", "[Http")
				.replace("(http", "(Http")
				.replace("\"http", "\"Http")
				.replaceAll("curl([\\-a-zA-Z0-9 ]+)http", "curl$1Http")
				.replace(": http", ": Http")
				.replaceAll("(" + URL_PATTERN + ")", "[$1]($1)")
				.replace("[Http", "[http")
				.replace("(Http", "(http")
				.replace("\"Http", "\"http")
				.replaceAll("curl([\\-a-zA-Z0-9 ]+)Http", "curl$1http")
				.replace(": Http", ": http")
				.replace("<!-- toc -->", "HEREISTOC")
				.replace("<br>", "HEREISBR")
				.replace("<img", "BEGINIMG")
				.replaceAll("\\[(https?://[a-zA-Z0-9]+)\\]\\(https?://[a-zA-Z0-9]+\\)", "$1");
		final Document document = this.parser.parse(preparedText);
		final TranslationHandler translationHandler = this.formatter.getTranslationHandler();
		this.formatter.translationRender(document, translationHandler, RenderPurpose.TRANSLATION_SPANS);
		final List<String> translatingTexts = translationHandler.getTranslatingTexts();
		translationHandler.setTranslatedTexts(translatingTexts);
		final String formattedDocument = this.formatter.translationRender(document, translationHandler, RenderPurpose.TRANSLATED_SPANS);
		final String formattedHtml = this.htmlRenderer.render(this.parser.parse(formattedDocument));
		final String formattedTranslate = this.converter.convert(this.doTranslate(formattedHtml, source, target))
				.replace("\\<", "<")
				.replace("\\>", ">")
				.replaceAll("\\*[^\\d*]*(\\d+)[^\\d*\n]*\\*", "_$1_")
				.replaceAll(" (_\\d+_) ", " `$1` ")
				.replaceAll("\\s(_\\d+_\\n)", "$1");
		if (false) {
			// debug
			return formattedDocument + System.lineSeparator() + "------------------------------------------------" + System.lineSeparator() + System.lineSeparator() +
					formattedTranslate.replace("HEREISTOC", System.lineSeparator() + "<!-- toc -->")
							.replace("HEREISBR", "<br>")
							.replace("BEGINIMG", "<img");
		}
		return this.formatter.translationRender(this.parser.parse(formattedTranslate), translationHandler, RenderPurpose.TRANSLATED)
				.replace("HEREISTOC", System.lineSeparator() + "<!-- toc -->")
				.replace("HEREISBR", "<br>")
				.replace("BEGINIMG", "<img");
	}

	String doTranslate(String text, String source, String target) {
		final URI uri = UriComponentsBuilder.fromHttpUrl(this.props.getGoogleApiUrl() + "/language/translate/v2")
				.queryParam("key", this.props.getGoogleApiKey())
				.build()
				.encode()
				.toUri();
		final Map<Object, Object> requestBody = Map.of(
				"q", text,
				"source", source,
				"target", target,
				"format", "html"
		);
		final String translated = this.restTemplate.postForObject(uri, requestBody, JsonNode.class)
				.get("data")
				.get("translations")
				.get(0)
				.get("translatedText")
				.asText();
		return translated;
	}
}
