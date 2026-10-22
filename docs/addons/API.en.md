# Java API reference

[Contents](README.en.md) | [Quick start](QUICKSTART.en.md) | [JSON formats](CONTENT.en.md) | [Русский](API.ru.md)

Reference for public contracts in API **1**, core **1.0.0**. Full signatures are included in the SDK's `lib/thaumcraft-api-1.0.0-sources.jar`. All Thaumcraft classes used below belong to `thaumcraft.api`; addons do not need core implementation imports.

## Lifecycle and events

| Stage / bus | Addon action |
| --- | --- |
| Mod constructor / mod bus | Attach item, block and BE DeferredRegisters and RegisterCapabilitiesEvent listener |
| Mod constructor / `NeoForge.EVENT_BUS` | Subscribe to API events |
| `AddonRegistrationEvent` / `AddonRegistrationEvent.BUS`, once | Register aspects, Java book definitions, PageTypes, materials, upgrades, FocusBehaviors, WandVisuals and RecipeDisplays |
| `FMLClientSetupEvent` / mod bus, `Dist.CLIENT` | Register BookRenderers through `enqueueWork` |
| Successful data loading/synchronization | Read fresh catalogs and invalidate addon caches |
| Gameplay operation / server thread | Validate knowledge, inventory and world; mutate resources and persistent state |

The core freezes **AddonRegistry** after `AddonRegistrationEvent`: `category`, `research` and `page` reject subsequent registrations. Populate other extension registries once at startup too, rather than on each `/reload`. Duplicate IDs/handlers throw. Register identical common definitions on client and dedicated server; isolate UI classes on the client.

| Game bus event | Data and usage |
| --- | --- |
| `CatalogChangedEvent` | `revision()`, `client()`, `server(): Optional<MinecraftServer>`; server catalog update or client sync/login/disconnect invalidation |
| `KnowledgeChangedEvent` | `player(): UUID`, `knowledge(): PlayerKnowledge`, `client(): boolean`; defensive knowledge snapshot |

Revisions are local rather than persistent global IDs. Refresh stale snapshots after update events and do not mutate the world from background work. Each event `knowledge()` call returns a new copy.

`install(...)`, `ResearchApi.accept(...)` and `AddonRegistry.freeze()` are core adapter/synchronization plumbing. Addons must not call them or replace core services. Do not access installed services from static initializers before core initialization.

## Book registration

| API | Purpose |
| --- | --- |
| `AddonRegistry.API_VERSION` | `1`; public contract version |
| `AddonRegistry.category(BookCategory)` | Java category definition |
| `AddonRegistry.research(ResearchDefinition)` | Java research definition |
| `AddonRegistry.page(BookPage)` | Java page definition |
| `PageTypes.register(ResourceLocation, Consumer<JsonObject>)` | Common validator for a custom page's `data` |
| `BookRenderers.register(ResourceLocation, BookPageRenderer)` | Client renderer for that same type ID |
| `BookNavigation.open(String)` | Client request to open the real server-authorized book |

`BookCategory` takes `(id, name, icon, background, order, requiredResearch)`. `BookPage` takes `(id, type, List<Element>, JsonObject data)` and returns a deep copy from `data()`. `BookCatalog` contains immutable category/page maps. `ResearchDefinition` is immutable and retains shorter compatibility constructors; JSON is usually more convenient than its long canonical constructor. See [CONTENT.en.md](CONTENT.en.md) for fields and references.

## Custom page

Common type registration; call `MeterPageType.register()` from `AddonRegistrationEvent`:

```java
// File: example/myaddon/MeterPageType.java
package example.myaddon;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.book.PageTypes;

public final class MeterPageType {
    public static void register() {
        PageTypes.register(ResourceLocation.parse("myaddon:meter"), data -> {
            if (!data.has("label") || !data.get("label").isJsonPrimitive()
                    || !data.getAsJsonPrimitive("label").isString()) {
                throw new IllegalArgumentException("myaddon:meter requires a string label");
            }
        });
    }
}
```

Page file, `data/myaddon/thaumcraft/meter.json`:

```json
{
  "kind": "book_page",
  "id": "myaddon:meter_page",
  "type": "myaddon:meter",
  "data": {"label": "page.myaddon.meter"}
}
```

Add `myaddon:meter_page` to research `page_order` and provide the `page.myaddon.meter` translation. Client class:

```java
// File: example/myaddon/MeterClient.java
package example.myaddon;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.api.book.BookPage;
import thaumcraft.api.client.BookPageRenderer;
import thaumcraft.api.client.BookRenderContext;
import thaumcraft.api.client.BookRenderers;

@EventBusSubscriber(modid = "myaddon", value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class MeterClient {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BookRenderers.register(
            ResourceLocation.parse("myaddon:meter"), new BookPageRenderer() {
                @Override
                public void render(BookPage page, BookRenderContext c) {
                    var label = Component.translatable(
                        page.data().get("label").getAsString());
                    c.graphics().drawWordWrap(c.font(), label,
                        c.x(), c.y(), 139, 0xff403020);
                    c.graphics().fill(c.x(), c.y() + 50,
                        c.x() + 120, c.y() + 59, 0xff403020);
                    c.graphics().fill(c.x() + 1, c.y() + 51,
                        c.x() + 81, c.y() + 58, 0xffdca73a);
                    c.link(Component.translatable("tc.research_name.myaddon:introduction"),
                        "myaddon:introduction", 0, 75);
                }
            }));
    }
}
```

The renderer owns the entire page type. Lay out content within 139×199, or 174 pixels of content height on the first page. Automatic flow pagination belongs to `thaumcraft:layout`, not arbitrary renderers.

`BookRenderContext.item`, `aspect`, `recipe`, `link`, `tooltip` and `image` take **page-local** coordinates. Direct `graphics()` calls add `c.x()`/`c.y()` exactly once, as above. Context helpers retain native tooltips and links. Do not multiply coordinates by GUI scale; the book already transforms the page.

Override `mouseMoved(BookPage,double x,double y)` and `mouseClicked(BookPage,double x,double y,int button)` for interactions. Coordinates are page-local after inverse scaling; clicks return `true` when handled. Request resource mutations through a validated addon-owned server packet, rather than performing them in the renderer.

## Knowledge and research navigation

| Method | Contract |
| --- | --- |
| `ResearchApi.knowledge(ServerPlayer)` | Defensive server knowledge snapshot |
| `ResearchApi.knows(ServerPlayer, String)` | Completed key check; empty requirement is satisfied |
| `ResearchApi.grant(ServerPlayer, String)` | Complete existing research through core persistence/synchronization; server thread only; unknown keys throw |
| `ResearchApi.definitions()`, `books()` | Latest synchronized client definitions/catalog |
| `ResearchApi.clientKnowledge()` | Defensive local client knowledge snapshot |
| `ResearchNavigation.resolve(key, definitions, books, knowledge)` | Pure link resolution without progress mutation |

`grant` is a privileged grant operation, not a learning-condition check: the addon must validate its reason, prerequisites and authorization. Never feed it an unchecked client packet key. Calling `completeResearch`, `discover`, `addPool` or `setWarp` on a copied `PlayerKnowledge` does not persist anything. API 1 has no generic public operation to mutate arbitrary pools/scans/warp.

Check aspect discovery with `knowledge.knows(aspect)`, not `pool(aspect) > 0`: discovered aspects remain known at zero points. Primals are initially known.

Navigation returns `Target(requested, focus, reason)`:

| `Reason` | Meaning |
| --- | --- |
| `KNOWN` | Research completed |
| `AVAILABLE` | Requested available node can be highlighted |
| `PREREQUISITES` | Nearest available ancestor/category requirement found |
| `UNDISCOVERED` | Discovery/trigger still needed; do not reveal the hidden node |
| `MISSING` | Unknown key |

Use client `BookNavigation.open(key)` for the core's actual navigation path. The server checks the research key and a Thaumonomicon in the inventory. Completed research opens a page; available research/ancestors are centered and highlighted; trigger-locked research gives a hint. Do not assume every result has a usable `focus`, or grant knowledge merely to open UI.

## Aspects and composition

`Aspect.registerCompound(ResourceLocation id, int color, ResourceLocation icon, String translationKey, Aspect first, Aspect second)` registers a compound aspect. `icon` is required and `color` is RGB. Use an addon-specific namespace; replacing primals is unsupported. Components must already exist. Avoid competing aspects with the same component pair when relying on unambiguous `Aspect.combine`, which returns the first match.

| Method | Result |
| --- | --- |
| `Aspect.getAspect(String)` | Aspect or `null`; supports legacy tags |
| `Aspect.all()` | Shared registry; do not mutate |
| `getTag()`, `getColor()`, `getComponents()`, `texture()`, `translationKey()` | Stable tag, color, components and resources |
| `AspectApi.aspects(Level, ItemStack)` | Effective stack composition including components |
| `AspectApi.aspects(Level, BlockState)` | Block composition |
| `AspectApi.aspects(Entity)` | Entity composition including native variants |
| `AspectApi.scanned(ItemStack)` | **Local client** scan check with grouping, not authorization for an arbitrary server player |
| `AspectApi.variants()` | Copies of filled phials, jars, crystals and essence pieces for indexing |

`Aspect.getName()` derives a name from the tag; it is not a general addon translation mechanism. In client interfaces, use `translationKey()` when present. `aspects(...)` returns actual composition without automatically hiding unknown knowledge: apply discovery/scanning checks before displaying it. Variant counts are dynamic; do not hardcode 240, since custom aspects change the count.

## Essentia and capabilities

Legacy wood classification is exposed as `BlockMaterials.WOOD` (`thaumcraft:materials/wood`). Use `state.is(BlockMaterials.WOOD)`; contribute original wooden blocks through the shared block tag with `replace:false`. This reloadable, synchronized material classification differs from the broader axe mining family. The core supplies pinned vanilla/core counterparts; addons and datapacks may extend it for newer wood families.

| Contract | Purpose |
| --- | --- |
| `IAspectContainer` | Read, accept and remove whole essentia units |
| `IAspectContainer.getJarType()` | Concrete native jar identity: `NORMAL`, `VOID`, or default `NONE`; forwarding containers remain `NONE` |
| `IAspectItem.getAspects(stack)` | Independent snapshot of actual item contents, distinct from research composition; native empty jars return null |
| `IEssentiaJarItem.setAspects(stack, contents)` | Write real portable jar contents, preserving its label and unrelated components; native placement consumes the same data |
| `IEssentiaTransport` | Connections, input/output directions, suction and quantities |
| `IAspectSource` | Marker for a container accessible to nearby rituals |
| `EssentiaCapabilities.CONTAINER` | Container capability, `Void` context |
| `EssentiaCapabilities.TRANSPORT` | Sided transport capability, `Direction` context |
| `EssentiaCapabilities.SOURCE` | Explicitly exposed ritual essentia source |
| `EssentiaStore(capacity, mixed, voidOverflow)` | Bounded storage; mixed + voidOverflow is invalid |

Use `RegisterCapabilitiesEvent` on the mod bus and `registerBlockEntity` with the addon block entity type and chosen capability; the SDK's `ExampleAddon.capabilities` is a working example. Follow normal NeoForge capability invalidation when availability/the exposed object changes.

**Return values differ:** `container.addToContainer(aspect, n)` returns the **unaccepted remainder**; `transport.addEssentia(aspect, n, face)` returns **accepted units**; `takeEssentia` returns actual units extracted. Do not debit requested units when the destination accepted fewer. A `null` transport direction denotes the internal view. Operations belong on the server thread; the transport implementation must honor `canInputFrom`, `canOutputTo` and suction.

For ordinary storage, start with `new EssentiaStore(64, false, false)`: at most 64 units of one aspect. It neither persists itself nor calls `setChanged()`. After mutations, persist contents, filter and other BE parameters; synchronize the necessary client display state. Validate unknown aspects, negative quantities and overflow when reading NBT. `voidOverflow` intentionally destroys excess and reports it accepted; it is unsuitable for ordinary storage.

The SDK's `DeviceEntity` implements every transport method, a 64-unit limit, save and load. Expose `SOURCE` only when the device should supply rituals without pipes: implementing a Java interface alone does not expose it to every capability consumer.

## Vis and networks

**100 centivis = 1 vis.** Essentia and research points are distinct resources and are not converted by multiplying by 100.

| Value | Unit |
| --- | --- |
| `WandApi.stored/capacity/set/quote/consume` | centivis |
| `ItemFocusBasic.getVisCost` | centivis |
| `VisNetHandler.drainVis`, `TileVisNode.consumeVis` | centivis; may return partial supply |
| `RecipeDisplay.vis` | centivis |
| `WandRod` capacity, `IArcaneRecipe.vis`, arcane JSON `vis` | Whole vis, retaining the legacy contract |

Pass base centivis to `WandApi.consume` without applying discounts first: `quote` and `consume` already include caps, armor and frugal. Otherwise discounts are applied twice. `centivis(int wholeVis)` checks multiplication overflow.

`WandApi.kind(stack)` distinguishes `NONE`, `WAND`, `STAFF` and `SCEPTRE`; `parts(stack)` reads the saved rod/cap identities. `quoteCrafting(wand, player, wholeVis)` accepts **whole vis** and returns centivis after cap, sceptre and optional equipment discounts, without focus frugal. A null player represents unattended crafting. `consumeCrafting(level, wand, player, wholeVis, commit)` checks the complete quote before debiting, requires the server thread when committing and supports simulation. Device-specific staff/cap restrictions remain the caller's responsibility.

`ParticleApi.blockRunes(level, x, y, z, red, green, blue, duration, gravity)` reuses the native TC4 block-rune particle, including its three-times-duration lifetime. Like other original client-proxy effects it does not send a new network packet; dedicated servers do nothing.

```java
// File: example/myaddon/VisPayment.java
package example.myaddon;

import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.api.wands.WandApi;

public final class VisPayment {
    public static boolean pay(ServerPlayer player, ItemStack wand) {
        // Server thread, after validating the item, knowledge and target.
        var base = Map.of(Aspects.AIR, WandApi.centivis(2));
        return WandApi.consume(wand, player, base, true);
    }
}
```

`consume(..., false)` checks affordability only. `true` debits the entire quoted map when all aspect balances suffice. This does not reserve world/items for a later operation. Validate feasibility first; do not charge for a known invalid target. `set` sets a wand balance rather than providing general machine storage. After modifying an inventory stack, perform normal inventory/menu updates.

A device may extend `TileVisNode`, implement `getRange()`/`isSource()` and call `consumeVis(primal, requestedCentivis)` on its server tick. A source returning `isSource() == true` must implement `consumeVis` by debiting bounded persistent reserves and returning only actual supply. Ordinary positional consumers can call `VisNetHandler.drainVis(level, pos, aspect, amount)`.

The network considers loaded nodes, visibility and attunement without forcing chunk loads. Do not call `addNode` every tick. Call `super` when overriding `onLoad`, `setRemoved`, `onChunkUnloaded`, `saveAdditional` and `loadAdditional`. Save addon-specific charge separately; the example caps it at 1000 centivis. Use `VisNetHandler.invalidate(level)` for topology changes.

`drainVis` returns actual supply from 0 to requested. Multiple aspect calls are not an atomic payment: accumulate partial supply in a persistent device buffer, then pay for the operation from that buffer when necessary. Client calls must not be treated as resource acquisition.

## Wand materials and models

During `AddonRegistrationEvent`, use namespaced constructors:

```text
WandRod(ResourceLocation id, String research, int capacityWholeVis,
        Supplier<ItemStack> item, int craftCost, BiConsumer<ItemStack,Player> update)
WandCap(ResourceLocation id, String research, float modifier,
        Set<Aspect> special, float specialModifier, Supplier<ItemStack> item, int craftCost)
```

Construction registers the material. Register its item normally first; the supplier returns the assembly component. Reference existing research. `craftCost` is the legacy assembly-cost coefficient, not stored centivis. Cap modifiers are multipliers: `.95f` means 95% of base cost. These materials participate in actual wand assembly and vis usage.

`WandApi.parts(stack, new WandParts("myaddon:rod", "myaddon:cap"))` writes the normal data component. `WandApi.focus(wand)` returns a copy; `focus(wand, focusStack)` stores a copy. Mutating a returned copy alone does not change the wand. The gameplay operation must enforce its restrictions, synchronize changes and preserve other stack components.

For each supported combination, register `WandVisuals.register(kind, parts, modelId)` with kind `wand`, `staff` or `sceptre`. Model ID `myaddon:item/wand` resolves to `assets/myaddon/models/item/wand.json`, without `.json` in the ID. Missing custom models fall back to native appearance. Registering ResourceLocations does not require client-class imports. `StaffRod` retains the legacy `_staff` suffix; account for its resulting tag/research key during porting instead of renaming saved identities.

## Foci and upgrades

Extend `ItemFocusBasic` and bind its server behavior/attached model with `FocusBehaviors.register(item, behavior, attachedModel)`. Register this item through DeferredRegister, then call `PulseFocus.registerBehavior(item)` once from `AddonRegistrationEvent`:

```java
// File: example/myaddon/PulseFocus.java
package example.myaddon;

import java.util.List;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.api.wands.*;

public final class PulseFocus extends ItemFocusBasic {
    @Override public Map<Aspect, Integer> getVisCost(ItemStack stack) {
        return Map.of(Aspects.AIR, 10); // 0.1 vis per successful cast before discounts.
    }
    @Override public int getActivationCooldown(ItemStack stack) { return 100; }
    @Override public boolean isVisCostPerTick(ItemStack stack) { return true; }
    @Override public List<FocusUpgradeType> getPossibleUpgradesByRank(ItemStack stack, int rank) {
        return List.of(FocusUpgradeType.frugal);
    }
    public static void registerBehavior(PulseFocus item) {
        FocusBehaviors.register(item, new FocusBehavior() {
            @Override public boolean continuous(ItemStack focus) { return true; }
            @Override public boolean cast(ServerPlayer player, ItemStack wand, ItemStack focus) {
                player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getEyeY(), player.getZ(), 4, .3, .3, .3, .02);
                return true;
            }
        }, ResourceLocation.parse("myaddon:item/focus_attached"));
    }
}
```

Supply `assets/myaddon/models/item/focus_attached.json` and an ordinary item model. Attached head coordinates use x/z center 8 and y≈18–22; the core adds the staff offset. Working models are included in the SDK.

| Callback | Behavior |
| --- | --- |
| `continuous(ItemStack)` | Defaults to `false`; `true` enables held use |
| `begin(ServerPlayer, wand, focus, hand)` | Begin; `false` rejects use; do not charge the same cost again here |
| `cast(ServerPlayer, wand, focus)` | Effect attempt after core cooldown checking and vis reservation |
| `end(ServerPlayer, wand, focus)` | End, including interrupted use; release temporary state |

`getActivationCooldown` uses **milliseconds**, not ticks. Continuous use is attempted on ticks with this cooldown. `isVisCostPerTick` describes the cost/tooltip; actual held behavior is selected by `continuous`. `cast == true` retains the payment, while `false` or an exception refunds reserved vis. A refund does not undo addon-created blocks/entities: validate before effects and avoid exceptions after partial actions. Do not charge again or change wand materials inside the callback.

Register an upgrade with `FocusUpgradeType.register(int id, ResourceLocation key, ResourceLocation icon, Map<Aspect,Integer> aspects)`. IDs range 0–32767; native 0–20 are occupied. The example uses 24000, which is not a globally reserved addon range. Choose a unique persistent ID and namespaced key; duplicate numbers/keys throw. Translations use `focus.upgrade.myaddon:pulse.name` and `.text`. Expose it through `getPossibleUpgradesByRank`/`canApplyUpgrade`; read levels with `getUpgradeLevel`. The focus behavior must implement the custom effect; registering the type alone does not create one.

## Recipes and viewer-independent integration

`RecipeDisplays.register(ResourceLocation providerId, RecipeDisplays.Provider)` registers `Collection<RecipeDisplay> recipes(Level level)`. Providers read actual recipes/Level state without mutating the world or granting resources, and operate on both logical sides. Results are cached per Level; call `RecipeDisplays.invalidate()` after addon-specific external catalog changes. Core data reload already invalidates this cache.

| `RecipeDisplay` field | Contract |
| --- | --- |
| `id` | Stable, globally unique ResourceLocation for the **display variant** |
| `recipeId` | Actual recipe ID for server transfer validation |
| `kind` | `ARCANE`, `CRUCIBLE`, `INFUSION`, `INFERNAL`, `DISTILLATION`, `CENTRIFUGE`, `CRYSTALLIZATION` |
| `research` | Required research keys |
| `inputs` | Ingredients; ARCANE has 9 positions including `Ingredient.EMPTY`; INFUSION starts with the central input, then components |
| `outputs` | Actual ItemStacks including counts/components; copies returned |
| `essentia`, `aspectOutputs` | Whole input/output aspect units, keyed by tag |
| `vis` | Base **centivis**, unlike arcane JSON units |
| `instability`, `experience` | Instability and experience levels |
| `catalysts` | Category workstations; copies returned |
| `notes` | Components describing probability and dynamic-example limitations |

Variants may share `recipeId`, but never `id`. A display provider does not register crafting logic or guarantee support for transferring an arbitrary custom RecipeType through “+”. The core already publishes ordinary Thaumcraft recipes; do not publish them twice.

AOP:TC4 Compact is the separate optional `aoptc4-compact-0.7.0.jar`, built on these contracts. It supports JEI `[19.8.4.110,20)` and EMI `[1.1.13,1.2)` for MC 1.21.1; both installed selects EMI. Content addons should not import viewer addon internals or bundle viewers. Avoid revealing unknown aspect names/composition or unscanned sources in the addon interface. Display settings do not authorize server crafting.

`WorkbenchApi.menuClass()` and `menuType()` expose actual menu identity without core imports. For a custom client adapter, the core already registers `ArcaneTransferRequest(menuId, stateId, displayId, maximum)`, which can be sent through normal `PacketDistributor.sendToServer`. Obtain the menu ID/`getStateId()` from the current menu and display ID from the catalog; do not send an invented ingredient list.

The server validates menu/state, recipe, research, ingredients and capacity, plans on copies, then applies a successful plan as a whole. Rejection changes nothing; previous grid items are returned/reused without dropping them. Transfer only fills the grid: it neither spends vis, crafts nor creates output. EMI automatic crafting destinations “cursor/inventory” are unsupported. `TransferPlanner.plan(...)` is a generic copy-based planner, not a complete network/gameplay handler for a custom menu.

## Additional contracts and API boundaries

| API | Purpose |
| --- | --- |
| `IVisDiscountGear.getVisDiscount(stack, player, aspect)` | Native equipment vis discount |
| `IRunicArmor.getRunicCharge(stack)` | Equipment runic charge |
| `IWarpingGear.getWarp(stack, player)` | Equipment warp |
| `IRepairable` | Native Repair enchantment support |
| `IInfusionStabiliser.canStabaliseInfusion(level, pos)` | Infusion stabilizer; legacy method spelling retained |
| `ThaumcraftApi.addLootBagItem(stack, weight, int... bagTypes)` | Loot bag entry; 0/1/2 are common/uncommon/rare; omitted types select common |

The API neither loads old binaries nor reproduces all of Forge 1.7.10. There is no public server operation for adding arbitrary scans, revealing arbitrary trigger nodes, changing arbitrary warp/pools, or reserving multiple network vis aspects atomically. Use available contracts; when a new extension point is required, add it to the API with an implementation and checks rather than shipping an addon linked to private core services.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Registration never fires | Subscribe on game bus in constructor; dependency ordered AFTER thaumcraft |
| Duplicate identifier | Repeated Java call, Java+JSON definition or duplicate keys across files |
| Unknown page/type/parent | Spelling, namespace, `page_order`, PageTypes registration on both sides |
| Dedicated server loads a client class | Remove client imports from common code and isolate through Dist.CLIENT |
| Granted progress disappears | A knowledge snapshot was modified instead of using server `grant` |
| Cost differs by 100× | Whole-vis recipe/rod values versus Java centivis |
| Essentia disappears | Accepted/remainder values reversed, BE not saved, or voidOverflow enabled |
| Duplicate viewer categories/recipes | Repeated providers, duplicate display IDs, or bypassing EMI selection |
| Phial/jar loses its aspect | Stack components lost during copying, saving or subtype comparison |
| UI remains stale after `/reload` | Catalog event not handled or the addon cache not invalidated |

Before distribution, compile against the API without core classes, check that the addon JAR contains no bundled API, then follow the [game checklist](CHECKLIST.en.md), including device save/unload and lossless transfer rejection. Compilation and actual world behavior are separate checks.

The gameplay contracts added for addon parity are documented in [GAMEPLAY_API.ru.md](GAMEPLAY_API.ru.md): live golem control, read-only aspect snapshots, brain/deconstruction/ear/bore device views, UUID research sharing, real infernal furnace nozzle transport, the live portable-hole blacklist, FXBurst particles and custom focus selector grouping. These contracts use native device state; callers must perform mutations on the server thread. Rebuild the current core/API even when an older JAR has the same version number.

`LivingAttackAttemptEvent.BUS` exposes the original pre-invulnerability timing at the entry of `LivingEntity.hurt`. It reports an attempt, including one later rejected by the entity; cancellation returns false from hurt. It runs on the invoking game side/thread, independently of NeoForge's later incoming-damage event. It does not synthesize additional attempts or intercept overrides that never call the base method. `AspectList.remove(aspect,amount)` deletes exhausted types, whereas `reduce` retains discovered zero entries; `remove(aspect)` removes a type directly. The modern empty-array behavior of `getAspects()` is unchanged.

`IEssentiaContainerItem` opts an addon item into the original positive contained-essentia aspect bonuses, added to ordinary object aspects for scanning/processing. Its setter may be a no-op for immutable fruit. The read-only `IAspectItem` inspection interface alone does not opt in. Existing native phial/crystal/wisp rules remain unchanged and are not duplicated.

`world.BlockEntityMovementApi` provides identity-preserving server-thread detach/relocate for moving devices. It uses already loaded full chunks, native ticker/listener bookkeeping, capability invalidation and destination synchronization. The caller owns block-state changes, movement eligibility and optional integration callbacks. Detach before source onRemove can drop inventory; relocate after placing a compatible destination block replaces its fresh placeholder with the same object. No NBT reconstruction or forced chunk load occurs. The verification suite without ComputerCraft covers native movement, menus, capabilities and same/cross-chunk behavior. Actual owner-chunk unload and restart require separate acceptance scenarios.

`world.ChunkPreparationApi.prepare(owner, columns)` creates an exact, deduplicated, owner-bound temporary FULL request. Retain the request between ticks and poll `advance(coldBudget)`; it schedules native region tickets without joining generation futures. Close after completion or cancellation. The core also closes requests when the registered owner is removed, changes position, stops block ticking, or its level/server closes. FULL tickets do not force block/entity ticking. Every mutation requires the server thread. `world.IMovableBlockEntity` supplies optional `prepareToMove`/`doneMoving` passenger callbacks; the mover/integration decides eligibility and the meaning of a false preparation result.


### Variant object aspects and infusion pedestal inventories

IObjectAspectItem provides intrinsic minimum object/scanning aspects for a real ItemStack, a stable scanVariant suffix and inventoryScanStack that strips incidental NBT while preserving the actual variant component. This is distinct from contained essentia. StackAspectRules merges its minima with existing data-pack rules on server and client; ordinary items retain their existing scan identity. Implementations must return independent, read-only values and perform no world mutation.

IInfusionPedestal extends the standard Container contract and identifies the native infusion pedestal; displayed contents are slot zero. PedestalBlockEntity implements it directly. Addons should use the actual inventory rather than its implementation class.


`ResearchApi.completeAspect(player, aspect, shortAmount)` exposes TC4 completeAspect semantics: discover the aspect without checking its components and **set** its pool to the supplied signed short. The core commits and synchronizes the change on the owning server thread. Null aspect is a no-op. This does not grant research entries; the maxresearch debug command uses 99 for every registered aspect.
