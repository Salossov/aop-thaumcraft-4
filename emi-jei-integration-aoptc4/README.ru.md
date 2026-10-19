# Thaumcraft Recipe Addon (JEI/EMI)

[English](README.md)

Необязательная клиентская интеграция AOP: Thaumcraft 4. Модуль исходников — `emi-jei-integration-aoptc4`; игровой mod ID сохраняется как `thaumcraft_viewers`, имя JAR — `thaumcraft-viewers-0.7.3.jar`.

Ядро работает без JAR этого модуля, JEI и EMI. Для интеграции установите ядро с его обязательными зависимостями, этот JAR и поддерживаемый просмотрщик. JEI/EMI внутрь JAR не включены. Если установлены оба, используется родной адаптер EMI.

| Компонент | Поддерживаемая версия |
| --- | --- |
| Minecraft / NeoForge | 1.21.1 / 21.1.252+ в ветке 21.1 |
| AOP: Thaumcraft 4 | 0.7.1+; входит в проверку версии 1.0.0 |
| JEI | `[19.8.4.110,20)` |
| EMI | `[1.1.13,1.2)` |

Команды из корня репозитория, в PowerShell:

```powershell
.\gradlew.ps1 :emi-jei-integration-aoptc4:build
.\gradlew.ps1 :emi-jei-integration-aoptc4:check -PviewerCompatibility=minimum
.\gradlew.ps1 :emi-jei-integration-aoptc4:check -PviewerCompatibility=middle
.\gradlew.ps1 :emi-jei-integration-aoptc4:check -PviewerCompatibility=latest
```

Результат: `emi-jei-integration-aoptc4/build/libs/thaumcraft-viewers-0.7.3.jar`. Версия аддона независима от версии ядра.

Общий каталог и отображение находятся в `src/main/java/thaumcraft/viewers/`, изолированные адаптеры — в `jei/` и `emi/`. Общая часть использует публичный API Thaumcraft. Метаданные и регистрация плагина EMI — в `src/main/resources/`. Конфиг — `thaumcraft_viewers-client.toml`.

[Документация API аддонов](../docs/addons/README.ru.md) описывает отображения рецептов, открытие аспектов, переходы к исследованиям и серверный перенос в магическую сетку без потерь. Оригинальный Thaumcraft принадлежит Azanor; см. [NOTICE](../NOTICE.md). Перенос модуля не меняет условия использования проекта.
