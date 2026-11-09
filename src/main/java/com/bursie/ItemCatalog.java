package com.bursie;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ItemCatalog {
    private final List<Item> items;

    public ItemCatalog(ObjectMapper mapper) throws IOException {
        List<String> ids;
        Map<String, String> names;
        try (var input = new ClassPathResource("minecraft/items.json").getInputStream()) {
            ids = mapper.readValue(input, new TypeReference<>() {});
        }
        try (var input = new ClassPathResource("minecraft/lang/sv_se.json").getInputStream()) {
            names = mapper.readValue(input, new TypeReference<>() {});
        }

        var loaded = new ArrayList<Item>();
        for (String id : ids) {
            String name = names.get(id);
            String iconUrl = "/item-icons/" + id.replace(':', '/') + ".png";
            if (name == null || name.isBlank()
                    || !new ClassPathResource("static" + iconUrl).exists()) {
                throw new IllegalStateException("Namn eller ikon saknas för " + id);
            }
            loaded.add(new Item(id, name, iconUrl));
        }
        if (loaded.stream().map(Item::name).distinct().count() < 2) {
            throw new IllegalStateException("Spelet behöver minst två olika itemnamn.");
        }
        items = List.copyOf(loaded);
    }

    public List<Item> items() {
        return items;
    }

    public record Item(String id, String name, String iconUrl) {}
}
