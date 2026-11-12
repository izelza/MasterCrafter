package com.bursie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeExamplesTest {
    private static RecipeCatalog recipes;

    @BeforeAll
    static void loadData() throws IOException {
        var mapper = new ObjectMapper();
        recipes = new RecipeCatalog(mapper, new ItemCatalog(mapper));
    }

    @Test
    void tagsUseFamiliarIngredientsInsteadOfAlphabeticalOrder() {
        assertThat(example("wooden_pickaxe")).isEqualTo(Arrays.asList(
                "minecraft:oak_planks", "minecraft:oak_planks", "minecraft:oak_planks",
                null, "minecraft:stick", null, null, "minecraft:stick", null));
        assertThat(example("campfire")).isEqualTo(Arrays.asList(
                null, "minecraft:stick", null, "minecraft:stick", "minecraft:coal", "minecraft:stick",
                "minecraft:oak_log", "minecraft:oak_log", "minecraft:oak_log"));
        assertThat(example("stone_pickaxe")).contains("minecraft:cobblestone")
                .doesNotContain("minecraft:blackstone", "minecraft:cobbled_deepslate");
        assertThat(example("painting").get(4)).isEqualTo("minecraft:white_wool");
    }

    @Test
    void explicitAlternativesAlsoUseCoalSandAndPlainBlocks() {
        assertThat(example("tnt")).contains("minecraft:sand").doesNotContain("minecraft:red_sand");
        assertThat(example("torch")).contains("minecraft:coal").doesNotContain("minecraft:charcoal");
        assertThat(example("fire_charge")).contains("minecraft:coal").doesNotContain("minecraft:charcoal");
        assertThat(example("quartz_stairs")).contains("minecraft:quartz_block")
                .doesNotContain("minecraft:chiseled_quartz_block", "minecraft:quartz_pillar");
        assertThat(example("sandstone_stairs")).contains("minecraft:sandstone")
                .doesNotContain("minecraft:chiseled_sandstone", "minecraft:cut_sandstone");
    }

    @Test
    void specificMaterialsAndNonPreferredAnswersRemainValid() {
        for (String wood : new String[]{"oak", "acacia", "birch", "mangrove", "pale_oak"}) {
            assertThat(example(wood + "_planks")).contains("minecraft:" + wood + "_log");
        }
        assertThat(example("red_sandstone_stairs")).contains("minecraft:red_sandstone")
                .doesNotContain("minecraft:sandstone");

        var acaciaPickaxe = new ArrayList<>(example("wooden_pickaxe"));
        acaciaPickaxe.replaceAll(id -> "minecraft:oak_planks".equals(id) ? "minecraft:acacia_planks" : id);
        assertThat(recipes.matches("minecraft:wooden_pickaxe", acaciaPickaxe)).isTrue();
        var redSandTnt = new ArrayList<>(example("tnt"));
        redSandTnt.replaceAll(id -> "minecraft:sand".equals(id) ? "minecraft:red_sand" : id);
        assertThat(recipes.matches("minecraft:tnt", redSandTnt)).isTrue();
        var charcoalTorch = new ArrayList<>(example("torch"));
        charcoalTorch.replaceAll(id -> "minecraft:coal".equals(id) ? "minecraft:charcoal" : id);
        assertThat(recipes.matches("minecraft:torch", charcoalTorch)).isTrue();
    }

    private static java.util.List<String> example(String result) {
        return recipes.recipesFor("minecraft:" + result).stream()
                .filter(recipe -> recipe.id().equals(result + ".json"))
                .findFirst().orElseThrow().exampleGrid();
    }
}
