package thaumcraft.verification;

import com.mojang.authlib.GameProfile;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.entities.R5Entities;
import thaumcraft.registry.SpawnEggs;

/** Exercise normal item use, spawner assignment and the vanilla dispenser block for every egg. */
final class SpawnEggVerification {
    static void run(ServerLevel level, BiConsumer<Boolean, String> check) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "SpawnEggQA"));
        player.setGameMode(GameType.SURVIVAL);
        var pos = new BlockPos(8, 200, 8);
        level.getChunkAt(pos);
        for (var cell : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 10, 4))) {
            level.setBlock(cell, cell.getY() == pos.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        }
        player.moveTo(pos.getCenter().add(0, 1, 3));
        var area = new AABB(pos).inflate(8);
        CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), true, level.registryAccess());
        var ownTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.fromNamespaceAndPath("thaumcraft", "thaumcraft"));
        var vanillaTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SPAWN_EGGS.location());
        var seen = new HashSet<net.minecraft.world.entity.EntityType<?>>();
        check.accept(SpawnEggs.ALL.size() == 29, "all 29 original TC4 spawn egg mappings are registered");
        for (var holder : SpawnEggs.ALL) {
            var egg = holder.get();
            var stack = new ItemStack(egg, 2);
            var type = egg.getType(stack);
            var id = holder.getId().toString();
            check.accept(seen.add(type) && SpawnEggItem.byId(type) == egg, id + " has a unique reverse entity mapping");
            check.accept(ownTab.getDisplayItems().stream().filter(s -> s.is(egg)).count() == 1
                    && vanillaTab.getDisplayItems().stream().filter(s -> s.is(egg)).count() == 1,
                    id + " occurs once in both creative tabs");
            var name = Component.literal("Egg QA " + holder.getId().getPath());
            stack.set(DataComponents.CUSTOM_NAME, name);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var hit = new BlockHitResult(pos.getCenter().add(0, .5, 0), Direction.UP, pos, false);
            var result = player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND, hit);
            var mobs = level.getEntitiesOfClass(Mob.class, area, m -> m.getType() == type);
            check.accept(result.consumesAction() && stack.getCount() == 1 && mobs.size() == 1,
                    id + " normal survival use creates the correct mob and consumes one egg");
            var mob = mobs.getFirst();
            check.accept(mob.hasCustomName() && (mob instanceof thaumcraft.entities.ThaumcraftBoss || mob.getCustomName().getString().contains(name.getString())) && mob.getHealth() > 0,
                    id + " retains the item name or the native boss-generated name on a living mob: " + mob.getName().getString());
            if (type == R5Entities.CULTIST_KNIGHT.get() || type == R5Entities.CULTIST_CLERIC.get()) {
                check.accept(!mob.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), id + " runs native cultist equipment initialization");
            }
            for (var entity : level.getEntitiesOfClass(Mob.class, area)) entity.discard();

            var dispenserPos = pos.offset(2, 1, 0);
            level.setBlock(dispenserPos, Blocks.AIR.defaultBlockState(), 2);
            var dispenserState = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST);
            level.setBlock(dispenserPos, dispenserState, 2);
            var dispenser = (DispenserBlockEntity) level.getBlockEntity(dispenserPos);
            dispenser.setItem(0, new ItemStack(egg, 2));
            dispenserState.tick(level, dispenserPos, level.random);
            mobs = level.getEntitiesOfClass(Mob.class, area, m -> m.getType() == type);
            check.accept(dispenser.getItem(0).getCount() == 1 && mobs.size() == 1,
                    id + " actual dispenser spawns its mob instead of ejecting the item");
            for (var entity : level.getEntitiesOfClass(Mob.class, area)) entity.discard();
            level.setBlock(dispenserPos, Blocks.AIR.defaultBlockState(), 2);

            var spawnerPos = pos.offset(-2, 1, 0);
            level.setBlock(spawnerPos, Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2);
            var spawnerStack = new ItemStack(egg, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, spawnerStack);
            hit = new BlockHitResult(spawnerPos.getCenter(), Direction.UP, spawnerPos, false);
            result = player.gameMode.useItemOn(player, level, spawnerStack, InteractionHand.MAIN_HAND, hit);
            var tag = ((SpawnerBlockEntity) level.getBlockEntity(spawnerPos)).getSpawner().save(new CompoundTag());
            check.accept(result.consumesAction() && spawnerStack.getCount() == 1
                    && tag.getCompound("SpawnData").getCompound("entity").getString("id").equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()),
                    id + " configures a vanilla spawner with the correct entity id");
            level.setBlock(spawnerPos, Blocks.AIR.defaultBlockState(), 2);
        }
        player.setGameMode(GameType.CREATIVE);
        var egg = SpawnEggs.ALL.get(5).get(); // Mind spider: no random equipment or extra spawn effects.
        var stack = new ItemStack(egg, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var result = player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND,
                new BlockHitResult(pos.getCenter(), Direction.UP, pos, false));
        check.accept(result.consumesAction() && stack.getCount() == 2
                && level.getEntitiesOfClass(Mob.class, area, m -> m.getType() == egg.getType(stack)).size() == 1,
                "normal creative use spawns a mob without consuming the egg");
        for (var entity : level.getEntitiesOfClass(Mob.class, area)) entity.discard();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private SpawnEggVerification() {}
}
