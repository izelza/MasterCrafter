package com.bursie;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Locale;
import java.text.Collator;
import java.text.Normalizer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ItemCatalog {
    private final List<Item> items;
    private final Map<String, Item> byId;

    public ItemCatalog(ObjectMapper mapper) throws IOException {
        List<String> ids;
        Map<String, String> names;
        try (var input = new ClassPathResource("minecraft/items.json").getInputStream()) {
            ids = mapper.readValue(input, new TypeReference<>() {});
        }
        try (var input = new ClassPathResource("minecraft/lang/en_us.json").getInputStream()) {
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
            // A fresh URL prevents previously cached white base textures from being reused.
            if (UndyedLeatherIcons.contains(id)) iconUrl += "?v=undyed-1";
            loaded.add(new Item(id, name, iconUrl));
        }
        if (loaded.stream().map(Item::name).distinct().count() < 2) {
            throw new IllegalStateException("Spelet behöver minst två olika itemnamn.");
        }
        var collator = Collator.getInstance(Locale.ENGLISH);
        loaded.sort(Comparator.comparing(Item::name, collator));
        items = List.copyOf(loaded);
        byId = items.stream().collect(Collectors.toUnmodifiableMap(Item::id, Function.identity()));
    }

    public List<Item> items() {
        return items;
    }

    public Item find(String id) {
        return byId.get(id);
    }

    public List<Item> search(String query) {
        String normalized = normalize(query.trim());
        return items.stream().filter(item -> normalize(item.name()).contains(normalized)
                || item.id().contains(normalized)).toList();
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    public record Item(String id, String name, String iconUrl) {}
}
