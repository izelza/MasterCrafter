# Minecraft-fonten

Spelet använder **Minecraft Seven v2**, hämtad oförändrad från Mojangs egen webbplats:

- [Originalfont, WOFF2](https://www.minecraft.net/etc.clientlibs/minecraftnet/clientlibs/clientlib-site/resources/fonts/Minecraft-Seven_v2.woff2)
- [Mojangs CSS som registrerar MinecraftSeven-v2](https://www.minecraft.net/etc.clientlibs/minecraftnet/clientlibs/clientlib-site.min.1099720261026113502000094003171793182723587.css)
- Storlek: 14 060 byte.
- SHA-256: `b2840d23454bc5cef67926850733b8b29b9bdd12a6fd75857c03aed7bbd2dc5d`.

Fonten finns lokalt i `src/main/resources/static/fonts/Minecraft-Seven_v2.woff2`.
`minecraft-font.css` registrerar den med `@font-face`. Båda spellägena förladdar
fonten och använder den även i knappar, sökfält och inventory.

Ingen extern fonttjänst, npm eller nytt byggverktyg behövs när appen körs.
Monospace finns som reserv om filen inte kan laddas. Syntetisk fetstil är
avstängd för att bevara pixelbokstävernas form, och inventoryts små etiketter
är minst 12 CSS-pixlar för läsbarhet.

Webbläsartestet verifierade att både engelska bokstäver, siffror och
`ÅÄÖåäö` verkligen renderas med den inlästa webbfonten, utan reservfont.
Desktop- och mobilvyer granskades visuellt.
