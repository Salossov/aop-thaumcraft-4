package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import thaumcraft.registry.*;
import thaumcraft.production.InfusionMatrixBlockEntity;

final class DecorVerification {
    private static final BlockPos POS=new BlockPos(1408,120,0);
    static void run(ServerLevel l,BiConsumer<Boolean,String> check)throws Exception{
        CandleGeometryVerification.run(l,check);
        l.getChunkAt(POS);for(int x=-13;x<=13;x++)for(int z=-13;z<=13;z++)for(int y=-3;y<=2;y++)l.setBlockAndUpdate(POS.offset(x,y,z),y==-3?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        for(var block:List.of(R3Content.GREATWOOD_PLANKS.get(),R3Content.SILVERWOOD_PLANKS.get()))check.accept(block.getFlammability(block.defaultBlockState(),l,POS,Direction.UP)==20&&block.getFireSpreadSpeed(block.defaultBlockState(),l,POS,Direction.UP)==5,"original magical plank fire rates: "+block);
        for(var block:List.of(R4Content.GREATWOOD_SLAB.get(),R4Content.SILVERWOOD_SLAB.get())){
            var single=block.defaultBlockState();l.setBlockAndUpdate(POS,single);var one=Block.getDrops(single,l,POS,null);
            var doubled=single.setValue(SlabBlock.TYPE,SlabType.DOUBLE);l.setBlockAndUpdate(POS,doubled);var two=Block.getDrops(doubled,l,POS,null);
            check.accept(one.size()==1&&one.getFirst().getCount()==1&&two.size()==1&&two.getFirst().getCount()==2&&two.getFirst().is(block.asItem()),"double slab drops exactly two matching magical slabs: "+block);
            check.accept(single.getShape(l,POS).max(Direction.Axis.Y)==.5&&doubled.getShape(l,POS).max(Direction.Axis.Y)==1,"single and double slab geometry: "+block);
        }
        var stairs=R4Content.GREATWOOD_STAIRS.get();var straight=stairs.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST);l.setBlockAndUpdate(POS,straight);l.setBlockAndUpdate(POS.east(),stairs.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        check.accept(l.getBlockState(POS).getValue(StairBlock.SHAPE)==StairsShape.OUTER_LEFT,"native magical stairs form outer corners when neighboring stairs turn");
        l.setBlockAndUpdate(POS,Blocks.AIR.defaultBlockState());
        for(int i=0;i<R4Content.CANDLES.size();i++){
            var candle=R4Content.CANDLES.get(i).get();var p=POS.offset(i+1,-1,0);l.getChunkAt(p);l.setBlockAndUpdate(p.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(p,candle.defaultBlockState());
            check.accept(l.getBlockState(p).canSurvive(l,p)&&l.getBlockState(p).getLightEmission(l,p)==14&&l.getBlockState(p).getCollisionShape(l,p,CollisionContext.empty()).isEmpty()&&candle.canStabaliseInfusion(l,p),"candle colour "+i+" lights, survives support, permits movement and stabilises infusion");
            var drops=Block.getDrops(l.getBlockState(p),l,p,null);check.accept(drops.size()==1&&drops.getFirst().is(candle.asItem()),"candle colour "+i+" preserves identity when broken");
        }
        var unsupported=POS.offset(1,-1,0);l.setBlockAndUpdate(unsupported.below(),Blocks.AIR.defaultBlockState());check.accept(l.getBlockState(unsupported).isAir(),"removing support removes the candle through native neighbor updates");
        for(int x=2;x<=16;x++)l.setBlockAndUpdate(POS.offset(x,-1,0),Blocks.AIR.defaultBlockState());
        l.setBlockAndUpdate(POS,R3Content.INFUSION_MATRIX.get().defaultBlockState());var matrix=(InfusionMatrixBlockEntity)l.getBlockEntity(POS);
        for(int x=1;x<=6;x++)for(int z=1;z<=2;z++)for(int sign:new int[]{-1,1}){var p=POS.offset(x*sign,-1,z*sign);l.setBlockAndUpdate(p.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(p,R4Content.CANDLES.get((x+z)%16).get().defaultBlockState());}
        matrix.getSurroundings();check.accept(matrix.symmetry==-2,"24 symmetrically placed candles reduce actual matrix instability by the original truncated 2 points");
        var recipeNames=new ArrayList<>(List.of("arcane_stone_stairs","greatwood_stairs","silverwood_stairs","greatwood_slab","silverwood_slab","amber_bricks","obsidian_tile","amber_from_block","amber_from_bricks","white_tallow_candle","thaumium_block"));
        for(var c:List.of("white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"))recipeNames.add(c+"_tallow_candle_dye");
        for(var id:recipeNames){var recipe=(CraftingRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:"+id)).orElseThrow().value();var stacks=recipe.getIngredients().stream().map(g->g==Ingredient.EMPTY?ItemStack.EMPTY:g.getItems()[0].copy()).toList();var grid=recipe instanceof ShapedRecipe s?CraftingInput.of(s.getWidth(),s.getHeight(),stacks):CraftingInput.of(stacks.size()>3?3:stacks.size(),stacks.size()>3?3:1,stacks);
            check.accept(recipe.matches(grid,l)&&!recipe.assemble(grid,l.registryAccess()).isEmpty()&&!recipe.assemble(grid,l.registryAccess()).is(Items.BARRIER),"original decor pattern assembles loaded native recipe: "+id);
        }
        var whiteRecipe=(ShapelessRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:white_tallow_candle_dye")).orElseThrow().value();
        for(var colored:R4Content.CANDLES)check.accept(whiteRecipe.matches(CraftingInput.of(2,1,List.of(new ItemStack(Items.BONE_MEAL),new ItemStack(colored.get()))),l),"bone meal restores white from candle colour: "+colored.get());
    }
    static void reload(ServerLevel l,BiConsumer<Boolean,String> check){for(int x=-13;x<=13;x+=13)for(int z=-13;z<=13;z+=13)l.getChunkAt(POS.offset(x,0,z));var tile=l.getBlockEntity(POS);check.accept(tile instanceof InfusionMatrixBlockEntity,"matrix fixture survives dedicated-server restart");var matrix=(InfusionMatrixBlockEntity)tile;matrix.getSurroundings();check.accept(matrix.symmetry==-2,"persisted candles still stabilize the matrix after dedicated-server restart");}
}
