package am.ik.blog.translation.web;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import am.ik.blog.translation.Translation;
import am.ik.blog.translation.TranslationKey;
import am.ik.blog.translation.TranslationMapper;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ViolationDetail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(path = "translations/{entryId}")
public class TranslationController {
	private final TranslationMapper translationMapper;

	public TranslationController(TranslationMapper translationMapper) {
		this.translationMapper = translationMapper;
	}

	@GetMapping(path = "")
	public ResponseEntity<List<Translation>> getAllRevisionsOfTranslation(@PathVariable("entryId") Long entryId, @RequestParam(name = "language", defaultValue = "en") String language) {
		return ResponseEntity.ok(this.translationMapper.getAllRevisionsOfTranslation(entryId, language));
	}

	@GetMapping(path = "latest")
	public ResponseEntity<Translation> getLatestTranslation(@PathVariable("entryId") Long entryId, @RequestParam(name = "language", defaultValue = "en") String language) {
		final Translation translation = this.translationMapper.getLatestTranslation(entryId, language)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "The requested translation is not found."));
		return ResponseEntity.ok(translation);
	}

	@GetMapping(path = "languages")
	public ResponseEntity<List<String>> getAvailableLanguage(@PathVariable("entryId") Long entryId) {
		final List<String> availableLanguage = this.translationMapper.getAvailableLanguage(entryId);
		return ResponseEntity.ok(availableLanguage);
	}

	@GetMapping(path = "revisions/{revision}")
	public ResponseEntity<Translation> getTranslation(@PathVariable("entryId") Long entryId, @PathVariable("revision") Integer revision, @RequestParam(name = "language", defaultValue = "en") String language) {
		final TranslationKey translationKey = new TranslationKey(entryId, language, revision);
		final Translation translation = this.translationMapper.getTranslation(translationKey)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "The requested translation is not found."));
		return ResponseEntity.ok(translation);
	}

	@PostMapping(path = "")
	public ResponseEntity<?> postTranslation(@PathVariable("entryId") Long entryId, @RequestBody TranslationCreateRequest request) {
		final ConstraintViolations violations = request.validate();
		if (violations.isValid()) {
			final Integer nextRevision = this.translationMapper.getLatestTranslation(entryId, request.language())
					.map(x -> x.translationKey().nextRevision())
					.orElse(1);
			final TranslationKey translationKey = new TranslationKey(entryId, request.language(), nextRevision);
			final Translation translation = new Translation(translationKey, request.title(), request.content(), Instant.now());
			this.translationMapper.insert(translation);
			return ResponseEntity.status(CREATED).body(translation);
		}
		else {
			final List<ViolationDetail> details = violations.stream().map(ConstraintViolation::detail).collect(Collectors.toList());
			final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
			return ResponseEntity.badRequest().body(Map.of(
					"error", httpStatus.getReasonPhrase(),
					"status", httpStatus.value(),
					"details", details));
		}
	}

	@DeleteMapping(path = "revisions/{revision}")
	public ResponseEntity<?> deleteTranslation(@PathVariable("entryId") Long entryId, @PathVariable("revision") Integer revision, @RequestParam(name = "language", defaultValue = "en") String language) {
		final TranslationKey translationKey = new TranslationKey(entryId, language, revision);
		this.translationMapper.delete(translationKey);
		return ResponseEntity.noContent().build();
	}
}
