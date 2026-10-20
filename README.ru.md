# AOP: Thaumcraft 4

[English](README.md)

Я поддерживаю неофициальный порт **Thaumcraft 4.2.3.5** на **Minecraft 1.21.1 / NeoForge**. В него входят исследования, аспекты, узлы ауры, жезлы и посохи, алхимия, инфузия, сети эссенции, големы, снаряжение и Внешние земли. Для аддонов предусмотрен публичный API.

> **Personal use only.** Thaumcraft and all of its assets belong to Azanor. This repository contains the original mod's logic and assets and is not intended for redistribution.

Источник вдохновения — [Thaumcraft 3: Reforged](https://github.com/notlikethiss/thaumcraft-3-reforged).

## Статус

Версия **1.0.0** предназначена для проверки. Автоматические сценарии покрывают правила API и часть игровых взаимодействий; ручная проверка графики и многопользовательских случаев продолжается. Скрытое хранилище Magic Box пока не реализовано.

## Установка

- Minecraft **1.21.1**, Java **21**, NeoForge **от 21.1.252 в пределах ветки 21.1**.
- Обязателен [Curios](https://www.curseforge.com/minecraft/mc-mods/curios) **от 9.5.1+1.21.1 в пределах 9.x**.
- [TerraBlender](https://www.curseforge.com/minecraft/mc-mods/terrablender) **от 4.1.0.8 в пределах 4.1** необязателен. Без него работает встроенное размещение биомов.

Поместите игровой JAR в `mods` клиента и сервера. API уже включён в него; отдельные API и sources JAR устанавливать как мод не нужно.

**AOP:TC4 Compact** — отдельный необязательный проект с рецептами JEI/EMI и подсказками Jade/WTHIT. Ядро не требует этих модов. Thaumic Tinkerer также устанавливается отдельно и требует совместимую сборку ядра.

## Начало игры

Используйте Таумономикон и исследуйте объекты таумометром. Стандартные клавиши: **F** — выбор набалдашника, **H** — ранец, **G** — режим архитектора, **Shift+G** — ось. Их можно переназначить в настройках Minecraft.

Клиентские параметры находятся в `config/thaumcraft-client.toml`, серверные — в папке мира. В Таумономиконе также есть настройки отображения. Перед обновлением тестовой сборки сделайте резервную копию мира.

## Сборка

Установите JDK 21, клонируйте репозиторий и выполните:

```powershell
./gradlew.ps1 build
```

Для Linux/macOS: `sh ./gradlew build`. При первом запуске Gradle загрузит зависимости. Версии Minecraft и NeoForge заданы в `gradle.properties`.

Результаты:

- `mod/build/libs/thaumcraft-1.21.1-1.0.0.jar` — игровой мод.
- `api/build/libs/thaumcraft-api-1.0.0.jar` — зависимость для разработки.
- `mod/build/libs/thaumcraft-1.21.1-1.0.0-sources.jar` — архив исходников.

Клиент для разработки: `./gradlew.ps1 :mod:runClient`. SDK аддонов: `./gradlew.ps1 addonSdk`. Контракты и примеры описаны в [руководстве по API](docs/addons/README.ru.md).

## Проверки и CI

`build` компилирует мод и выполняет проверки API и отдельные проверки правил. Игровые сценарии находятся в отдельных проверочных наборах и не входят в игровой JAR. Успешная сборка не заменяет проверку в игре.

GitHub workflow запускается при изменении кода и вручную. Изменения только документации его не запускают. Push в `main` создаёт nightly; ручной запуск использует `mod_version` и не перезаписывает существующий релиз. Локальный запуск через `act` ничего не публикует.

Для сообщения об ошибке приложите версии модов, порядок воспроизведения и соответствующий лог или crash report. Перед отправкой удалите из логов токены и личные данные.

## Авторы и условия

- [Azanor](https://www.curseforge.com/minecraft/mc-mods/thaumcraft) — оригинальный Thaumcraft, механики и ресурсы.
- CovertJaguar, Dan Scott, Lorc, bonemouse, Scott Killen, Rorax и chicken_bones — участники из оригинальных благодарностей.
- [Salossov](https://github.com/Salossov) — порт на Minecraft 1.21.1 с применением ИИ при реализации и проверке.
- [NeoForge](https://github.com/neoforged/NeoForge), Curios и TerraBlender — платформа и интеграции.

См. [NOTICE.md](NOTICE.md). `TEMPLATE_LICENSE.txt` относится только к сборочному шаблону и не переводит Thaumcraft или его ресурсы под MIT. Проект неофициальный.
