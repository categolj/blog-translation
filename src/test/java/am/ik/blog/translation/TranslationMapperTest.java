package am.ik.blog.translation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = {
		"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
		"spring.datasource.url=jdbc:tc:postgresql:11:///translation?TC_INITSCRIPT=file:src/main/resources/schema.sql"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:init.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
class TranslationMapperTest {
	private final TranslationMapper translationMapper;

	TranslationMapperTest(JdbcTemplate jdbcTemplate) {
		this.translationMapper = new TranslationMapper(jdbcTemplate);
	}

	@ParameterizedTest
	@CsvSource({
			"1, en\tcn",
			"2, en"
	})
	void getAvailableLanguage(long entryId, String languages) {
		final List<String> availableLanguage = this.translationMapper.getAvailableLanguage(entryId);
		assertThat(availableLanguage).containsExactly(languages.split("\t"));
	}

	@Test
	void getAllRevisionsOfTranslation() {
		final List<Translation> translationsEn = this.translationMapper.getAllRevisionsOfTranslation(1L, "en");
		assertThat(translationsEn).hasSize(2);
		assertThat(translationsEn.get(0).translationKey()).isEqualTo(new TranslationKey(1L, "en", 2));
		assertThat(translationsEn.get(0).title()).isEqualTo("title2");
		assertThat(translationsEn.get(0).content()).isNull();
		assertThat(translationsEn.get(0).createdAt()).isNotNull();
		assertThat(translationsEn.get(1).translationKey()).isEqualTo(new TranslationKey(1L, "en", 1));
		assertThat(translationsEn.get(1).title()).isEqualTo("title1");
		assertThat(translationsEn.get(1).content()).isNull();
		assertThat(translationsEn.get(1).createdAt()).isNotNull();
	}

	@ParameterizedTest
	@CsvSource({
			"1, 2, title2, content2",
			"2, 1, hello1, Hello1"
	})
	void getLatestTranslation(long entryId, int revision, String title, String content) {
		final Optional<Translation> optionalTranslation = this.translationMapper.getLatestTranslation(entryId, "en");
		assertThat(optionalTranslation.isPresent()).isTrue();
		final Translation translation = optionalTranslation.get();
		assertThat(translation.translationKey()).isEqualTo(new TranslationKey(entryId, "en", revision));
		assertThat(translation.title()).isEqualTo(title);
		assertThat(translation.content()).isEqualTo(content);
		assertThat(translation.createdAt()).isNotNull();
	}

	@Test
	void getLatestTranslation_notFound() {
		final Optional<Translation> optionalTranslation = this.translationMapper.getLatestTranslation(100L, "en");
		assertThat(optionalTranslation.isPresent()).isFalse();
	}

	@ParameterizedTest
	@CsvSource({
			"1, en, 2, title2, content2",
			"1, en, 2, title2, content2",
			"1, cn, 1, 标题1, 内容1",
			"2, en, 1, hello1, Hello1"
	})
	void getTranslation(long entryId, String language, int revision, String title, String content) {
		final TranslationKey translationKey = new TranslationKey(entryId, language, revision);
		final Optional<Translation> optionalTranslation = this.translationMapper.getTranslation(translationKey);
		assertThat(optionalTranslation.isPresent()).isTrue();
		final Translation translation = optionalTranslation.get();
		assertThat(translation.translationKey()).isEqualTo(translationKey);
		assertThat(translation.title()).isEqualTo(title);
		assertThat(translation.content()).isEqualTo(content);
		assertThat(translation.createdAt()).isNotNull();
	}

	@ParameterizedTest
	@CsvSource({
			"1, en, 3",
			"1, fr, 1",
			"3, en, 1"
	})
	void getTranslation_notFound(long entryId, String language, int revision) {
		final TranslationKey translationKey = new TranslationKey(entryId, language, revision);
		final Optional<Translation> optionalTranslation = this.translationMapper.getTranslation(translationKey);
		assertThat(optionalTranslation.isPresent()).isFalse();
	}

	@ParameterizedTest
	@CsvSource({
			"1, en, 3, title3, content3, 1",
			"3, en, 1, foo1, Foo1, 1"
	})
	void insert(long entryId, String language, int revision, String title, String content, int inserted) {
		final TranslationKey translationKey = new TranslationKey(entryId, language, revision);
		final Translation translation = new Translation(translationKey, title, content, Instant.now());
		final int insert = this.translationMapper.insert(translation);
		assertThat(insert).isEqualTo(inserted);
		final Optional<Translation> optionalTranslation = this.translationMapper.getTranslation(translationKey);
		final Translation retrieved = optionalTranslation.get();
		assertThat(retrieved.translationKey()).isEqualTo(translationKey);
		assertThat(retrieved.title()).isEqualTo(title);
		assertThat(retrieved.content()).isEqualTo(content);
		assertThat(retrieved.createdAt()).isNotNull();
	}

	@ParameterizedTest
	@CsvSource({
			"1, en, 2, 1",
			"1, en, 3, 0"
	})
	void delete(long entryId, String language, int revision, int deleted) {
		final TranslationKey translationKey = new TranslationKey(entryId, language, revision);
		final int delete = this.translationMapper.delete(translationKey);
		assertThat(delete).isEqualTo(deleted);
	}
}