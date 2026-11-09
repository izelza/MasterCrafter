
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
