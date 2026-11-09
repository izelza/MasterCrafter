# Beslut

- 2026-11-09: Java 24 (redan installerat), Spring Boot 3.5.7 och Maven Wrapper. Enkel lokal körning utan databas eller Docker.
- 2026-11-09: Första tekniska steget är `GET /api/hello`, som returnerar JSON. Därefter prioriteras det minimala Minecraft-frågespelet enligt AGENTS.md.
- 2026-11-09: Oscar väljer svenska itemnamn. Frågespelet använder namn och ikoner från mc-resources, som paketeras med appen via Maven.
- 2026-11-09: Frontend är vanlig HTML/CSS/JavaScript som serveras av Spring Boot. `POST /api/quiz/questions` skapar en fråga, `POST /api/quiz/answers` kontrollerar svaret. Aktuell fråga sparas i webbläsarens serversession; inga poäng eller tidsgränser ännu.
