# Powers Unleashed

Мод про супергероев для **Minecraft 26.3**. Сейчас собирается только под **NeoForge**; модуль Fabric временно отключён (вернуть: раскомментировать `include('fabric')` в `settings.gradle`).

## Структура

```
common/    — почти весь код мода: способности, костюмы, логика. Только ванильный Minecraft, без API загрузчиков.
neoforge/  — тонкий слой под NeoForge: точка входа, регистрация, события.
fabric/    — тонкий слой под Fabric: то же самое.
build-logic/ — общие настройки сборки (трогать редко).
gradle.properties — ВСЕ версии (Minecraft, NeoForge, Fabric) и данные мода в одном месте.
```

Общий код вызывается из `PowersUnleashedNeoForge` и `PowersUnleashedFabric` через `PowersUnleashed.init()`.
Если общему коду нужно что-то от загрузчика, это делается через интерфейс в `common/.../platform/services`
и две реализации в `neoforge` и `fabric` (см. `IPlatformHelper`).

## Что нужно установить

1. **JDK 25** (например, Eclipse Temurin 25).
2. **IntelliJ IDEA 2025.3 или новее** (Community подойдёт) + плагин **Minecraft Development**.
3. **Git**.

## Первый запуск

1. `git clone https://github.com/TheKrepe/powers_unchained.git`
2. В IDEA: *File → Open* → папка проекта. Дождаться импорта Gradle (первый раз качает Minecraft — это долго).
3. *Settings → Build Tools → Gradle → Gradle JVM* → выбрать JDK 25.
4. Запуск игры с модом — в панели Gradle или в терминале:
   - `./gradlew :neoforge:runClient` — NeoForge
   - `./gradlew :fabric:runClient` — Fabric (когда будет снова включён)
   (на Windows — `gradlew.bat ...`)
5. В логе должно появиться: `Powers Unleashed is loading on NeoForge` (или `Fabric`).

## Сборка .jar

`./gradlew build` → готовые файлы:
- `neoforge/build/libs/powers_unleashed-neoforge-26.3-<версия>.jar`
- (Fabric — когда будет снова включён)

Файлы `-sources.jar` и `-javadoc.jar` в игру ставить не нужно.

Также каждый push в GitHub автоматически собирается (вкладка **Actions**), готовые jar можно скачать
из раздела *Artifacts* у нужного запуска.

## Как обновляться с GitHub

`git pull` (или *Git → Pull* в IDEA), затем перезапустить игру/сборку.

## Если что-то сломалось

Присылайте `runs/client/logs/latest.log` (или `crash-reports/...`) из папки `neoforge` или `fabric`
и скриншоты, если проблема визуальная.
