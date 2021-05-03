package am.ik.blog.translator;

public interface EntryTranslator {
	Translated translate(Long entryId, String language);
}
