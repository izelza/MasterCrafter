package com.bursie;

import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemIconController {
    private final UndyedLeatherIcons leatherIcons;

    public ItemIconController(UndyedLeatherIcons leatherIcons) {
        this.leatherIcons = leatherIcons;
    }

    // Other icons continue to use Spring's ordinary static resource handler.
    @GetMapping(value = "/item-icons/minecraft/{name:leather_helmet|leather_chestplate|leather_leggings|leather_boots|leather_horse_armor}.png",
            produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> leather(@PathVariable String name) {
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(leatherIcons.get(name));
    }
}
