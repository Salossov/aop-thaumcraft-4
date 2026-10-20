# AOP: Thaumcraft 4

[Русский](README.ru.md)

I maintain an unofficial port of **Thaumcraft 4.2.3.5** to **Minecraft 1.21.1 / NeoForge**.
Research, aspects, aura nodes, wands and staves, alchemy, infusion, essentia networks, golems, equipment and the Outer Lands are included, together with a public API for addons.

> **Personal use only.** Thaumcraft and all of its assets belong to Azanor. This repository contains the original mod's logic and assets and is not intended for redistribution.

The project was inspired by [Thaumcraft 3: Reforged](https://github.com/notlikethiss/thaumcraft-3-reforged).

## Status

Version **1.0.0** is available for testing. Automated checks cover API rules and selected gameplay scenarios; manual visual and multiplayer acceptance is still in progress. Hidden Magic Box storage remains unimplemented.

## Installation

- Minecraft **1.21.1**, Java **21**, NeoForge **21.1.252 or newer within 21.1**.
- [Curios](https://www.curseforge.com/minecraft/mc-mods/curios) **9.5.1+1.21.1 or newer within 9.x** is required.
- [TerraBlender](https://www.curseforge.com/minecraft/mc-mods/terrablender) **4.1.0.8 or newer within 4.1** is optional. A built-in biome placement fallback is used without it.

Place the gameplay JAR in `mods` on the client and server. The public API is already bundled; do not install the API or sources JAR as a mod.

**AOP:TC4 Compact** is a separate optional project for JEI/EMI recipes and Jade/WTHIT block tooltips. The core does not require those mods. Thaumic Tinkerer is also a separate addon and requires a matching core build.

## Playing

Start with the Thaumonomicon. Scan objects with the Thaumometer to discover aspects and progress through research. Default controls: **F** selects a focus, **H** toggles the harness, **G** switches the architect mode, and **Shift+G** switches its axis. Controls can be reassigned in Minecraft settings.

Client settings are in `config/thaumcraft-client.toml`; server settings are stored with the world. The Thaumonomicon also exposes display settings. Back up worlds before testing updates.

## Building

Install JDK 21, clone this repository, and run:

```powershell
./gradlew.ps1 build
```

On Linux/macOS, use `sh ./gradlew build`. Dependencies are downloaded on the first build. Minecraft and NeoForge versions are defined in `gradle.properties`.

Outputs:

- `mod/build/libs/thaumcraft-1.21.1-1.0.0.jar` — gameplay mod.
- `api/build/libs/thaumcraft-api-1.0.0.jar` — development dependency.
- `mod/build/libs/thaumcraft-1.21.1-1.0.0-sources.jar` — source archive.

Run a development client with `./gradlew.ps1 :mod:runClient`. Build the addon SDK with `./gradlew.ps1 addonSdk`. See the [addon development guide](docs/addons/README.en.md) for public API contracts and examples.

## Verification and CI

`build` compiles the mod and executes the API and standalone rule checks. Game scenarios are kept in separate verification source sets and excluded from the gameplay JAR. A successful build does not replace an in-game test.

The GitHub workflow builds code changes and supports manual releases. Changes limited to documentation do not trigger it. Pushes to `main` produce a nightly build; a manual run uses `mod_version` and refuses to overwrite an existing release. Local `act` runs build without publishing.

For bug reports, include the mod versions, a reproduction, and the relevant log or crash report. Remove access tokens and personal data before attaching logs.

## Credits and terms

- [Azanor](https://www.curseforge.com/minecraft/mc-mods/thaumcraft) — original Thaumcraft, gameplay and assets.
- CovertJaguar, Dan Scott, Lorc, bonemouse, Scott Killen, Rorax and chicken_bones — original credits.
- [Salossov](https://github.com/Salossov) — Minecraft 1.21.1 port, with AI-assisted implementation and verification.
- [NeoForge](https://github.com/neoforged/NeoForge), Curios and TerraBlender — platform and integrations.

See [NOTICE.md](NOTICE.md). `TEMPLATE_LICENSE.txt` covers the build template only; it does not license Thaumcraft or its assets under MIT. This is an unofficial project.
