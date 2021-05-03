package am.ik.blog.translation;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TranslationMapper {
	public final JdbcTemplate jdbcTemplate;

	public TranslationMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Translation> translationRowMapper = (rs, i) -> {
		final TranslationKey translationKey = new TranslationKey(rs.getLong("entry_id"), rs.getString("language"), rs.getInt("revision"));
		final String title = rs.getString("title");
		final String content = rs.getString("content");
		final Instant createdAt = rs.getTimestamp("created_at").toInstant();
		return new Translation(translationKey, title, content, createdAt);
	};

	public List<String> getAvailableLanguage(Long entryId) {
		return this.jdbcTemplate.queryForList("SELECT DISTINCT language FROM translation WHERE entry_id = ? ORDER BY language DESC", String.class, entryId);
	}

	public List<Translation> getAllRevisionsOfTranslation(Long entryId, String language) {
		return this.jdbcTemplate.query("""
						SELECT entry_id, language, revision, title, NULL AS content, created_at FROM translation WHERE entry_id = ? AND language = ? ORDER BY revision DESC 
						""", this.translationRowMapper,
				entryId, language);
	}

	public Optional<Translation> getLatestTranslation(Long entryId, String language) {
		try (final Stream<Translation> stream = this.jdbcTemplate.queryForStream("""
						SELECT entry_id, language, revision, title, content, created_at FROM translation WHERE entry_id = ? AND language = ? ORDER BY revision DESC LIMIT 1
						""", this.translationRowMapper,
				entryId, language)) {
			return stream.findFirst();
		}
	}

	public Optional<Translation> getTranslation(TranslationKey translationKey) {
		try (final Stream<Translation> stream = this.jdbcTemplate.queryForStream("""
						SELECT entry_id, language, revision, title, content, created_at FROM translation WHERE entry_id = ? AND language = ? AND revision = ?
						""", this.translationRowMapper,
				translationKey.entryId(),
				translationKey.language(),
				translationKey.revision())) {
			return stream.findFirst();
		}
	}

	@Transactional
	public int insert(Translation translation) {
		final TranslationKey translationKey = translation.translationKey();
		return this.jdbcTemplate.update("""
				INSERT INTO translation(entry_id, language, revision, title, content, created_at) VALUES (?, ?, ?, ?, ?, ?)
				""", translationKey.entryId(), translationKey.language(), translationKey.revision(), translation.title(), translation.content(), Date.from(translation.createdAt()));
	}

	@Transactional
	public int delete(TranslationKey translationKey) {
		return this.jdbcTemplate.update("""
				DELETE FROM translation WHERE entry_id = ? AND language = ? AND revision = ?
				""", translationKey.entryId(), translationKey.language(), translationKey.revision());
	}
}
