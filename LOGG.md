
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

## 2026-11-09 – Nytt praktiskt push-test
- Magnus bad om en liten README-ändring och en ny commit/push för att kontrollera terminalens Git-inloggning. Lade till en länk till projektets GitHub-repository och testar push med den lokala Git Credential Manager-inloggningen.

## 2026-11-09 – Minecraft-font i gränssnittet
- Oscar gillade inventory-färgerna och önskade Minecraft-font om det var rimligt att ordna.
- Kontrollerade den lokala Java-klienten: dess standardfont refererar till bitmap-resurser. Hittade därefter Mojangs färdiga Minecraft Seven v2-webbfont och verifierade att Mojangs egen CSS registrerar den som MinecraftSeven-v2.
- Sparade den oförändrade WOFF2-filen lokalt (14 060 byte). Registrerade den i minecraft-font.css, lade till font-preload och aktiverade den i crafting och quiz, inklusive knappar, sökfält och föremålsetiketter. Inga nya ramverk eller beroenden infördes.
- Tog bort negativ bokstavsspärning från rubrik/logotyp och stängde av syntetisk fetstil för att hålla pixelbokstäverna tydliga. Ökade de minsta inventoryetiketterna till 12 CSS-pixlar.
- Maven-paketering och båda webbläsartesterna passerar. Kontrollerade via webbläsarens faktiska fontval att engelsk text, siffror och ÅÄÖåäö renderas med Minecraft Seven v2 som custom font utan reservfont. Muskontroller, native cursor, facit, automatisk fortsättning, quiz och mobilvy fungerar fortsatt; inga JavaScript-fel eller horisontella överflöden.
- Granskade desktop- och mobilskärmbilder. Testservern stoppades. Fontkälla och SHA-256 finns i docs/MINECRAFT-FONT.md; README.md och BESLUT.md är uppdaterade. Ändringen committas och pushas enligt Magnus regel.

## 2026-11-09 – Minecraft-lik hover i crafting-griden
- Oscar tyckte att den vita hover-ramen inte liknade Minecraft och misstänkte att bredden jämfört med rutans skuggkanter var orsaken. Han bad att originalets utseende skulle undersökas innan ändring.
- Sökte i Mojangs officiella releaseinformation och granskade de två riktiga slot_highlight_back/front-texturerna med metadata i den tidigare kontrollsummeverifierade Java 26.3-klienten. Originalet använder ljusning av hela insidan, inte en tunn vit ring.
- Båda PNG-filerna är 24×24 med en transparent marginal på 4 pixlar och en vit mitt på 16×16. Baklagrets alfa är 96/255 och framlagrets 32/255. Metadata anger nine_slice med kantbredd 4.
- Verifierade placeringen mot AbstractContainerScreens highlight-metoder: båda ritar 24×24 vid slotens föremålsposition minus fyra. Lokal javap 24 kunde inte läsa Java 25-klassversionen; använde en separat analyskopia med justerat versionsfält för statisk disassembly. Originalklienten förblev oförändrad och ingen spelkod kördes.
- Kopierade de två originalbilderna oförändrade till static/ui/minecraft. Med vår 4x GUI-skala blir lagren 96×96, med 16 transparenta pixlar runt en 64×64-insida. Fyrpixlars slot-kanter behålls. CSS-pseudoelement lägger baklagret bakom itemet och framlagret ovanpå; båda är klickgenomsläppliga. Ingen JavaScript-logik ändrades.
- Maven-paketering passerar. Utökat webbläsartest kontrollerar originaltexturer, 96×96-storlek, 16-pixlars marginal, lagerordning, borttagen hover-outline och att markeringen försvinner när musen lämnar rutan. Hela befintliga flödet med native cursor, dragning, crafting/facit/fortsättning samt mobilvy passerar utan JS-fel.
- Granskade hover-skärmbild och mätte faktiska pixlar: insidan #C0C0C0, skuggkant #373737, ljus kant #FFFFFF och opåverkad grannruta #8B8B8B. Proverna matchar originalens alfablendning och bevarad slot-textur.
- Dokumenterade källor, kontrollsummor och geometri i docs/MINECRAFT-HOVER.md samt uppdaterade README.md, färgdokumentet och BESLUT.md. Testservern stoppades. Ändringen committas och pushas enligt projektregeln.

## 2026-11-09 – Dockerfile för Render
- Magnus har kopplat Render till GitHub-repot och bad om en Dockerfile som bygger och startar Spring Boot-tjänsten. Detta är en uttrycklig utökning från den tidigare avgränsningen till lokal körning.
- Kontrollerade Renders officiella Docker- och web service-dokumentation: Docker-runtime, Dockerfile i roten, bindning för inkommande HTTP och PORT-miljövariabeln. Verifierade också att officiella eclipse-temurin-taggarna 24-jdk och 24-jre finns för Linux/amd64.
- Lade till tvåstegs-Dockerfile. Byggsteget använder Java 24 och Maven Wrapper, cachar beroendesteget och paketerar jar med frontend/Minecraft-resurser. Körsteget innehåller Java 24 JRE och jar-filen, kör som UID/GID 10001 och använder exec-form ENTRYPOINT för direkt Java-start.
- Lade till application.properties med server.port=${PORT:8080}. Docker sätter PORT=10000 som standard och SERVER_ADDRESS=0.0.0.0; Render kan ange sin egen port. Vanlig lokal körning behåller standardport 8080.
- .dockerignore använder en lista över tillåtna byggindata och lämnar ute lokala Git-/IDE-/verktygsfiler, dokumentation, tidigare byggen och arkiverade smithing template-recept.
- README.md beskriver befintlig Render-tjänst: Docker-runtime, ./Dockerfile, repo-roten som kontext, master, tom Docker Command och valfri health check /api/hello. Lade också till lokala docker build/run-exempel.
- Docker finns inte installerat på datorn, så själva image-bygget eller Linux-containern kunde inte köras här. Verifierade Unix-wrapperns LF-radslut, officiella bildtaggar, samma Maven dependency:go-offline/package-steg lokalt och alla tjugo tester; allt passerar.
- Startade den byggda jar-filen med PORT=18081 och SERVER_ADDRESS=0.0.0.0 utan kommandoargument som skriver över porten. Verifierade HTTP via IPv4 för health, frontend, aktuell utmaning, facit, item-ikon, WOFF2-font och hover-textur. Windows rapporterade en dual-stack-listener och IPv4-anropen fungerade. Testservern stoppades efteråt.
- Ett tillfälligt testscriptsvariabelnamn krockade med PowerShells skrivskyddade HOME. Bytte till ett uppgiftsspecifikt namn och körde om kontrollen; ingen ändring av appen behövdes.
- Dockerfilen och dokumentationen committas/pushas så att den anslutna Render-tjänsten kan bygga den. Render-deployens faktiska resultat har inte verifierats från denna miljö.

## 2026-11-09 – Bort med kvarvarande vita slot-ramar
- Oscar bad att ta bort kritvita outlines när man hovrar en slot, men behålla ljusningen av själva rutan.
- Hittade kvarvarande vit hover-outline i inventoryt, den gemensamma vita fokusramen för knappar och en separat vit ram runt crafting-rutor när facit visas.
- Gridens fokusram och extra box-shadow tas nu bort. Tangentbordsfokus ljusar upp insidan med samma originaltexturer som hover. Tog även bort facitets vita slot-ramar.
- Inventoryts vita hover-outline ersattes med originalens bak-/framlager, beskurna till den rektangulära insidan. Item och etikett ligger mellan lagren. Ingen JavaScript eller backend behövde ändras. Vald ingrediens behåller sin svarta markering.
- Maven-paketering och hela crafting-webbläsartestet passerar. Nya kontroller verifierar riktig tangentbordsfokus i både grid och inventory med outline=none, box-shadow=none och synlig ljusning, inventory-hover utan outline samt att samtliga facitrutor saknar outline. Befintlig kontroll av hovertexturer, lagrens mått, native cursor, musknappar/dragning, crafting och mobilvy fungerar fortsatt utan JS-fel eller horisontell scroll.
- Uppdaterade BESLUT.md och docs/MINECRAFT-HOVER.md. Testservern stoppades. Ändringen committas och pushas så den anslutna Render-tjänsten kan deploya den.

## 2026-11-09 15:51 – Kvadratiskt inventory
Oscar ville att inventory-rutorna skulle se likadana ut som crafting table. De delar nu CSS: 72×72 pixlar, 4-pixlars kanter, 48×48-ikoner och samma hover-lager. Namn visas i hovertext och finns kvar som tillgängliga knappnamn; kolumner anpassas utan att sträcka rutorna. Byggde jar-filen och verifierade i Edge att geometri och texturer matchar, att rutorna förblir kvadratiska utan sidscroll vid 320/390/650/900 pixlar och att sökning, val, drag, sudd, rätt/fel svar och tvåstegs-skip fungerar. Inga JavaScript-fel. Nästa steg är Oscars visuella test; välj sedan eventuella fortsatta förbättringar tillsammans.
