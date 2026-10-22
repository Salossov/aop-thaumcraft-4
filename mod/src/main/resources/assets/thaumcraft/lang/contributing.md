# Localization contribution guidelines

[Localization format](readme.md)

## Translation changes

Use `en_us.json` as the source catalog. Preserve translation keys, formatting placeholders, item and research identifiers, and supported book markup. Language files must be valid JSON encoded as UTF-8 without a byte-order mark.

A translation change should identify the language and affected entries. Reference-language changes should be submitted separately from the translation. Interface text should be checked in the client for clipping, wrapping and consistent terminology.

## Issue reports

Before submitting a report, check existing issues for the same problem. Each report should describe one reproducible issue and include:

- Minecraft, NeoForge and Thaumcraft versions, plus relevant addon versions.
- Language, GUI scale and affected screen or book page.
- The translation key or a screenshot locating the affected text.
- Expected and actual behavior, with reproduction steps.
- A relevant log or crash report when applicable, with credentials and personal information removed.

Rendering problems should identify graphics modifications and state whether the problem reproduces without them. Reproduction on a supported NeoForge installation should be distinguished from behavior specific to another server platform or development environment.

Additional information about an existing report belongs in that report, including after it has been closed.

## Original project references

The original [Thaumcraft forum thread and FAQ](http://www.minecraftforum.net/topic/2011841-) and [bug reporting guide](http://vazkii.us/br101/) are retained as historical references. Their Minecraft, Forge and legacy-tool instructions do not define the supported environment of this port.

Logs can be attached through [GitHub Gist](https://gist.github.com/) or another suitable text host.
