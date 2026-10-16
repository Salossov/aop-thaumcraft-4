package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.items.FocusPouchItem;
import thaumcraft.entities.R5Entities;
import thaumcraft.registry.*;

/** Isolated input and shield acceptance, excluded from the distributable mod. */
final class FocusSelectionScene {
    static void register(MinecraftServer server){
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event)->{
            if(!(event.getEntity() instanceof ServerPlayer player))return;
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
            server.getPlayerList().op(player.getGameProfile());
            player.removeAllEffects();server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),"execute in minecraft:overworld run tp @s 608 118 603 0 0");
            player.getInventory().clearContent();player.getInventory().selected=0;
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));
            player.getInventory().setItem(0,new ItemStack(R2Content.WAND.get()));
            player.getInventory().setItem(9,new ItemStack(R3Equipment.FOCI.get("frost").get()));
            player.getInventory().setItem(10,new ItemStack(R3Equipment.FOCI.get("fire").get()));
            var pouch=new ItemStack(R3Equipment.FOCUS_POUCH.get());var contents=FocusPouchItem.contents(pouch);contents.set(0,new ItemStack(R3Equipment.FOCI.get("shock").get()));FocusPouchItem.store(pouch,contents);player.getInventory().setItem(2,pouch);
            player.inventoryMenu.broadcastChanges();shield(player);for(var stale:player.serverLevel().getEntitiesOfClass(thaumcraft.entities.EldritchWarden.class,new net.minecraft.world.phys.AABB(600,110,608,620,121,620)))if(stale.isNoAi()&&stale.isNoGravity()&&stale.hasCustomName()&&Set.of("Zathog","Second Warden").contains(stale.getCustomName().getString()))stale.discard();
            var visible=R5Entities.ELDRITCH_WARDEN.get().create(player.serverLevel());visible.setNoAi(true);visible.setNoGravity(true);thaumcraft.entities.ChampionSystem.makeChampion(visible,10,false);visible.setTitle(13);visible.setPos(608.5,115,613.5);visible.setCustomName(net.minecraft.network.chat.Component.literal("Zathog"));visible.setAbsorptionAmount(80);visible.setAnger(200);player.serverLevel().addFreshEntity(visible);visible.startSeenByPlayer(player);
            var second=R5Entities.ELDRITCH_WARDEN.get().create(player.serverLevel());second.setNoAi(true);second.setNoGravity(true);thaumcraft.entities.ChampionSystem.makeChampion(second,0,false);second.setPos(615,115,613.5);second.setCustomName(net.minecraft.network.chat.Component.literal("Second Warden"));second.setAbsorptionAmount(20);player.serverLevel().addFreshEntity(second);second.startSeenByPlayer(player);
            int[] age={0};NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post tick)->{if(tick.getServer()!=server)return;for(var stale:player.serverLevel().getEntitiesOfClass(thaumcraft.entities.EldritchWarden.class,new net.minecraft.world.phys.AABB(600,110,608,620,121,620)))if(stale!=visible&&stale!=second&&stale.isNoAi()&&stale.isNoGravity())stale.discard();if(++age[0]%10==0){try{Files.writeString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/focus-selection-server-state.json"),new com.google.gson.Gson().toJson(Map.of("selected",player.getInventory().selected,"held",player.getMainHandItem().toString(),"focus",thaumcraft.items.WandFocus.get(player.getMainHandItem()).toString(),"using",player.isUsingItem(),"menu",player.containerMenu.getClass().getSimpleName(),"slot9",player.getInventory().getItem(9).toString())));}catch(Exception failure){throw new RuntimeException(failure);}}if(age[0]==40){visible.setHealth(160);visible.setAbsorptionAmount(30);}});
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event)->server.halt(false));
    }
    private static void shield(ServerPlayer player){
        var checks=new ArrayList<String>();java.util.function.BiConsumer<Boolean,String> check=(pass,name)->{if(!pass)throw new AssertionError(name);checks.add(name);};
        var boss=R5Entities.ELDRITCH_WARDEN.get().create(player.serverLevel());boss.setNoAi(true);boss.setPos(player.getX()+10,player.getY()+20,player.getZ());boss.setAbsorptionAmount(132);
        float health=boss.getHealth();var source=player.damageSources().playerAttack(player);
        check.accept(boss.hurt(source,8)&&boss.getAbsorptionAmount()<132&&boss.getHealth()==health,"ordinary weapon damage drains the absorption shield before boss health");
        boss.setAbsorptionAmount(120);boss.invulnerableTime=0;boss.tickCount=25;boss.tick();
        check.accept(boss.getAbsorptionAmount()==121,"shield regenerates one point on an eligible 25th tick");
        boss.setAbsorptionAmount(1);boss.invulnerableTime=0;boss.hurt(source,8);
        check.accept(boss.getAbsorptionAmount()==0&&boss.getFieldFrenzyCounter()==150,"breaking the first shield triggers a 150-tick field frenzy");
        check.accept(!boss.hurt(source,8),"field frenzy blocks further hits until its timer ends");
        var enraged=R5Entities.ELDRITCH_WARDEN.get().create(player.serverLevel());enraged.setAbsorptionAmount(132);enraged.hurt(source,100);
        var resistance=enraged.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE);
        check.accept(enraged.getAnger()==200&&resistance!=null&&resistance.getAmplifier()==6,"100-point hit triggers ten seconds of anger and resistance amplifier six");
        try{Files.writeString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/focus-boss-shield-server.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"checks",checks,"count",checks.size())));}catch(Exception failure){throw new RuntimeException(failure);}
    }
}
