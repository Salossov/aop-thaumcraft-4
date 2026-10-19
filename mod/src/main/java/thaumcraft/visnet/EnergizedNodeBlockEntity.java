package thaumcraft.visnet;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.api.visnet.*;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.registry.R3Content;

public final class EnergizedNodeBlockEntity extends TileVisNode implements thaumcraft.api.aspects.IAspectDisplay {
    private final Map<String,Integer> aura = new LinkedHashMap<>(), capacity = new LinkedHashMap<>(), available = new LinkedHashMap<>();
    private UUID id = UUID.randomUUID();
    private NodeState.Type nodeType = NodeState.Type.NORMAL;
    private NodeState.Modifier modifier = NodeState.Modifier.NONE;
    private long budgetTick = Long.MIN_VALUE;
    public EnergizedNodeBlockEntity(BlockPos pos, BlockState state) {
        super(R3Content.ENERGIZED_NODE_ENTITY.get(), pos, state);
        // Original internal block starts with 20 of each primal; conversion replaces these.
        for(var tag:List.of("aer","ignis","terra","aqua","ordo","perditio"))aura.put(tag,20);
        setupNode();
    }
    @Override public int getRange() { return 8; }
    @Override public boolean isSource() { return true; }
    public Map<String,Integer> capacity() { return Collections.unmodifiableMap(capacity); }
    @Override public thaumcraft.api.aspects.AspectList displayedAspects(){return thaumcraft.api.aspects.IAspectDisplay.snapshot(capacity);}
    public Map<String,Integer> aura() { return Collections.unmodifiableMap(aura); }
    public UUID id() { return id; }
    public NodeState.Type nodeType() { return nodeType; }
    public NodeState.Modifier modifier() { return modifier; }
    public void initialize(AuraNodeBlockEntity node) {
        aura.clear(); aura.putAll(node.state.base); id = node.id(); nodeType = node.state.type; modifier = node.state.modifier;
        setupNode(); changedAndSync(); VisNetHandler.invalidate(level);
    }
    private static void reduce(Aspect aspect, int amount, Map<String,Integer> output) {
        if (aspect.isPrimal()) output.merge(aspect.getTag(), amount, Math::max);
        else for (var component : aspect.getComponents()) reduce(component, amount, output);
    }
    private void setupNode() {
        var primals = new LinkedHashMap<String,Integer>();
        aura.forEach((tag,amount) -> { var aspect = Aspect.getAspect(tag); if (aspect != null && amount > 0) reduce(aspect, amount, primals); });
        capacity.clear();
        double factor = switch (modifier) { case BRIGHT -> 1.2; case PALE -> .8; case FADING -> .5; default -> 1; };
        primals.forEach((tag,amount) -> {
            int value = (int)Math.sqrt(amount * factor);
            if (nodeType == NodeState.Type.UNSTABLE && level != null) value += level.random.nextInt(5) - 2;
            if (value > 0) capacity.put(tag, value);
        });
        budgetTick = Long.MIN_VALUE;
    }
    private void renewBudget() {
        if (level != null && budgetTick != level.getGameTime()) { available.clear(); available.putAll(capacity); budgetTick = level.getGameTime(); }
    }
    @Override public int consumeVis(Aspect aspect, int amount) {
        if (amount <= 0 || level == null || level.isClientSide) return 0;
        renewBudget(); int taken = Math.min(amount, available.getOrDefault(aspect.getTag(), 0));
        if (taken > 0) { available.merge(aspect.getTag(), -taken, Integer::sum); triggerConsumeEffect(aspect); }
        return taken;
    }
    public void serverTick() {
        if (level == null || level.isClientSide) return;
        if (nodeType == NodeState.Type.UNSTABLE && level.random.nextInt(500) == 1) { setupNode(); changedAndSync(); }
        renewBudget(); VisNetHandler.refresh(level);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup); tag.putUUID("NodeId", id); tag.putString("NodeType", nodeType.name()); tag.putString("Modifier", modifier.name());
        var base = new CompoundTag(); aura.forEach(base::putInt); tag.put("Aura", base);
        var supply = new CompoundTag(); capacity.forEach(supply::putInt); tag.put("Capacity", supply);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup); if (tag.hasUUID("NodeId")) id = tag.getUUID("NodeId");
        try { nodeType = NodeState.Type.valueOf(tag.getString("NodeType")); modifier = NodeState.Modifier.valueOf(tag.getString("Modifier")); } catch (IllegalArgumentException ignored) { nodeType = NodeState.Type.NORMAL; modifier = NodeState.Modifier.NONE; }
        aura.clear(); var base = tag.getCompound("Aura"); for (var key : base.getAllKeys()) if (base.getInt(key) > 0) aura.put(key, base.getInt(key));
        capacity.clear(); var supply = tag.getCompound("Capacity"); for (var key : supply.getAllKeys()) if (supply.getInt(key) > 0) capacity.put(key, supply.getInt(key));
        budgetTick = Long.MIN_VALUE;
    }
}
