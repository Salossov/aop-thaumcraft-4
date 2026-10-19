package thaumcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.R2Content;
import thaumcraft.world.NodeFactory;
import thaumcraft.world.StructureIndexData;
import thaumcraft.world.WorldBiomes;

/**
 * English-only QA commands for playtesting the port. Parent {@code /thaumcraft}
 * requires operator level 2, matching vanilla cheat commands.
 */
public final class ThaumcraftCommands {
    private static final SuggestionProvider<CommandSourceStack> RESEARCH_SUGGESTIONS=(context,builder)->{
        for(var key:ThaumcraftData.get(context.getSource().getServer()).research().keySet())
            if(key.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))builder.suggest(key);
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> ASPECT_SUGGESTIONS=(context,builder)->{
        for(var aspect:Aspect.all().values())
            if(aspect.getTag().startsWith(builder.getRemainingLowerCase()))builder.suggest(aspect.getTag());
        return builder.buildFuture();
    };
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("thaumcraft").requires(source->source.hasPermission(2))
            .then(Commands.literal("research").then(Commands.argument("target",EntityArgument.player())
                .then(Commands.argument("key",StringArgumentType.word()).suggests(RESEARCH_SUGGESTIONS)
                    .executes(context->grant(EntityArgument.getPlayer(context,"target"),StringArgumentType.getString(context,"key"),true)))
                .then(Commands.literal("all").executes(context->grantAll(EntityArgument.getPlayer(context,"target"),true)))))
            .then(Commands.literal("forget").then(Commands.argument("target",EntityArgument.player())
                .then(Commands.argument("key",StringArgumentType.word()).suggests(RESEARCH_SUGGESTIONS)
                    .executes(context->grant(EntityArgument.getPlayer(context,"target"),StringArgumentType.getString(context,"key"),false)))
                .then(Commands.literal("all").executes(context->grantAll(EntityArgument.getPlayer(context,"target"),false)))))
            .then(Commands.literal("reset").then(Commands.argument("target",EntityArgument.player())
                .executes(context->reset(EntityArgument.getPlayer(context,"target")))))
            .then(Commands.literal("aspect").then(Commands.argument("target",EntityArgument.player())
                .then(Commands.literal("all").then(Commands.argument("amount",IntegerArgumentType.integer(0,32767))
                    .executes(context->poolAll(EntityArgument.getPlayer(context,"target"),IntegerArgumentType.getInteger(context,"amount")))))
                .then(Commands.argument("aspect",StringArgumentType.word()).suggests(ASPECT_SUGGESTIONS)
                    .then(Commands.argument("amount",IntegerArgumentType.integer(0,32767))
                        .executes(context->pool(EntityArgument.getPlayer(context,"target"),StringArgumentType.getString(context,"aspect"),IntegerArgumentType.getInteger(context,"amount"),false))))))
            .then(Commands.literal("aspects").then(Commands.argument("target",EntityArgument.player())
                .then(Commands.argument("amount",IntegerArgumentType.integer(0,32767))
                    .executes(context->pool(EntityArgument.getPlayer(context,"target"),null,IntegerArgumentType.getInteger(context,"amount"),true)))))
            .then(Commands.literal("warp").then(Commands.argument("target",EntityArgument.player())
                .then(Commands.argument("permanent",IntegerArgumentType.integer(0,100))
                    .then(Commands.argument("sticky",IntegerArgumentType.integer(0,100))
                        .then(Commands.argument("temporary",IntegerArgumentType.integer(0,100))
                            .executes(ThaumcraftCommands::warp))))))
            .then(Commands.literal("scan").then(Commands.argument("target",EntityArgument.player())
                .then(Commands.argument("kind",StringArgumentType.word()).suggests((context,builder)->SharedSuggestionProvider.suggest(new String[]{"object","entity","phenomenon"},builder))
                    .then(Commands.argument("key",StringArgumentType.greedyString())
                        .executes(ThaumcraftCommands::scan)))))
            .then(Commands.literal("node").executes(ThaumcraftCommands::nodeEditor)
                .then(Commands.argument("type",StringArgumentType.word()).suggests((context,builder)->SharedSuggestionProvider.suggest(new String[]{"normal","dark","unstable","hungry","pure","tainted","silverwood","totem"},builder))
                    .executes(context->node(context,null,StringArgumentType.getString(context,"type")))
                    .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(context->node(context,BlockPosArgument.getBlockPos(context,"pos"),StringArgumentType.getString(context,"type"))))))
            .then(Commands.literal("debug").then(Commands.literal("golems").executes(context->golemDebug(context,null)).then(Commands.literal("on").executes(context->golemDebug(context,true))).then(Commands.literal("off").executes(context->golemDebug(context,false)))).then(Commands.literal("infusion").executes(context->infusionDebug(context,null)).then(Commands.literal("on").executes(context->infusionDebug(context,true))).then(Commands.literal("off").executes(context->infusionDebug(context,false)))))
            .then(Commands.literal("biome")
                .then(Commands.argument("biome",StringArgumentType.word()).suggests((context,builder)->SharedSuggestionProvider.suggest(new String[]{"magical_forest","tainted_land","eerie"},builder))
                    .executes(context->biome(context,null,StringArgumentType.getString(context,"biome")))
                    .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(context->biome(context,BlockPosArgument.getBlockPos(context,"pos"),StringArgumentType.getString(context,"biome"))))))
            .then(Commands.literal("structure")
                .then(Commands.argument("kind",StringArgumentType.word()).suggests((context,builder)->SharedSuggestionProvider.suggest(new String[]{"mound","hilltop","ring","totem","greatwood","silverwood","spider_tree"},builder))
                    .executes(context->structure(context,null,StringArgumentType.getString(context,"kind")))
                    .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(context->structure(context,BlockPosArgument.getBlockPos(context,"pos"),StringArgumentType.getString(context,"kind"))))))
            .then(Commands.literal("cultists").executes(context->cultists(context,null))
                .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(context->cultists(context,BlockPosArgument.getBlockPos(context,"pos")))))
            .then(Commands.literal("maze")
                .then(Commands.argument("width",IntegerArgumentType.integer(11,29))
                    .then(Commands.argument("height",IntegerArgumentType.integer(11,29))
                        .executes(context->maze(context,null,IntegerArgumentType.getInteger(context,"width"),IntegerArgumentType.getInteger(context,"height")))
                        .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(context->maze(context,BlockPosArgument.getBlockPos(context,"pos"),IntegerArgumentType.getInteger(context,"width"),IntegerArgumentType.getInteger(context,"height")))))))
            .then(Commands.literal("locate")
                .then(Commands.argument("kind",StringArgumentType.word()).suggests((context,builder)->SharedSuggestionProvider.suggest(thaumcraft.world.NaturalStructureLocate.KINDS,builder))
                    .executes(context->locate(context,StringArgumentType.getString(context,"kind"))))));
        var root=dispatcher.getRoot().getChild("thaumcraft");
        root.addChild(Commands.literal("wand")
            .then(Commands.literal("fill").executes(context->wand(context,null))
                .then(Commands.argument("amount",com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0,32767))
                    .executes(context->wand(context,com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(context,"amount")))))
            .then(Commands.literal("empty").executes(context->wand(context,0d))).build());
        var research=root.getChild("research");research.addChild(alias("grant",research));research.addChild(alias("forget",root.getChild("forget")));
        var knowledge=Commands.literal("knowledge").build();for(String name:new String[]{"aspect","aspects","warp","scan","reset"})knowledge.addChild(alias(name,root.getChild(name)));root.addChild(knowledge);
        var world=Commands.literal("world").build();for(String name:new String[]{"node","biome","structure","cultists","maze","locate"})world.addChild(alias(name,root.getChild(name)));root.addChild(world);
    }

    private static com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> alias(String name,com.mojang.brigadier.tree.CommandNode<CommandSourceStack> target){var builder=Commands.literal(name).redirect(target);if(target.getCommand()!=null)builder.executes(target.getCommand());return builder.build();}
    private static int wand(CommandContext<CommandSourceStack> context,Double wholeVis)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var source=context.getSource();var player=source.getPlayerOrException();var stack=player.getMainHandItem();
        if(!(stack.getItem() instanceof thaumcraft.items.CastingWandItem)){source.sendFailure(Component.translatable("thaumcraft.command.wand.required"));return 0;}
        int capacity=thaumcraft.items.WandVis.capacity(stack),amount=wholeVis==null?capacity:Math.min(capacity,(int)Math.round(wholeVis*100));
        for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS)thaumcraft.items.WandVis.set(stack,aspect,amount);
        player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();source.sendSuccess(()->Component.translatable("thaumcraft.command.wand.filled",String.format(Locale.ROOT,"%.2f",amount/100d)),true);return 1;
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> context,String argument){
        return context.getArgument(argument,ServerPlayer.class);
    }

    private static int grant(ServerPlayer target,String key,boolean give){
        var definitions=ThaumcraftData.get(target.server).research();
        if(!definitions.containsKey(key)){
            target.sendSystemMessage(Component.literal("Unknown research "+key));
            return 0;
        }
        boolean changed=give
            ?KnowledgeService.mutate(target,knowledge->{grantRecursive(knowledge,definitions,key);return true;})
            :KnowledgeService.mutate(target,knowledge->knowledge.forgetResearch(key));
        target.sendSystemMessage(Component.literal((give?"Granted research and its requisites ":"Removed research ")+key));
        return changed?1:0;
    }
    /** Original CommandThaumcraft.giveRecursiveResearch: parents, hidden parents and siblings come too. */
    private static void grantRecursive(PlayerKnowledge knowledge,java.util.Map<String,thaumcraft.api.research.ResearchDefinition> definitions,String key){
        if(knowledge.completedResearch().contains(key))return;
        knowledge.completeResearch(key);
        var definition=definitions.get(key);if(definition==null)return;
        for(var parent:definition.parents())grantRecursive(knowledge,definitions,parent);
        for(var parent:definition.hiddenParents())grantRecursive(knowledge,definitions,parent);
        for(var sibling:definition.siblings())grantRecursive(knowledge,definitions,sibling);
    }

    private static int grantAll(ServerPlayer target,boolean give){
        var definitions=ThaumcraftData.get(target.server).research();
        boolean changed=give
            ?KnowledgeService.mutate(target,knowledge->{boolean any=false;for(var key:definitions.keySet())any|=knowledge.completeResearch(key);return any;})
            :KnowledgeService.mutate(target,knowledge->{boolean any=false;for(var key:definitions.keySet())any|=knowledge.forgetResearch(key);return any;});
        target.sendSystemMessage(Component.literal(give?"Granted all research ("+definitions.size()+" entries)":"Revoked all research ("+definitions.size()+" entries)"));
        return changed?1:0;
    }

    /** Full QA reset: revoke every research, clear scans, pools, and warp. */
    private static int reset(ServerPlayer target){
        var definitions=ThaumcraftData.get(target.server).research();
        boolean changed=KnowledgeService.mutate(target,knowledge->{
            boolean any=false;
            for(var key:definitions.keySet())any|=knowledge.forgetResearch(key);
            knowledge.clearScans();
            for(var aspect:Aspect.all().values())knowledge.setPool(aspect.getTag(),0);
            knowledge.setWarp(0,0,0,0);
            return true;
        });
        target.sendSystemMessage(Component.literal("Reset all Thaumcraft progress"));
        return changed?1:0;
    }

    private static int pool(ServerPlayer target,String tag,int amount,boolean allPrimals){
        boolean known=false;
        if(allPrimals){
            for(var primal:thaumcraft.api.aspects.Aspects.PRIMALS)
                known|=KnowledgeService.mutate(target,knowledge->{knowledge.discover(primal);knowledge.addPool(primal,amount);return true;});
            target.sendSystemMessage(Component.literal("Granted "+amount+" of every primal aspect"));
        }else{
            var aspect=Aspect.getAspect(tag);
            if(aspect==null){target.sendSystemMessage(Component.literal("Unknown aspect "+tag));return 0;}
            known=KnowledgeService.mutate(target,knowledge->{knowledge.discover(aspect);knowledge.addPool(aspect,amount);return true;});
            target.sendSystemMessage(Component.literal("Granted "+amount+" "+tag));
        }
        return known?1:0;
    }
    /** Original /thaumcraft aspect <player> all <n>: every known aspect, not only primals. */
    private static int poolAll(ServerPlayer target,int amount){
        boolean changed=KnowledgeService.mutate(target,knowledge->{for(var aspect:Aspect.all().values()){knowledge.discover(aspect);knowledge.addPool(aspect,amount);}return true;});
        target.sendSystemMessage(Component.literal("Granted "+amount+" of every aspect"));
        return changed?1:0;
    }

    private static int warp(CommandContext<CommandSourceStack> context){
        var target=player(context,"target");
        int permanent=IntegerArgumentType.getInteger(context,"permanent");
        int sticky=IntegerArgumentType.getInteger(context,"sticky");
        int temporary=IntegerArgumentType.getInteger(context,"temporary");
        boolean changed=KnowledgeService.mutate(target,knowledge->{knowledge.setWarp(permanent,sticky,temporary,permanent+sticky+temporary);return true;});
        target.sendSystemMessage(Component.literal("Warp set to "+permanent+"/"+sticky+"/"+temporary));
        return changed?1:0;
    }

    private static int scan(CommandContext<CommandSourceStack> context){
        var target=player(context,"target");
        PlayerKnowledge.ScanKind kind=switch(StringArgumentType.getString(context,"kind").toLowerCase(Locale.ROOT)){
            case "object"->PlayerKnowledge.ScanKind.OBJECT;
            case "entity"->PlayerKnowledge.ScanKind.ENTITY;
            case "phenomenon"->PlayerKnowledge.ScanKind.PHENOMENON;
            default->null;
        };
        if(kind==null){target.sendSystemMessage(Component.literal("Scan kind must be object, entity, or phenomenon"));return 0;}
        String key=StringArgumentType.getString(context,"key");
        boolean changed=KnowledgeService.mutate(target,knowledge->knowledge.recordScan(kind,key));
        target.sendSystemMessage(Component.literal("Recorded "+kind.name().toLowerCase(Locale.ROOT)+" scan "+key));
        return changed?1:0;
    }

    private static int nodeEditor(CommandContext<CommandSourceStack> context)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var player=context.getSource().getPlayerOrException();var pos=player.blockPosition().relative(player.getDirection(),3).above();net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,new thaumcraft.network.AdminUiPayload(0,pos,false));return 1;
    }
    private static int golemDebug(CommandContext<CommandSourceStack> context,Boolean requested)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var player=context.getSource().getPlayerOrException();boolean enabled=requested==null?!thaumcraft.entities.GolemDebugService.enabled(player):requested;
        thaumcraft.entities.GolemDebugService.enabled(player,enabled);
        context.getSource().sendSuccess(()->Component.translatable(enabled?"thaumcraft.golem.debug.on":"thaumcraft.golem.debug.off"),false);return 1;
    }
    private static int infusionDebug(CommandContext<CommandSourceStack> context,Boolean requested)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var player=context.getSource().getPlayerOrException();boolean enabled=requested==null?!player.getPersistentData().getBoolean("ThaumcraftInfusionDebug"):requested;player.getPersistentData().putBoolean("ThaumcraftInfusionDebug",enabled);net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,new thaumcraft.network.AdminUiPayload(1,BlockPos.ZERO,enabled));context.getSource().sendSuccess(()->Component.translatable(enabled?"thaumcraft.infusion.debug_on":"thaumcraft.infusion.debug_off"),false);return 1;
    }
    private static int node(CommandContext<CommandSourceStack> context,BlockPos argument,String type){
        var source=context.getSource();
        var level=source.getLevel();
        var anchor=argument==null?BlockPos.containing(source.getPosition()):argument;
        var pos=level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,anchor);
        NodeState.Type nodeType=switch(type.toLowerCase(Locale.ROOT)){
            case "dark"->NodeState.Type.DARK;
            case "unstable"->NodeState.Type.UNSTABLE;
            case "hungry"->NodeState.Type.HUNGRY;
            case "pure"->NodeState.Type.PURE;
            case "tainted"->NodeState.Type.TAINTED;
            case "silverwood"->NodeState.Type.PURE;
            case "totem"->NodeState.Type.DARK;
            default->NodeState.Type.NORMAL;
        };
        boolean silverwood=type.equalsIgnoreCase("silverwood");
        boolean eerie=type.equalsIgnoreCase("totem");
        boolean small=silverwood||type.equalsIgnoreCase("normal");
        if(!level.isEmptyBlock(pos))pos=pos.above();
        final var placedAt=pos;
        level.setBlock(placedAt,R2Content.AURA_NODE.get().defaultBlockState(),2);
        if(level.getBlockEntity(placedAt) instanceof AuraNodeBlockEntity node){
            var aspects=new java.util.LinkedHashMap<String,Integer>();
            aspects.put("aer",20);aspects.put("terra",20);
            node.initialize(aspects,nodeType,silverwood?NodeState.Modifier.NONE:NodeState.Modifier.NONE);
            if(eerie)node.state.type=NodeState.Type.DARK;
            if(small)node.state.current.replaceAll((tag,amount)->Math.max(4,amount/4));
            node.setChanged();
            source.sendSuccess(()->Component.literal("Created "+type+" node at "+placedAt.getX()+" "+placedAt.getY()+" "+placedAt.getZ()),true);
            return 1;
        }
        final var failedAt=pos;
        source.sendFailure(Component.literal("Could not create node at "+failedAt.getX()+" "+failedAt.getY()+" "+failedAt.getZ()));
        return 0;
    }

    private static int biome(CommandContext<CommandSourceStack> context,BlockPos argument,String name){
        var source=context.getSource();
        var level=source.getLevel();
        var anchor=argument==null?BlockPos.containing(source.getPosition()):argument;
        var key=switch(name.toLowerCase(Locale.ROOT)){
            case "tainted_land"->WorldBiomes.TAINT;
            case "eerie"->WorldBiomes.EERIE;
            default->WorldBiomes.MAGICAL;
        };
        boolean changed=WorldBiomes.change(level,anchor.getX(),anchor.getZ(),key);
        if(changed){source.sendSuccess(()->Component.literal("Changed biome column at "+anchor.getX()+" "+anchor.getZ()+" to "+name),true);return 1;}
        source.sendFailure(Component.literal("Could not change biome column at "+anchor.getX()+" "+anchor.getZ()));
        return 0;
    }

    private static int structure(CommandContext<CommandSourceStack> context,BlockPos argument,String kind){
        var source=context.getSource();
        var level=source.getLevel();
        var anchor=argument==null?BlockPos.containing(source.getPosition()):argument;
        var random=level.random;
        boolean placed=switch(kind.toLowerCase(Locale.ROOT).replace("-","_")){
            case "mound"->new thaumcraft.world.LegacyMound().generate(level,random,anchor);
            case "hilltop"->new thaumcraft.world.LegacyHilltopStones().generate(level,random,anchor);
            case "ring"->{
                var ring=new thaumcraft.world.LegacyEldritchRing();
                ring.chunkX=anchor.getX()>>4;ring.chunkZ=anchor.getZ()>>4;
                ring.width=thaumcraft.world.PopulationPolicy.mazeSize(random);
                ring.height=thaumcraft.world.PopulationPolicy.mazeSize(random);
                yield ring.generate(level,random,anchor);
            }
            case "totem"->thaumcraft.world.LegacyTotem.generate(level,random,anchor.getX()>>4,anchor.getZ()>>4,false,true);
            case "silverwood"->new thaumcraft.world.LegacySilverwoodTree(false,7,4).func_76484_a(level,random,anchor.getX(),anchor.getY(),anchor.getZ());
            case "spider_tree"->new thaumcraft.world.LegacyGreatwoodTree().generate(level,new java.util.Random(random.nextLong()),anchor.getX(),anchor.getY(),anchor.getZ(),true);
            default->new thaumcraft.world.LegacyGreatwoodTree().generate(level,new java.util.Random(random.nextLong()),anchor.getX(),anchor.getY(),anchor.getZ(),false);
        };
        if(placed){
            var index=StructureIndexData.get(level);
            var mapped=switch(kind.toLowerCase(Locale.ROOT).replace("-","_")){
                case "mound"->StructureIndexData.Kind.MOUND;
                case "hilltop"->StructureIndexData.Kind.HILLTOP;
                case "ring"->StructureIndexData.Kind.RING_ALTAR;
                case "spider_tree"->StructureIndexData.Kind.SPIDER_TREE;
                default->null;
            };
            if(mapped!=null)index.record(level,mapped,anchor.getX(),anchor.getY(),anchor.getZ());
            NodeFactory.createSmall(level,anchor.above(2),random);
            source.sendSuccess(()->Component.literal("Placed "+kind+" at "+anchor.getX()+" "+anchor.getY()+" "+anchor.getZ()),true);
            return 1;
        }
        source.sendFailure(Component.literal("Could not place "+kind+" at "+anchor.toShortString()));
        return 0;
    }

    private static int cultists(CommandContext<CommandSourceStack> context,BlockPos argument){
        var source=context.getSource();
        var level=source.getLevel();
        var anchor=argument==null?BlockPos.containing(source.getPosition()):argument;
        int placed=0;
        for(int[] offset:new int[][]{{-2,-2},{-2,2},{2,-2},{2,2}}){
            var pos=anchor.offset(offset[0],0,offset[1]);
            var mob=thaumcraft.entities.R5Entities.CULTIST_CLERIC.get().create(level);
            if(mob==null)continue;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),net.minecraft.world.entity.MobSpawnType.COMMAND,null);
            if(level.addFreshEntity(mob)){placed++;mob.setRitualist(true);}
        }
        for(int i=0;i<2;i++){
            var mob=thaumcraft.entities.R5Entities.CULTIST_KNIGHT.get().create(level);
            if(mob==null)continue;
            var pos=anchor.offset(i==0?-4:4,0,0);
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),net.minecraft.world.entity.MobSpawnType.COMMAND,null);
            if(level.addFreshEntity(mob))placed++;
        }
        if(placed>0){final var total=placed;source.sendSuccess(()->Component.literal("Summoned "+total+" cultists"),true);return total;}
        source.sendFailure(Component.literal("Could not summon cultists"));
        return 0;
    }

    private static int maze(CommandContext<CommandSourceStack> context,BlockPos argument,int width,int height){
        var source=context.getSource();
        var level=source.getLevel();
        var anchor=argument==null?BlockPos.containing(source.getPosition()):argument;
        var data=thaumcraft.world.dim.LabyrinthData.get(level);
        if(data.inRange(anchor.getX()>>4,anchor.getZ()>>4,width,height)){
            source.sendFailure(Component.literal("Maze overlaps an existing labyrinth allocation"));
            return 0;
        }
        var generated=data.generate(anchor.getX()>>4,anchor.getZ()>>4,width,height,level.random.nextLong());
        if(generated==null){source.sendFailure(Component.literal("Maze overlaps an existing labyrinth allocation"));return 0;}
        source.sendSuccess(()->Component.literal("Allocated "+width+"x"+height+" maze at chunk "+(anchor.getX()>>4)+" "+(anchor.getZ()>>4)),true);
        return 1;
    }

    private static int locate(CommandContext<CommandSourceStack> context,String kind){
        return thaumcraft.world.NaturalStructureLocate.start(context.getSource(),kind);
    }

    private ThaumcraftCommands(){}
}
