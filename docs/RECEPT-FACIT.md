# Representativa ingredienser i Skip recipe

Inställningarna finns i
[`src/main/resources/minecraft/recipe-examples.json`](../src/main/resources/minecraft/recipe-examples.json).
De används bara för att välja ingredienserna i det facit som Skip visar.
Spelarens svar kontrolleras mot samtliga giltiga ingredienser som tidigare.

Filen har två delar:

- `tagDefaults` anger ett representativt item för varje `#`-tagg. Alla 28 taggar som används i dagens recept har ett val.
- `alternativePriority` anger en prioritetsordning för ingredienser med flera alternativ. Det första tillåtna itemet väljs. Listan täcker också exempelvis sand/röd sand och coal/charcoal, som ibland är explicita listor i recepten.

Exempel:

```json
"#minecraft:planks": "minecraft:oak_planks",
"#minecraft:logs": "minecraft:oak_log",
"#minecraft:stone_tool_materials": "minecraft:cobblestone",
"#minecraft:coals": "minecraft:coal"
```

Generiska träingredienser visar oak. Recept som kräver ett särskilt träslag
behåller det: acacia-plankor visar en acacia-log, inte oak. Prioritetslistan
väljer även vanliga sandstone/quartz-block framför deras mejslade varianter
och vita ullföremål framför färgade när vitt är tillåtet.

Ett fast item i originalreceptet ersätts aldrig med något otillåtet. Saknas
ett konfigurerat alternativ används ett giltigt item i alfabetisk ordning.
Vid serverstart kontrolleras att taggarnas standardval faktiskt tillhör
respektive tagg och att alla angivna item finns. Varje recept kontrollerar
också att dess facitingredienser är tillåtna.

Filen läses av backend när appen startar och följer med i jar-filen.
Starta om appen efter ändringar. API och frontend behöver inga nya inställningar.
