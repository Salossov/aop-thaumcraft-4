package thaumcraft.verification;

import java.util.*;
import java.util.function.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import thaumcraft.blocks.*;
import thaumcraft.registry.R4Content;

final class EarLifterVerification {
    private static final BlockPos EAR=new BlockPos(1584,120,0),LIFT=EAR.east(8);
    private static void play(ServerLevel l,BlockPos p,int note,NoteBlockInstrument instrument){l.getChunkAt(p);var s=Blocks.NOTE_BLOCK.defaultBlockState().setValue(NoteBlock.NOTE,note).setValue(NoteBlock.INSTRUMENT,instrument);l.setBlock(p,s,2);s.triggerEvent(l,p,0,0);}
    static void run(ServerLevel l,BiConsumer<Boolean,String> check){
        l.getChunkAt(EAR);l.getChunkAt(LIFT);for(int y=-3;y<=32;y++)for(int x=-1;x<=9;x++)for(int z=-1;z<=1;z++)l.setBlockAndUpdate(EAR.offset(x,y,z),y==-3?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        l.setBlockAndUpdate(EAR,R4Content.ARCANE_EAR.get().defaultBlockState());var ear=(ArcaneEarBlockEntity)l.getBlockEntity(EAR);for(int i=0;i<25;i++)ear.changePitch();check.accept(ear.note()==0,"ear pitch cycles through all original twenty-five notes");for(int i=0;i<7;i++)ear.changePitch();
        var supports=List.of(Blocks.DIRT,Blocks.STONE,Blocks.SAND,Blocks.GLASS,Blocks.OAK_PLANKS);
        for(int i=0;i<supports.size();i++){l.setBlockAndUpdate(EAR.below(),supports.get(i).defaultBlockState());ear.updateTone();check.accept(ear.tone()==i,"ear derives original instrument "+ArcaneEarBlockEntity.INSTRUMENTS[i]+" from its support");}
        l.setBlockAndUpdate(EAR.below(),Blocks.DIRT.defaultBlockState());ear.updateTone();
        play(l,EAR.east(3),8,NoteBlockInstrument.HARP);ear.tick();check.accept(ear.redstoneTicks()==0,"wrong pitch does not activate receiver");
        play(l,EAR.east(3),7,NoteBlockInstrument.BASS);ear.tick();check.accept(ear.redstoneTicks()==0,"wrong instrument does not activate receiver");
        play(l,EAR.east(65),7,NoteBlockInstrument.HARP);ear.tick();check.accept(ear.redstoneTicks()==0,"matching note outside original sixty-four block radius is ignored");
        play(l,EAR.east(64),7,NoteBlockInstrument.HARP);ear.tick();check.accept(ear.redstoneTicks()==10&&l.getSignal(EAR,Direction.NORTH)==15&&l.getDirectSignal(EAR,Direction.DOWN)==15,"native note playback at inclusive radius boundary starts full weak and strong redstone pulse");
        for(int i=0;i<9;i++)ear.tick();check.accept(ear.redstoneTicks()==1&&l.getBlockState(EAR).getValue(ArcaneEarBlock.POWERED),"receiver pulse remains active through nine ticks");ear.tick();check.accept(ear.redstoneTicks()==0&&!l.getBlockState(EAR).getValue(ArcaneEarBlock.POWERED),"receiver pulse ends on the tenth tick and old queued event is not replayed");
        l.setBlockAndUpdate(EAR.above(),Blocks.STONE.defaultBlockState());play(l,EAR.east(3),7,NoteBlockInstrument.HARP);ear.tick();check.accept(ear.redstoneTicks()==10,"blocked receiver still activates redstone, suppressing only its visual note");for(int i=0;i<10;i++)ear.tick();l.setBlockAndUpdate(EAR.above(),Blocks.AIR.defaultBlockState());
        Consumer<NoteBlockEvent.Play> cancel=e->{if(e.getPos().equals(EAR.east(3)))e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,cancel);try{play(l,EAR.east(3),7,NoteBlockInstrument.HARP);ear.tick();check.accept(ear.redstoneTicks()==0,"cancelled NeoForge note playback does not activate ear");}finally{NeoForge.EVENT_BUS.unregister(cancel);}
        play(l,EAR.east(3),7,NoteBlockInstrument.BELL);ear.tick();check.accept(ear.redstoneTicks()==0,"modern instruments without an original TC4 tone do not alias a legacy tone");
        var badTag=new CompoundTag();badTag.putByte("note",(byte)-9);badTag.putByte("tone",(byte)0);ear.loadWithComponents(badTag,l.registryAccess());check.accept(ear.note()==0,"corrupt saved negative pitch is clamped like the original receiver");for(int i=0;i<7;i++)ear.changePitch();
        l.setBlockAndUpdate(LIFT,R4Content.LEVITATOR.get().defaultBlockState());var lift=(LevitatorBlockEntity)l.getBlockEntity(LIFT);lift.tick();check.accept(lift.rangeAbove()==10,"one levitator reaches ten air blocks");
        var stack=R4Content.LEVITATOR.get().defaultBlockState();l.setBlockAndUpdate(LIFT.below(),stack);for(int i=0;i<100;i++)lift.tick();check.accept(lift.rangeAbove()==20,"second unpowered levitator adds ten blocks of reach");l.setBlockAndUpdate(LIFT.below(2),stack);for(int i=0;i<100;i++)lift.tick();check.accept(lift.rangeAbove()==30,"third levitator extends reach to thirty");
        l.setBlockAndUpdate(LIFT.below().east(),Blocks.REDSTONE_BLOCK.defaultBlockState());lift.invalidateRange();lift.tick();check.accept(lift.rangeAbove()==10,"powered lower levitator cuts off stack contribution");l.setBlockAndUpdate(LIFT.below().east(),Blocks.AIR.defaultBlockState());
        l.setBlockAndUpdate(LIFT.above(6),Blocks.STONE.defaultBlockState());lift.invalidateRange();lift.tick();check.accept(lift.rangeAbove()==5,"first opaque obstacle stops lift range before the blocked cell");
        var entity=new ItemEntity(l,LIFT.getX()+.5,LIFT.getY()+1.5,.5,new ItemStack(Items.COAL));l.addFreshEntity(entity);entity.fallDistance=8;entity.setDeltaMovement(.02,-.2,.03);lift.tick();check.accept(Math.abs(entity.getDeltaMovement().y+.1)<.00001&&entity.getDeltaMovement().x==.02&&entity.getDeltaMovement().z==.03&&entity.fallDistance==0,"loose item gains original upward acceleration and resets falling distance without lateral drift");
        entity.setDeltaMovement(0,.36,0);lift.tick();check.accept(entity.getDeltaMovement().y==.36,"lift does not accelerate entities already above the original .35 threshold");
        var sheep=EntityType.SHEEP.create(l);sheep.moveTo(LIFT.getX()+.5,LIFT.getY()+2,.5,0,0);sheep.setDeltaMovement(Vec3.ZERO);l.addFreshEntity(sheep);lift.tick();check.accept(Math.abs(sheep.getDeltaMovement().y-.1)<.00001,"pushable creature receives lift acceleration");
        var player=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("01234567-1111-2222-3333-012345678905"),"LifterQA"));player.moveTo(LIFT.getX()+.5,LIFT.getY()+2,.5,0,0);player.setShiftKeyDown(true);player.setDeltaMovement(0,-.2,0);l.addFreshEntity(player);lift.tick();check.accept(Math.abs(player.getDeltaMovement().y+.18)<.00001,"sneaking player slows descent by original .9 factor instead of rising");player.setShiftKeyDown(false);
        l.setBlockAndUpdate(LIFT.east(),Blocks.REDSTONE_BLOCK.defaultBlockState());entity.setDeltaMovement(Vec3.ZERO);lift.tick();check.accept(entity.getDeltaMovement().equals(Vec3.ZERO)&&l.getBlockState(LIFT).getValue(LevitatorBlock.POWERED),"adjacent redstone shuts off lift motion and its glowing state");l.setBlockAndUpdate(LIFT.east(),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(LIFT.above().east(),Blocks.REDSTONE_BLOCK.defaultBlockState());entity.setDeltaMovement(Vec3.ZERO);lift.tick();check.accept(entity.getDeltaMovement().equals(Vec3.ZERO),"redstone powering the block above also shuts off lift");l.setBlockAndUpdate(LIFT.above().east(),Blocks.AIR.defaultBlockState());
        l.setBlockAndUpdate(LIFT.below(),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(LIFT.below(2),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(LIFT.above(6),Blocks.AIR.defaultBlockState());for(int i=0;i<100;i++)lift.tick();check.accept(lift.rangeAbove()==10,"removing supporting lift stack refreshes range within original hundred-tick interval");
        check.accept(stack.isFaceSturdy(l,LIFT,Direction.NORTH)&&!stack.isFaceSturdy(l,LIFT,Direction.UP)&&!stack.isFaceSturdy(l,LIFT,Direction.DOWN),"levitator retains solid support on side faces only");
        for(var id:List.of("arcane_ear","levitator")){var recipe=(thaumcraft.crafting.ArcaneRecipe)l.getRecipeManager().byKey(ResourceLocation.parse("thaumcraft:"+id)).orElseThrow().value();var items=recipe.getIngredients().stream().map(g->g==Ingredient.EMPTY?ItemStack.EMPTY:g.getItems()[0].copy()).toList();var grid=CraftingInput.of(recipe.pattern().width(),recipe.pattern().height(),items);check.accept(recipe.matches(grid,l)&&recipe.assemble(grid,l.registryAccess()).getCount()==1,"loaded native original arcane recipe crafts device: "+id);var research=thaumcraft.data.ThaumcraftData.get(l.getServer()).research().get(recipe.research());check.accept(research!=null&&research.recipePages().stream().anyMatch(k->thaumcraft.data.ResearchRecipePages.resolve(k).equals(id)),"device research exposes its recipe in the book: "+id);}
        entity.discard();sheep.discard();player.discard();l.setBlockAndUpdate(EAR.below(),Blocks.GLASS.defaultBlockState());ear.updateTone();play(l,EAR.east(3),7,NoteBlockInstrument.HAT);ear.tick();
    }
    static void reload(ServerLevel l,BiConsumer<Boolean,String> check){l.getChunkAt(EAR);l.getChunkAt(LIFT);var ear=(ArcaneEarBlockEntity)l.getBlockEntity(EAR);check.accept(ear!=null&&ear.note()==7&&ear.tone()==3,"ear pitch and glass-support tone persist across a real server restart");ear.tick();check.accept(ear.redstoneTicks()==0&&!l.getBlockState(EAR).getValue(ArcaneEarBlock.POWERED),"transient ear redstone pulse resets on load without leaving a permanently powered block");play(l,EAR.east(3),7,NoteBlockInstrument.HAT);ear.tick();check.accept(ear.redstoneTicks()==10,"restored tuned receiver still hears native note block playback");var lift=(LevitatorBlockEntity)l.getBlockEntity(LIFT);lift.tick();check.accept(lift.rangeAbove()==10&&!lift.gettingPower(),"restored levitator reconstructs its air range without stale power state");}
}
