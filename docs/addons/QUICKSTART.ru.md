# Первый аддон

[К оглавлению](README.ru.md) | [English](QUICKSTART.en.md)

Целевая среда: Minecraft **1.21.1**, NeoForge **21.1.252**, JDK **21**, Thaumcraft **1.0.0**, API **1**. Ниже используется mod ID `myaddon`. Замените его последовательно в Java, метаданных, именах ресурсов и переводах.

## 1. Распакуйте SDK

Архив `thaumcraft-addon-sdk-1.0.0.zip` собирается в `build/distributions/` репозитория. Он содержит:

```text
lib/
  thaumcraft-api-1.0.0.jar
  thaumcraft-api-1.0.0-sources.jar
docs/
  README.ru.md
  QUICKSTART.ru.md
  CONTENT.ru.md
  API.ru.md
  NOTICE.md
example-addon/
  build.gradle
  settings.gradle
  gradlew.ps1
  gradle/wrapper/
  src/main/java/example/thaumcraft/
  src/main/resources/
```

SDK содержит исходники примера. Игровое ядро и Curios нужно получить отдельно; отдельный JAR API уже встроен в ядро. Для первой проверки соберите неизменённый пример из каталога `example-addon`:

```powershell
java -version
.\gradlew.ps1 build
```

Результат: `build/libs/thaumcraft-example-1.0.0.jar`. Первый запуск Gradle требует доступа к репозиториям зависимостей. `--offline` работает после их загрузки. Для сборки не требуется запуск Minecraft.

## 2. Подключите API

В SDK уже настроен самостоятельный проект. Существенная часть `build.gradle`:

```groovy
plugins {
    id 'java-library'
    id 'net.neoforged.moddev' version '2.0.148'
}
group = 'your.name'
version = '1.0.0'
base { archivesName = 'myaddon' }
java { toolchain.languageVersion = JavaLanguageVersion.of(21) }
neoForge { version = '21.1.252' }
dependencies { compileOnly files('../lib/thaumcraft-api-1.0.0.jar') }
tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }
```

Путь API считается от папки проекта аддона. Если переносите проект, измените путь. Подключите `sources.jar` в IDE как исходники библиотеки. Не включайте API в Shadow JAR/Jar-in-Jar и не добавляйте зависимость на внутренние классы `thaumcraft.items`, `thaumcraft.registry`, `thaumcraft.knowledge` и другие пакеты реализации. Внутри `thaumcraft.api` пакет `internal` также не предназначен для аддонов.

## 3. Создайте точку входа

В копии примера замените демонстрационные Java-классы и ресурсы своим содержимым. Минимальный `src/main/java/example/myaddon/MyAddon.java`:

```java
// File: example/myaddon/MyAddon.java
package example.myaddon;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import thaumcraft.api.addon.AddonRegistrationEvent;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.Aspects;

@Mod(MyAddon.ID)
public final class MyAddon {
    public static final String ID = "myaddon";
    public static Aspect RESONANCE;

    public MyAddon(IEventBus modBus) {
        // DeferredRegister предметов/блоков подключайте к modBus.
        AddonRegistrationEvent.BUS.addListener(MyAddon::registerThaumcraft);
    }

    private static void registerThaumcraft(AddonRegistrationEvent event) {
        RESONANCE = Aspect.registerCompound(
            ResourceLocation.parse("myaddon:resonance"), 0xdca73a,
            ResourceLocation.parse("thaumcraft:textures/aspects/auram.png"),
            "aspect.myaddon.resonance", Aspects.AURA, Aspects.CRYSTAL);
    }
}
```

Это регистрация нового составного аспекта с существующей иконкой для примера. Для собственного изображения положите PNG в `assets/myaddon/textures/aspects/resonance.png` и укажите `myaddon:textures/aspects/resonance.png`. Определение аспекта регистрируется при запуске; `/reload` его не создаёт и не удаляет.

`AddonRegistrationEvent` приходит на отдельную публичную **AddonRegistrationEvent.BUS**, активную во время common setup. Обе физические стороны должны зарегистрировать одинаковые ID. Не обращайтесь к `Minecraft`, `GuiGraphics` или другим клиентским классам из этой точки входа.

## 4. Объявите зависимости

Файл `src/main/resources/META-INF/neoforge.mods.toml`:

```toml
modLoader="javafml"
loaderVersion="[4,)"
license="All Rights Reserved"
[[mods]]
modId="myaddon"
version="1.0.0"
displayName="My Thaumcraft Addon"
[[dependencies.myaddon]]
modId="thaumcraft"
type="required"
versionRange="[1.0.0,)"
ordering="AFTER"
side="BOTH"
[[dependencies.myaddon]]
modId="neoforge"
type="required"
versionRange="[21.1.252,21.2)"
ordering="NONE"
side="BOTH"
[[dependencies.myaddon]]
modId="minecraft"
type="required"
versionRange="[1.21.1]"
ordering="NONE"
side="BOTH"
```

Укажите действительную лицензию своего аддона. Диапазон ядра здесь повторяет пример SDK: это минимальная версия, а не подтверждение проверки будущих выпусков. Для публикации ограничивайте диапазон реально поддерживаемыми версиями.

## 5. Добавьте вкладку, исследование и рецепт

В [руководстве по контенту](CONTENT.ru.md) приведён согласованный набор файлов `myaddon`: категория `workshop`, исследование `introduction`, страница `introduction_page`, рецепт `diamond`. Скопируйте эти четыре JSON и файл перевода. Они используют обычные предметы Minecraft, поэтому не требуют собственного DeferredRegister. Определение нового аспекта из шага 3 можно оставить для следующих рецептов.

Для примера `auto_unlock: true` автоматически выдаёт введение; это не способ объявить исследование с условиями изучения. Для обычного исследования оставьте `auto_unlock: false` и настройте родителей/стоимость. Подробности — в [таблице полей](CONTENT.ru.md#исследование).

## 6. Соберите и установите

В самостоятельном проекте выполните `.\gradlew.ps1 build`. Скопируйте только свой `build/libs/myaddon-1.0.0.jar` в тестовый NeoForge-экземпляр с ядром Thaumcraft и его обязательными зависимостями, включая Curios. Для контентного аддона JAR обычно нужен и клиенту, и серверу. API JAR в `mods` не кладите. JEI/EMI нужны только для проверки соответствующей интеграции, а не для работы этого примера.

После входа в тестовый мир вкладка и введение должны появиться в книге. Рецепт требует исследования и 5 вис воздуха; проверяйте крафт в магическом верстаке. Изменение контентных JSON проверяйте через `/reload`, текстур и переводов — через перезагрузку клиентских ресурсов. Полный список сценариев: [CHECKLIST.ru.md](CHECKLIST.ru.md).

## Куда смотреть в рабочем примере SDK

| Файл в `example-addon/src/main/` | Что показывает |
| --- | --- |
| `java/example/thaumcraft/ExampleAddon.java` | DeferredRegister, событие API, аспект, книга, материалы палочек, обработчик набалдашника |
| `java/example/thaumcraft/ExampleClient.java` | Отдельный клиентский рендерер страницы |
| `java/example/thaumcraft/ExampleFocus.java` | Стоимость, задержка и улучшения набалдашника |
| `java/example/thaumcraft/DeviceEntity.java` | Ограниченные запасы эссенции/вис, capabilities и сохранение |
| `java/example/thaumcraft/DeviceBlock.java` | Подключение серверного тика устройства |
| `resources/data/thaumcraft_example/` | Смешанная страница, аспекты предметов и настоящие рецепты |
| `resources/assets/thaumcraft_example/` | Переводы и модели предметов/палочек |

Java-регистрации книги примера нельзя дублировать JSON с теми же ID. Для собственных данных выберите один способ регистрации каждого определения.
