# Manual gameplay checklist

[SDK guide](README.en.md) | [Quick start](QUICKSTART.en.md) | [Русский](CHECKLIST.ru.md)

## Scope and environment

The following scenarios define manual acceptance criteria for addons and viewer integration. Use a separate test instance and world at normal tick speed, with compatible core 1.0.0 and server builds, Curios and AOP: UI. Install the SDK example only for scenarios that exercise its content.

Historical client checks for integration version 0.7.3 used JEI 19.64.0.455 and EMI 1.1.24 at GUI scales 1–4, covering aspect tooltips, filled JEI variants and research navigation. This record does not establish completion of every scenario below or verification of later artifacts.

## Scenarios

1. **Existing saves.** Compare completed research, aspect pools, wand parts, focus upgrades and stored vis before and after an update. Repeat after reconnecting and changing dimensions. Persistent identifiers and values must remain available.
2. **Core book.** With no content addon installed, open all six legacy tabs, ordinary research text, native diagrams and grouped recipe pages at every book window and zoom setting. Existing progress and layouts must remain accessible.
3. **SDK example book.** Install the example and verify its Workshop tab and introduction after Thaumonomicon basics. Check flowed text, positioned images, item/aspect tooltips, links, native recipes and the Java meter page at original, square and full-window sizes.
4. **Data reload.** Update mixed-page text and coordinates through a datapack, then run `/reload` on a server with two clients. Both must receive the catalog update. Independently introduce invalid JSON, an invalid parent, page link and page type. Each failed reload must retain the previous working book and progress. Restore valid data and repeat.
5. **Wand and focus lifecycle.** Obtain the example rod, cap, focus and device with `/give`. Assemble the rod/cap using the normal diagonal recipe. Verify research gates, exact vis cost, held/inventory models and attached-head alignment. Upgrade the focus in the focal manipulator. Test activation, holding, release, changing the held item, death and disconnect. Verify centivis, cooldown and end callbacks; rejected effects must not retain a charge.
6. **Device persistence.** Fill the example relay through phials and tubes. Verify single-aspect capacity 64. In range of an energized node or relay, charge must increase only when vis is supplied and remain at or below 1000 centivis. Unload and reload the chunk, then save and reopen the world. Stored contents must not be lost or duplicated, and the device must not force-load chunks.
7. **Viewer combinations.** Run separate launches without viewers, with JEI 19.64.0.455, with EMI 1.1.24+1.21.1 and with both. The core must also run without Compact. With both viewers installed, TC4 categories and recipes must appear once through EMI.
8. **Recipe accuracy.** Inspect all seven recipe categories and compare previews with actual crafting or ritual outcomes. Check central items, dynamic runic/enchantment variants, quantities, centivis conversion, instability, experience, research requirements, probabilistic infernal bonuses and catalysts. Verify recipe/use lookup for custom aspects and primal vis ingredients.
9. **Visibility and refresh.** Defaults must list unresearched recipes with their requirements and hide unscanned item compositions. Scan an item and complete research without reconnecting; filters must update. Test `RESEARCHED_ONLY` and `ALL`, then restore defaults. Repeat `/reload` with addon recipes added and removed.
10. **Research navigation.** With a Thaumonomicon in inventory, open a research link from a viewer. Completed research must open directly; locked research must center its category without granting progress. Removing the book must cause the server to reject opening the menu.
11. **Arcane ingredient transfer.** Test `+` for one set and maximum sets, overlapping/tag ingredients, component-bearing stacks, occupied grids, full inventories, missing ingredients, missing research, absent or empty wands, closed or unrelated menus and stale requests. Transfer may move ingredients but must not craft output or spend vis. On rejection, every inventory/grid stack and component must remain unchanged. Repeat on a dedicated server; subsequent normal crafting must retain its research and cost checks.
12. **Addon removal and restoration.** In a test instance, remove the example, reload the world and restore it. Saved knowledge keys must remain intact. This scenario does not establish recovery guarantees for arbitrary missing-mod block data.
13. **Category presentation.** In JEI, verify that the workstation list contains each device once, including after navigating multiple recipes. In both viewers, inspect native arcane, crucible and infusion artwork, grouped ingredient quantities, research status and parent tooltips. Version 0.7.3 is the historical reference for this scenario.
14. **Aspect discovery.** A new player must see six named primals and `?` for unknown compounds. Hidden tooltips must not reveal names, components or sources. A discovered compound must remain visible after its research pool reaches zero. Vis and essentia must share one aspect lookup identity.
15. **Scanned sources.** Scan one source and inspect its known aspects, yields and source pages. Scan another source and discover a further aspect without reconnecting. Names and recipe/use indexes must update without duplicate pages. Unscanned items and block items must remain absent from source pages, including when composition visibility is `ALL`. Repeat after reconnecting, changing worlds and `/reload`.

## Recording results

Record component versions, configuration, scenario, expected and actual behavior, and relevant logs for each failure. Compilation and linkage checks do not validate rendered output, viewer discovery, interactive menus or dedicated-server behavior.
