package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.entities.*;
import thaumcraft.loot.*;
import thaumcraft.menu.PechMenu;
import thaumcraft.world.*;

final class PechIntegrationVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        check.accept(thaumcraft.data.ThaumcraftData.aspects(level.getServer(),new ItemStack(thaumcraft.registry.R5Content.PECH_FOCUS.get())).equals(Map.of("praecantatio",5,"venenum",5,"perditio",5,"alienis",5,"telum",5)),"Pech focus actual scan aspects match original ConfigAspects");
        var pos=new BlockPos(3820,160,80);var biome=WorldBiomes.effective(level,pos).unwrapKey().orElseThrow();
        boolean enabled=ThaumcraftConfig.BIOME_CREATURE_SPAWNS.get(),modern=ThaumcraftConfig.MODERN_PECH_DUNGEON.get();var entities=new ArrayList<Pech>();
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"PechIntegration"));player.moveTo(pos.getCenter());
        try {
            ThaumcraftConfig.BIOME_CREATURE_SPAWNS.set(true);
            for(var expected:List.of(WorldBiomes.MAGICAL,WorldBiomes.EERIE,WorldBiomes.TAINT)){
                WorldBiomes.change(level,pos.getX(),pos.getZ(),expected);
                check.accept(BiomeSpawns.pechBiome(level,pos)==!expected.equals(WorldBiomes.TAINT),"Pech biome gate "+expected.location());
                var event=new LevelEvent.PotentialSpawns(level,MobCategory.MONSTER,pos,WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE,100,1,4)));
                BiomeSpawns.potential(event);BiomeSpawns.potential(event);
                var pech=event.getSpawnerDataList().stream().filter(s->s.type==R5Entities.PECH.get()).toList();
                check.accept(pech.size()==1&&pech.getFirst().getWeight().asInt()==10&&pech.getFirst().minCount==1&&pech.getFirst().maxCount==1,"effective magical spawn list has exactly one original10/1/1 Pech entry "+expected.location());
                ThaumcraftConfig.BIOME_CREATURE_SPAWNS.set(false);BiomeSpawns.potential(event);check.accept(event.getSpawnerDataList().stream().noneMatch(s->s.type==R5Entities.PECH.get()),"config disables natural Pech without removing existing NPCs");ThaumcraftConfig.BIOME_CREATURE_SPAWNS.set(true);
            }
            WorldBiomes.change(level,pos.getX(),pos.getZ(),WorldBiomes.MAGICAL);
            for(int i=0;i<4;i++){var pech=R5Entities.PECH.get().create(level);pech.moveTo(pos.getCenter().add(i,0,0));entities.add(pech);level.addFreshEntity(pech);}
            check.accept(!BiomeSpawns.pech(R5Entities.PECH.get(),level,MobSpawnType.NATURAL,pos,net.minecraft.util.RandomSource.create(0)),"original four-Pech neighborhood cap blocks further natural spawn");
            var pech=entities.getFirst();pech.moveTo(player.position());
            var entries=PechDungeonRewards.entries();check.accept(entries.size()==38,"owned vanilla dungeon plus TC4 registration has38 entries");
            for(var entry:entries)check.accept(!LegacyLoot.stack(entry.item(),1).isEmpty(),"real native item for every original dungeon entry "+entry.item());
            for(boolean variant:new boolean[]{false,true}){
                ThaumcraftConfig.MODERN_PECH_DUNGEON.set(variant);
                for(int seed=0;seed<24;seed++){
                    level.random.setSeed(seed);var reward=PechDungeonRewards.generate(level,player);
                    check.accept(!reward.isEmpty()&&reward.getCount()==1,"actual random dungeon reward in mode"+variant+" seed"+seed);
                    pech.tamed(true);var menu=new PechMenu(30,player.getInventory(),pech);menu.contents.setItem(0,new ItemStack(Items.DIAMOND,2));
                    check.accept(menu.slots.get(0).x==PechMenu.PREVIEW_WIDTH+36&&menu.slots.get(5).x==PechMenu.PREVIEW_WIDTH+8,"list panel keeps server and client input/inventory slot coordinates aligned");
                    check.accept(menu.clickMenuButton(player,0)&&menu.contents.getItem(0).getCount()==1&&java.util.stream.IntStream.range(1,5).anyMatch(i->!menu.contents.getItem(i).isEmpty()),"high-value trade consumes one and produces randomized real outputs mode"+variant+" seed"+seed);
                    for(int i=0;i<5;i++)menu.contents.setItem(i,ItemStack.EMPTY);menu.removed(player);
                }
            }
        }finally{entities.forEach(Entity::discard);WorldBiomes.change(level,pos.getX(),pos.getZ(),biome);ThaumcraftConfig.BIOME_CREATURE_SPAWNS.set(enabled);ThaumcraftConfig.MODERN_PECH_DUNGEON.set(modern);}
    }
    private PechIntegrationVerification(){}
}
