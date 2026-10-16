package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.*;
import thaumcraft.entities.*;
import thaumcraft.blocks.*;
import thaumcraft.items.*;
import thaumcraft.registry.*;
import thaumcraft.api.aspects.*;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.api.wands.*;
import thaumcraft.production.TransportBlockEntity;

/** Exportable vanilla save: no verification mod is required to play this world. */
final class ManualMechanicsWorldBuilder {
    private static final UUID OWNER=UUID.fromString("8ea0a045-55ca-4ad0-9f14-1c154e582e67");
    private static BlockPos pos(int x,int y,int z){return new BlockPos(x,y,z);}
    private static void block(ServerLevel l,BlockPos p,Block b){l.setBlockAndUpdate(p,b.defaultBlockState());}
    private static ChestBlockEntity chest(ServerLevel l,BlockPos p,String name){block(l,p,Blocks.CHEST);var chest=(ChestBlockEntity)l.getBlockEntity(p);var tag=chest.saveWithoutMetadata(l.registryAccess());tag.putString("CustomName",Component.Serializer.toJson(Component.literal(name),l.registryAccess()));chest.loadCustomOnly(tag,l.registryAccess());return chest;}
    private static void sign(ServerLevel l,BlockPos p,String...lines){block(l,p,Blocks.OAK_SIGN);var tile=(SignBlockEntity)l.getBlockEntity(p);var text=new SignText();for(int i=0;i<4;i++)text=text.setMessage(i,Component.literal(i<lines.length?lines[i]:""));tile.setText(text,true);tile.setText(text,false);}
    private static void drop(ServerLevel l,BlockPos p,ItemStack stack){var e=new ItemEntity(l,p.getX()+.5,p.getY()+.2,p.getZ()+.5,stack);e.setNoPickUpDelay();e.setUnlimitedLifetime();l.addFreshEntity(e);}
    private static void marker(ServerLevel l,ThaumicGolem g,BlockPos p){g.mark(new GolemMarker(l.dimension(),p,Direction.UP,-1),false);}
    private static AuraNodeBlockEntity node(ServerLevel l,BlockPos p,NodeState.Type type){block(l,p,R2Content.AURA_NODE.get());var n=(AuraNodeBlockEntity)l.getBlockEntity(p);n.initialize(Map.of("aer",40,"ignis",30,"aqua",25,"terra",25,"ordo",20,"perditio",20),type,NodeState.Modifier.BRIGHT);return n;}
    static void run(ServerLevel l,BiConsumer<Boolean,String> check){
        l.getServer().getWorldData().setGameType(GameType.CREATIVE);l.setDefaultSpawnPos(pos(0,4,-10),0);l.setDayTime(6000);l.setWeatherParameters(240000,0,false,false);
        l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,l.getServer());l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,l.getServer());l.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true,l.getServer());l.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0,l.getServer());
        for(var p:BlockPos.betweenClosed(pos(-12,3,-16),pos(148,3,84))){l.getChunkAt(p);block(l,p,Blocks.STONE);}
        var tools=chest(l,pos(0,4,-7),"Инструменты, книга и очки");int slot=0;for(var item:List.of(R3Equipment.GOLEM_BELL.get(),R2Content.GOGGLES.get(),R2Content.THAUMOMETER.get(),net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:thaumonomicon"))))tools.setItem(slot++,new ItemStack(item));for(var item:R3Equipment.TOOLS.values())if(item.getId().getPath().startsWith("elemental_"))tools.setItem(slot++,new ItemStack(item.get()));
        var wand=new ItemStack(R3Equipment.WAND.get());wand.set(WandComponents.PARTS.get(),new WandParts("silverwood","thaumium"));for(var a:Aspects.PRIMALS)WandVis.set(wand,a,10000);tools.setItem(slot++,wand);tools.setItem(slot++,new ItemStack(Items.WATER_BUCKET));tools.setItem(slot++,new ItemStack(Items.BUCKET));tools.setItem(slot++,new ItemStack(Items.ZOMBIE_SPAWN_EGG,32));tools.setItem(slot++,new ItemStack(Items.COW_SPAWN_EGG,32));tools.setItem(slot++,new ItemStack(Items.CREEPER_SPAWN_EGG,32));
        var parts=chest(l,pos(3,4,-7),"Големы, ядра и улучшения");slot=0;for(var body:R3Equipment.GOLEM_BODIES.values())parts.setItem(slot++,new ItemStack(body.get()));for(var core:R3Equipment.GOLEM_CORES.values())if(core.get().kind!=GolemCoreItem.Kind.BLANK)parts.setItem(slot++,new ItemStack(core.get(),8));for(var upgrade:R3Equipment.GOLEM_UPGRADES)parts.setItem(slot++,new ItemStack(upgrade.get(),8));
        var supplies=chest(l,pos(6,4,-7),"Запас для повторной проверки");slot=0;for(var item:List.of(Items.EMERALD,Items.OAK_LOG,Items.WHEAT_SEEDS,Items.DIRT,Items.STONE,Items.SUGAR,Items.GLASS,Items.OAK_SLAB))supplies.setItem(slot++,new ItemStack(item,64));
        sign(l,pos(-3,4,-7),"Проверка TC4","12 площадок","Рычаг = пуск","пустой рукой");
        String[] names={"Заполнение","Опустошение","Сбор","Урожай","Страж","Жидкости","Алхимия","Рубка","Использование","Мясник","Сортировка","Рыбалка"};
        for(int core=0;core<12;core++){
            int x=core%4*28,z=8+core/4*24;var source=pos(x,4,z);for(int xx=-7;xx<=7;xx++)for(int zz=-7;zz<=7;zz++)if(Math.abs(xx)==7||Math.abs(zz)==7)block(l,pos(x+xx,4,z+zz),Blocks.OAK_FENCE);
            var home=chest(l,source,"Дом: "+names[core]);var material=core==5||core==6?GolemMaterial.CLAY:GolemMaterial.values()[core%8];var body=new ItemStack(R3Equipment.GOLEM_BODIES.get(material).get());final int kind=core;CustomData.update(DataComponents.CUSTOM_DATA,body,t->{t.putInt("core",kind);t.putBoolean("advanced",true);});
            var g=R3Entities.GOLEM.get().create(l);g.initialize(material,OWNER,source.above(),Direction.UP,body);g.toggle(3,true);g.setCustomName(Component.literal(core+" "+names[core]));
            var fetter=source.east();block(l,fetter,R3Content.GOLEM_FETTER.get());l.setBlockAndUpdate(fetter.south(),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR).setValue(LeverBlock.POWERED,true));l.setBlockAndUpdate(fetter,R3Content.GOLEM_FETTER.get().defaultBlockState().setValue(GolemFetterBlock.POWERED,true));g.moveTo(fetter.getX()+.5,5,fetter.getZ()+.5);l.addFreshEntity(g);sign(l,pos(x-6,4,z-6),core+" "+names[core],"Пуск: рычаг","Голем ждёт","на оковах");
            if(core==0){var supplier=chest(l,source.east(4),"Источник: изумруды");supplier.setItem(0,new ItemStack(Items.EMERALD,64));g.filters.setItem(0,new ItemStack(Items.EMERALD,32));marker(l,g,source.east(4));}
            if(core==1||core==10){home.setItem(0,new ItemStack(Items.EMERALD,64));var receiver=chest(l,source.east(4),core==10?"Пустой: пропустить":"Приёмник");marker(l,g,source.east(4));if(core==10){var match=chest(l,source.west(4),"Подходящий приёмник");match.setItem(0,new ItemStack(Items.EMERALD));marker(l,g,source.west(4));}}
            if(core==2){g.filters.setItem(0,new ItemStack(Items.EMERALD));for(int i=0;i<6;i++)drop(l,source.offset(3,0,i%3-1),new ItemStack(Items.EMERALD,8));}
            if(core==3)for(int xx=2;xx<=5;xx++)for(int zz=-3;zz<=3;zz++){block(l,pos(x+xx,3,z+zz),Blocks.FARMLAND);l.setBlockAndUpdate(pos(x+xx,4,z+zz),Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));}
            if(core==4||core==9)for(int i=0;i<(core==9?6:1);i++){var cow=EntityType.COW.create(l);cow.moveTo(x-3+i%3,4,z-2+i/3);cow.setPersistenceRequired();cow.tickCount=core==9?1000-i*100:0;l.addFreshEntity(cow);}
            if(core==5){block(l,source,R3Content.CRUCIBLE.get());for(int xx=3;xx<=5;xx++)for(int zz=-1;zz<=1;zz++){block(l,pos(x+xx,2,z+zz),Blocks.STONE);block(l,pos(x+xx,3,z+zz),Blocks.WATER);}marker(l,g,pos(x+4,3,z));}
            if(core==6){block(l,source,R3Content.ALEMBIC.get());((TransportBlockEntity)l.getBlockEntity(source)).store.setAspects(new AspectList().add(Aspects.FIRE,32));for(int dx:new int[]{-4,4}){var jp=source.offset(dx,0,0);block(l,jp,R3Content.WARDED_JAR.get());var jar=(TransportBlockEntity)l.getBlockEntity(jp);if(dx<0){jar.store.filter(Aspects.FIRE);jar.store.setAspects(new AspectList().add(Aspects.FIRE,5));}marker(l,g,jp);}}
            if(core==7){for(int xx=3;xx<=4;xx++)for(int yy=4;yy<=10;yy++)block(l,pos(x+xx,yy,z),Blocks.OAK_LOG);for(int xx=2;xx<=5;xx++)for(int zz=-1;zz<=1;zz++)block(l,pos(x+xx,11,z+zz),Blocks.OAK_LEAVES);}
            if(core==8){home.setItem(0,new ItemStack(Items.DIAMOND_HOE));for(int xx=3;xx<=5;xx++){block(l,source.east(xx),Blocks.DIRT);marker(l,g,source.east(xx));}}
            if(core==11){for(int xx=2;xx<=5;xx++)for(int zz=-3;zz<=3;zz++){block(l,pos(x+xx,2,z+zz),Blocks.STONE);block(l,pos(x+xx,3,z+zz),Blocks.WATER);}}
            check.accept(g.inactive(),"manual core "+core+" waits on powered fetter until its lever is switched");
        }
        var wall=pos(122,4,12);for(int x=-3;x<=3;x++)for(int y=0;y<4;y++)block(l,wall.offset(x,y,0),Blocks.STONE);for(int x:new int[]{-2,0,2})block(l,wall.offset(x,1,-1),x<0?Blocks.GOLD_ORE:x>0?Blocks.DIAMOND_ORE:Blocks.IRON_ORE);block(l,wall.offset(-1,0,-1),Blocks.WATER);block(l,wall.offset(1,0,-1),Blocks.LAVA);sign(l,wall.south(3),"Кирка огня","ПКМ по стене","Руды и жидкости","за камнем");
        for(int x=116;x<=124;x++)for(int z=26;z<=34;z++)block(l,pos(x,4,z),Blocks.DIRT);sign(l,pos(116,4,24),"Лопата / мотыга","Земля 3x3","Shift и ПКМ","есть запас блоков");
        for(int y=4;y<=14;y++)block(l,pos(136,y,28),Blocks.OAK_LOG);for(int x=134;x<=138;x++)for(int z=26;z<=30;z++)block(l,pos(x,15,z),Blocks.OAK_LEAVES);sign(l,pos(132,4,24),"Топор потока","Рубка снизу","Shift: обычно","ПКМ: притяжение");
        for(int i=0;i<3;i++){var p=pos(116+i*10,5,48);block(l,p.below(),(i==1?R3Content.ADVANCED_NODE_STABILIZER:R3Content.NODE_STABILIZER).get());node(l,p,NodeState.Type.NORMAL);if(i==2){block(l,p.above(),R3Content.NODE_CONVERTER.get());block(l,p.east(),Blocks.STONE);l.setBlockAndUpdate(p.above().east(),Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE,AttachFace.FLOOR));}sign(l,p.offset(-2,-1,4),i==0?"Стабилизатор":i==1?"Продвинутый":"Преобразователь","Очки: виден узел","Рычаг: redstone","");}
        var jarPos=pos(116,5,68);var captured=node(l,jarPos,NodeState.Type.PURE);block(l,jarPos,R3Content.NODE_JAR.get());var jar=(JarredNodeBlockEntity)l.getBlockEntity(jarPos);jar.capture(captured);tools.setItem(24,jar.item());sign(l,jarPos.offset(-2,-1,4),"Узел в банке","Палочка: выпуск","Сломать: предмет","Перенос сохраняет");
        var pack=pos(136,5,68);node(l,pack,NodeState.Type.NORMAL);for(var shell:NodeJarActions.shell(pack))block(l,shell,shell.getY()==pack.getY()+2?Blocks.OAK_SLAB:Blocks.GLASS);sign(l,pack.offset(-2,-1,4),"Упаковка узла","Изучи NODEJAR","ПКМ палочкой","по стеклу");
        for(int i=0;i<NodeState.Type.values().length;i++){var type=NodeState.Type.values()[i];var p=pos(176+i%3*24,5,8+i/3*32);for(var q:BlockPos.betweenClosed(p.offset(-8,-2,-8),p.offset(8,-2,8))){l.getChunkAt(q);block(l,q,Blocks.OBSIDIAN);}node(l,p,type);sign(l,p.offset(-3,-1,18),"Узел "+type.name(),"Отдельная площадка","Надень очки","");}
        check.accept(l.getSharedSpawnPos().equals(pos(0,4,-10)),"manual world has the player spawn beside the tool chests");check.accept(tools.getItem(0).is(R3Equipment.GOLEM_BELL.get()),"manual kit contains the production bell and test equipment");
    }
}
