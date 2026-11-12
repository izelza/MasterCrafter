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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DebugControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void simulatedAnswersUseRealGameStateAndCannotDoubleCountOrAffectOtherPlayers() throws Exception {
        var session = new MockHttpSession();
        var start = start(session);
        var id = start.at("/challenge/id").asText();
        var failed = debug(session, id, "FAILURE");
        assertThat(failed.at("/result/correct").asBoolean()).isFalse();
        assertThat(failed.at("/game/status").asText()).isEqualTo("ACTIVE");
        assertThat(failed.at("/game/stack/completed").asInt()).isZero();
        var correct = debug(session, id, "SUCCESS");
        assertThat(correct.at("/result/correct").asBoolean()).isTrue();
        assertThat(correct.at("/game/status").asText()).isEqualTo("SOLVED");
        assertThat(correct.at("/game/stack/completed").asInt()).isEqualTo(1);
        assertThat(debug(session, id, "SUCCESS").at("/game/stack/completed").asInt()).isEqualTo(1);
        mvc.perform(post("/api/crafting/debug").session(new MockHttpSession()).contentType(MediaType.APPLICATION_JSON)
                        .content(command(id, "SUCCESS"))).andExpect(status().isConflict());
        var next = mapper.readTree(mvc.perform(post("/api/crafting/challenges").session(session))
                .andReturn().getResponse().getContentAsByteArray());
        assertThat(next.get("id").asText()).isNotEqualTo(id);
        mvc.perform(post("/api/crafting/debug").session(session).contentType(MediaType.APPLICATION_JSON)
                .content(command(id, "SUCCESS"))).andExpect(status().isConflict());
    }

    @Test
    void completionSolvesRemainingTargetsIncludingPendingNextAndFreezesTime() throws Exception {
        var session = new MockHttpSession();
        var start = start(session);
        var id = start.at("/challenge/id").asText();
        debug(session, id, "SUCCESS"); // A correct response can arrive before Next has been requested.
        var completed = debug(session, id, "COMPLETE_STACK").get("game");
        assertThat(completed.at("/stack/status").asText()).isEqualTo("COMPLETE");
        assertThat(completed.at("/stack/completed").asInt()).isEqualTo(64);
        assertThat(completed.at("/stack/remaining").asInt()).isZero();
        assertThat(completed.at("/stack/entries")).allSatisfy(entry -> assertThat(entry.get("completed").asBoolean()).isTrue());
        var retry = debug(session, completed.at("/challenge/id").asText(), "COMPLETE_STACK");
        assertThat(retry.get("game")).isEqualTo(completed);
        mvc.perform(post("/api/crafting/stack/stop").session(session))
                .andExpect(jsonPath("$.stack.status").value("INACTIVE"))
                .andExpect(jsonPath("$.stack.elapsedMillis").value(0));
    }

    @Test
    void freePlaySupportsAnswersButRejectsCompletionAndRevealedAnswers() throws Exception {
        var session = new MockHttpSession();
        var challenge = mapper.readTree(mvc.perform(post("/api/crafting/challenges").session(session))
                .andReturn().getResponse().getContentAsByteArray());
        var id = challenge.get("id").asText();
        assertThat(debug(session, id, "FAILURE").at("/result/correct").asBoolean()).isFalse();
        mvc.perform(post("/api/crafting/debug").session(session).contentType(MediaType.APPLICATION_JSON)
                .content(command(id, "COMPLETE_STACK"))).andExpect(status().isConflict());
        mvc.perform(post("/api/crafting/challenges/" + id + "/solution").session(session)).andExpect(status().isOk());
        mvc.perform(post("/api/crafting/debug").session(session).contentType(MediaType.APPLICATION_JSON)
                .content(command(id, "SUCCESS"))).andExpect(status().isConflict());
        mvc.perform(post("/api/crafting/challenges").session(session)).andExpect(status().isOk());
        var game = mapper.readTree(mvc.perform(get("/api/crafting/game").session(session))
                .andReturn().getResponse().getContentAsByteArray());
        assertThat(debug(session, game.at("/challenge/id").asText(), "SUCCESS").at("/result/correct").asBoolean()).isTrue();
    }

    @Test
    void missingOrUnknownActionsAreBadRequests() throws Exception {
        mvc.perform(post("/api/crafting/debug").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/crafting/debug").contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
    }

    private JsonNode start(MockHttpSession session) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/crafting/stack/start").session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }

    private JsonNode debug(MockHttpSession session, String id, String action) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/crafting/debug").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(command(id, action)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }

    private String command(String id, String action) throws Exception {
        return mapper.writeValueAsString(Map.of("challengeId", id, "action", action));
    }
}
