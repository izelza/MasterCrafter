package com.bursie;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class UndyedLeatherIconsTest {
    @Test
    void allFiveIconsHaveBrownLeatherAndRestoreUntintedOverlayPixels() throws Exception {
        var icons = new UndyedLeatherIcons();
        int restoredDetails = 0;
        for (String part : new String[]{"helmet", "chestplate", "leggings", "boots", "horse_armor"}) {
            String name = "leather_" + part;
            var rendered = ImageIO.read(new ByteArrayInputStream(icons.get(name)));
            try (var baseInput = new ClassPathResource("static/item-icons/minecraft/" + name + ".png").getInputStream();
                 var overlayInput = new ClassPathResource("minecraft/icon-overlays/" + name + "_overlay.png").getInputStream()) {
                var base = ImageIO.read(baseInput);
                var overlay = ImageIO.read(overlayInput);
                assertThat(rendered.getWidth()).isEqualTo(16);
                assertThat(rendered.getHeight()).isEqualTo(16);
                int brownPixels = 0;
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        int result = rendered.getRGB(x, y);
                        int detail = overlay.getRGB(x, y);
                        int source = base.getRGB(x, y);
                        if ((detail >>> 24) == 255) {
                            assertThat(result).as(name + " untinted detail").isEqualTo(detail);
                            if ((source >>> 24) == 0) restoredDetails++;
                        } else if ((source >>> 24) == 255 && (detail >>> 24) == 0) {
                            assertThat((result >>> 16) & 255).isGreaterThan((result >>> 8) & 255);
                            assertThat((result >>> 8) & 255).isGreaterThan(result & 255);
                            brownPixels++;
                        }
                    }
                }
                assertThat(brownPixels).as(name + " visible leather").isPositive();
            }
        }
        assertThat(restoredDetails).as("formerly missing details restored by the overlays").isPositive();
    }
}
