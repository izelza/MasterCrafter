# MasterCrafter

## Starta i IntelliJ

1. Läs in `pom.xml` som Maven-projekt (Reload All Maven Projects).
2. Använd JDK 24 som projektets SDK.
3. Kör `com.bursie.Main` med den gröna pilen vid `main`.
4. Öppna http://localhost:8080 i webbläsaren för att spela Minecraft-frågespelet.

Välj vilket av två svenska namn som hör till bilden. Servern kontrollerar svaret, visar facit och låter dig gå vidare till nästa fråga. Stoppa servern med IntelliJs stoppknapp.

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

- `POST /api/quiz/questions` ger `{id, iconUrl, options: [{id, name}, ...]}`.
- `POST /api/quiz/answers` tar emot `{questionId, optionId}` och ger `{correct, correctName, correctOptionId}`.
- Webbläsaren skickar automatiskt en sessionscookie så att servern vet vilken fråga spelaren har. En fråga kan bara besvaras en gång. En ny fråga ersätter den tidigare, även om flera flikar är öppna.

Minecraft-data och båda ikonformaten läses från `mc-resources/resources` och paketeras i appen vid Maven-bygget. Kör Reload All Maven Projects efter ändringar av `pom.xml`.

Kör tester med `.\mvnw.cmd test`.
