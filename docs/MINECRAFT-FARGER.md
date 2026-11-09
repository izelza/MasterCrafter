# Minecrafts inventory-färger

Färgerna är avlästa direkt ur PNG-filerna i Mojangs **Minecraft Java 26.3**,
inte uppskattade från skärmbilder eller en färgpalett från tredje part.

- [Officiell versionsmetadata](https://piston-meta.mojang.com/v1/packages/80608ff6753705b6a82e2cb86a5937d4d233abd4/26.3.json)
- [Mojangs klientarkiv](https://piston-data.mojang.com/v1/objects/e877b6a07acd633fb3bb475002175cec036e7b87/client.jar)
- Klientens SHA-1 kontrollerades mot metadata: `e877b6a07acd633fb3bb475002175cec036e7b87`.

| Användning | Exakt färg | Källa i klientarkivet |
|---|---|---|
| Inventory-panel | `#C6C6C6` | `assets/minecraft/textures/gui/container/inventory.png` |
| Rutornas insida | `#8B8B8B` | Samma fil samt `gui/sprites/container/slot.png` |
| Mörk kant i rutor | `#373737` | Samma filer |
| Yttre skuggkant | `#555555` | `inventory.png` |
| Ljus kant | `#FFFFFF` | `inventory.png` och `slot.png` |
| Yttre kontur | `#000000` | `inventory.png` |
| Vanlig knapps dominerande gråton | `#6F6F6F` | `gui/sprites/widget/button.png` |
| Markerad knapps dominerande gråton | `#757575` | `gui/sprites/widget/button_highlighted.png` |
| Avstängd knapps dominerande gråton | `#2C2C2C` | `gui/sprites/widget/button_disabled.png` |

Klientarkivets fullständiga sökvägar, bildstorlekar, SHA-256 och pixelkoordinater
för färgproverna finns i [mätdata](references/minecraft-26.3-gui-colors.json).
Den ursprungliga [inventory-texturen](references/minecraft-26.3-inventory.png)
är bevarad som referens. Koordinaterna räknas från noll, med origo uppe till vänster.

Spelets oförändrade knapp- och ruttexturer används direkt i CSS från
`src/main/resources/static/ui/minecraft/`. Då bevaras även knapptexturernas
grå variationer. Paletten ligger samlad i `minecraft-theme.css`.

Hover använder även originalets genomskinliga bak- och framlager. De blandas
med rutans färg enligt [docs/MINECRAFT-HOVER.md](MINECRAFT-HOVER.md).

Webbsidans yttre bakgrund använder `#555555` från inventoryts skuggkant.
Det är en tillämpning av paletten på vår webbsida; Minecrafts bakgrund bakom
inventory-fönstret är spelvärlden och har ingen enda fast grå färg.
