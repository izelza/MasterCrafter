package com.bursie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuizControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ItemCatalog catalog;

    @Test
    void leatherIconsAreServedAsRenderedPngAndOtherIconsStillWork() throws Exception {
        for (String part : new String[]{"helmet", "chestplate", "leggings", "boots", "horse_armor"}) {
            var response = mvc.perform(get("/item-icons/minecraft/leather_" + part + ".png"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_PNG))
                    .andReturn().getResponse();
            var image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(response.getContentAsByteArray()));
            assertThat(image.getWidth()).isEqualTo(16);
            assertThat(image.getHeight()).isEqualTo(16);
        }
        mvc.perform(get("/item-icons/minecraft/stone.png"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void questionContainsTwoDistinctNamesAndAnAvailableIcon() throws Exception {
        var question = newQuestion(new MockHttpSession());
        assertThat(question.get("options").size()).isEqualTo(2);
        assertThat(question.at("/options/0/name").asText())
                .isNotEqualTo(question.at("/options/1/name").asText());
        assertThat(question.has("correctOptionId")).isFalse();
        mvc.perform(get(question.get("iconUrl").asText()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
        var page = mvc.perform(get("/quiz.html")).andExpect(status().isOk()).andReturn().getResponse();
        assertThat(page.getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .contains("What is this?");
    }

    @Test
    void correctAnswerIsAcceptedOnlyOnce() throws Exception {
        var session = new MockHttpSession();
        var question = newQuestion(session);
        String optionId = optionId(question, true);
        mvc.perform(submit(question, optionId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.correctOptionId").value(optionId));
        mvc.perform(submit(question, optionId).session(session)).andExpect(status().isConflict());
    }

    @Test
    void wrongAnswerReturnsCorrectName() throws Exception {
        var session = new MockHttpSession();
        var question = newQuestion(session);
        String correctId = optionId(question, true);
        mvc.perform(submit(question, optionId(question, false)).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.correctName").value(correctName(question)))
                .andExpect(jsonPath("$.correctOptionId").value(correctId));
    }

    @Test
    void anotherBrowserCannotAnswerYourQuestion() throws Exception {
        var session = new MockHttpSession();
        var question = newQuestion(session);
        mvc.perform(submit(question, optionId(question, true)).session(new MockHttpSession()))
                .andExpect(status().isConflict());
        mvc.perform(submit(question, optionId(question, true)).session(session))
                .andExpect(status().isOk());
    }

    @Test
    void invalidOptionDoesNotConsumeQuestionAndNewQuestionReplacesOldOne() throws Exception {
        var session = new MockHttpSession();
        var first = newQuestion(session);
        mvc.perform(submit(first, "not-an-option").session(session)).andExpect(status().isBadRequest());
        var second = newQuestion(session);
        mvc.perform(submit(first, optionId(first, true)).session(session)).andExpect(status().isConflict());
        mvc.perform(submit(second, optionId(second, true)).session(session)).andExpect(status().isOk());
    }

    private JsonNode newQuestion(MockHttpSession session) throws Exception {
        var response = mvc.perform(post("/api/quiz/questions").session(session))
                .andExpect(status().isOk()).andReturn().getResponse();
        return mapper.readTree(response.getContentAsByteArray());
    }

    private String correctName(JsonNode question) {
        return catalog.items().stream()
                .filter(item -> item.iconUrl().equals(question.get("iconUrl").asText()))
                .findFirst().orElseThrow().name();
    }

    private String optionId(JsonNode question, boolean correct) {
        for (var option : question.get("options")) {
            if (option.get("name").asText().equals(correctName(question)) == correct) {
                return option.get("id").asText();
            }
        }
        throw new AssertionError("Svarsalternativ saknas");
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder submit(
            JsonNode question, String optionId) throws Exception {
        return post("/api/quiz/answers").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "questionId", question.get("id").asText(), "optionId", optionId)));
    }
}
