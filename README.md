# MasterCrafter

## Starta i IntelliJ

1. Läs in `pom.xml` som Maven-projekt (Reload All Maven Projects).
2. Använd JDK 24 som projektets SDK.
3. Kör `com.bursie.Main` med den gröna pilen vid `main`.
4. Öppna http://localhost:8080 i webbläsaren för att spela crafting-spelet.

Spelet har engelska föremålsnamn och engelskt gränssnitt. Sök fram en ingrediens, välj den och klicka i crafting-gridens rutor. Håll höger musknapp nere och dra över griden för att fylla flera rutor med samma ingrediens. Klicka på **Craft!** för att låta servern kontrollera receptet. Vid fel kan du ändra griden och försöka igen. Vid rätt hämtas nästa recept direkt och griden töms automatiskt; bekräftelsen på det klarade receptet ligger kvar tills du börjar nästa försök. Vald ingrediens och inventory-sökning behålls. **Skip recipe** byter ett recept du inte vill lösa.

Välj **Erase** och klicka för att tömma en ruta, eller håll höger musknapp nere och dra för att sudda flera rutor. Delete/Backspace fungerar också när en ruta har tangentbordsfokus. **Clear grid** tar bort alla ingredienser. Listan kan sökas med engelska namn (t.ex. **oak planks**) och bläddras i; alla tillgängliga föremål går att välja.

Frågespelet finns kvar via länken **Item quiz** eller http://localhost:8080/quiz.html. Stoppa servern med IntelliJs stoppknapp.

Hello-exemplet finns kvar på http://localhost:8080/api/hello och svarar `{"message":"Hello Oscar!"}`.

`Main` startar servern. `HelloController` tar emot HTTP GET-anrop och Spring omvandlar svaret till JSON.

## PowerShell

Med `JAVA_HOME` inställt till din JDK-katalog:

```powershell
.\mvnw.cmd spring-boot:run
```

Bygg med `.\mvnw.cmd package`. Starta den byggda appen med
`java -jar target/MasterCrafter-1.0-SNAPSHOT.jar`.

Maven Wrapper hämtar Maven automatiskt första gången. Internet behövs även för att hämta projektets beroenden.

## Spelets API

- `GET /api/items?q=oak%20planks&page=0` söker bland ingredienser och ger `{items, total, page, pages}` med högst 48 föremål per sida. Sidnummer börjar på noll.
- `POST /api/crafting/challenges` skapar en utmaning och ger `{id, target: {id, name, iconUrl}}`.
- `POST /api/crafting/attempts` tar emot `{challengeId, grid}` där `grid` är nio item-ID:n eller `null` för tomma rutor, rad för rad. Ger `{correct, message}`. Försök kan upprepas; en ny utmaning ersätter den tidigare i samma webbläsarsession.
- Servern accepterar alla medföljande recept för målföremålet, ingrediensalternativ och taggar. Formade recept får flyttas och spegelvändas vågrätt, men inte roteras. Formlösa recept kontrolleras oberoende av rutornas placering, med rätt antal av varje ingrediens.
- `POST /api/quiz/questions` ger `{id, iconUrl, options: [{id, name}, ...]}`.
- `POST /api/quiz/answers` tar emot `{questionId, optionId}` och ger `{correct, correctName, correctOptionId}`.
- Webbläsaren skickar automatiskt en sessionscookie så att servern vet vilken utmaning eller quizfråga spelaren har. En quizfråga kan bara besvaras en gång. En ny quizfråga ersätter den tidigare, även om flera flikar är öppna. Quiz och crafting har separata utmaningar.

Minecraft-data och båda ikonformaten läses från `mc-resources/resources` och paketeras i appen vid Maven-bygget. Kör Reload All Maven Projects efter ändringar av `pom.xml`.

Kör tester med `.\mvnw.cmd test`.
