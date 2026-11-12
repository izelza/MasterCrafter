# Snabbstart inför presentationen

Den färdiga versionen med The Stack körs lokalt på **http://localhost:8080**.
Ladda om sidan med Ctrl+F5 efter uppdateringen.

Start The Stack startar 64 mål och klockan. Stop / Reset nollställer
omgången och återgår till vanlig crafting. Show checklist visar kön och
kryssar av lösta mål. Skip används i vanlig crafting; under The Stack
behöver varje mål lösas eller omgången stoppas.

## Snabb reserv om något skulle krångla

Den fungerande versionen före The Stack finns redan byggd på Oscars laptop:

```powershell
& .\.tools\presentation-backup\start-backup.cmd
```

Öppna sedan **http://localhost:8082**. Detta kör den gamla versionen i en
egen process och ändrar inga projektfiler eller Git-commits. Stäng den
med Ctrl+C när den inte behövs längre. Skriptet kan också dubbelklickas
i Utforskaren: `.tools/presentation-backup/start-backup.cmd`.

Jar-filen heter `.tools/presentation-backup/MasterCrafter-stable.jar`.
Den är byggd från commit **80ff4ef**, Git-märkt som
**before-the-stack-2026-11-12**. Märkningen är också pushad till GitHub.
Originalkoden är dessutom arkiverad i `.tools/presentation-backup/before-the-stack.zip`.
De lokala backupfilerna ligger i `.tools`, som inte versionshanteras.

## Om appen ska startas från IntelliJ igen

Codex har startat den nya versionen som en bakgrundsprocess på port 8080.
Stoppa den först med:

```powershell
& .\.tools\runtime\stop-server.cmd
```

Starta därefter `com.bursie.Main` som vanligt i IntelliJ.

## Kontrollerat

- Alla 34 backendtester passerar, inklusive en komplett Stack via HTTP.
- En hel omgång med 64 mål är löst genom frontend och checklistan avprickad.
- Tiden fortsätter efter omladdning, fryser vid avslut och nollställs vid Stop.
- Tappade nätverkssvar har testats utan dubblerat framsteg eller överhoppade mål.
- Reserv-jar har startats separat och dess vanliga crafting-API har kontrollerats.
