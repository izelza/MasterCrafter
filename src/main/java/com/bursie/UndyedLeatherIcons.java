package com.bursie;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Renders the supplied leather textures like Minecraft: default dye, then untinted details. */
@Component
public class UndyedLeatherIcons {
    // Minecraft 26.3 assets/minecraft/items/leather_*.json: dye default -6265536.
    private static final int DEFAULT_COLOR = 0xA06540;
    private final Map<String, byte[]> icons;

    public UndyedLeatherIcons() throws IOException {
        var rendered = new HashMap<String, byte[]>();
        for (String part : new String[]{"helmet", "chestplate", "leggings", "boots", "horse_armor"}) {
            String name = "leather_" + part;
            rendered.put(name, render(name));
        }
        icons = Map.copyOf(rendered);
    }

    public byte[] get(String name) {
        return icons.get(name);
    }

    private byte[] render(String name) throws IOException {
        BufferedImage base;
        BufferedImage overlay;
        try (var input = new ClassPathResource("static/item-icons/minecraft/" + name + ".png").getInputStream();
             var details = new ClassPathResource("minecraft/icon-overlays/" + name + "_overlay.png").getInputStream()) {
            base = ImageIO.read(input);
            overlay = ImageIO.read(details);
        }
        var result = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < base.getHeight(); y++) {
            for (int x = 0; x < base.getWidth(); x++) {
                int pixel = base.getRGB(x, y);
                int red = ((pixel >>> 16) & 255) * ((DEFAULT_COLOR >>> 16) & 255) / 255;
                int green = ((pixel >>> 8) & 255) * ((DEFAULT_COLOR >>> 8) & 255) / 255;
                int blue = (pixel & 255) * (DEFAULT_COLOR & 255) / 255;
                result.setRGB(x, y, (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue);
            }
        }
        var graphics = result.createGraphics();
        try {
            graphics.drawImage(overlay, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        var output = new ByteArrayOutputStream();
        ImageIO.write(result, "png", output);
        return output.toByteArray();
    }
}
