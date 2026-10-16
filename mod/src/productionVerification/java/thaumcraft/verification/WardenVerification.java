package thaumcraft.verification;

import java.util.*;
import java.util.function.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import thaumcraft.entities.*;
import thaumcraft.registry.*;

/** Real server damage, teleport events, field collisions and phase transitions. */
final class WardenVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var position=new BlockPos(3780,160,80);level.getChunkAt(position);var cleanup=new ArrayList<Entity>();
        for(int x=-21;x<=21;x++)for(int z=-21;z<=21;z++){level.setBlock(position.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);for(int y=0;y<5;y++)level.setBlock(position.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
        try{
            var boss=R5Entities.ELDRITCH_WARDEN.get().create(level);boss.getAttribute(ChampionSystem.MODIFIER).setBaseValue(-1);cleanup.add(boss);boss.moveTo(position.getCenter());boss.setNoAi(true);level.addFreshEntity(boss);
            check.accept(boss.getMaxHealth()==200&&boss.getAttributeValue(Attributes.ATTACK_DAMAGE)==10&&boss.getAttributeValue(Attributes.ARMOR)==4&&boss.getAttributeValue(Attributes.MOVEMENT_SPEED)==.33,"Warden source health200/damage10/armour4/speed.33");
            check.accept(boss.getBbWidth()==1.5f&&boss.getBbHeight()==3.5f&&boss.getEyeHeight()==3.1f,"Warden source dimensions and eye height");
            boss.finalizeSpawn(level,level.getCurrentDifficultyAt(position),MobSpawnType.COMMAND,null);
            check.accept(boss.getAbsorptionAmount()==132&&boss.getSpawnTimer()==150&&boss.getRestrictRadius()==24&&boss.getRestrictCenter().equals(position),"spawn creates real132-point shield,150-tick freeze and24-block home");
            check.accept(boss.getTitleIndex()>=0&&boss.getTitleIndex()<15&&!boss.getTitle().isEmpty(),"spawn selects one of15 original titles");
            check.accept(!boss.hurt(level.damageSources().generic(),10),"spawn rejects damage");
            for(int i=0;i<150;i++){boss.tickCount++;boss.tick();}
            check.accept(boss.getSpawnTimer()==0,"spawn freeze expires on150th tick");
            boss.getAttribute(Attributes.ARMOR).setBaseValue(0);float hp=boss.getHealth();boss.invulnerableTime=0;check.accept(boss.hurt(level.damageSources().generic(),10)&&boss.getHealth()==hp&&boss.getAbsorptionAmount()==122,"native absorption actually consumes damage without losing health");
            boss.invulnerableTime=0;boss.setAbsorptionAmount(131.75f);boss.tickCount=24;boss.tickCount++;boss.tick();check.accept(boss.getAbsorptionAmount()==132.75f,"source shield regeneration permits fractional overshoot of132 threshold");
            boss.setAbsorptionAmount(132);boss.invulnerableTime=0;boss.tickCount=49;boss.tickCount++;boss.tick();check.accept(boss.getAbsorptionAmount()==132,"shield at base threshold does not regenerate");
            boss.setAbsorptionAmount(100);boss.invulnerableTime=5;boss.tickCount=74;boss.tickCount++;boss.tick();check.accept(boss.getAbsorptionAmount()==100,"recent hurt delays shield regeneration");
            boss.invulnerableTime=0;check.accept(!boss.hurt(level.damageSources().drown(),10)&&!boss.hurt(level.damageSources().wither(),10),"source drowning and Wither immunity");
            boss.setAbsorptionAmount(3);boss.invulnerableTime=0;boss.setHealth(200);check.accept(boss.hurt(level.damageSources().generic(),10)&&boss.getAbsorptionAmount()==0&&boss.getHealth()==193&&boss.getFieldFrenzyCounter()==150,"real shield depletion triggers single150-tick frenzy");
            check.accept(!boss.hurt(level.damageSources().magic(),10),"field frenzy blocks incoming damage");
            var teleport=EldritchWarden.class.getDeclaredMethod("teleportHome");teleport.setAccessible(true);boss.moveTo(position.getCenter().add(10,0,0));Vec3 old=boss.position();
            Consumer<EntityTeleportEvent.EnderEntity> veto=event->{if(event.getEntity()==boss)event.setCanceled(true);};NeoForge.EVENT_BUS.addListener(veto);try{teleport.invoke(boss);}finally{NeoForge.EVENT_BUS.unregister(veto);}check.accept(boss.position().equals(old),"cancelled NeoForge teleport preserves prior position");
            Consumer<EntityTeleportEvent.EnderEntity> redirect=event->{if(event.getEntity()==boss){event.setTargetX(position.getX()+3);event.setTargetY(position.getY());event.setTargetZ(position.getZ()+4);}};NeoForge.EVENT_BUS.addListener(redirect);try{teleport.invoke(boss);}finally{NeoForge.EVENT_BUS.unregister(redirect);}check.accept(boss.position().distanceTo(new Vec3(position.getX()+3.5,position.getY()+.1,position.getZ()+4.5))<1e-8,"teleport honors event redirection and source half-block/.1 offsets");
            boss.restrictTo(position.above(6),24);old=boss.position();teleport.invoke(boss);check.accept(boss.position().equals(old),"home without supporting floor restores prior position");boss.restrictTo(position,24);boss.moveTo(position.getCenter());
            var frenzy=EldritchWarden.class.getDeclaredMethod("performFieldFrenzy");frenzy.setAccessible(true);for(int i=0;i<30;i++)frenzy.invoke(boss);check.accept(boss.getFieldFrenzyCounter()==120&&level.getBlockState(position.offset(3,0,0)).isAir(),"first30 frenzy ticks do not emit ring");
            frenzy.invoke(boss);check.accept(boss.getFieldFrenzyCounter()==119,"first ring tick decrements120 to119");
            int step=1+120/8;for(int q=0;q<180/step;q++){double radians=Math.toRadians(q*2*step);var p=position.offset((int)(3.75*Math.cos(radians)),0,(int)(3.75*Math.sin(radians)));check.accept(level.getBlockState(p).is(R5Content.ELDRITCH_FIELD.get()),"first source ring at angular sample"+q+" position="+p+" actual="+level.getBlockState(p)+" boss="+boss.position()+" below="+level.getBlockState(p.below()));}
            for(int i=0;i<119;i++)frenzy.invoke(boss);check.accept(boss.getFieldFrenzyCounter()==0,"frenzy expires after exactly150 ticks");
            boss.invulnerableTime=0;boss.setAbsorptionAmount(0);check.accept(boss.hurt(level.damageSources().generic(),1)&&boss.getFieldFrenzyCounter()==0,"second shield depletion cannot retrigger frenzy in same runtime");
            var tag=new CompoundTag();boss.setTitle(14);boss.addAdditionalSaveData(tag);var loaded=R5Entities.ELDRITCH_WARDEN.get().create(level);loaded.getAttribute(ChampionSystem.MODIFIER).setBaseValue(-1);cleanup.add(loaded);loaded.readAdditionalSaveData(tag);check.accept(loaded.getTitle().equals("Zushakon")&&loaded.getRestrictCenter().equals(position),"original title byte and home survive NBT");check.accept(loaded.getFieldFrenzyCounter()==0&&loaded.getSpawnTimer()==0&&!tag.contains("fieldFrenzy"),"source frenzy/countdown remain transient");
            var victim=EntityType.COW.create(level);cleanup.add(victim);victim.setNoAi(true);victim.moveTo(position.getCenter().add(0,0,6));level.addFreshEntity(victim);boss.setYRot(90);victim.setDeltaMovement(Vec3.ZERO);boss.sonicAttack(victim);check.accept(victim.getEffect(MobEffects.WITHER).getDuration()==400&&victim.getEffect(MobEffects.BLINDNESS).getDuration()==400,"sonic attack applies original400-tick Wither and blindness");check.accept(Math.abs(victim.getDeltaMovement().x+1.5)<1e-6&&victim.getDeltaMovement().y==.1,"sonic uses source yaw knockback and vertical.1");
            boss.shootOrb(victim);var orbs=level.getEntitiesOfClass(EldritchOrb.class,boss.getBoundingBox().inflate(4));cleanup.addAll(orbs);check.accept(orbs.size()==1&&orbs.getFirst().getOwner()==boss,"Warden actually spawns registered owned eldritch orb");var first=orbs.getFirst().position();boss.shootOrb(victim);var next=level.getEntitiesOfClass(EldritchOrb.class,boss.getBoundingBox().inflate(4));cleanup.addAll(next);check.accept(next.size()==2&&next.stream().anyMatch(o->o.position().distanceTo(first)>.5),"successive orb attacks alternate source hands");
            boss.handleEntityEvent((byte)15);check.accept(boss.armLiftL==.5f,"left attack event animates left arm");boss.handleEntityEvent((byte)16);check.accept(boss.armLiftR==.5f,"right attack event animates right arm");boss.handleEntityEvent((byte)17);check.accept(boss.armLiftL==.9f&&boss.armLiftR==.9f,"field event raises both arms");
            var field=R5Content.ELDRITCH_FIELD.get().defaultBlockState();var fieldPos=position.offset(19,0,0);level.setBlock(fieldPos,field,2);check.accept(field.canBeReplaced()&&!field.blocksMotion()&&field.getCollisionShape(level,fieldPos).isEmpty()&&field.getLightEmission()==8&&field.getRenderShape()==RenderShape.INVISIBLE,"stored airy field replaceable/invisible/passable/light8 rather than solid trap");
            victim.removeAllEffects();victim.setDeltaMovement(1,2,3);field.entityInside(level,fieldPos,victim);check.accept(victim.getDeltaMovement().distanceTo(new Vec3(.66,2,1.98))<1e-8&&victim.getEffect(MobEffects.BLINDNESS).getAmplifier()==1&&victim.getEffect(MobEffects.BLINDNESS).getDuration()==100,"field collision slows XZ only and blinds100 ticks at amplifier1");boss.setDeltaMovement(1,2,3);field.entityInside(level,fieldPos,boss);check.accept(boss.getDeltaMovement().equals(new Vec3(1,2,3))&&!boss.hasEffect(MobEffects.BLINDNESS),"IEldritchMob remains immune to field collision");field.tick(level,fieldPos,level.random);check.accept(level.getBlockState(fieldPos).is(Blocks.AIR),"scheduled field tick removes transient hazard");
            boss.aiStep();check.accept(level.getBlockState(position).is(R5Content.ELDRITCH_FIELD.get()),"actual living tick places field under Warden feet");
            var guardian=R2Entities.ELDRITCH_GUARDIAN.get().create(level);cleanup.add(guardian);guardian.getAttribute(Attributes.ARMOR).setBaseValue(0);guardian.setAbsorptionAmount(25);float before=guardian.getHealth();guardian.hurt(level.damageSources().generic(),10);check.accept(guardian.getAbsorptionAmount()==15&&guardian.getHealth()==before,"existing Guardian has real native absorption capacity instead of clamping shield to0");
            var proof=new com.google.gson.JsonObject();var hashes=new com.google.gson.JsonObject();for(String name:List.of("entities/EldritchWarden","entities/EldritchWarden$1","entities/EldritchWarden$2","entities/EldritchGuardian","entities/R5Entities","registry/R5Content","blocks/EldritchFieldBlock","network/SonicPayload","network/SonicPayload$1","network/BlockArcPayload","network/BlockArcPayload$1","network/ThaumcraftNetwork")){String resource="thaumcraft/"+name+".class";try(var stream=WardenVerification.class.getClassLoader().getResourceAsStream(resource)){hashes.addProperty(resource,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}}proof.addProperty("passed",true);proof.addProperty("time",java.time.Instant.now().toString());proof.add("payload_sha256",hashes);java.nio.file.Files.writeString(java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/warden-server-payload.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof)+"\n");
        }finally{cleanup.forEach(Entity::discard);}
    }
    private WardenVerification(){}
}
