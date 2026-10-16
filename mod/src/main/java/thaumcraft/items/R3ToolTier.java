package thaumcraft.items;

import java.util.function.Supplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import thaumcraft.registry.R3Equipment;
import thaumcraft.registry.ThaumcraftContent;

public enum R3ToolTier implements Tier {
    THAUMIUM(400, 7, 2, 22, () -> Ingredient.of(ThaumcraftContent.THAUMIUM_INGOT.get())),
    VOID(150, 8, 3, 10, () -> Ingredient.of(R3Equipment.VOID_INGOT.get())),
    ELEMENTAL(1500, 10, 3, 18, () -> Ingredient.of(ThaumcraftContent.THAUMIUM_INGOT.get())),
    PRIMAL(500, 8, 4, 20, () -> Ingredient.of(R3Equipment.VOID_INGOT.get()));

    private final int uses, enchantability;
    private final float speed, attack;
    private final Supplier<Ingredient> repair;

    R3ToolTier(int uses, float speed, float attack, int enchantability, Supplier<Ingredient> repair) {
        this.uses = uses;
        this.speed = speed;
        this.attack = attack;
        this.enchantability = enchantability;
        this.repair = repair;
    }

    public int getUses() { return uses; }
    public float getSpeed() { return speed; }
    public float getAttackDamageBonus() { return attack; }
    public int getEnchantmentValue() { return enchantability; }
    public Ingredient getRepairIngredient() { return repair.get(); }
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return this == THAUMIUM || this == ELEMENTAL
                ? BlockTags.INCORRECT_FOR_DIAMOND_TOOL : BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }
}
