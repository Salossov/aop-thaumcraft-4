# Thaumcraft localization

This directory contains language resources for the Minecraft 1.21.1 port of Thaumcraft 4.

## Translation format

Translations use Minecraft JSON language files, such as `en_us.json` and `ru_ru.json`. Files must be encoded as UTF-8 without a byte-order mark and contain a valid JSON object mapping translation keys to strings.

`en_us.json` is the reference catalog. Additional languages should preserve its keys, formatting placeholders and supported book markup. Translation additions should not modify the reference language as part of the same change.

## Review

Changes should be checked in the client, including long labels, tooltips and book pages. Language loading and text layout are separate checks; valid JSON alone does not establish that a translation fits its interface.

Contributor guidance is available in [contributing.md](contributing.md).
