# The first addon

[Contents](README.en.md) | [Русский](QUICKSTART.ru.md)

Target: Minecraft **1.21.1**, NeoForge **21.1.252**, JDK **21**, Thaumcraft **1.0.0**, API **1**. Examples use mod ID `myaddon`. Replace it consistently in Java, metadata, resource names and translations.

## 1. Extract the SDK

The repository builds `thaumcraft-addon-sdk-1.0.0.zip` under `build/distributions/`. Its layout is:

```text
lib/
  thaumcraft-api-1.0.0.jar
  thaumcraft-api-1.0.0-sources.jar
docs/
  README.en.md
  QUICKSTART.en.md
  CONTENT.en.md
  API.en.md
  NOTICE.md
example-addon/
  build.gradle
  settings.gradle
  gradlew.ps1
  gradle/wrapper/
  src/main/java/example/thaumcraft/
  src/main/resources/
```

The SDK contains example sources. Obtain the runtime core and Curios separately; the runtime core already contains the API. First build the unchanged example from `example-addon`:

```powershell
java -version
.\gradlew.ps1 build
```

Output: `build/libs/thaumcraft-example-1.0.0.jar`. The initial Gradle build needs access to dependency repositories. Use `--offline` after dependencies have been downloaded. Building does not require launching Minecraft.

## 2. Depend on the API

The SDK already includes a standalone project. Essential `build.gradle` configuration:

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

The API path is resolved relative to the addon project and must be updated when its location changes. Attach `sources.jar` in the IDE. Do not shade or Jar-in-Jar the API. Do not import implementation packages such as `thaumcraft.items`, `thaumcraft.registry` or `thaumcraft.knowledge`. The `internal` package within `thaumcraft.api` is also implementation support rather than an addon extension point.

## 3. Create the entry point

In a copy of the example, replace its demonstration Java classes and resources with addon-specific. Minimal `src/main/java/example/myaddon/MyAddon.java`:

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
        // Attach item/block DeferredRegisters to modBus.
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

This example registers a compound aspect using an existing icon. For addon-specific icon, add `assets/myaddon/textures/aspects/resonance.png` and use `myaddon:textures/aspects/resonance.png`. Aspect definitions are startup registrations; `/reload` does not create or remove them.

`AddonRegistrationEvent` uses the dedicated public **AddonRegistrationEvent.BUS**, active during common setup. Register the same IDs on both physical sides. Keep client classes such as `Minecraft` and `GuiGraphics` out of this entry point.

## 4. Declare dependencies

`src/main/resources/META-INF/neoforge.mods.toml`:

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

The metadata must declare the addon's actual license. The core range follows the SDK example: it declares a minimum, not verified compatibility with future releases. Distributed addons should constrain the range to versions with verified support.

## 5. Add a category, research and recipe

The [content guide](CONTENT.en.md) contains a matching `myaddon` set: category `workshop`, research `introduction`, page `introduction_page`, recipe `diamond`. Copy those four JSON documents and the translation file. They use vanilla items and need no item DeferredRegister. Keep the aspect from step 3 for further recipes.

The introductory example uses `auto_unlock: true` to grant the introduction automatically. This does not declare prerequisite-based research. For normal research, use `auto_unlock: false` and configure parents/costs; see the [field reference](CONTENT.en.md#research).

## 6. Build and install

Run `.\gradlew.ps1 build` in the standalone project. Copy only `build/libs/myaddon-1.0.0.jar` to a test NeoForge instance containing Thaumcraft and its required dependencies, including Curios and a compatible AOP: UI build. Content addons normally belong on both client and server. Do not put the API JAR in `mods`. JEI/EMI are needed only when testing viewer integration.

After joining a test world, the category and introduction should appear in the book. The recipe requires the research and 5 air vis; test it at an arcane workbench. Use `/reload` for content JSON changes and client resource reload for translations/textures. See [CHECKLIST.en.md](CHECKLIST.en.md) for further scenarios.

## SDK example source map

| File under `example-addon/src/main/` | Demonstrates |
| --- | --- |
| `java/example/thaumcraft/ExampleAddon.java` | DeferredRegister, API event, aspect, book, wand materials, focus behavior |
| `java/example/thaumcraft/ExampleClient.java` | Separate client page renderer |
| `java/example/thaumcraft/ExampleFocus.java` | Focus cost, cooldown and upgrades |
| `java/example/thaumcraft/DeviceEntity.java` | Bounded essentia/vis storage, capabilities and persistence |
| `java/example/thaumcraft/DeviceBlock.java` | Server ticking for the device |
| `resources/data/thaumcraft_example/` | Mixed page, object aspects and actual recipes |
| `resources/assets/thaumcraft_example/` | Translations and item/wand models |

Do not duplicate the example's Java book definitions with JSON using the same IDs. Choose one registration mechanism per definition.
