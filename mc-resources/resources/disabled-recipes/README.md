# Inaktiverade recept

`smithing-templates/` innehåller de 19 ursprungliga recepten för att kopiera
smithing templates. Oscar valde att utesluta dem från crafting-spelets mål.
JSON-filerna är bevarade utan innehållsändringar.

Av receptfilerna paketerar Maven bara dem direkt i `resources/recipes/`. Den här katalogen
läses inte av RecipeCatalog. Backend ignorerar även recept med resultat-ID som
slutar på `_smithing_template`, så äldre byggkopior inte kan återaktivera dem.

Föremålens namn och ikoner finns kvar i inventoryt och frågespelet. Slimeball
finns fortfarande bland aktiva recept (ett Slime Block ger nio Slimeballs).
