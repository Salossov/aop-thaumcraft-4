# Thaumcraft addon SDK

Development kit for **Thaumcraft API 1**, core **1.0.0**, Minecraft **1.21.1**, NeoForge **21.1.252** and Java **21**. The API JAR is a compile-only dependency; the runtime core already includes it. Forge 1.7.10 addon binaries require porting.

Runtime installations also require the core dependencies, including Curios and a compatible AOP: UI build. The SDK does not bundle those gameplay dependencies.

## Addon author guide

The [quick start](QUICKSTART.en.md) covers SDK setup and a minimal addon with a category, research and recipe. The following documents describe the supported extension points and verification procedures.

| Document | Contents |
| --- | --- |
| [Quick start](QUICKSTART.en.md) | SDK, Gradle, metadata, first mod, building and installation |
| [Book and JSON](CONTENT.en.md) | Categories, research, pages, translations, aspects, recipes and `/reload` |
| [Java API](API.en.md) | Events, knowledge, renderers, essentia/vis devices, wands, foci and viewers |
| [Game checklist](CHECKLIST.en.md) | Gameplay verification scenarios |
| [Additional gameplay contracts](GAMEPLAY_API.ru.md) | Device and integration contracts; available in Russian |
| [Русская документация](README.ru.md) | The same guides in Russian |

The overview below is supplemented by the detailed contracts and limitations in these guides. They target `AddonRegistry.API_VERSION == 1` in core **1.0.0**; this is neither a Minecraft version nor a promise of compatibility with every future release. This is an unofficial port. Original Thaumcraft and its assets were created by Azanor. See `NOTICE.md`, included in the SDK, for usage conditions; exposing an API does not relicense the whole mod.

## Build an independent addon

Unzip the SDK, open `example-addon`, and run `./gradlew.ps1 build` with Java 21 available through JAVA_HOME or PATH. The result is `build/libs/thaumcraft-example-1.0.0.jar`. The project compiles against `../lib/thaumcraft-api-1.0.0.jar`, Minecraft and NeoForge, with no core implementation dependency. The sources JAR is included for IDE navigation. No Maven publishing or account is necessary. Keep the SDK folder layout, or update the files dependency to the API JAR.

Install the core and the addon into the same NeoForge instance; the normal core dependencies, including Curios, still apply. The example content is intended for a separate test instance. Core and addon versions are declared in `META-INF/neoforge.mods.toml`. Choose addon-specific namespace and keep it stable across versions. Never shade the API, Minecraft, JEI or EMI into an addon.

## Startup and server/client separation

Subscribe with `AddonRegistrationEvent.BUS.addListener(...)` from the mod constructor. This dedicated public bus is active during common setup; `NeoForge.EVENT_BUS` is shut down then and cannot deliver this event. It fires once on the common setup work queue after the core wand materials are ready, before client model loading and server data reload. Register aspects, book categories, Java research/page defaults, `PageTypes` validators, wand parts, focus behaviors, upgrades and recipe-display providers there. Duplicate identifiers throw; book defaults close after this event. Register items, blocks and block entities normally with DeferredRegister on the mod bus. Do not register a second set of primary aspects. CatalogChangedEvent and KnowledgeChangedEvent remain on NeoForge.EVENT_BUS.

`thaumcraft.api.client` is client-only. Register `BookRenderers` on client setup, using a Dist.CLIENT subscriber. Keep these imports out of common device and data classes. Resource models are registered by the core through `FocusBehaviors` and `WandVisuals`; their identifiers are common data, not client rendering classes.

Use `ResearchApi.knowledge(serverPlayer)` for a defensive snapshot and `ResearchApi.grant(serverPlayer,key)` for an authoritative persisted transaction, including the normal research effects. The latter requires the server thread. `ResearchApi.definitions()`, `books()` and `clientKnowledge()` are synchronized client snapshots, replaced together. Do not use a client snapshot to authorize server actions.

Events:

- `AddonRegistrationEvent`: startup registrations on the dedicated `AddonRegistrationEvent.BUS`.
- `CatalogChangedEvent` on `NeoForge.EVENT_BUS`: validated catalog synchronization, login, reload and disconnect. `client()` selects side; server events expose `server()`. Use the event as an invalidation signal; revision is local, not a persisted identifier.
- `KnowledgeChangedEvent` on `NeoForge.EVENT_BUS`: UUID, copied knowledge and logical side after a committed update/sync. Research/pool/scan fields retain old string keys.

## Reloadable data and compatibility

Place content in `data/<your_namespace>/thaumcraft/**/*.json`. These are ordinary data-pack resources, synchronized by the core after `/reload`. Highest-priority resources replace lower-priority files with the same resource path. IDs inside separate files may not collide. Java defaults and JSON content must use distinct IDs. Changing a Java registration requires restarting the game; changing JSON does not.

The loader first parses and validates a candidate catalog: categories, parents, siblings, cycles, page requirements, custom page types and layout links. It publishes only after validation succeeds. A malformed JSON file or invalid reference aborts reload and retains the prior working catalog and progress. On the first load an invalid addon is an error, not an empty replacement. Removing a definition does not erase saved player keys; restoring that definition restores its link to the existing progress.

Legacy keys such as `GOGGLES`, `ROD_greatwood`, `FOCUSFIRE`, aspect tags such as `aer`, wand part tags and focus upgrade numbers 0–20 remain unchanged. `Aspect.getAspect("thaumcraft:aer")` is an alias for `aer`; custom aspects use `yourmod:aspect`. Use namespaced research/category/page IDs for new content. Research names are translation keys `tc.research_name.<research id>`; category names are the translation key supplied in BookCategory.

### Category, research and page JSON

A category file:

```json
{"kind":"book_category","id":"yourmod:workshop","name":"category.yourmod.workshop","icon":"minecraft:textures/item/diamond.png","order":100}
```

A research file:

```json
{"kind":"research","key":"yourmod:introduction","category":"yourmod:workshop","parents":["THAUMONOMICON"],"auto_unlock":true,"icon":"minecraft:diamond","column":0,"row":0,"text_pages":[],"recipe_pages":[],"page_order":["yourmod:intro_page"]}
```

Research supports the existing flags, hidden parents, triggers, warp, secondary research and recipe_groups. Omitted list/position/basic fields have defaults; the original built-in layout is preserved.

A mixed page:

```json
{"kind":"book_page","id":"yourmod:intro_page","type":"thaumcraft:layout","elements":[
 {"kind":"text","value":"page.yourmod.introduction"},
 {"kind":"item","value":"minecraft:diamond","width":16,"height":16,"tooltip":["page.yourmod.tip"]},
 {"kind":"aspect","value":"aer","count":5,"width":16,"height":16},
 {"kind":"image","value":"minecraft:textures/item/diamond.png","x":100,"y":40,"width":24,"height":24},
 {"kind":"recipe","value":"yourmod:example_recipe","height":160},
 {"kind":"link","value":"yourmod:introduction"}
]}
```

Coordinates are in logical **139 × 199** page units, transformed by the existing book fitting/zoom. Omit x/y (or use -1) for vertical flow; text wraps to width and automatically continues on further pages. Explicit y positions are manual layout and should fit the page. First-page capacity accounts for the title. Elements: text, image, item, aspect, recipe, link, rule. `translate` defaults true for text; false displays a literal. Item IDs, texture IDs, recipe IDs and research links are identifiers, not translations. Images use width/height and optional scale. Tooltips are translation-key lists. The item/aspect drawing paths retain the core tooltips. A link opens known research, or centers its category if still locked.

For a special page, register `PageTypes.register(ResourceLocation, Consumer<JsonObject>)` for validation and `BookRenderers.register(type, BookPageRenderer)` on the client. JSON uses that type and a `data` object. The renderer gets GuiGraphics, Font, page origin and native item/aspect/recipe/link/tooltip operations through BookRenderContext. Mouse callbacks use page-local coordinates. The example includes a custom meter page and hover/click links.

## Aspects, recipes and devices

Register a compound with `Aspect.registerCompound(id,color,icon,translationKey,first,second)`. Keep components registered, provide the icon in the addon resources and register the same definition on both physical sides at startup. There remain six primary aspects. `AspectApi.aspects(level,itemStack)`, `aspects(level,blockState)` and `aspects(entity)` return defensive maps, including data-pack and stack-specific rules. `AspectApi.scanned(stack)` queries the current client's scan knowledge.

Assign aspects with `kind: object_aspects` or `entity_aspects` and an `entries` object mapping registry IDs to aspect quantity maps. Block item IDs work as object assignments. Other supported existing rules include item_tag_aspects, entity_variant_aspects, derived_object_aspects, stack_aspect_minimums and scan_groups. See the independent example's JSON. Quantities are positive integers; aspect IDs may be namespaced.

Standard recipes use `data/<namespace>/recipe/*.json` (singular in 1.21): `thaumcraft:arcane`, `thaumcraft:arcane_shapeless`, `thaumcraft:crucible`, `thaumcraft:infusion` and existing dynamic/infernal serializers. Standard addon recipes are picked up automatically by the shared display catalog. Infusion and crucible recipes also have public Java base contracts. Arcane recipes implement IArcaneRecipe. All outputs use the 1.21 ItemStack codec, e.g. `{ "id":"yourmod:device", "count":1 }`.

`RecipeDisplays.register(id, provider)` adds effective displays for a custom recipe type. RecipeDisplay includes a unique display ID, actual recipe ID, kind, research, input ingredients, output stacks, aspect inputs/outputs, centivis, instability, XP levels, catalysts and notes. Do not reuse a display ID for multiple dynamic variants. Provider results are cached per Level and invalidated on catalog reload. Call invalidate when an external catalog changes. The server resolves display IDs from its own provider, never from client-supplied ingredients.

Essentia amounts are indivisible units. `IAspectContainer.addToContainer` returns **unaccepted remainder**; `IEssentiaTransport.addEssentia` returns **accepted units**. Expose `EssentiaCapabilities.CONTAINER` and TRANSPORT from addon-specific BE in RegisterCapabilitiesEvent. Respect face direction, suction and server-thread mutations. EssentiaStore supplies bounded storage; persist its contents explicitly and call setChanged after transfers.

Vis storage and focus costs use **100 centivis = 1 vis**. WandRod capacity and native arcane JSON `vis` costs remain whole-vis values for compatibility. `WandApi.centivis` is an explicit checked conversion. Use WandApi.stored/capacity/set/quote/consume; consume(..., false) simulates, true debits on the server thread. Quote includes the existing cap, armor and frugal modifiers. Native arcane transfer does not call consume.

A relay/source may extend TileVisNode. Inherited onLoad/setRemoved/onChunkUnloaded keep only loaded nodes connected. A source overrides consumeVis and returns the amount actually supplied; never promise more than its persisted reserve. A consumer may use `VisNetHandler.drainVis(level,pos,primal,centivis)` or its parent connection. These operations may return a partial amount, never force-load chunks, and are not multi-aspect atomic transactions. Persist addon-specific state and call super in save/load methods. The example is a relay with bounded stored charge, not an infinite source.

## Wand parts, foci and upgrades

Register WandRod and WandCap with namespaced ResourceLocation constructors, an actual ingredient stack supplier and the required research key. Costs/modifiers feed the real assembly and casting handlers. WandApi.parts writes existing saved components. To show custom geometry in hand and inventory, register `WandVisuals.register(kind, WandParts, modelId)` for each supported rod/cap combination. Supply a model for every supported combination; absent models fall back to the standard wand. The example includes a composite model and an attached focus model. Attached wand head coordinates are centered around x/z=8 and y=18..22; the core applies the staff offset.

Extend ItemFocusBasic and register FocusBehavior plus the attached model through FocusBehaviors. begin, cast and end run on the server. Continuous foci receive tick attempts, limited by getActivationCooldown in **milliseconds**, matching native foci. The core verifies affordability and reserves the discounted centivis before cast; true retains the debit, false/exception refunds the reservation. begin/end manage use lifecycle, not extra charges. The same cost must not be debited again by the addon or replace the wand's parts during a callback. Failed effects should return false. The implementation also ends use on stop/logout.

FocusUpgradeType.register retains stable integer persistence; addon keys must be namespaced by convention and integer IDs must be unique. The example uses ID 24000 for demonstration; it is not a globally reserved addon range. Production addons require a stable, nonconflicting ID. Duplicate numbers or names fail registration. The ResourceLocation overload also accepts a separate icon resource (including a dependency texture). getPossibleUpgradesByRank and canApplyUpgrade control the real focal manipulator. Modifications should preserve all other ItemStack components.

## JEI and EMI addon

`aoptc4-compact-0.7.0.jar` is a separate optional integration mod. Server installation is optional and enables additional Jade/WTHIT data. It requires the updated core; it does not include or install a viewer. Supported runtime ranges for Minecraft 1.21.1: **JEI >=19.8.4.110 and <20**, **EMI >=1.1.13 and <1.2**. The build compiles against the minimum supported APIs; runtime versions are not pinned to a single build. No NEI binary exists in this target; use JEI or EMI. No viewer dependency is bundled. Both present selects native EMI; the JEI plugin registers no TC4 categories/ingredients/recipes into the bridge.

The shared catalog covers arcane recipes, crucible, infusion variants (including enchantments/runic augmentation), probabilistic infernal bonuses, distillation, centrifuging and crystallization. Each aspect has one searchable identity shared by vis and essentia, with the correct amount/unit retained in recipe slots. Item aspect composition appears as distillation outputs and honors scan visibility. Requirement text links to the corresponding research through a real server-opened Thaumonomicon menu; keep the book in inventory. Dynamic variants are representative: extra enchantments on a real central item can affect actual ritual cost. Infernal bonus previews describe their probability instead of promising a guaranteed output.

Client config `thaumcraft_viewers-client.toml`, also available through the mod's config screen:

- recipes = ALL_WITH_REQUIREMENTS (default), or RESEARCHED_ONLY.
- aspects = SCANNED_ONLY (default), or ALL.

Knowledge/scan changes refresh filters after a short debounce. JEI uses its runtime API. EMI 1.1.x has no public invalidation method; one isolated reflection bridge calls EmiReloadManager.reload. It is isolated in the EMI adapter and logs a clear error if the reload hook becomes unavailable. `/reload` also invalidates the core recipe-display cache. The dependency matrix checks class linkage with neither viewer, JEI, EMI, and both; it does not substitute for a running-game check.

WorkbenchApi exposes the runtime menu Class and MenuType without an internal-class import; JEI selects handlers by the exact runtime class.

“+” sends only menu ID, state ID, display ID and quantity intent to the core. The server validates its current menu, recipe, research, ingredients and inventory capacity; it plans on copies and then commits. Existing grid stacks are returned or reused without dropping items. A failure changes nothing. It fills ingredients only: no crafting, vis expenditure, output creation or automatic click loop. EMI destinations that request automatic output/crafting are deliberately unsupported.


### Aspect discovery and native recipe layouts

The seven recipe categories use the core book artwork: arcane grid, central infusion item with grouped components around it, crucible glyph, aspect amounts, instability, and research icons. Hover research icons to see translated category/name and parent requirements; click to open the book. Each workstation is registered once per category, so JEI does not build a sidebar full of duplicate blocks.

Both viewers include an aspect index, aspect combinations and scanned-source pages. Unknown aspects render as `?`; their names, components and sources remain hidden. The six primals are known initially. Discovered aspects remain known at zero research points. Vis and essentia share a sidebar entry and recipe/use lookup identity; recipe amounts still distinguish whole essentia units from centivis.

Hover a discovered aspect to see its components (unknown components stay masked), up to five scanned sources and their yields. Use the viewer recipe/usage controls to open full combination/source pages or recipes consuming the aspect. Source pages always require scanning and omit unscanned items, including block items, even with the composition reveal setting set to ALL. Scanning/discovery refreshes names, pages and lookup indexes without reconnecting.

The behavior and native ritual arrangement were informed by [GTNH Aspect Recipe Index](https://github.com/GTNewHorizons/AspectRecipeIndex). No GTNH code or assets are copied; the addon references Thaumcraft's existing book artwork.

## Verification

See CHECKLIST.en.md / CHECKLIST.ru.md for manual gameplay cases. Automated build checks run the original rules, addon registration/references/collisions/immutable snapshots/progress/unit tests, 2000 randomized transfer trials and optional-class linkage matrix. SDK artifact inspection verifies public-only addon dependencies, no shaded API/viewer classes, JSON syntax and JAR content. These tests run without starting Minecraft.


## Pinned dependency references

[JEI Maven 19.64.0.453](https://maven.blamejared.com/mezz/jei/jei-1.21.1-neoforge/19.64.0.453/) | [EMI 1.21 version properties](https://github.com/emilyploszaj/emi/blob/1.21/gradle.properties) | [EMI reload implementation](https://github.com/emilyploszaj/emi/blob/1.21/xplat/src/main/java/dev/emi/emi/runtime/EmiReloadManager.java).


### Viewer compatibility regression checks

Run `./gradlew.ps1 check -PviewerCompatibility=minimum`, then `middle` and `latest`. Each profile checks neither viewer, JEI alone, EMI alone and both. It resolves the actual API method/field symbols used by the compiled adapters, verifies required interface implementations and the EMI refresh hook, and checks the real Maven version-range parser (including JEI 19.64.0.455 and EMI build qualifiers). No Minecraft instance is launched.

Checked profiles: JEI 19.8.4.110 / 19.19.6.233 / 19.64.0.455; EMI 1.1.13 / 1.1.20 / 1.1.24 for Minecraft 1.21.1. This is API/linkage verification, not a claim of gameplay testing for every release in the range.

### AOP:TC4 Compact 0.7.0

The mod-list title is AOP:TC4 Compact; the stable mod ID remains thaumcraft_viewers. Core/API 1.0.0 adds AspectApi.variants(), returning copied filled phials, jars, crystals and essence pieces. JEI indexes component subtypes and all 240 built-in variants.

Diagrams and hover/click regions fit the available card height, including EMI's 125-logical-pixel card at GUI scale 4. Dark text on light cards has no shadow. Larger colored status marks sit beside research icons. Large quantities use compact labels (524k, 2.1G), retaining exact values in tooltips. Aspect names, units and auxiliary tooltip text use different colors.

ResearchNavigation.resolve(...) is a shared pure resolver: completed research opens its page; available research is centered/highlighted; missing parents select the closest available ancestor. Trigger-locked nodes remain hidden, with an exploration hint. The server still authorizes opening and requires a book.

Additional gameplay contracts (Russian): [ink containers, focuses, particles and content settings](GAMEPLAY_API.ru.md).
