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

    @Test
    void choosingTargetReplacesRevealedChallengeAndAcceptsItsActualRecipe() throws Exception {
        var session = new MockHttpSession();
        var original = mapper.readTree(mvc.perform(post("/api/crafting/challenges").session(session))
                .andReturn().getResponse().getContentAsByteArray());
        String oldId = original.get("id").asText();
        mvc.perform(post("/api/crafting/challenges/" + oldId + "/solution").session(session)).andExpect(status().isOk());
        var selected = mapper.readTree(mvc.perform(post("/api/crafting/debug/target").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(targetCommand(oldId, "minecraft:stick")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.challenge.target.id").value("minecraft:stick"))
                .andExpect(jsonPath("$.status").value("ACTIVE")).andExpect(jsonPath("$.solution").isEmpty())
                .andReturn().getResponse().getContentAsByteArray());
        var id = selected.at("/challenge/id").asText();
        assertThat(id).isNotEqualTo(oldId);
        mvc.perform(post("/api/crafting/attempts").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("challengeId", id, "grid", java.util.Arrays.asList(
                                "minecraft:oak_planks", null, null, "minecraft:oak_planks", null, null, null, null, null)))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(true));
        mvc.perform(post("/api/crafting/debug/target").session(new MockHttpSession())
                .contentType(MediaType.APPLICATION_JSON).content(targetCommand(id, "minecraft:stick")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidOrStaleTargetsAndStackChangesAreRejectedWithoutChangingTheGame() throws Exception {
        var session = new MockHttpSession();
        var original = mapper.readTree(mvc.perform(post("/api/crafting/challenges").session(session))
                .andReturn().getResponse().getContentAsByteArray());
        var id = original.get("id").asText();
        for (String item : java.util.List.of("minecraft:flint", "minecraft:not_an_item", "minecraft:coast_armor_trim_smithing_template")) {
            mvc.perform(post("/api/crafting/debug/target").session(session).contentType(MediaType.APPLICATION_JSON)
                    .content(targetCommand(id, item))).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/crafting/debug/target").session(session).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("challengeId", id)))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/crafting/debug/target").session(session).contentType(MediaType.APPLICATION_JSON)
                .content(targetCommand("stale-id", "minecraft:stick"))).andExpect(status().isConflict());
        mvc.perform(get("/api/crafting/game").session(session)).andExpect(jsonPath("$.challenge.id").value(id));
        var stack = start(session);
        mvc.perform(post("/api/crafting/debug/target").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content(targetCommand(stack.at("/challenge/id").asText(), "minecraft:stick")))
                .andExpect(status().isConflict());
        var after = mapper.readTree(mvc.perform(get("/api/crafting/game").session(session))
                .andReturn().getResponse().getContentAsByteArray());
        assertThat(after.get("challenge")).isEqualTo(stack.get("challenge"));
        assertThat(after.at("/stack/entries")).isEqualTo(stack.at("/stack/entries"));
    }

    private String targetCommand(String id, String item) throws Exception {
        return mapper.writeValueAsString(Map.of("challengeId", id, "targetId", item));
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
