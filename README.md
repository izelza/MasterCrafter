# MasterCrafter

Källkod och utvecklingshistorik finns på [GitHub: izelza/MasterCrafter](https://github.com/izelza/MasterCrafter).

## Starta i IntelliJ

1. Läs in `pom.xml` som Maven-projekt (Reload All Maven Projects).
2. Använd JDK 24 som projektets SDK.
3. Kör `com.bursie.Main` med den gröna pilen vid `main`.
4. Öppna http://localhost:8080 i webbläsaren för att spela crafting-spelet.

Spelet har engelska föremålsnamn och engelskt gränssnitt. Sök fram och välj en ingrediens; föremålet blir själva muspekaren med klickpunkten i ikonens mitt. Högerklicka i crafting-griden för att placera den eller håll höger musknapp nere och dra för att fylla flera rutor. Vänsterklicka eller vänsterdra för att sudda, även när du håller i ett föremål. Högerknappen gör inget när handen är tom.

Klicka på **Craft!** för att låta servern kontrollera receptet. Vid fel kan du ändra griden och försöka igen. Vid rätt hämtas nästa recept direkt; bekräftelsen ligger kvar tills du börjar nästa försök. **Skip recipe** visar först ett korrekt recept i griden. Då låses griden och Craft!-knappen, och knappen byter namn till **Next recipe**. Klicka igen för att hämta ett nytt recept. Handen och griden töms vid varje nytt recept. Inventory-sökningen behålls.

**Craft!** och **Skip recipe** sitter direkt under griden. Vänsterklick eller högerklick på ett inventory-item väljer det. Med tangentbord kan du använda Enter/Space på en crafting-ruta för att placera, eller Delete/Backspace för att sudda. Listan kan sökas med engelska namn (t.ex. **oak planks**) och bläddras i; alla tillgängliga föremål går att välja. Inventory-rutorna är kvadratiska med centrerade ikoner och samma utseende som crafting-rutorna. Håll musen över en ikon för att se namnet i en separat Minecraft-liknande tooltip. Drop/Clear-knapparna är borttagna; handen töms automatiskt vid nästa recept.

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

## Docker och Render

`Dockerfile` i projektroten bygger appen med Maven Wrapper och Java 24 och startar den automatiskt med Java 24 JRE. Frontend, font, ikoner och aktiva recept paketeras i samma jar-fil. Containern kör som icke-root och behöver ingen databas eller diskvolym.

I den redan anslutna Render-tjänsten:

- Välj **Docker** som Language/runtime och repots rot som byggkontext.
- Använd **Dockerfile Path: `./Dockerfile`** och branch **`master`**.
- Låt **Docker Command** vara tomt; Dockerfilens startkommando används.
- Använd **Health Check Path: `/api/hello`** om en kontrollsökväg ska anges.

Appen lyssnar på `0.0.0.0` i containern och använder miljövariabeln `PORT` som Render anger. Containerstandard är 10000; lokal körning utan `PORT` använder fortfarande 8080. Render behöver internet under bygget för att hämta Java-bilder och Maven-beroenden. Senare körning använder de lokalt paketerade resurserna.

På en dator med Docker kan du testa med:

```powershell
docker build -t mastercrafter .
docker run --rm -p 8080:10000 mastercrafter
```

Öppna sedan http://localhost:8080. Egen port kan exempelvis provas med `docker run --rm -e PORT=9090 -p 8080:9090 mastercrafter`.

`.dockerignore` tar endast med byggindata. Lokala verktyg, `.git`, dokumentation, tidigare byggen och arkiverade mallrecept skickas inte som byggkontext. Dockerbygget cachar beroendesteget och hoppar över testkörning; kör projektets tester separat med Maven.

## Spelets API

- `GET /api/items?q=oak%20planks&page=0` söker bland ingredienser och ger `{items, total, page, pages}` med högst 48 föremål per sida. Sidnummer börjar på noll.
- `POST /api/crafting/challenges` skapar en utmaning och ger `{id, target: {id, name, iconUrl}}`.
- `POST /api/crafting/attempts` tar emot `{challengeId, grid}` där `grid` är nio item-ID:n eller `null` för tomma rutor, rad för rad. Ger `{correct, message}`. Försök kan upprepas; en ny utmaning ersätter den tidigare i samma webbläsarsession.
- `POST /api/crafting/challenges/{challengeId}/solution` avslöjar ett giltigt facit för aktuell utmaning och ger `{challengeId, grid, message}`. Griden innehåller nio fullständiga item-objekt eller `null`. Upprepad hämtning ger samma facit. Försök efter avslöjat facit avvisas med HTTP 409 tills en ny utmaning har skapats.
- Servern accepterar alla medföljande recept för målföremålet, ingrediensalternativ och taggar. Formade recept får flyttas och spegelvändas vågrätt, men inte roteras. Formlösa recept kontrolleras oberoende av rutornas placering, med rätt antal av varje ingrediens.
- Målföremål väljs enbart bland crafting-receptens resultat, inte bland alla föremål i inventoryt. Även uppdelningsrecept räknas: ett Slime Block i valfri ruta ger nio Slimeballs enligt de medföljande Minecraft-recepten.
- Smithing templates är uteslutna som crafting-mål. Deras 19 receptfiler är bevarade i `mc-resources/resources/disabled-recipes/smithing-templates/`, utanför det aktiva recepturvalet. Föremålen finns kvar i inventoryt.
- `POST /api/quiz/questions` ger `{id, iconUrl, options: [{id, name}, ...]}`.
- `POST /api/quiz/answers` tar emot `{questionId, optionId}` och ger `{correct, correctName, correctOptionId}`.
- Webbläsaren skickar automatiskt en sessionscookie så att servern vet vilken utmaning eller quizfråga spelaren har. En quizfråga kan bara besvaras en gång. En ny quizfråga ersätter den tidigare, även om flera flikar är öppna. Quiz och crafting har separata utmaningar.

Minecraft-data och båda ikonformaten läses från `mc-resources/resources` och paketeras i appen vid Maven-bygget. Kör Reload All Maven Projects efter ändringar av `pom.xml`.

Kör tester med `.\mvnw.cmd test`.

## Backendens ansvarsfördelning

- `CraftingController` tar emot HTTP-anrop, kopplar dem till rätt webbläsarsession och översätter fel till befintliga HTTP-statuskoder.
- `CraftingService` väljer målföremål, hanterar sökning/sidindelning och bedömer spelarens försök. Den arbetar med en spelmodell och använder inte HTTP-sessioner.
- `CraftingSession` är en vanlig Java-klass för en spelares aktuella utmaning, status (`NOT_STARTED`, `ACTIVE`, `SOLVED`, `REVEALED`), senaste försöksresultat och eventuellt avslöjat facit. Ett identiskt inskickat försök ger samma resultat igen så länge facit inte avslöjats. En löst eller avslöjad utmaning kan inte ändras med ett nytt försök; en ny utmaning återställer statusen.
- `ItemCatalog` och `RecipeCatalog` läser Minecraft-data. `CraftingRecipe` kontrollerar om griden matchar ett recept.

Varje webbläsarsession får en egen `CraftingSession`. Servicen är gemensam, men delar inga spelares framsteg. `CraftingServiceTest` demonstrerar testning av spellogiken utan att starta HTTP eller Spring.

## Frontend

Det grå inventory-temat använder färger uppmätta i Mojangs originaltexturer för Java 26.3 samt oförändrade knapp- och ruttexturer. Paletten ligger i `minecraft-theme.css`; källor och exakta värden finns i [docs/MINECRAFT-FARGER.md](docs/MINECRAFT-FARGER.md).

Crafting-rutornas hover-markering använder originalets två genomskinliga lager och täcker hela insidan utan att ändra skuggkanterna. Mått och pixelkontroller beskrivs i [docs/MINECRAFT-HOVER.md](docs/MINECRAFT-HOVER.md).

Båda spellägena använder Mojangs **Minecraft Seven v2** som lokal webbfont via `minecraft-font.css`. Den behöver ingen extern fonttjänst när spelet körs. Källa och kontrollsumma finns i [docs/MINECRAFT-FONT.md](docs/MINECRAFT-FONT.md).

`src/main/resources/static` innehåller vanlig HTML/CSS/JavaScript. `crafting.js` styr spelvyn och API-anrop; `inventory.js` sköter sökning, sidbyte och val; `item-tooltip.js` visar namn ovanpå menyn; `crafting-input.js` hanterar dragning, tangentbord och touch. `item-cursor.js` skapar och cachar native CSS-muspekare från item-ikoner (32×32 med pixel-skalning och centrerad klickpunkt). Markören följer musen utan att JavaScript måste flytta någon bild på sidan. Filerna laddas som JavaScript-moduler utan npm eller separat byggsteg. Facit hämtas från backend och beräknas inte i frontend.

Sökikonen, tooltipens originalfärger och läderikonerna beskrivs i [docs/MINECRAFT-INVENTORY.md](docs/MINECRAFT-INVENTORY.md). Backend renderar de fem ofärgade läderikonerna med originalens standardfärg och detaljlager. Samma ikon-URL används i inventory, grid, mål och frågespel.
