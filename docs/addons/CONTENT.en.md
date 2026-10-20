# Book, research and data

[Contents](README.en.md) | [Quick start](QUICKSTART.en.md) | [Java API](API.en.md) | [Русский](CONTENT.ru.md)

Paths below are relative to your addon's `src/main/resources`. Datapacks use the same `data/` layout. Aspect definitions and custom page types require the Java addon to be installed.

## Files and identifiers

| Content | Path |
| --- | --- |
| Categories, research, pages, aspect assignments | `data/myaddon/thaumcraft/<any_name>.json` |
| Minecraft/Thaumcraft recipes | `data/myaddon/recipe/<name>.json` |
| Translations | `assets/myaddon/lang/en_us.json`, `ru_ru.json` |
| Aspect icon | `assets/myaddon/textures/aspects/<name>.png` |

In 1.21.1 the recipe directory is singular: **`recipe`**. Each file under `thaumcraft/` is one object with a `kind` field; subdirectories are allowed. Recipes use `type` and belong in their separate directory. Unrelated JSON configuration files with unknown kinds abort loading.

New keys are namespaced, such as `myaddon:introduction`. Book keys have a 128-character limit. Legacy core keys (`THAUMONOMICON`, `GOGGLES`, `ROD_greatwood`) retain their spelling and case. A recipe's path defines its ID: `data/myaddon/recipe/diamond.json` → `myaddon:diamond`.

Category/page `id` may be omitted, defaulting to the file ID relative to `thaumcraft/`. Examples specify it explicitly. Research requires a `key` field.

## Category

`data/myaddon/thaumcraft/workshop.json`:

```json
{
  "kind": "book_category",
  "id": "myaddon:workshop",
  "name": "category.myaddon.workshop",
  "icon": "minecraft:textures/item/diamond.png",
  "background": "thaumcraft:textures/gui/gui_researchback.png",
  "order": 100,
  "required_research": ""
}
```

`name` is a translation key. `icon` and `background` are PNG resource paths, including `textures/` and the extension. `order` defaults to 100; categories sort by order, then ID. Empty `required_research` is the default; otherwise that research must be completed before the tab appears. The research must exist and its dependencies must leave the category reachable.

Java equivalent: `AddonRegistry.category(new BookCategory(id, name, icon, background, order, requiredResearch))` during registration. Do not also declare that category in JSON.

## Research

`data/myaddon/thaumcraft/introduction.json`:

```json
{
  "kind": "research",
  "key": "myaddon:introduction",
  "category": "myaddon:workshop",
  "parents": ["THAUMONOMICON"],
  "auto_unlock": true,
  "column": 0,
  "row": 0,
  "icon": "minecraft:diamond",
  "text_pages": [],
  "recipe_pages": ["myaddon:diamond"],
  "page_order": ["myaddon:introduction_page", "myaddon:diamond"]
}
```

This introduction is granted automatically. `auto_unlock` completes research during automatic knowledge processing; do not assume it checks parents. For ordinary research, set it to `false` and, for example, specify `aspects: {"aer": 1, "ordo": 1}` and `complexity: 1`.

| Field | Type / default | Meaning |
| --- | --- | --- |
| `key`, `category`, `icon` | Required strings | Persistent key, category ID and item icon |
| `parents`, `hidden_parents` | String arrays / `[]` | All are required for availability; the latter are hidden dependencies |
| `aspects` | Object / `{}` | Research aspect quantities, integers 1–32767 |
| `complexity` | Integer / `0` | Complexity 0–3 |
| `column`, `row` | Integers / `0` | Research map position, not page pixels |
| `auto_unlock` | Boolean / `false` | Complete during automatic knowledge processing |
| `hidden` | Boolean / `false` | Hidden branch with separate discovery state |
| `concealed`, `virtual`, `round`, `stub`, `secondary`, `special` | Boolean / `false` | TC4 flags; `virtual` excludes an ordinary map/available node, `round`/`special` affect framing. These do not replace server authorization |
| `siblings` | String array / `[]` | Related research; every reference must exist |
| `warp` | Integer / `0` | Warp, 0–127 |
| `text_pages`, `recipe_pages` | String arrays / `[]` | Legacy text translation keys and recipe IDs |
| `page_order` | String array | Explicit order of texts, recipes, recipe groups and new `book_page` IDs |
| `recipe_groups` | Object / `{}` | Page name → nonempty array of variants from `recipe_pages` |
| `page_requirements` | Object / `{}` | Page key → research required to show that page |
| `item_triggers`, `entity_triggers`, `aspect_triggers` | String arrays / `[]` | Scan-based discovery conditions for hidden research |
| `required_item_tags` | String array / `[]` | Loading filter requiring available items in those tags |

Without `page_order`, text pages precede ungrouped recipes and recipe groups. Include new `book_page` IDs explicitly in `page_order`. Recipe group variants must also occur in `recipe_pages`.

Trigger arrays alone are insufficient: the standard scan handler considers `hidden: true` research with nonempty `aspects`. Normal scanning and research access rules still apply. API 1 has no generic registry for arbitrary new discovery trigger types. For your own gameplay event, check conditions on the server and, when appropriate, grant completed research through `ResearchApi.grant`. That method does not merely reveal a hidden node.

Research names use the exact translation key `tc.research_name.myaddon:introduction`, including the colon. Entries in `text_pages` are legacy translation keys supporting `<BR>`, `<LINE>` and `<IMG>namespace:path:u:v:w:h:scale</IMG>` markup. New mixed pages use the elements below; legacy image markup is not interpreted by their `text` element.

## Mixed page

`data/myaddon/thaumcraft/introduction_page.json`:

```json
{
  "kind": "book_page",
  "id": "myaddon:introduction_page",
  "type": "thaumcraft:layout",
  "elements": [
    {"kind": "text", "value": "page.myaddon.intro", "width": 90},
    {"kind": "image", "value": "minecraft:textures/item/diamond.png", "x": 105, "y": 0, "width": 24, "height": 24},
    {"kind": "rule", "width": 90, "height": 9},
    {"kind": "item", "value": "minecraft:diamond", "width": 16, "height": 16, "tooltip": ["page.myaddon.tip"]},
    {"kind": "aspect", "value": "aer", "count": 5, "width": 16, "height": 16},
    {"kind": "link", "value": "myaddon:introduction"}
  ]
}
```

To embed a diagram, add `{"kind":"recipe","value":"myaddon:diamond","height":160}`. It occupies at least 160 logical pixels and may flow onto another page. This example already includes the recipe as a separate research page.

| Element | `value` | Behavior |
| --- | --- | --- |
| `text` | Translation key or text | Wraps at `width`; `translate: false` selects literal text |
| `image` | Full PNG path | Draws `width × height`, multiplied by `scale` |
| `item` | Item ID | Native 16×16 item, with `count` |
| `aspect` | Aspect tag | Native 16×16 aspect with `count` |
| `recipe` | Recipe ID | Native recipe diagram |
| `link` | Research key | Link labeled using `tc.research_name.<key>` |
| `rule` | Unused | Horizontal line of `width` |

Common defaults: `kind: "text"`, `value: ""`, `x: -1`, `y: -1`, `width: 139`, `height: 9`, `scale: 1`, `count: 1`, `translate: true`, `tooltip: []`. Tooltips contain translation keys. Set an explicit 16×16 tooltip area for item/aspect elements.

The logical canvas is **139×199**. The first research page reserves 25 pixels for its title, leaving 174 for content. Coordinates are independent of screen scale. `y: -1` participates in vertical flow and pagination; `x: -1` starts at the left edge. An explicit `y` does not advance the flow cursor: reserve space yourself to avoid overlaps. Manually positioned text wraps downward from that point.

Input limits: `x` -1–139, `y` -1–199, `width` 1–139, `height` 1–199, `scale` greater than 0 and at most 4, nonnegative `count`. These validate fields rather than every overlap: account for `x + width`, `y + height` and the smaller first page. `scale` affects images only; items/aspects remain 16×16 and text uses normal font wrapping. Test long translations.

A custom page specifies its own `type`, such as `myaddon:meter`, and a `data` object. See [Java API](API.en.md#custom-page) for validator and client renderer registration.

## Example translations

`assets/myaddon/lang/en_us.json`:

```json
{
  "category.myaddon.workshop": "Workshop",
  "tc.research_name.myaddon:introduction": "First experiments",
  "page.myaddon.intro": "An arcane workbench uses vis from a wand. Your workshop begins here.",
  "page.myaddon.tip": "The result of your first experiment",
  "aspect.myaddon.resonance": "Resonance"
}
```

Add the matching `ru_ru.json`. Server datapacks do not synchronize `assets`; translations and images must be available on the client.

## Item, block and entity aspects

Register a new compound aspect in Java first, as in the [quick start](QUICKSTART.en.md). The six primals remain `aer`, `terra`, `ignis`, `aqua`, `ordo`, `perditio`. `thaumcraft:aer` aliases legacy `aer`; custom aspects use their full tags, such as `myaddon:resonance`.

`data/myaddon/thaumcraft/object_aspects.json` assigns items/blocks:

```json
{
  "kind": "object_aspects",
  "entries": {
    "minecraft:diamond": {"vitreus": 4, "myaddon:resonance": 2}
  }
}
```

This deliberately changes an existing item for illustration; normally assign your own item/block IDs. `entity_aspects` uses the same structure with entity type IDs. Quantities are integers 1–32767, not centivis. Native stack rules may add component, potion, enchantment and filled-container aspects; use `AspectApi` to read effective composition.

Additional loader rules:

| `kind` | Values under `entries` |
| --- | --- |
| `item_tag_aspects` | Item tag ID → `{"aspects":{"aer":1},"required_item_tags":[]}` |
| `derived_object_aspects` | Item ID → `{"inherit":"minecraft:diamond","add":{"aer":1},"merge":{"ordo":1},"remove":["perditio"]}` |
| `stack_aspect_minimums` | Item ID → minimum stack aspect map |
| `potion_aspect_bases` | Potion ID → aspect map |
| `potion_aspect_effects` | Effect ID → aspect map |
| `enchantment_aspect_minimums` | Enchantment ID → aspect map |
| `scan_groups` | Item/block ID → shared string scan group ID |
| `entity_variant_aspects` | Entity type ID → array of `{"field":"...","value":"...","aspects":{"aer":1}}` |

Derived rule fields are optional: `add` adds quantities, `merge` imposes minima, `remove` removes tags. Entity variant fields are those supported by the native handler, not arbitrary Java predicates or NBT paths. Preserve saved scan keys when changing an already released addon's scan groups.

## Recipes

The quick-start recipe, `data/myaddon/recipe/diamond.json`:

```json
{
  "type": "thaumcraft:arcane",
  "research": "myaddon:introduction",
  "vis": {"aer": 5},
  "pattern": [" I ", "IDI", " I "],
  "key": {
    "I": {"item": "minecraft:iron_ingot"},
    "D": {"item": "minecraft:diamond"}
  },
  "result": {"id": "minecraft:diamond", "count": 2}
}
```

`vis.aer: 5` means **5 vis = 500 centivis** before discounts. This is a recipe-format compatibility exception: `RecipeDisplay.vis()` and `WandApi` use centivis. Arcane recipes accept only primals, 1–32767 whole vis per aspect.

| `type` | Additional fields |
| --- | --- |
| `thaumcraft:arcane` | `research`, `vis`, `pattern`, `key`, `result` |
| `thaumcraft:arcane_shapeless` | `research`, `vis`, `ingredients` (1–9 Ingredient), `result` |
| `thaumcraft:crucible` | `research`, `catalyst` (Ingredient), `aspects` (essentia), `result` |
| `thaumcraft:infusion` | `research`, `input` (central Ingredient), `components` (1–64 Ingredient), `aspects`, `instability` (0–25), `result` |
| `thaumcraft:infernal_bonus` | `ingredient`, `result`, optional `base_count` (0–64, default 0); an infernal furnace bonus, not guaranteed ordinary output |

Ingredients accept `item` or `tag`. Results use the 1.21.1 ItemStack format: `id`, optional `count`/`components`. Essentia `aspects` amounts are integers 1–32767. Crucible recipes require a nonempty aspect map. Reference an existing research key; appearing in a book or viewer does not authorize crafting.

An infusion example using vanilla items, for its own recipe file:

```json
{
  "type": "thaumcraft:infusion",
  "research": "myaddon:introduction",
  "input": {"item": "minecraft:iron_ingot"},
  "components": [{"item": "minecraft:diamond"}, {"item": "minecraft:quartz"}],
  "aspects": {"ordo": 8, "vitreus": 4},
  "instability": 2,
  "result": {"id": "minecraft:gold_ingot", "count": 1}
}
```

Core and AOP:TC4 Compact discover standard supported recipes automatically. Java contracts `IArcaneRecipe`, `CrucibleRecipe` and `InfusionRecipe` support custom serializers, which still need normal RecipeType/RecipeSerializer registration and connection to the appropriate gameplay handler. `RecipeDisplays.register` alone adds a display, not a crafting mechanic.

## Reload and compatibility

`/reload` rereads `data/`, validating references, page types, collisions and parent cycles. Successful catalogs synchronize to clients. A failure in this catalog retains its previous working snapshot; this does not promise rollback of every independent Minecraft or other mod reload listener. A failed initial load does not produce a valid catalog automatically.

To change a datapack definition, override the **same resource path** from a higher-priority pack. Different files declaring one category/page/research ID cause a collision. JSON does not override a Java book registration with the same ID; Java entries are initial definitions, not duplicate fallbacks. Aspect assignments have separate merge rules: repeated `entries` keys replace their maps in file loading order, rather than summing them.

Keep research, aspect and material keys and numeric upgrade IDs stable between releases. Removing a research definition leaves its saved knowledge key intact. Renaming a key does not migrate progress. Forge 1.7.10 addon binaries require porting their Java, registrations, recipes and persistence; they are not loaded automatically.
