# The Stack

En omgång innehåller 64 unika target items i slumpad ordning:

| Lägsta möjliga svårighetsgrad för target | Antal |
|---|---:|
| 1 | 19 slumpade |
| 2 | 20 slumpade |
| 3 | 20 slumpade |
| 4 | Alla 4 |
| 5 | Det enda: Rabbit Stew |

Varje target räknas en gång på sin lägsta nivå, enligt Oscars senaste
klassificering. Rabbit Stews två recept är samma mål och båda godkänns.
Även övriga targets får lösas med vilket giltigt recept som helst.

Start ersätter det aktuella receptet med första målet och startar tiden.
Ett rätt svar kryssar av målet exakt en gång; därefter kommer nästa mål
från kön. Ett felaktigt svar kan rättas utan att målet byts.
Skip är spärrad under omgången. Stop / Reset kan alltid återföra spelet
till vanlig crafting efter att en pågående serverförfrågan avslutats.

Panelen ovanför inventoryt visar tiden, antal kvar och en framstegsmätare.
Show checklist fäller ut alla 64 mål med nuvarande och avklarade mål.
Vid sista lösningen fryser tiden och behålls tills Stop eller en ny Start.
Avslutningen firas med tre större pixelburstar, 72 partiklar, cirka 2,2 sekunder.
Minskad rörelse respekteras och effekten blockerar inga klick.

Stop tar bort omgången, nollställer timern och väljer ett vanligt recept.
Start under en redan pågående omgång behåller omgången, vilket gör omförsök
säkra. Start efter en färdig omgång skapar en ny kö och ny tid.

## Backend och API

- `StackPlanner` grupperar unika targets efter lägsta receptnivå, samplar utan dubletter och blandar kön.
- `StackRun` håller ordning, avklarat antal och en monoton serverklocka baserad på `System.nanoTime()`.
- `CraftingSession` innehåller spelarens omgång. `CraftingService` synkroniserar tillståndsändringar och validerar svar.
- Kö och tid ligger i HTTP-sessionen. Omladdning av sidan återupptar samma mål, kö och tid. En serveromstart avslutar sessionerna.

| Anrop | Resultat |
|---|---|
| `GET /api/crafting/game` | Aktuell utmaning, status, eventuellt facit och Stack-status; svaret får inte cachas |
| `POST /api/crafting/stack/start` | Starta omgång eller återge redan pågående omgång |
| `POST /api/crafting/stack/stop` | Återgå till vanlig crafting och nollställ Stack |
| `POST /api/crafting/attempts` | Befintligt API; rätt svar kryssar även av aktuellt Stack-mål |
| `POST /api/crafting/challenges` | Befintligt API; ger nästa könmål efter rätt svar, annars samma pågående Stack-mål |

Befintliga JSON-format för utmaningar, försök och facit behålls. De nya
anropen återger en `GameSnapshot` med `challenge`, `status`, `solution` och
`stack`. Stack innehåller `id`, `status`, `total`, `completed`, `remaining`,
`elapsedMillis` och `entries` med targets och avklarat/current-markeringar.
Backend tar inga köer, tidvärden eller framsteg från klienten.

## Frontend och återhämtning

`crafting.js` samordnar spelets vy. `stack.js` visar serverns checklista
och interpolerar endast tidsvisningen med `performance.now()` mellan svaren.
`game-api.js` hanterar HTTP och timeout. Vid tappat svar läses den aktuella
serverstatusen tillbaka, så att samma lösning inte räknas två gånger eller
ett mål hoppas över. Vid avbrott finns Retry connection. Den aktuella
gissningen behålls om samma mål fortfarande gäller.

Testat med alla 64 mål genom frontend, rätt/fel svar, omladdning mitt i
omgången och efter avslut, fryst sluttid, reset, omstart, gamla Skip-flödet,
responsiva knappar, samt tappade svar för Start/Craft/Next/Stop och sista målet.
Reservversion och snabbstart beskrivs i [PRESENTATION.md](../PRESENTATION.md).
