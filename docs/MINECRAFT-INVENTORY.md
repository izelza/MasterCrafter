# Inventory, sökfält och läderikoner

Referens är samma [officiella Minecraft Java 26.3-klient](https://piston-data.mojang.com/v1/objects/e877b6a07acd633fb3bb475002175cec036e7b87/client.jar)
som övriga GUI-resurser. Klientens SHA-1 är `e877b6a07acd633fb3bb475002175cec036e7b87`.

## Inventory och tooltip

En slot är 72×72 CSS-pixlar med fyrpixlars skuggkant och en centrerad
48×48-ikon. Ikoner från både 16×16-texturer och 64×64-renderingar visas
med nearest-neighbor-skalning. Explicit enradig/enkolumnig CSS-grid och
dolda gamla namn-spans skyddar geometrin även mot äldre markup.

Itemnamn finns som `aria-label`, men ingen text eller native `title`
läggs inne i knappen. `item-tooltip.js` visar ett enda överlägg direkt
under `body`, utanför inventoryts scrollande yta. Det följer pekaren,
hålls inom skärmen och visas också vid tangentbordsfokus. Klick,
scroll, Escape, sidbyte och fönsterbyte döljer det. Överlägget tar inga klick.

Tooltipens färger är pixelmätta i Mojangs texturer under
`assets/minecraft/textures/gui/sprites/tooltip/`:

| Källa | Pixel | RGBA |
|---|---|---|
| `background.png` | 50,50 | 16,0,16,240 |
| `frame.png` | 50,9 | 80,0,255,80 |
| `frame.png` | 50,90 | 40,0,127,80 |

CSS återger bakgrunden och kantens gradient med dessa RGBA-värden,
vit text och mörk textskugga. Oförändrade referensbilder ligger i
`docs/references/minecraft-tooltip-background.png` och `minecraft-tooltip-frame.png`.
Deras SHA-256 är `b3b7fdcec719bd1ad2dfc655d79d61aff57aaed7065874e1e8235b32039434a4`
respektive `8cd60f7b1dc3f43335a8c97ee50d24f937e958ea9ba36634aae492eccd871ad5`.

Sökfältets ikon är originalet `assets/minecraft/textures/gui/sprites/icon/search.png`,
sparad som `static/ui/minecraft/search.png`, SHA-256
`c3e27020baba359cb74a5c340b377986d08e0d17259a723183bc5049e192b898`.
Det grå infällda fältet följer färgerna i `container/creative_inventory/tab_item_search.png`.
Fältet saknar placeholder och har kvar en tillgänglig label.

## Ofärgat läder

Originalens vita `textures/item/leather_*.png` är färgningsbara baslager.
`models/item/leather_*.json` anger ett andra, ofärgat `*_overlay.png`-lager.
`items/leather_*.json` anger standardfärgen `-6265536`, dvs. RGB `#A06540`,
för helmet, chestplate, leggings, boots och horse_armor.

`UndyedLeatherIcons` multiplicerar baslagrets RGB med standardfärgen,
behåller alfa och ritar sedan detaljlagret med vanlig alfakomposition.
De fem PNG-resultaten cachas i minnet vid start. `ItemIconController`
serverar dem på samma `/item-icons/minecraft/leather_*.png`-URL:er som tidigare;
övriga ikoner förblir statiska resurser. Detta gäller också quiz, mål och muspekare.

Bastexturerna behålls i `mc-resources`; oförändrade överlägg finns i
`src/main/resources/minecraft/icon-overlays/`. Renderingen behöver varken
extern tjänst, grafikskärm eller nya beroenden.
