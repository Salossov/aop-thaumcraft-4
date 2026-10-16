package thaumcraft.entities;

import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.util.RandomSource;
import thaumcraft.registry.*;

public final class CultistKnight extends Cultist {
    public CultistKnight(EntityType<? extends CultistKnight> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder attributes(){return Cultist.attributes().add(Attributes.MAX_HEALTH,36);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(3,new Melee(this,1));goalSelector.addGoal(5,new OpenDoorGoal(this,true));goalSelector.addGoal(6,new MoveTowardsRestrictionGoal(this,.8));goalSelector.addGoal(7,new RandomStrollGoal(this,.8));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new Retaliate(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
    @Override protected void populateDefaultEquipmentSlots(RandomSource rng,DifficultyInstance difficulty){setItemSlot(EquipmentSlot.HEAD,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_plate_helmet").get()));setItemSlot(EquipmentSlot.CHEST,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_plate_chestplate").get()));setItemSlot(EquipmentSlot.LEGS,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_plate_leggings").get()));setItemSlot(EquipmentSlot.FEET,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_boots").get()));if(rng.nextFloat()<(level().getDifficulty()==Difficulty.HARD?.05f:.01f)){if(rng.nextInt(5)==0){setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(R3Equipment.TOOLS.get("void_sword").get()));setItemSlot(EquipmentSlot.HEAD,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_robe_helmet").get()));}else{setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(R3Equipment.TOOLS.get("thaumium_sword").get()));if(rng.nextBoolean())setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);}}else setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){var result=super.finalizeSpawn(level,difficulty,reason,data);populateDefaultEquipmentSlots(random,difficulty);populateDefaultEquipmentEnchantments(level,random,difficulty);return result;}
}
