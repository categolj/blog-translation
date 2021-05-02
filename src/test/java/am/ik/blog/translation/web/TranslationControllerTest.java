package am.ik.blog.translation.web;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import am.ik.blog.translation.Translation;
import am.ik.blog.translation.TranslationKey;
import am.ik.blog.translation.TranslationMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TranslationController.class)
class TranslationControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	TranslationMapper translationMapper;

	@Test
	void getAllRevisionsOfTranslation() throws Exception {
		final long entryId = 100L;
		final String language = "en";
		given(this.translationMapper.getAllRevisionsOfTranslation(entryId, language))
				.willReturn(List.of(
						new Translation(new TranslationKey(entryId, language, 2), "Hello World!", null, Instant.now()),
						new Translation(new TranslationKey(entryId, language, 1), "Hello World!", null, Instant.now().minusSeconds(10000))));
		this.mockMvc.perform(get("/translations/{entryId}", entryId).param("language", language))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].entryId").value(entryId))
				.andExpect(jsonPath("$[0].language").value(language))
				.andExpect(jsonPath("$[0].revision").value(2))
				.andExpect(jsonPath("$[0].title").value("Hello World!"))
				.andExpect(jsonPath("$[0].content").doesNotExist())
				.andExpect(jsonPath("$[0].createdAt").isString())
				.andExpect(jsonPath("$[1].entryId").value(entryId))
				.andExpect(jsonPath("$[1].language").value(language))
				.andExpect(jsonPath("$[1].revision").value(1))
				.andExpect(jsonPath("$[1].title").value("Hello World!"))
				.andExpect(jsonPath("$[1].content").doesNotExist())
				.andExpect(jsonPath("$[1].createdAt").isString());
	}

	@Test
	void getLatestTranslation() throws Exception {
		final long entryId = 100L;
		final String language = "en";
		given(this.translationMapper.getLatestTranslation(entryId, language))
				.willReturn(Optional.of(new Translation(new TranslationKey(entryId, language, 100), "Hello World!", "This is a test content.", Instant.now())));
		this.mockMvc.perform(get("/translations/{entryId}/latest", entryId).param("language", language))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.entryId").value(entryId))
				.andExpect(jsonPath("$.language").value(language))
				.andExpect(jsonPath("$.revision").value(100))
				.andExpect(jsonPath("$.title").value("Hello World!"))
				.andExpect(jsonPath("$.content").value("This is a test content."))
				.andExpect(jsonPath("$.createdAt").isString());
	}

	@Test
	void getTranslation() throws Exception {
		final long entryId = 100L;
		final int revision = 1;
		final String language = "en";
		given(this.translationMapper.getTranslation(new TranslationKey(entryId, language, revision)))
				.willReturn(Optional.of(new Translation(new TranslationKey(entryId, language, revision), "Hello World!", "This is a test content.", Instant.now())));
		this.mockMvc.perform(get("/translations/{entryId}/revisions/{revision}", entryId, revision).param("language", language))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.entryId").value(entryId))
				.andExpect(jsonPath("$.language").value(language))
				.andExpect(jsonPath("$.revision").value(1))
				.andExpect(jsonPath("$.title").value("Hello World!"))
				.andExpect(jsonPath("$.content").value("This is a test content."))
				.andExpect(jsonPath("$.createdAt").isString());
	}

	@Test
	void getAvailableLanguage() throws Exception {
		final long entryId = 100L;
		final int revision = 1;
		given(this.translationMapper.getAvailableLanguage(entryId))
				.willReturn(List.of("en", "cn"));
		this.mockMvc.perform(get("/translations/{entryId}/languages", entryId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0]").value("en"))
				.andExpect(jsonPath("$[1]").value("cn"));
	}

	@Test
	void postTranslation_created_new() throws Exception {
		final long entryId = 100L;
		final String language = "en";
		given(this.translationMapper.getLatestTranslation(entryId, language)).willReturn(Optional.empty());
		given(this.translationMapper.insert(any())).willReturn(1);
		this.mockMvc.perform(post("/translations/{entryId}", entryId)
				.content("""
						{"title": "Hello World!", "content": "This is a test content.", "language": "%s"}
						""".formatted(language))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.entryId").value(entryId))
				.andExpect(jsonPath("$.language").value(language))
				.andExpect(jsonPath("$.revision").value(1))
				.andExpect(jsonPath("$.title").value("Hello World!"))
				.andExpect(jsonPath("$.content").value("This is a test content."))
				.andExpect(jsonPath("$.createdAt").isString());
	}

	@Test
	void postTranslation_created_increment() throws Exception {
		final long entryId = 100L;
		final String language = "en";
		given(this.translationMapper.getLatestTranslation(entryId, language))
				.willReturn(Optional.of(new Translation(new TranslationKey(entryId, language, 100), "Hello World!", "This is a test content.", Instant.now())));
		given(this.translationMapper.insert(any())).willReturn(1);
		this.mockMvc.perform(post("/translations/{entryId}", entryId)
				.content("""
						{"title": "Hello World!", "content": "This is a test content.", "language": "%s"}
						""".formatted(language))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.entryId").value(entryId))
				.andExpect(jsonPath("$.language").value(language))
				.andExpect(jsonPath("$.revision").value(101))
				.andExpect(jsonPath("$.title").value("Hello World!"))
				.andExpect(jsonPath("$.content").value("This is a test content."))
				.andExpect(jsonPath("$.createdAt").isString());
	}

	@Test
	void postTranslation_badRequest() throws Exception {
		final long entryId = 100L;
		this.mockMvc.perform(post("/translations/{entryId}", entryId)
				.content("""
						{}
						""")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.details.length()").value(3))
				.andExpect(jsonPath("$.details[0].args[0]").value("language"))
				.andExpect(jsonPath("$.details[1].args[0]").value("title"))
				.andExpect(jsonPath("$.details[2].args[0]").value("content"));
	}

	@Test
	void deleteTranslation() throws Exception {
		final long entryId = 100L;
		final int revision = 1;
		final String language = "en";
		given(this.translationMapper.delete(any())).willReturn(1);
		this.mockMvc.perform(delete("/translations/{entryId}/revisions/{revision}", entryId, revision).param("language", language))
				.andExpect(status().isNoContent());
	}
}