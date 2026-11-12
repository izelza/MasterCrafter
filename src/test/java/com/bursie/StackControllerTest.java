package com.bursie;

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
class StackControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired RecipeCatalog recipes;

    @Test
    void completeRunAndStopWorkThroughTheRealHttpApi() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(get("/api/crafting/game").session(session)).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.stack.status").value("INACTIVE"));
        var start = mapper.readTree(mvc.perform(post("/api/crafting/stack/start").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stack.total").value(64))
                .andReturn().getResponse().getContentAsByteArray());
        var challenge = start.get("challenge");
        mvc.perform(post("/api/crafting/challenges/" + challenge.get("id").asText() + "/solution").session(session))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/crafting/game").session(new MockHttpSession()))
                .andExpect(jsonPath("$.stack.status").value("INACTIVE"));
        for (int index = 0; index < 64; index++) {
            var recipe = recipes.recipesFor(challenge.at("/target/id").asText()).getFirst();
            mvc.perform(post("/api/crafting/attempts").session(session).contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(Map.of("challengeId", challenge.get("id").asText(), "grid", recipe.exampleGrid()))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(true));
            mvc.perform(get("/api/crafting/game").session(session)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.stack.completed").value(index + 1))
                    .andExpect(jsonPath("$.stack.remaining").value(63 - index));
            if (index < 63) challenge = mapper.readTree(mvc.perform(post("/api/crafting/challenges").session(session))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        }
        var finished = mapper.readTree(mvc.perform(get("/api/crafting/game").session(session))
                .andExpect(jsonPath("$.stack.status").value("COMPLETE")).andReturn().getResponse().getContentAsByteArray());
        mvc.perform(get("/api/crafting/game").session(session))
                .andExpect(jsonPath("$.stack.elapsedMillis").value(finished.at("/stack/elapsedMillis").asLong()));
        var stopped = mapper.readTree(mvc.perform(post("/api/crafting/stack/stop").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stack.status").value("INACTIVE"))
                .andExpect(jsonPath("$.stack.elapsedMillis").value(0)).andReturn().getResponse().getContentAsByteArray());
        assertThat(stopped.at("/challenge/id").asText()).isNotEqualTo(challenge.get("id").asText());
        mvc.perform(post("/api/crafting/challenges/" + stopped.at("/challenge/id").asText() + "/solution").session(session))
                .andExpect(status().isOk());
    }
}
