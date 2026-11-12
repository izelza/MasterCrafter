# Markering av crafting-rutor

Mojangs original använder två genomskinliga sprite-lager för markerade rutor:
ett bakom föremålet och ett framför. Det beskrivs i
[Mojangs releaseinformation](https://www.minecraft.net/it-it/article/minecraft-snapshot-24w37a).
Vi använder de oförändrade texturerna från samma
[officiella Java 26.3-klient](https://piston-data.mojang.com/v1/objects/e877b6a07acd633fb3bb475002175cec036e7b87/client.jar)
som inventory-temat.

| Textur under `assets/minecraft/textures/gui/sprites/container/` | Storlek | Vitt i mitten | SHA-256 |
|---|---|---|---|
| `slot_highlight_back.png` | 24×24 | Alfa 96/255 | `6e844a8cf0ae479e3c3846b6e9a004ac805e937bf9889b4f605bba270b3b47f7` |
| `slot_highlight_front.png` | 24×24 | Alfa 32/255 | `7612266799b7c7bdec2a9ef562fd1c0ec7ae28c048d3e0f7ed5e08a2cc2ced26` |

I båda bilderna är pixlarna x=4–19, y=4–19 vita med ovanstående alfa.
Den fyra pixlar breda marginalen runt dem är helt transparent.
Originalens `.mcmeta` anger `nine_slice`, 24×24 och kantbredd 4.
Klientens `AbstractContainerScreen.extractSlotHighlightBack/Front` placerar
24×24-lagren vid föremålspositionen minus 4 i båda riktningarna.

Vår grid använder fyra CSS-pixlar per Minecraft-pixel: en ruta är 72×72,
skuggkanterna 4 och insidan 64×64. Varje highlight-bild visas därför i
96×96, med sin transparenta marginal på 16 pixlar utanför insidan.
Det synliga lagret täcker exakt insidan och lämnar skuggkanterna orörda.

`::before` visar baklagret, item-ikonen ligger ovanför det och `::after`
visar framlagret. Lagren är klickgenomsläppliga och visas endast när en
redigerbar ruta hovras eller har tangentbordsfokus. Fokus använder samma
ljusning utan vit outline eller extra box-shadow. Inte heller facit får en vit
ram runt varje ruta.

Inventory och crafting-grid delar samma CSS för kvadratiska rutor på 72×72,
fyrpixlars kanter, 48×48-ikoner och bak-/framlager utan vit hover-ram.
Inventoryts kolumner anpassas efter utrymmet utan att rutorna sträcks.
Itemnamnet visas vid hover och finns som tillgängligt namn för skärmläsare;
texten tar ingen plats inne i rutan. Valt item får ingen bestående markering
i inventoryt; bara muspekarens ikon visar vilket item spelaren håller.

Webbläsarens skärmbild verifierades med pixelprover: tom markerad insida
blev `#C0C0C0` av originalens två alfalager över `#8B8B8B`. Skuggkanten
förblev `#373737`, den ljusa kanten `#FFFFFF` och grannrutan `#8B8B8B`.
