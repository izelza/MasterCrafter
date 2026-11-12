# Receptens svårighetsgrad

Oscars regel: svårighetsgrad = antal olika ingredienstyper i receptet.

- Flera rutor med samma ingredienstyp räknas en gång.
- Tomma rutor räknas inte.
- En tagg som tillåter flera träslag räknas som en ingredienstyp.
- En alternativlista som coal/charcoal eller sand/red sand räknas som en ingredienstyp.
- Mängd, placering, spegling och vilket giltigt alternativ som väljs påverkar inte nivån.

Ingredienstyper jämförs genom sina uppsättningar av accepterade item-ID:n.
Två ingredienser med exakt samma tillåtna alternativ är därför samma typ,
även om alternativen är angivna i olika ordning. Det representativa Skip-facitet
används inte för beräkningen.

Exempel: en wooden pickaxe innehåller plankor och pinnar och får nivå 2,
trots att fem slots är fyllda. Ett campfire innehåller pinnar, kol och logs
och får nivå 3.

Fördelning bland de 1 007 aktiva recepten den 12 november 2026:

| Nivå | Antal recept |
|---|---:|
| 1 | 520 |
| 2 | 398 |
| 3 | 83 |
| 4 | 4 |
| 5 | 2 |

De två recepten på nivå 5 är rabbit stew med brun respektive röd svamp.
Nivån räknas per receptvariant; flera recept för samma target kan ha olika nivå.
Smithing template-recept som är inaktiverade ingår inte.

`CraftingRecipe.difficulty()` beräknar nivån från receptets ingredienser.
`RecipeCatalog.recipesByDifficulty()` innehåller grupperna i stigande
nivåordning; både kartan och listorna är skrivskyddade. Grupperna byggs
automatiskt när katalogen läses och kräver ingen separat klassificeringsfil.

Detta är intern backend-funktionalitet för framtida användning. Inga
nya API-fält, spelregler eller synliga kontroller har lagts till.
