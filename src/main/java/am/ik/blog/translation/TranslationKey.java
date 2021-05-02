package am.ik.blog.translation;

public record TranslationKey(Long entryId,
							 String language,
							 Integer revision) {

	public int nextRevision() {
		return this.revision + 1;
	}
}
