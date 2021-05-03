package am.ik.blog.translator;

public record Translated(Long entryId,
						 String language,
						 String title,
						 String content) {
}
