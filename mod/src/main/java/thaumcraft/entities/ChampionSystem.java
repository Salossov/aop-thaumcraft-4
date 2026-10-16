package thaumcraft.entities;

import java.util.*;
import java.util.function.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import thaumcraft.registry.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.world.dim.LabyrinthData;

/** Original thirteen modifiers. The synced, persisted attribute keeps -2/unprocessed and -1/normal distinct. */
public final class ChampionSystem {
    private static final DeferredRegister<Attribute> ATTRIBUTES=DeferredRegister.create(Registries.ATTRIBUTE,"thaumcraft");
    public static final DeferredHolder<Attribute,Attribute> MODIFIER=ATTRIBUTES.register("champion_modifier",()->new RangedAttribute("attribute.thaumcraft.champion_modifier",-2,-2,100).setSyncable(true));
    public static final String[] NAMES={"bold","spine","armor","mighty","grim","warded","warp","undying","fiery","sickly","venomous","vampiric","infested"};
    public static final int[] EFFECT_TYPES={-1,2,2,-1,1,0,1,0,1,1,1,1,2};
    public static BooleanSupplier enabled=()->true;
    public static Consumer<Monster> clientEffects=mob->{};
    private static final ResourceLocation HEALTH=id("champion_health"),DAMAGE=id("champion_damage"),BOLD=id("champion_bold"),MIGHTY=id("champion_mighty"),NORMAL=id("champion_normal");
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("thaumcraft",path);}
    public static void register(IEventBus bus){ATTRIBUTES.register(bus);bus.addListener(ChampionSystem::attributes);NeoForge.EVENT_BUS.addListener(ChampionSystem::join);NeoForge.EVENT_BUS.addListener(ChampionSystem::tick);NeoForge.EVENT_BUS.addListener(ChampionSystem::hurt);NeoForge.EVENT_BUS.addListener(ChampionSystem::drops);}
    private static void attributes(EntityAttributeModificationEvent event){for(var type:event.getTypes())event.add(type,MODIFIER);}
    public static int modifier(LivingEntity mob){var a=mob.getAttribute(MODIFIER);return a==null?-2:(int)a.getValue();}
    public static void makeChampion(Mob mob,boolean persist){int selected=mob.level().random.nextInt(NAMES.length);if(mob instanceof Creeper)selected=0;makeChampion(mob,selected,persist);}
    public static void makeChampion(Mob mob,int selected,boolean persist){
        if(selected<0||selected>=NAMES.length)throw new IllegalArgumentException("Champion modifier "+selected);
        var attr=mob.getAttribute(MODIFIER);if(attr==null)throw new IllegalStateException("Champion attribute was not installed");
        replace(attr,id("champion_"+selected),selected+2,AttributeModifier.Operation.ADD_VALUE);
        if(!(mob instanceof ThaumcraftBoss)){
            replace(mob.getAttribute(Attributes.MAX_HEALTH),HEALTH,30,AttributeModifier.Operation.ADD_VALUE);
            replace(mob.getAttribute(Attributes.ATTACK_DAMAGE),DAMAGE,2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            mob.heal(25);mob.setCustomName(Component.translatable("champion.mod."+NAMES[selected]).append(" ").append(mob.getName()));
        }else ((ThaumcraftBoss)mob).generateChampionName(selected);
        if(persist)mob.setPersistenceRequired();
        if(selected==0)replace(mob.getAttribute(Attributes.MOVEMENT_SPEED),BOLD,.3,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        if(selected==3)replace(mob.getAttribute(Attributes.ATTACK_DAMAGE),MIGHTY,3,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if(selected==5){int amount=(int)mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue()/2;allowAbsorption(mob,mob.getAbsorptionAmount()+amount);mob.setAbsorptionAmount(mob.getAbsorptionAmount()+amount);}
    }
    private static void replace(AttributeInstance attr,ResourceLocation id,double amount,AttributeModifier.Operation op){if(attr==null)return;attr.removeModifier(id);attr.addPermanentModifier(new AttributeModifier(id,amount,op));}
    private static void allowAbsorption(LivingEntity mob,float amount){var max=mob.getAttribute(Attributes.MAX_ABSORPTION);if(max!=null&&max.getBaseValue()<amount)max.setBaseValue(amount);}
    public static int selectionScore(int roll,Difficulty difficulty,boolean enabled,boolean outer,boolean spooky,boolean dangerous,int whitelist){int c=roll;if(difficulty==Difficulty.EASY||!enabled)c+=2;if(difficulty==Difficulty.HARD&&enabled)c-=2;if(outer)c-=3;if(spooky)c-=enabled?2:1;if(dangerous)c-=enabled?10:3;return c-Math.max(0,whitelist-1);}
    /** Assignable classes retain original whitelist inheritance, including giant zombies and cave spiders. */
    private static int whitelist(Monster mob){if(mob instanceof ThaumcraftBoss)return 200;if(mob instanceof InhabitedZombie)return 3;if(mob instanceof Taintacle)return 2;if(mob instanceof Witch||mob instanceof Cultist||mob instanceof Pech)return 1;if(mob instanceof Zombie||mob instanceof Spider||mob instanceof Blaze||mob instanceof EnderMan||mob instanceof AbstractSkeleton||mob instanceof EldritchCrab)return 0;return -1;}
    private static void join(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide||!(event.getEntity() instanceof Monster mob)||modifier(mob)>=-1)return;
        int roll=mob.level().random.nextInt(100),whitelist=whitelist(mob);boolean on=enabled.getAsBoolean(),outer=mob.level().dimension().location().toString().equals("thaumcraft:outer_lands"),dangerous=false;
        if(outer&&mob.level() instanceof ServerLevel server){var cell=LabyrinthData.get(server).cell(mob.blockPosition().getX()>>4,mob.blockPosition().getZ()>>4);dangerous=cell!=null&&(cell.feature==6||cell.feature==8);}
        var biome=mob.level().getBiome(mob.blockPosition());String name=biome.unwrapKey().map(k->k.location().toString()).orElse("");boolean spooky=mob.level().dimension()==Level.NETHER||mob.level().dimension()==Level.END||name.equals("thaumcraft:eerie")||name.equals("thaumcraft:eldritch");
        int bonus=!on&&!(mob instanceof ThaumcraftBoss)?0:whitelist;
        if(whitelist>=0&&selectionScore(roll,mob.level().getDifficulty(),on,outer,spooky,dangerous,bonus)<=0&&mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue()>=10)makeChampion(mob,false);else replace(mob.getAttribute(MODIFIER),NORMAL,1,AttributeModifier.Operation.ADD_VALUE);
    }
    private static void tick(EntityTickEvent.Post event){if(!(event.getEntity() instanceof Monster mob)||!mob.isAlive())return;int type=modifier(mob);if(type<0||type>=NAMES.length)return;if(mob.level().isClientSide){clientEffects.accept(mob);return;}if(type==7&&mob.tickCount%20==0)mob.heal(1);if(type==5&&mob.invulnerableTime<=0&&mob.tickCount%25==0){int threshold=(int)mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue()/2;if(mob.getAbsorptionAmount()<threshold){allowAbsorption(mob,mob.getAbsorptionAmount()+1);mob.setAbsorptionAmount(mob.getAbsorptionAmount()+1);}}}
    private static void hurt(LivingIncomingDamageEvent event){var victim=event.getEntity();var source=event.getSource();float amount=event.getAmount();Entity direct=source.getDirectEntity();
        if(victim instanceof Monster mob){int type=modifier(mob);boolean shield=(type==5||mob instanceof thaumcraft.api.entities.IEldritchMob)&&mob.getAbsorptionAmount()>0;if(shield&&mob.level() instanceof ServerLevel server){int target=source.getEntity()==null?-1:source.getEntity().getId();if(source.is(DamageTypes.FALL))target=-2;if(source.is(DamageTypes.FALLING_BLOCK))target=-3;net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(server,null,mob.getX(),mob.getY(),mob.getZ(),32,new thaumcraft.network.ChampionShieldPayload(mob.getId(),target));mob.playSound(R2Sounds.RUNIC_SHIELD_EFFECT.get(),.66f,1.1f+server.random.nextFloat()*.1f);}if(!shield&&type>=0&&type<NAMES.length&&EFFECT_TYPES[type]==2&&direct instanceof LivingEntity target){amount=defense(mob,target,source,amount,type);}}
        if(amount>0&&direct instanceof Monster mob){int type=modifier(mob);if(type>=0&&type<NAMES.length&&EFFECT_TYPES[type]==1)amount=offense(mob,victim,source,amount,type);}
        event.setAmount(amount);
    }
    public static float defense(Monster mob,LivingEntity target,DamageSource source,float amount,int type){var random=mob.level().random;
        if(type==1&&!source.is(DamageTypes.THORNS)){target.hurt(mob.damageSources().thorns(mob),1+random.nextInt(3));target.playSound(net.minecraft.sounds.SoundEvents.THORNS_HIT,.5f,1);}
        else if(type==2&&!source.is(DamageTypeTags.BYPASSES_ARMOR))amount=amount*19/25;
        else if(type==12&&random.nextFloat()<.4f&&!mob.level().isClientSide){var spider=R2Entities.TAINT_SPIDER.get().create(mob.level());if(spider!=null){spider.moveTo(mob.getX(),mob.getY()+mob.getBbHeight()/2f,mob.getZ(),random.nextFloat()*360,0);mob.level().addFreshEntity(spider);mob.playSound(R2Sounds.GORE.get(),.5f,1);}}
        return amount;
    }
    public static float offense(Monster mob,LivingEntity target,DamageSource source,float amount,int type){var random=mob.level().random;
        switch(type){case 4->{if(random.nextFloat()<.4f)target.addEffect(new MobEffectInstance(MobEffects.WITHER,200));}case 6->{if(random.nextFloat()<.33f&&target instanceof ServerPlayer player)KnowledgeService.mutate(player,k->{int add=1+random.nextInt(3);k.setWarp(k.permanentWarp(),k.stickyWarp(),k.temporaryWarp()+add,k.warpCounter());return true;});}case 8->{if(random.nextFloat()<.4f)target.igniteForSeconds(4);}case 9->{if(random.nextFloat()<.4f)target.addEffect(new MobEffectInstance(MobEffects.HUNGER,500));}case 10->{if(random.nextFloat()<.4f)target.addEffect(new MobEffectInstance(MobEffects.POISON,100));}case 11->mob.heal(Math.max(2,amount/2));default->{}}
        return amount;
    }
    private static void drops(LivingDropsEvent event){if(!(event.getEntity() instanceof Monster mob)||mob instanceof ThaumcraftBoss||modifier(mob)<0||!event.isRecentlyHit()||event.getSource().getEntity() instanceof net.neoforged.neoforge.common.util.FakePlayer||!(mob.level() instanceof ServerLevel server))return;
        ExperienceOrb.award(server,mob.position(),5+server.random.nextInt(3));int looting=0;if(event.getSource().getEntity() instanceof ServerPlayer player)looting=net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING),player);
        int rarity=Math.min(2,(int)Math.floor((server.random.nextInt(9)+looting)/5f));event.getDrops().add(new ItemEntity(server,mob.getX(),mob.getY()+mob.getEyeHeight(),mob.getZ(),new ItemStack(R5Content.LOOT_BAGS.get(rarity).get())));
    }
    private ChampionSystem(){}
}
