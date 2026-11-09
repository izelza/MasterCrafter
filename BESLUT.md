# Beslut

- 2026-11-09: Java 24 (redan installerat), Spring Boot 3.5.7 och Maven Wrapper. Enkel lokal körning utan databas eller Docker.
- 2026-11-09: Första tekniska steget är `GET /api/hello`, som returnerar JSON. Därefter prioriteras det minimala Minecraft-frågespelet enligt AGENTS.md.
- 2026-11-09: Oscar väljer engelska itemnamn och engelskt gränssnitt i båda spellägena (ersätter det ursprungliga valet svenska). Namn och ikoner kommer från mc-resources och paketeras med appen via Maven.
- 2026-11-09: Frontend är vanlig HTML/CSS/JavaScript som serveras av Spring Boot. `POST /api/quiz/questions` skapar en fråga, `POST /api/quiz/answers` kontrollerar svaret. Aktuell fråga sparas i webbläsarens serversession; inga poäng eller tidsgränser ännu.
- 2026-11-09: Oscar har testat frågespelet och vill gå vidare till crafting. Han godkänner att välja en ingrediens och sedan klicka i 3×3-griden. Alla föremål är sökbara och bläddringsbara; suddverktyg och tömning stöds.
- 2026-11-09: Crafting är startsidan; frågespelet finns kvar på /quiz.html. Backend kontrollerar alla medföljande receptvarianter, taggar, förflyttningar och vågräta speglingar. Felaktiga försök kan rättas och skickas igen; recept kan hoppas över. Inga poäng eller tidsgränser.
- 2026-11-09: Oscars senaste inventory-val: vald ikon följer muspekaren. Högerklick/högerdrag placerar, vänsterklick/vänsterdrag suddar oberoende av valt föremål. Högerknappen gör inget med tom hand. Drop item släpper valet; Enter/Space placerar med tangentbord, Delete suddar.
- 2026-11-09: Rätt svar ger omedelbart nästa recept med bekräftelsen kvar. Handen och griden töms vid varje nytt recept, även Skip recipe; inventory-sökningen behålls. Detta ersätter det tidigare beteendet att behålla vald ingrediens.
