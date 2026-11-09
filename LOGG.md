
## 2026-11-09 – Initial Spring Boot-server
- Magnus bad om ett minimalt Hello world-API inför arbetet med Oscar.
- Ersatte IntelliJs Java-exempel med Spring Boot 3.5.7 och en separat HelloController. GET /api/hello returnerar JSON med message.
- Lade till Maven Wrapper, startinstruktioner i README.md och initiala BESLUT.md.
- Byggde körbar jar med Java 24 och verifierade HTTP 200 samt exakt svar: {"message":"Hello world!"}.
- Testservern stoppades efter kontrollen så att port 8080 är ledig för IntelliJ.
- Git kunde inte hittas i PATH eller vanliga installationskataloger; ingen commit gjordes. Git behöver bli tillgängligt innan första snapshot kan sparas.
- Nästa steg: låt Oscar starta Main i IntelliJ och prova API:t, därefter det minimala Minecraft-frågespelet.

## 2026-11-09 – Personlig hälsning
- Oscar bad att hälsningen skulle ändras till Hello Oscar! Uppdaterade API-svaret och exemplet i README.md.

## 2026-11-09 – Första spelbara Minecraft-frågespelet
- Oscar bekräftade att Hello Oscar fungerade och ville börja med frågespelet. Han valde svenska itemnamn.
- Undersökte resursbeskrivningen och JSON-formatet. Spelet använder alla 1 038 föremål med svenska namn och de tillhandahållna 16×16- och 64×64-ikonerna.
- Lade till ItemCatalog, två quiz-endpoints och en grön Minecraft-inspirerad webbsida med två slumpade svarsalternativ, rätt/fel-återkoppling, facit och nästa fråga.
- Servern sparar aktuell fråga per webbläsarsession, kontrollerar svar och stoppar återanvändning av gamla frågor. Alternativen har olika namn även om två föremål skulle dela ett översatt namn.
- Förklarade hur webbsidan ber backend om frågor och skickar tillbaka ett valt alternativ via API:t.
- Paketerade data och ikoner via Maven så att även en körbar jar innehåller alla nödvändiga resurser.
- Fem integrationstester passerar: frågor/ikoner/webbsida, rätt svar och dubbelsvar, fel svar med facit, separata sessioner samt ogiltiga och ersatta frågor. Justerade testet för startsidan eftersom MockMvc visar dess vidarebefordran till index.html utan att följa den automatiskt.
- Byggde jar och provade den på separat testport 8081 i headless Edge: laddad ikon, rätt svar, nästa fråga, fel svar, mobilvy utan horisontell scroll och inga JavaScript-fel. Skärmbilder granskades visuellt.
- Hämtade portabelt Git till den ignorerade katalogen .tools/git och initierade projektets Git-repository. Ingen global installation eller konfigurationsändring gjordes.
- Nästa steg: Oscar laddar om Maven-projektet, startar om Main och provar spelet på http://localhost:8080. Låt återkopplingen styra nästa steg.

## 2026-11-09 – Första crafting-spelet
- Oscar testade frågespelet utan uppenbara fel och ville gå vidare till crafting. Han godkände upplägget att välja ingrediens och klicka i griden.
- Byggde en 3×3-grid med sökning bland samtliga 1 038 föremål, sidindelning, markerad ingrediens, suddverktyg, högerklick, Delete/Backspace och tömning av hela griden.
- Läste in 1 026 riktiga recept med 883 olika målföremål. Kontrollen hanterar taggar, alternativa ingredienser, alla recept för samma resultat, förflyttning, vågrät spegling och formlösa recept med upprepade ingredienser. Överlappande taggar hanteras genom att prova möjliga ingrediensfördelningar.
- Lade till GET /api/items samt POST /api/crafting/challenges och /api/crafting/attempts. Aktuell utmaning sparas per session. Fel kan rättas utan att receptet byts; efter rätt svar låser webbsidan griden tills nästa recept. En upprepad HTTP-sändning fungerar också om nätverkssvaret skulle försvinna.
- Crafting ligger på startsidan. Frågespelet finns kvar på /quiz.html och båda nås via navigationen.
- Tolv automatiska tester passerar, inklusive tidigare quiztester. Crafting-testet provar samtliga riktiga recept och deras tillåtna förflyttningar/speglingar, fel/missade ingredienser, taggar, alternativa recept, sessioner, ogiltiga försök och bläddring genom hela itemlistan.
- Testade den byggda jar-filen på separat port 8081 i headless Edge: bläddring, sökning, placering, högerklick/sudda/töm, felaktigt försök, korrekt crafting och nästa utmaning. Granskade desktop- och mobilskärmbilder och kontrollerade att mobilvyn inte får horisontell scroll. Även frågespelets webbläsartest passerar.
- Uppdaterade README.md och BESLUT.md. Sparar fungerande milstolpe i Git.
- Nästa steg: Oscar laddar om Maven, startar om Main och provar crafting på http://localhost:8080. Utvärdera framför allt hur enkelt det är att söka och placera ingredienser innan fler funktioner väljs.

## 2026-11-09 – Engelskt gränssnitt och engelska föremålsnamn
- Oscar tyckte att crafting-prototypen var bra och ville börja finslipningen med att byta hela hemsidan till engelska.
- Bytte till Minecrafts engelska språkfil en_us.json för alla föremål. Inventory, målföremål, sökning, sortering och quizalternativ använder nu engelska namn.
- Översatte båda webbsidorna, knappar, instruktioner, platshållare, sidindelning, återkoppling, felmeddelanden, verktygstips och tillgänglighetstexter. Sidorna har lang=en och föremålsantal formateras med engelsk lokalinställning.
- Uppdaterade det tidigare språkbeslutet och README.md enligt Oscars nya önskemål. Projektdokumentationen fortsätter vara på svenska.
- Rent Maven-bygge och alla tolv automatiska tester passerar. Testet kontrollerar sökning efter OAK PLANKS och att samtliga 1 038 namn motsvarar den engelska resursfilen.
- Båda webbläsartesterna passerar för den byggda jar-filen: rätt/fel, fortsatt spel, engelskt målnamn och Craft!-knapp, inventory, placering/suddning samt mobilvy. Uppdaterade testskriptens gamla svenska förväntningar till engelska.
- Nästa steg: ladda om Maven-projektet och starta om Main; uppdatera webbläsaren för att prova engelska versionen.

## 2026-11-09 – Högerdrag och automatiskt nästa recept
- Oscar bekräftade att engelska versionen fungerade och ville förbättra känslan av inventory och placering.
- Lade till placering genom att hålla höger musknapp nere och dra över griden. Samma ingrediens används under hela dragningen och varje passerad ruta fylls. Även snabba rörelser mellan mus-events fångas genom att provta sträckan, inklusive sista rörelsen vid släpp.
- Med Erase valt kan samma rörelse användas för att sudda. Högerklick med vald ingrediens placerar nu föremålet i stället för att ta bort det. Vanligt vänsterklick och Delete/Backspace finns kvar.
- Pointer capture håller ihop dragningen även utanför griden. Släpp, avbruten pointer, förlorad capture, fönsterfokusförlust, nytt recept och inskickat försök avslutar dragningen. Ikoner går inte att dra som vanliga webbläsarbilder.
- Rätt försök hämtar nästa recept direkt, utan timer eller extra klick. Griden töms men vald ingrediens och sökning behålls. Bekräftelsen på föregående recept ligger kvar vid det nya tills spelaren börjar ändra eller skicka nästa försök. Felaktiga försök stannar på samma recept.
- Maven-bygget och alla tolv backend/integrationstester passerar. Webbläsartest med riktiga högerknapps-events verifierade snabb dragning över mellanliggande rutor, släpp utanför griden, fokusförlust, suddragning, felaktigt försök, korrekt crafting med automatisk fortsättning, behållen ingrediens och mobilvy utan horisontell scroll eller JS-fel.
- Uppdaterade instruktioner och beslut. Nästa steg är att starta om Main, uppdatera webbsidan och låta Oscar prova känslan med flera olika recept.

## 2026-11-09 – Föremål vid muspekaren och fasta musknappar
- Oscar bekräftade att högerdrag och automatisk fortsättning fungerade. Han ville att föremålets ikon skulle följa muspekaren, att vänsterknappen alltid skulle sudda, högerknappen alltid placera och handen tömmas vid nästa recept.
- Lade till en pixel-skalad ikon intill muspekaren. Den kan inte fånga klick, hålls inom fönstrets kanter och döljs när handen är tom eller pekaren/fokus lämnar fönstret.
- Högerklick/högerdrag placerar valt föremål. Vänsterklick/vänsterdrag suddar utan att ändra vad spelaren håller i. Högerknappen med tom hand ändrar inga rutor. Båda dragningarna hanterar snabba rörelser och avslutas vid släpp eller fokusförlust.
- Bytte tidigare Erase-knappen mot Drop item, eftersom suddning nu alltid ligger på vänsterknappen. Bevarade tangentbordsplacering med Enter/Space och suddning med Delete/Backspace; touch kan fortfarande placera genom att trycka i en ruta.
- Varje ny utmaning tömmer nu både handen och griden, även vid Skip recipe och automatisk fortsättning efter rätt svar. Inventory-sökningen behålls.
- Alla tolv automatiska tester passerar. Webbläsartest med riktiga mus-events verifierade ikonens följning och klickgenomsläpp, inga överflöden vid fönsterkant, vänster/höger-klick och dragning med valt föremål, vänstersuddning med tom hand, oförändrad grid vid högerdrag utan valt föremål, släpp utanför griden, fokusförlust, fel/rätt försök och tömd hand vid både automatisk fortsättning och Skip recipe. Desktop-skärmbild granskades.
- Vid sista paketeringen var testserverns jar-fil låst av Windows. Stoppade testservern och byggde om paketet utan att köra om redan godkända tester; bygget lyckades.
- Uppdaterade BESLUT.md och README.md enligt de nya kontrollerna. Nästa steg: starta om Main och låt Oscar testa känslan med ikonen och båda musknapparna.

## 2026-11-09 – Centrerad ikon och kontroll av målurval
- Oscar rapporterade att vald ikon låg för långt från muspekaren och att Slimeball verkade vara ett omöjligt målföremål.
- Hittade den avsiktliga förskjutningen i cursor-koden: ikonens övre vänstra hörn låg 12 pixlar nedåt/höger, vilket gav cirka 28 pixlars avstånd till ikonens mitt. Tog bort förskjutningen och centrerade ikonen direkt vid pekarens koordinater. Ett fast, klickgenomsläppligt lager klipper ikonen vid fönsterkanten utan att flytta den eller skapa horisontell scroll.
- Granskade CraftingController och RecipeCatalog: slumpningen använder redan enbart receptresultat, inte hela inventoryt. Slimeball har ett giltigt medföljande crafting_shapeless-recept: ett minecraft:slime_block ger nio minecraft:slime_ball. Informerade Oscar om detta.
- Frågade om uppdelningsrecept ska ingå eller om föremål som bara fås genom uppdelning av block ska uteslutas. Inget svar hade kommit när ändringen sparades; nuvarande urval med alla giltiga crafting-recept behålls tills Oscar väljer.
- Lade till regressionstest som kontrollerar att varje möjligt mål har ett accepterat recept, att ett föremål utan crafting-recept (Apple) inte är ett mål, och att Slime Block i en valfri ruta godkänns som Slimeball-recept.
- Alla tretton automatiska tester passerar. Webbläsartest verifierade ikonens exakta mitt vid pekaren, även efter rullning, klickgenomsläpp, fasta musknappar, automatisk fortsättning och tom hand efter nästa recept. Även mobilvyn och kontrollen av horisontell scroll passerar.
- En start av testservern fastnade i automatisk tillståndskontroll och nådde dess tidsgräns. Ett nytt startförsök fungerade och kontrollerna kunde slutföras. Testservern stoppades efteråt.
- Uppdaterade BESLUT.md och README.md med den verifierade skillnaden mellan inventory och målföremål samt exemplet med Slimeballs.

## 2026-11-09 – Tunn controller och tydlig spelmodell
- Magnus bad om genomförande av förslag 1 (tunn controller/service) och 4 (tydlig spelarstatus). Han avböjde förslag 2 (nästa utmaning i samma API-svar) och 3 (nya DTO/felkontrakt/Bean Validation).
- Flyttade målval, inventory-sökning/sidindelning, bedömning och hantering av spelstatus till CraftingService. Servicen använder inte HttpSession eller HTTP-statuskoder.
- CraftingController ansvarar nu för HTTP-anrop, befintliga felstatuskoder och att hämta/skapa spelarens CraftingSession i webbläsarsessionen.
- Lade till CraftingSession som vanlig Java-modell med aktuell utmaning, NOT_STARTED/ACTIVE/SOLVED, en skyddad kopia av senaste griden och senaste resultatet. Separata webbläsarsessioner har egna spelmodeller.
- Identiska inskickningar ger samma sparade resultat igen. Felaktiga försök lämnar utmaningen aktiv. Efter rätt svar är utmaningen löst; ett annat försök kan inte ändra den och ger befintlig 409-status. Ny utmaning återställer modellens försöksstatus. Service-operationer låses per spelmodell vid samtidiga anrop.
- Endpoints, JSON-format och frontend är oförändrade. Ingen ny infrastruktur eller nya beroenden infördes. Automatiskt nästa recept ligger kvar i frontend enligt Magnus avgränsning.
- Lade till tre rena servicetester utan Spring eller HTTP: livscykel/upprepningar, kopiering av grid och återställning samt separata spelares framsteg. Utökade HTTP-testet för att säkerställa att ändrade försök efter lösning avvisas.
- Alla sexton automatiska tester och Maven-paketering passerar. Den byggda jar-filen provades i webbläsare på port 8081 med inventory, musknappar/dragning, cursorikon, fel/rätt försök, automatisk fortsättning, tom hand inför nästa recept och mobilvy. Testservern stoppades efteråt.
- README.md beskriver ansvarsfördelningen för pedagogisk genomgång med Oscar. BESLUT.md dokumenterar Magnus avgränsning. Redo för fortsatt arbete med Oscar efter omstart av Main.

## 2026-11-09 – Frontendens hanterbarhet
- Magnus bad att frontend också ska delas upp i fler filer när det behövs för begriplighet och underhåll. Noterade detta som en löpande utvecklingsprincip i BESLUT.md. Ingen kodändring gjordes i detta steg.

## 2026-11-09 – Skip visar facit före nästa recept
- Oscar bekräftade att ikonen följer muspekaren korrekt och att tidigare invändning mot vissa craftbara föremål var ett misstag. Uppdelningsrecept behålls enligt resursfilerna.
- Oscar önskade två steg för Skip recipe: första klicket visar rätt recept, andra klicket ger en ny utmaning. Craft! ska vara spärrad när facit visas.
- Lade till POST /api/crafting/challenges/{challengeId}/solution i den tunna controllern. CraftingService väljer ett befintligt recept och använder CraftingRecipe.exampleGrid för att konkretisera taggar/alternativ till nio rutor.
- CraftingSession har nu REVEALED och sparar facit för säkra upprepade hämtningar. Backend avvisar försök efter avslöjat facit, även om ett tidigare felaktigt försök finns i cachen. Ny utmaning rensar facit/status. Övriga endpoints och svar behåller sina format.
- Frontend visar facit med markerade gridrutor, spärrad grid/Craft!/Clear grid och knappen Next recipe. Ett andra klick byter utmaning och återställer griden samt handen. Ett misslyckat reveal-anrop kan försöka hämtas igen utan att oavsiktligt låsa upp crafting.
- Delade frontend enligt Magnus princip: crafting-input.js ansvarar för pekarikon och mus/tangentbord/touch; crafting.js ansvarar för spel/inventory/API. Använder inbyggda JavaScript-moduler utan nya verktyg eller beroenden.
- Nitton automatiska tester passerar. Alla 1 026 recept kontrolleras även med det konkreta facit som genereras. Nya service- och HTTP-tester verifierar giltigt facit, upprepning, sessioner, spärrade försök, gammal/löst utmaning och återställning.
- Webbläsartest av den paketerade jar-filen kontrollerade hela befintliga flödet samt tvåklicksskip: samma target vid facit, synliga ingredienser, spärrad Craft!-knapp även vid syntetiskt klick, låst grid vid vänster/högerklick och upplåst ny utmaning vid nästa klick. Även muspekare, dragning, normal automatisk fortsättning och mobilvy passerar. Skärmbild av facit granskades.
- Testservern stoppades. README.md och BESLUT.md uppdaterades. Nästa steg: Oscar startar om Main och provar Skip recipe följt av Next recipe.

## 2026-11-09 – Smithing templates arkiverade
- Oscar önskade att alla smithing template-recept skulle inaktiveras men bevaras, och att Slimeball skulle vara craftbart.
- Identifierade 19 recept vars resultat är smithing templates och flyttade dem från aktiva recipes till mc-resources/resources/disabled-recipes/smithing-templates. Verifierade SHA-256 före och efter varje flytt; JSON-innehållet är oförändrat.
- Lade till en regel i RecipeCatalog som ignorerar mallrecept utifrån resultat-ID, så gamla IDE-/Maven-kopior eller återimporterade filer inte återaktiverar dem som mål. Inventory och quiz behåller föremålens namn och ikoner.
- Slimeball var redan aktivt och har aldrig tagits bort. Förstärkte regressionstestet så att Slimeball uttryckligen måste finnas bland målen och ett Slime Block godkänns i griden.
- Nytt test kontrollerar att samtliga smithing template-föremål saknar aktiva recept och inte finns bland målen, men fortfarande går att söka fram i inventoryt.
- PowerShells JSON-läsare hade problem med ett annat Minecraft-recept som använder nycklarna I och i. Verifierade därefter samtliga kvarvarande och arkiverade filer med Nodes JSON-läsare, som skiljer på versaler och gemener. Alla 19 mallrecept arkiverades korrekt.
- Rent Maven-bygge och alla tjugo tester passerar. Kontrollerade även den färdiga jar-filen: 1 007 aktiva recept, inga mallrecept och Slimeball kvar. Det aktiva målurvalet innehåller 864 föremål.
- Dokumenterade avgränsningen i BESLUT.md, README.md och resurskatalogens beskrivningar. Nästa steg: starta om Main och fortsätt spela; smithing templates ska inte längre kunna bli crafting-mål.

## 2026-11-09 – Native item-muspekare utan DOM-eftersläpning
- Oscar rapporterade att itemet nu var rätt placerat men att det följde musrörelsen med märkbar fördröjning. Han ville att det skulle vara fastklistrat vid pekaren.
- Ersatte den JavaScript-flyttade DOM-bilden med en native CSS-cursor. item-cursor.js skalar den befintliga ikonen till 32×32 i en canvas med avstängd bildutjämning och använder mitten (16,16) som klickpunkt.
- Markörbilder cachas per ikon. Versionskontroll hindrar en försenad bildladdning från att återställa ett föremål efter Drop item, facit eller ny utmaning. Vanlig muspekare återställs direkt när handen töms; ogiltig bildladdning ger vanlig markör och kan försöka laddas igen senare.
- Tog bort det gamla överläggslagret och all positionsuppdatering vid musrörelse. Gridens dragkontroller ligger kvar i crafting-input.js. Backend och API är oförändrade.
- JavaScript-syntaxkontroll och Maven-paketering passerar. Ingen upprepning av backendens redan godkända tester behövdes för denna frontendändring.
- Webbläsartest av den paketerade appen passerar: native PNG-cursor över grid, inventory och sökfält, 32×32-format, centrerad klickpunkt, inga DOM-attributändringar vid vanliga musrörelser/rullning, inga horisontella överflöden, placering/suddning/dragning, rätt/fel, automatisk fortsättning, tvåklicksskip och låst facit. Kontrollerade även att fördröjd bildavkodning inte kan återställa cursor efter att handen tömts. Inga JavaScript-fel.
- Testservern stoppades och BESLUT.md/README.md uppdaterades. Oscar behöver prova den upplevda följsamheten på sin egen mus efter omstart av Main och uppdaterad webbsida; headless-skärmbilder visar inte operativsystemets muspekare.

## 2026-11-09 – Minecrafts verifierade grå inventory-tema
- Oscar önskade grått tema och bakgrund som ett Minecraft-inventory och betonade att färgerna skulle undersökas, inte gissas.
- Sökte efter GUI-texturernas placering och hämtade därefter Mojangs officiella versionsmetadata och klientarkiv för Java 26.3. Kontrollerade klientens SHA-1 mot metadata innan PNG-filerna lästes. Det stora klientarkivet ligger bara i den ignorerade .tools-katalogen.
- Läste faktiska RGBA-pixlar i inventory.png, crafting_table.png, slot.png samt knapptexturerna med System.Drawing. Verifierade panel #C6C6C6, rutor #8B8B8B, mörk inset-kant #373737, yttre skuggkant #555555, ljus kant #FFFFFF och kontur #000000. Knapptexturernas dominerande gråtoner är #6F6F6F, #757575 och #2C2C2C.
- Samlade färger och texturadresser i minecraft-theme.css. Kopierade originalens slot- och tre knapptexturer oförändrade till static/ui/minecraft och använde dem som border-image för att bevara de riktiga kanterna och knappfärgernas variationer.
- Bytte gröna bakgrunder, gradienter, accenter och återkopplingsfärger i båda spellägena till palettens gråtoner. Paneler har ljusa/mörka inventory-kanter, rutorna originalets inset-utformning och knapparna normala/markerade/avstängda originaltexturer. Yttre webbsidesbakgrunden använder inventoryts skugggrå #555555; detta är vår tillämpning av paletten, eftersom Minecraft annars visar spelvärlden bakom inventoryt.
- Sparade källänkar, SHA-1/SHA-256, bildstorlekar och provpixelkoordinater i docs/references/minecraft-26.3-gui-colors.json samt inventory.png som referens. docs/MINECRAFT-FARGER.md beskriver färgerna, deras ursprung och användning.
- Maven-paketering passerar. Webbläsartester av den byggda jar-filen passerar för crafting och quiz: sökning, mus/cursor, placering/suddning/dragning, rätt/fel, automatisk fortsättning, tvåklicksfacit, låst Craft-knapp och mobilvy utan horisontell scroll eller JS-fel.
- Kontrollerade runtime-CSS för färger och originaltexturer i normala, hover- och avstängda knappar. Mätte dessutom fem faktiska pixlar ur en skärmbild: sida #555555, panel #C6C6C6, rutans insida #8B8B8B, mörk kant #373737 och vit kant #FFFFFF. Alla matchade källpaletten exakt. Desktop- och mobilbilder granskades visuellt.
- Testservern stoppades. BESLUT.md och README.md uppdaterades. Nästa steg: Oscar startar om Main och uppdaterar webbsidan för att prova gråtemat.

## 2026-11-09 – Push efter varje commit
- Magnus bekräftade att GitHub-push fungerar och instruerade att varje framtida commit ska pushas till origin. Regeln finns nu i BESLUT.md för kommande arbetspass.
