package thaumcraft.items;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import thaumcraft.api.knowledge.PlayerKnowledge.ScanKind;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.ScanService;

public final class ThaumometerItem extends Item {
    private record Target(ScanKind kind,String key,Object identity,Map<String,Integer> aspects) {}
    private static final class Scan {Target target;int ticks;}
    private static final Map<ServerPlayer,Scan> ACTIVE=new WeakHashMap<>();
    public ThaumometerItem() { super(new Properties().stacksTo(1)); }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<net.minecraft.network.chat.Component> lines,TooltipFlag flags){lines.add(net.minecraft.network.chat.Component.translatable("thaumcraft.inventory_scanning.available").withStyle(net.minecraft.ChatFormatting.GOLD));}
    @Override public boolean shouldCauseReequipAnimation(ItemStack oldStack,ItemStack newStack,boolean slotChanged) {
        // Vis charge, scan progress and other component writes arrive as stack updates
        // every tick; only a real item/slot change should replay the raise animation.
        // Compare only the item identity (never the mutating components) and the slot.
        return slotChanged||!oldStack.is(newStack.getItem());
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity) { return 72000; }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        if(player instanceof ServerPlayer server)ACTIVE.put(server,new Scan());
        player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remaining) {
        if(level.isClientSide&&entity instanceof Player player&&remaining%2==0&&thaumcraft.client.ScannerHandRenderer.hasTarget())
            player.playSound(thaumcraft.registry.R2Sounds.CAMERA_TICKS.get(),.2f,.45f+level.random.nextFloat()*.1f);
        if(!(entity instanceof ServerPlayer player)) return;
        var scan=ACTIVE.get(player);if(scan==null)return;var current=target(player);
        if(current==null){scan.target=null;scan.ticks=0;return;}
        var original=scan.target;
        if(original==null||original.kind!=current.kind||!original.key.equals(current.key)||!original.identity.equals(current.identity)){scan.target=current;scan.ticks=0;}
        if(++scan.ticks>=20){ACTIVE.remove(player);ScanService.scan(player,current.kind,current.key,current.aspects);player.stopUsingItem();}
    }
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining) { if(entity instanceof ServerPlayer player) ACTIVE.remove(player); }
    private static Target target(ServerPlayer player) {
        var level=player.serverLevel();var eye=player.getEyePosition();var end=eye.add(player.getLookAngle().scale(10));
        var hit=player.pick(5,1,true);double max=hit.getType()==HitResult.Type.MISS?100:eye.distanceToSqr(hit.getLocation());
        Entity closest=null;
        for(var entity:level.getEntities(player,player.getBoundingBox().expandTowards(player.getLookAngle().scale(10)).inflate(0.5),e->e.isAlive()&&!e.isSpectator())) {
            var intersection=entity.getBoundingBox().inflate(0.5).clip(eye,end);
            if(intersection.isPresent()&&eye.distanceToSqr(intersection.get())<max) { max=eye.distanceToSqr(intersection.get());closest=entity; }
        }
        if(closest instanceof ItemEntity dropped) {
            var stack=dropped.getItem();return new Target(ScanKind.OBJECT,ThaumcraftData.objectScanKey(player.server,stack),dropped.getUUID(),ThaumcraftData.aspects(player.server,stack));
        }
        if(closest!=null) return new Target(ScanKind.ENTITY,ThaumcraftData.entityScanKey(player.server,closest),closest.getUUID(),ThaumcraftData.aspects(player.server,closest));
        if(hit instanceof BlockHitResult blockHit&&hit.getType()==HitResult.Type.BLOCK) {
            var pos=blockHit.getBlockPos();
            if(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) return new Target(ScanKind.PHENOMENON,"NODE"+node.id(),pos.immutable(),node.scanAspects());
            if(level.getBlockEntity(pos) instanceof thaumcraft.blocks.JarredNodeBlockEntity jar)return new Target(ScanKind.PHENOMENON,"NODE"+jar.id(),pos.immutable(),AuraNodeBlockEntity.scanAspects(jar.state));
            var state=level.getBlockState(pos);return new Target(ScanKind.OBJECT,ThaumcraftData.objectScanKey(player.server,state),pos.immutable(),ThaumcraftData.aspects(player.server,state));
        }return null;
    }
}
