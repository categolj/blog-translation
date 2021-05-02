package am.ik.blog.translation.web;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public record TranslationCreateRequest(String language,
									   String title,
									   String content) {
	static Validator<TranslationCreateRequest> validator = ValidatorBuilder.<TranslationCreateRequest>of()
			.constraint(TranslationCreateRequest::language, "language", c -> c.notNull())
			.constraint(TranslationCreateRequest::title, "title", c -> c.notNull())
			.constraint(TranslationCreateRequest::content, "content", c -> c.notNull())
			.build();

	public ConstraintViolations validate() {
		return validator.validate(this);
	}
}
