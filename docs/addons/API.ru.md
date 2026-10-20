# Справочник Java API

[К оглавлению](README.ru.md) | [Быстрый старт](QUICKSTART.ru.md) | [Форматы JSON](CONTENT.ru.md) | [English](API.en.md)

Контракты ниже сверены с API **1**, ядро **1.0.0**. Полные сигнатуры доступны в `lib/thaumcraft-api-1.0.0-sources.jar` SDK. Все приведённые классы Thaumcraft находятся в `thaumcraft.api`; аддону не нужны импорты реализации ядра.

## Жизненный цикл и события

| Когда / шина | Действие аддона |
| --- | --- |
| Конструктор мода / mod bus | Подключить DeferredRegister предметов, блоков, BE и обработчик RegisterCapabilitiesEvent |
| Конструктор мода / `NeoForge.EVENT_BUS` | Подписаться на события API |
| `AddonRegistrationEvent` / game bus, один раз | Зарегистрировать аспекты, Java-определения книги, PageTypes, материалы, улучшения, FocusBehaviors, WandVisuals, RecipeDisplays |
| `FMLClientSetupEvent` / mod bus, `Dist.CLIENT` | В `enqueueWork` зарегистрировать BookRenderers |
| Успешная загрузка данных, синхронизация | Получать свежий каталог, сбрасывать собственные кеши |
| Игровое действие / серверный поток | Проверять знания, инвентарь и мир; изменять ресурсы и сохраняемое состояние |

После `AddonRegistrationEvent` ядро закрывает **AddonRegistry**: `category`, `research`, `page` больше не принимаются. Остальные реестры также наполняйте один раз при запуске, не при каждом `/reload`. Повторная регистрация ID/обработчика вызывает ошибку. Регистрируйте одинаковые общие определения на клиенте и выделенном сервере; UI-код держите в отдельном клиентском классе.

| Событие game bus | Доступные данные и применение |
| --- | --- |
| `CatalogChangedEvent` | `revision()`, `client()`, `server(): Optional<MinecraftServer>`; сигнал обновления серверного каталога либо клиентской синхронизации/входа/выхода |
| `KnowledgeChangedEvent` | `player(): UUID`, `knowledge(): PlayerKnowledge`, `client(): boolean`; безопасный снимок изменившихся знаний |

Ревизия локальна и не является сохраняемым глобальным ID. Не удерживайте старые снимки после события обновления и не изменяйте мир из фоновой задачи. `knowledge()` события возвращает новую копию при каждом вызове.

Методы `install(...)`, `ResearchApi.accept(...)` и `AddonRegistry.freeze()` предназначены для адаптера/синхронизации ядра. Аддон их не вызывает и не подменяет основной сервис. Не обращайтесь к установленным сервисам из статического инициализатора мода до инициализации ядра.

## Книга и регистрация данных

| API | Назначение |
| --- | --- |
| `AddonRegistry.API_VERSION` | Сейчас `1`; версия контракта |
| `AddonRegistry.category(BookCategory)` | Java-определение категории |
| `AddonRegistry.research(ResearchDefinition)` | Java-определение исследования |
| `AddonRegistry.page(BookPage)` | Java-определение страницы |
| `PageTypes.register(ResourceLocation, Consumer<JsonObject>)` | Валидатор `data` особой страницы на обеих сторонах |
| `BookRenderers.register(ResourceLocation, BookPageRenderer)` | Клиентский рендерер для того же ID типа |
| `BookNavigation.open(String)` | Клиентский запрос открытия настоящей серверной книги |

`BookCategory` принимает `(id, name, icon, background, order, requiredResearch)`. `BookPage` принимает `(id, type, List<Element>, JsonObject data)`; `data()` возвращает глубокую копию. `BookCatalog` содержит неизменяемые карты категорий/страниц. `ResearchDefinition` - неизменяемое определение с совместимыми короткими конструкторами; для большого исследования JSON обычно удобнее длинного канонического конструктора. Поля и ссылки описаны в [CONTENT.ru.md](CONTENT.ru.md).

## Особая страница

Общий код регистрации типа (вызовите `MeterPageType.register()` из `AddonRegistrationEvent`):

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

Страница `data/myaddon/thaumcraft/meter.json`:

```json
{
  "kind": "book_page",
  "id": "myaddon:meter_page",
  "type": "myaddon:meter",
  "data": {"label": "page.myaddon.meter"}
}
```

Добавьте `myaddon:meter_page` в `page_order` исследования и перевод `page.myaddon.meter`. Клиентский класс:

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

Рендерер обслуживает весь тип страницы. Он сам размещает содержимое в поле 139×199 (на первой странице доступно 174 пикселя). Автоматическая разбивка потока относится к `thaumcraft:layout`, не к произвольному рендереру.

`BookRenderContext.item`, `aspect`, `recipe`, `link`, `tooltip`, `image` принимают **локальные** координаты страницы. При прямом вызове `graphics()` добавляйте `c.x()`/`c.y()` один раз, как выше. Контекст сохраняет штатные подсказки и ссылки. Не умножайте координаты на GUI scale: книга уже применяет преобразование.

Для взаимодействий переопределите `mouseMoved(BookPage,double x,double y)` и `mouseClicked(BookPage,double x,double y,int button)`. Координаты локальные, после обратного преобразования масштаба; клик возвращает `true`, если обработан. Изменения ресурсов запрашивайте своим серверным пакетом с проверками, а не выполняйте прямо в рендерере.

## Знания и переходы к исследованиям

| Метод | Контракт |
| --- | --- |
| `ResearchApi.knowledge(ServerPlayer)` | Копия серверных знаний для чтения |
| `ResearchApi.knows(ServerPlayer, String)` | Проверка завершённого ключа; пустое требование считается выполненным |
| `ResearchApi.grant(ServerPlayer, String)` | Завершить существующее исследование и сохранить/синхронизировать через ядро; только серверный поток; неизвестный ключ вызывает исключение |
| `ResearchApi.definitions()`, `books()` | Клиентские определения и каталог последней синхронизации |
| `ResearchApi.clientKnowledge()` | Копия локальных клиентских знаний |
| `ResearchNavigation.resolve(key, definitions, books, knowledge)` | Чистый расчёт назначения ссылки без изменения прогресса |

`grant` - привилегированная операция выдачи, а не проверка условий изучения: аддон сам проверяет причину выдачи, требования и права. Не передавайте в неё непроверенный ключ из пакета клиента. Вызов `completeResearch`, `discover`, `addPool` или `setWarp` на копии `PlayerKnowledge` не сохраняет изменения. Общего публичного API произвольной мутации пулов/сканов/warp в версии 1 нет.

Обнаруженность аспекта проверяйте `knowledge.knows(aspect)`, а не `pool(aspect) > 0`: открытый аспект с нулём очков остаётся известным. Первичные аспекты известны изначально.

Результат навигации `Target(requested, focus, reason)`:

| `Reason` | Значение |
| --- | --- |
| `KNOWN` | Исследование изучено |
| `AVAILABLE` | Можно выделить запрошенный доступный узел |
| `PREREQUISITES` | Найден ближайший доступный предок/необходимое исследование категории |
| `UNDISCOVERED` | Нужен ещё не выполненный триггер/обнаружение; не раскрывайте скрытый узел |
| `MISSING` | Ключ отсутствует |

На клиенте вызывайте `BookNavigation.open(key)`, чтобы использовать настоящий маршрут ядра: сервер проверяет ключ и наличие Таумономикона в инвентаре. Изученное открывается страницей; доступное/ближайший предок центрируется и подсвечивается; закрытое триггером сопровождается подсказкой. Не считайте `focus` обязательным для каждого результата и не выдавайте знания ради открытия UI.

## Аспекты и состав предметов

`Aspect.registerCompound(ResourceLocation id, int color, ResourceLocation icon, String translationKey, Aspect first, Aspect second)` регистрирует составной аспект. `icon` обязателен, `color` - RGB. Используйте собственное пространство имён; замена первичных аспектов не поддерживается. Составные компоненты должны быть уже зарегистрированы. Не определяйте конкурирующие аспекты одной парой компонентов, если рассчитываете на однозначный `Aspect.combine`: он возвращает первое совпадение.

| Метод | Результат |
| --- | --- |
| `Aspect.getAspect(String)` | Аспект либо `null`; поддерживает старые теги |
| `Aspect.all()` | Общий реестр; не изменять |
| `getTag()`, `getColor()`, `getComponents()`, `texture()`, `translationKey()` | Постоянный тег, цвет, компоненты и ресурсы |
| `AspectApi.aspects(Level, ItemStack)` | Конечный состав стека, учитывая его компоненты |
| `AspectApi.aspects(Level, BlockState)` | Состав блока |
| `AspectApi.aspects(Entity)` | Состав экземпляра сущности с учётом штатных вариантов |
| `AspectApi.scanned(ItemStack)` | Проверка сканирования **локального клиента** с группировкой; не серверная проверка произвольного игрока |
| `AspectApi.variants()` | Копии наполненных флаконов, банок, кристаллов и сгустков для индекса просмотра |

`Aspect.getName()` возвращает имя на основе тега; это не общий механизм перевода названия аддона. В собственном клиентском UI используйте `translationKey()` при его наличии. `aspects(...)` сообщает реальный состав, а не скрывает знания автоматически: перед показом учитывайте обнаруженность и сканирование. Количество вариантов динамическое; не фиксируйте число 240 в аддоне - с новыми аспектами оно меняется.

## Эссенция и capabilities

| Контракт | Назначение |
| --- | --- |
| `IAspectContainer` | Чтение, принятие и изъятие целых единиц эссенции |
| `IEssentiaTransport` | Направления подключения, ввод/вывод, suction и количество |
| `IAspectSource` | Маркер контейнера-источника для ближайших ритуалов |
| `EssentiaCapabilities.CONTAINER` | Capability контейнера, контекст `Void` |
| `EssentiaCapabilities.TRANSPORT` | Sided capability транспорта, контекст `Direction` |
| `EssentiaCapabilities.SOURCE` | Явно открытый источник эссенции для ритуалов |
| `EssentiaStore(capacity, mixed, voidOverflow)` | Ограниченное хранилище; режим mixed + voidOverflow недопустим |

На mod bus в `RegisterCapabilitiesEvent` используйте `registerBlockEntity` с вашим BE type и нужной capability; полный рабочий код - `ExampleAddon.capabilities` в SDK. При изменении доступности/объекта capability учитывайте стандартную инвалидацию NeoForge.

**Разные возвращаемые значения:** `container.addToContainer(aspect, n)` возвращает **остаток**, `transport.addEssentia(aspect, n, face)` - **принятое количество**; `takeEssentia` - реально выданное количество. Не уменьшайте источник на запрошенное количество, если получатель принял меньше. `null` у направления транспорта означает внутреннее представление. Контракт серверный; собственный транспорт должен соблюдать `canInputFrom`, `canOutputTo` и suction.

Для обычного хранилища начните с `new EssentiaStore(64, false, false)`: максимум 64 единицы одного аспекта. Оно само не сохраняется и не вызывает `setChanged()`. После успешной мутации BE сохраняет содержимое, фильтр и иные параметры; синхронизирует клиенту только нужное отображение. Проверяйте неизвестные аспекты/отрицательные значения/переполнение при чтении NBT. `voidOverflow` намеренно уничтожает избыток и сообщает его принятым; не используйте его для обычного контейнера.

Рабочий пример `DeviceEntity` содержит все методы транспорта, ограничение 64, сохранение и загрузку. Capability `SOURCE` открывайте только если устройство должно отдавать содержимое ритуалам без труб: одной Java-реализации интерфейса недостаточно для всех потребителей capability.

## Вис и сеть

**100 centivis = 1 вис.** Эссенция и очки исследований - другие ресурсы, переводить их через 100 нельзя.

| Значение | Единицы |
| --- | --- |
| `WandApi.stored/capacity/set/quote/consume` | centivis |
| `ItemFocusBasic.getVisCost` | centivis |
| `VisNetHandler.drainVis`, `TileVisNode.consumeVis` | centivis; возможен частичный результат |
| `RecipeDisplay.vis` | centivis |
| `WandRod` capacity, `IArcaneRecipe.vis`, JSON магического рецепта `vis` | Целые вис, сохранённый старый контракт |

Стоимость из базовых centivis передавайте в `WandApi.consume` без предварительного применения скидки: `quote` и `consume` уже учитывают наконечники, броню и frugal. Иначе скидка применится дважды. Метод `centivis(int wholeVis)` проверяет переполнение умножения.

`WandApi.kind(stack)` различает `NONE`, `WAND`, `STAFF`, `SCEPTRE`; `parts(stack)` читает сохранённые материалы. `quoteCrafting(wand, player, wholeVis)` принимает **целые вис** и возвращает centivis после скидок наконечника, скипетра и экипировки, без frugal набалдашника. Null вместо игрока означает автономный крафт. `consumeCrafting(level, wand, player, wholeVis, commit)` проверяет всю стоимость до списания, поддерживает симуляцию и требует серверный поток при commit. Ограничения конкретного устройства на посохи/наконечники проверяет вызывающий код.

`ParticleApi.blockRunes(level, x, y, z, red, green, blue, duration, gravity)` использует настоящую частицу рун ядра с исходной длительностью duration × 3. Как исходный клиентский proxy, вызов не отправляет новый сетевой пакет; выделенный сервер ничего не рисует.

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
        // Вызывать после проверки предмета, знаний и цели, в серверном потоке.
        var base = Map.of(Aspects.AIR, WandApi.centivis(2));
        return WandApi.consume(wand, player, base, true);
    }
}
```

`consume(..., false)` только проверяет достаточность; `true` списывает всю рассчитанную карту, если запаса хватает по всем аспектам. Это не резервирование мира/предметов для последующей операции. Сначала проверяйте её выполнимость; не списывайте вис за заведомо недопустимую цель. `set` устанавливает запас палочки, а не создаёт универсальное хранилище устройства. После изменения инвентарного стека обеспечьте обычное обновление инвентаря/меню.

Для устройства можно наследовать `TileVisNode`, реализовать `getRange()`/`isSource()` и вызывать `consumeVis(primal, requestedCentivis)` в серверном тике. `isSource() == true` требует собственного `consumeVis`, который уменьшает ограниченный сохранённый запас и возвращает только выданное. Для обычного потребителя по позиции есть `VisNetHandler.drainVis(level, pos, aspect, amount)`.

Сеть учитывает только загруженные узлы, видимость/настройку и не загружает чанки принудительно. Не вызывайте вручную `addNode` каждый тик. При переопределении `onLoad`, `setRemoved`, `onChunkUnloaded`, `saveAdditional`, `loadAdditional` вызывайте `super`. Сохраняйте свой заряд отдельно; пример ограничивает его 1000 centivis. Для изменения топологии есть `VisNetHandler.invalidate(level)`.

`drainVis` возвращает фактическое количество 0…requested. Серия вызовов по нескольким аспектам не является атомарной оплатой: при необходимости накапливайте частичные поступления в сохраняемом буфере устройства, а затем оплачивайте операцию из него. Клиентский вызов не должен считаться получением ресурса.

## Материалы палочек и модели

В `AddonRegistrationEvent` используйте namespaced-конструкторы:

```text
WandRod(ResourceLocation id, String research, int capacityWholeVis,
        Supplier<ItemStack> item, int craftCost, BiConsumer<ItemStack,Player> update)
WandCap(ResourceLocation id, String research, float modifier,
        Set<Aspect> special, float specialModifier, Supplier<ItemStack> item, int craftCost)
```

Конструктор сам регистрирует материал. Предмет уже должен быть зарегистрирован обычным способом; поставщик возвращает компонент для сборки. Укажите существующее исследование. `craftCost` - старый коэффициент стоимости сборки, не значение запаса centivis. Модификатор наконечника - множитель: `.95f` означает 95% базовой стоимости. Эти материалы используются настоящим обработчиком сборки и расхода.

`WandApi.parts(stack, new WandParts("myaddon:rod", "myaddon:cap"))` записывает материалы в стандартный компонент данных. `WandApi.focus(wand)` возвращает копию головки, `focus(wand, focusStack)` записывает копию. Изменение полученной копии само по себе не меняет палочку. Аддон отвечает за ограничения своего игрового действия, синхронизацию и сохранение остальных компонентов стека.

Для каждой поддерживаемой комбинации задайте `WandVisuals.register(kind, parts, modelId)`, где `kind` - `wand`, `staff` или `sceptre`. `modelId` вида `myaddon:item/wand` соответствует `assets/myaddon/models/item/wand.json`, без `.json`. Без такой модели действует штатный вид. Регистрация ResourceLocation не требует импорта клиентских классов. `StaffRod` сохраняет старое добавление суффикса `_staff`; при порте учитывайте получающийся тег/ключ исследования, а не переименовывайте существующие сохранения.

## Набалдашники и улучшения

Предмет наследует `ItemFocusBasic`, а `FocusBehaviors.register(item, behavior, attachedModel)` связывает его с серверным действием и моделью на палочке. Ниже класс предмета; зарегистрируйте его через DeferredRegister, затем один раз вызовите `PulseFocus.registerBehavior(item)` в `AddonRegistrationEvent`:

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
        return Map.of(Aspects.AIR, 10); // 0.1 вис за успешный cast до скидок.
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

Добавьте модель `assets/myaddon/models/item/focus_attached.json` и обычную модель предмета. Для прикреплённой головки ориентир - центр x/z=8, высота y≈18–22 в модельных координатах; смещение посоха добавляет ядро. Готовые модели есть в SDK.

| Callback | Поведение |
| --- | --- |
| `continuous(ItemStack)` | По умолчанию `false`; `true` включает применение при удержании |
| `begin(ServerPlayer, wand, focus, hand)` | Начало; `false` запрещает применение; не место для повторного списания цены |
| `cast(ServerPlayer, wand, focus)` | Попытка эффекта после проверки задержки и резервирования стоимости ядром |
| `end(ServerPlayer, wand, focus)` | Завершение; освобождайте временное состояние и при прерывании применения |

`getActivationCooldown` измеряется **миллисекундами**, не тиками. Непрерывный режим проверяется на тиках с этой задержкой. `isVisCostPerTick` обозначает характер цены/подсказки; фактическое удержание включает `continuous`. `cast == true` оставляет оплату, `false` или исключение возвращает резерв вис. Возврат вис не откатывает уже созданные аддоном блоки/сущности: проверяйте условия до эффекта и не бросайте исключение после частичного действия. Не списывайте ту же цену повторно, не меняйте материалы палочки внутри callback.

Свой тип улучшения регистрируется через `FocusUpgradeType.register(int id, ResourceLocation key, ResourceLocation icon, Map<Aspect,Integer> aspects)`. Допустим ID 0–32767, родные 0–20 заняты. Пример использует 24000, но это не выделенный всем аддонам диапазон. Выберите уникальный постоянный ID и namespaced-ключ; коллизия числа или ключа вызывает ошибку. Переводы: `focus.upgrade.myaddon:pulse.name` и `.text`. Открывайте улучшение через `getPossibleUpgradesByRank`/`canApplyUpgrade`; уровень читается `getUpgradeLevel`. Собственный эффект улучшения реализует ваш обработчик; одно объявление нового типа его не создаёт.

## Рецепты и независимая интеграция с просмотрщиками

`RecipeDisplays.register(ResourceLocation providerId, RecipeDisplays.Provider)` регистрирует поставщика `Collection<RecipeDisplay> recipes(Level level)`. Он читает фактические рецепты/состояние Level, не меняет мир, не выдаёт ресурсы и работает на обеих логических сторонах. Результат кешируется по Level; после изменения собственного внешнего каталога вызывайте `RecipeDisplays.invalidate()`. Перезагрузка данных ядра уже очищает кеш.

| Поле `RecipeDisplay` | Контракт |
| --- | --- |
| `id` | Уникальный стабильный ResourceLocation **варианта отображения**, не дублируется у других поставщиков |
| `recipeId` | ID настоящего рецепта, используемый серверной проверкой переноса |
| `kind` | `ARCANE`, `CRUCIBLE`, `INFUSION`, `INFERNAL`, `DISTILLATION`, `CENTRIFUGE`, `CRYSTALLIZATION` |
| `research` | Список требуемых ключей исследований |
| `inputs` | Ingredient; для ARCANE 9 позиций с `Ingredient.EMPTY`, для INFUSION сначала центр, затем компоненты |
| `outputs` | Реальные ItemStack с количествами/компонентами; API возвращает копии |
| `essentia`, `aspectOutputs` | Вход и выход целых единиц аспектов, ключи - теги |
| `vis` | Базовая стоимость **centivis**, не JSON-единицы магического рецепта |
| `instability`, `experience` | Нестабильность и уровни опыта |
| `catalysts` | Устройства категории; возвращаются копии |
| `notes` | Component-пояснения, включая случайность выхода и ограничения динамического примера |

Одинаковый `recipeId` у вариантов допустим, одинаковый `id` - нет. Поставщик отображения не регистрирует рецепт, не создаёт серверный крафт и не гарантирует поддержку «+» для произвольного пользовательского RecipeType. Обычные рецепты Thaumcraft ядро добавляет само; повторно публиковать их не нужно.

AOP:TC4 Compact - отдельный необязательный JAR `aoptc4-client-compact-0.8.0.jar`, использующий эти контракты. Он поддерживает JEI `[19.8.4.110,20)` и EMI `[1.1.13,1.2)` для MC 1.21.1; оба вместе выбирают EMI. Не импортируйте его внутренние классы в контентный аддон и не включайте просмотрщики в собственный JAR. Названия/состав неизвестных аспектов и несканированные источники не следует раскрывать своим UI; настройки отображения не являются разрешением серверного крафта.

`WorkbenchApi.menuClass()` и `menuType()` дают настоящую идентичность меню без импорта класса реализации. Для собственного клиентского адаптера ядро уже регистрирует пакет `ArcaneTransferRequest(menuId, stateId, displayId, maximum)`; его можно отправить стандартным `PacketDistributor.sendToServer`. Берите `menuId`/`getStateId()` из текущего меню и ID из каталога, а не передавайте серверу собственный список предметов.

Сервер проверяет меню, его состояние, рецепт, исследования, ингредиенты и вместимость, планирует изменения на копиях и применяет успешный план целиком. Отказ ничего не меняет; старые предметы сетки возвращаются/используются без выбрасывания. Перенос заполняет сетку, не тратит вис, не крафтит и не создаёт результат. Автоматические назначения EMI «в курсор/инвентарь» не поддерживаются. `TransferPlanner.plan(...)` - общий типизированный планировщик на копиях, а не готовый сетевой или игровой обработчик своего меню.

## Другие контракты и границы API

| API | Назначение |
| --- | --- |
| `IVisDiscountGear.getVisDiscount(stack, player, aspect)` | Штатная скидка с экипировки |
| `IRunicArmor.getRunicCharge(stack)` | Рунный заряд экипировки |
| `IWarpingGear.getWarp(stack, player)` | Искажение экипировки |
| `IRepairable` | Поддержка штатного зачарования Repair |
| `IInfusionStabiliser.canStabaliseInfusion(level, pos)` | Стабилизатор инфузии; историческое написание метода сохранено |
| `ThaumcraftApi.addLootBagItem(stack, weight, int... bagTypes)` | Добавить предмет в мешочки; типы 0/1/2 - обычный/необычный/редкий, без типов - обычный |

API не является автоматическим загрузчиком старых модов или полной копией Forge API 1.7.10. Отдельных публичных серверных операций «добавить скан», «открыть произвольный триггер», «изменить любой warp/pool» и общего атомарного резервирования сети вис сейчас нет. Стройте аддон на доступных контрактах; если нужна новая точка расширения, добавляйте её в API с реализацией и проверками, а не связывайте выпускаемый аддон с приватными сервисами ядра.

## Диагностика

| Симптом | Проверить |
| --- | --- |
| Не вызывается регистрация | Подписка на game bus в конструкторе; зависимость AFTER thaumcraft |
| Duplicate identifier | Повторная Java-регистрация, Java+JSON одного ID или два файла с одним ключом |
| Unknown page/type/parent | Опечатку, namespace, `page_order`, регистрацию PageTypes на обеих сторонах |
| На сервере падает клиентский класс | Убрать клиентские импорты из общих классов, использовать Dist.CLIENT |
| Исследование «выдано», но пропадает | Не изменялась ли только копия знаний вместо серверного `grant` |
| Цена отличается в 100 раз | Целые вис JSON/стержня против centivis Java API |
| Теряется эссенция | Перепутан принятый объём и остаток; не сохранён BE; включён voidOverflow |
| Дублируются категории/рецепты просмотрщика | Повторный поставщик, одинаковые display ID, регистрация мимо механизма выбора EMI |
| Флакон/банка теряет аспект | Потеря компонентов ItemStack при копировании, сохранении или сравнении подтипов |
| После `/reload` старый UI | Не обработано событие каталога или не сброшен собственный кеш |

Для выпуска соберите аддон против API без классов реализации, проверьте JAR на отсутствие вложенного API, затем пройдите [игровой checklist](CHECKLIST.ru.md), включая сохранение/выгрузку устройств и отказ переноса без потерь. Сборка подтверждает компиляцию; поведение в мире проверяется отдельно.

Дополнительные контракты для аддонов описаны в [GAMEPLAY_API.ru.md](GAMEPLAY_API.ru.md): управление живыми големами, снимки аспектов, интерфейсы мозга/стола расщепления/уха/бура и настоящий транспорт сопел адской печи. Изменения выполняются в серверном потоке и затрагивают штатные данные устройств. Требуется пересобрать текущие ядро/API даже при совпадающем номере старого JAR.

`LivingAttackAttemptEvent.BUS` предоставляет исходный момент попытки урона в начале `LivingEntity.hurt`, до его проверок неуязвимости. Событие описывает попытку, а не успешный урон; отмена возвращает false. Оно вызывается на стороне/потоке штатного вызова отдельно от более позднего события урона NeoForge; override без вызова базового hurt не перехватывается. `AspectList.remove(aspect,amount)` удаляет исчерпанные типы, а `reduce` сохраняет нулевые записи; `remove(aspect)` удаляет тип целиком. Пустой `getAspects()` современного API по-прежнему возвращает пустой массив.

`IEssentiaContainerItem` подключает предмет аддона к исходным бонусным аспектам содержимого: положительные количества суммируются с обычными аспектами при сканировании/обработке. У неизменяемого плода setter может быть no-op. Интерфейс чтения `IAspectItem` сам по себе бонусы не включает. Штатные специальные правила фиалов/кристаллов/wisp essence не изменяются и не дублируются.

`world.BlockEntityMovementApi` переносит существующий объект BlockEntity в серверном потоке через detach/relocate, с нативными тикерами/слушателями, capabilities и синхронизацией. Чанки должны быть уже загружены; состояния блоков, допустимость пассажира и интеграционные callbacks задаёт само устройство. Источник отсоединяют до удаления блока; после установки совместимого назначения relocate заменяет свежий placeholder прежним объектом. Пересоздания из NBT нет. В текущем прогоне без CC прошли нативные проверки объекта, меню, capabilities, NBT и границы чанка; фактическая выгрузка чанка владельца и перезапуск ещё ожидают приёмки.

`world.ChunkPreparationApi.prepare(owner, columns)` создаёт временный FULL-запрос для точного набора колонок с удалением повторов. Храните запрос между тиками и опрашивайте `advance(coldBudget)`: нативные tickets готовят чанки без join ожидания генерации. Закрывайте запрос после завершения или отмены. Ядро также закрывает его при удалении/перемещении зарегистрированного владельца, прекращении тиков его чанка и закрытии мира/сервера. FULL-ticket не форсирует тики блоков и сущностей. Изменения выполняются только в серверном потоке. `world.IMovableBlockEntity` предоставляет необязательные callbacks `prepareToMove`/`doneMoving`; допустимость пассажира и значение false определяет конкретное устройство/интеграция.


### Вариантные объектные аспекты и инвентарь пьедестала

IObjectAspectItem предоставляет минимальные объектные/сканируемые аспекты настоящего ItemStack, стабильный суффикс scanVariant и inventoryScanStack, который убирает посторонний NBT, сохраняя компонент варианта. Это отдельный контракт от содержащейся эссенции. StackAspectRules объединяет минимумы с существующими данными на обеих сторонах; обычные предметы сохраняют прежнюю идентичность. Реализация не должна изменять мир и обязана отдавать независимые данные.

IInfusionPedestal расширяет стандартный Container и обозначает настоящий пьедестал инфузии; отображаемый предмет находится в слоте 0. Нативный PedestalBlockEntity реализует контракт напрямую. Аддон обращается к настоящему инвентарю через API.


`ResearchApi.completeAspect(player, aspect, shortAmount)` сохраняет семантику TC4 completeAspect: открывает аспект без проверки компонентов и **устанавливает** его запас в переданный signed short. Ядро сохраняет и синхронизирует изменение в потоке сервера. Null-аспект ничего не меняет. Метод не выдаёт исследования; отладочная команда maxresearch использует 99 для каждого зарегистрированного аспекта.
