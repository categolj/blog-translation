package am.ik.blog.translation;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record Translation(TranslationKey translationKey,
						  String title,
						  String content,
						  Instant createdAt) {

	@Override
	@JsonUnwrapped
	public TranslationKey translationKey() {
		return translationKey;
	}
}
