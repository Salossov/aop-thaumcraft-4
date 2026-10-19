package thaumcraft.production;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aspects.*;
import thaumcraft.api.crafting.*;
import thaumcraft.crafting.R3Recipes;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.items.WandVis;
import thaumcraft.registry.*;
import thaumcraft.effects.TaintEffects;
import thaumcraft.fluids.FluxBlock;

/** Server-owned TC4 ritual, including the in-progress output and remaining resources. */
public final class InfusionMatrixBlockEntity extends BlockEntity implements thaumcraft.api.aspects.IAspectDisplay {
    public boolean active,crafting;
    public int symmetry,instability,stabilizerCount,pedestalPenalty,pedestalCount;
    public float stabilization;
    public int recipeInstability(){return recipeInstability;}
    public BlockPos effectSource;
    public int effectColor,effectKind;
    public ItemStack effectItem=ItemStack.EMPTY;
    public int effectSerial(){return effectSerial;}
    public float clientStartup;
    public int clientCraftTicks;
    private int effectSerial;
    private final List<BlockPos> pedestals=new ArrayList<>();
    private final LinkedHashMap<String,Integer> remainingEssentia=new LinkedHashMap<>();
    private final List<Ingredient> remainingIngredients=new ArrayList<>();
    private ItemStack originalInput=ItemStack.EMPTY,output=ItemStack.EMPTY;
    private UUID owner;
    private int recipeInstability,count,itemCount,essentiaFlightTicks,remainingExperience,xpCooldown;
    public int essentiaFlightTicks(){return essentiaFlightTicks;}
    private boolean checkSurroundings=true;
    private final RitualEssentia sources=new RitualEssentia();
    public InfusionMatrixBlockEntity(BlockPos p,BlockState s){super(R3Content.INFUSION_ENTITY.get(),p,s);}
    public Map<String,Integer> remainingEssentia(){return Collections.unmodifiableMap(remainingEssentia);}
    @Override public thaumcraft.api.aspects.AspectList displayedAspects(){return thaumcraft.api.aspects.IAspectDisplay.snapshot(remainingEssentia);}
    public int remainingIngredients(){return remainingIngredients.size();}
    public List<BlockPos> pedestals(){return List.copyOf(pedestals);}
    private PedestalBlockEntity pedestal(BlockPos p){return level!=null&&level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof PedestalBlockEntity tile?tile:null;}
    public boolean validLocation(){
        if(pedestal(worldPosition.below(2))==null)return false;
        for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){var p=worldPosition.offset(x,-2,z);if(!level.hasChunkAt(p)||!level.getBlockState(p).is(R3Content.INFUSION_PILLAR.get())||level.getBlockState(p).getValue(InfusionPillarBlock.HALF)!=DoubleBlockHalf.LOWER)return false;}
        return true;
    }
    private boolean rawStructure(){
        if(pedestal(worldPosition.below(2))==null)return false;
        for(int y=-2;y<=0;y++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
            var p=worldPosition.offset(x,y,z);if(!level.hasChunkAt(p))return false;
            if(x==0&&z==0&&(y==0||y==-2))continue;
            var s=level.getBlockState(p);
            if(Math.abs(x)==1&&Math.abs(z)==1&&y<0){if(!s.is((y==-2?R3Content.ARCANE_STONE_BRICKS:R3Content.ARCANE_STONE).get()))return false;}
            else if(!s.isAir())return false;
        }return true;
    }
    public boolean wandClick(ServerPlayer player){return wandClick(player,player.getMainHandItem());}
    public boolean wandClick(ServerPlayer player,ItemStack wand){
        if(crafting)return false;
        if(!active){
            if(validLocation()){active=true;sync();return true;}
            if(!KnowledgeService.get(player).completedResearch().contains("INFUSION")||!rawStructure())return false;
            var cost=new LinkedHashMap<String,Integer>();Aspects.PRIMALS.forEach(a->cost.put(a.getTag(),25));
            if(!WandVis.consume(wand,cost,WandVis.discount(player),true))return false;
            for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){int orientation=x<0?(z<0?2:3):(z<0?4:5);var state=R3Content.INFUSION_PILLAR.get().defaultBlockState().setValue(InfusionPillarBlock.ORIENTATION,orientation);level.setBlock(worldPosition.offset(x,-2,z),state,2);level.setBlock(worldPosition.offset(x,-1,z),state.setValue(InfusionPillarBlock.HALF,DoubleBlockHalf.UPPER),3);}
            active=true;checkSurroundings=true;sync();return true;
        }
        return start(player);
    }
    public boolean start(ServerPlayer player){
        if(!active||crafting||!validLocation())return false;getSurroundings();var centre=pedestal(worldPosition.below(2));if(centre.isEmpty())return false;
        var stacks=pedestals.stream().map(this::pedestal).filter(Objects::nonNull).filter(t->!t.isEmpty()).map(t->t.getItem(0).copy()).toList();
        if(stacks.isEmpty())return false;var input=new InfusionInput(centre.getItem(0),stacks);
        var recipe=level.getRecipeManager().getAllRecipesFor(R3Recipes.INFUSION_TYPE.get()).stream().filter(h->KnowledgeService.get(player).completedResearch().contains(h.value().research())&&h.value().matches(input)&&h.value().mayStart(player,input.central())).sorted(Comparator.comparing(h->h.id().toString())).findFirst().orElse(null);
        if(recipe==null)return false;
        originalInput=input.central().copy();remainingExperience=recipe.value().experienceCost(originalInput);xpCooldown=0;output=recipe.value().getRecipeOutput(originalInput);remainingIngredients.clear();remainingIngredients.addAll(recipe.value().components(originalInput));remainingEssentia.clear();remainingEssentia.putAll(recipe.value().aspects(originalInput));recipeInstability=recipe.value().instability(originalInput);instability=symmetry+recipeInstability;owner=player.getUUID();crafting=true;itemCount=0;essentiaFlightTicks=0;sources.refresh();sound(R2Sounds.CRAFT_START.get(),.5f,1);sync();return true;
    }
    public void serverTick(){
        count++;if(essentiaFlightTicks>0){essentiaFlightTicks--;setChanged();}if(checkSurroundings){checkSurroundings=false;getSurroundings();}
        if(count%(crafting?20:100)==0&&!validLocation()){active=false;sync();return;}
        if(xpCooldown>0)xpCooldown--;if(active&&crafting&&count%10==0&&xpCooldown==0)craftCycle();
    }
    public void clientTick(){
        if(active&&clientStartup<1){clientStartup+=Math.max(clientStartup/10,.001f);if(clientStartup>.999)clientStartup=1;}else if(!active&&clientStartup>0){clientStartup-=clientStartup/10;if(clientStartup<.001)clientStartup=0;}
        clientCraftTicks=crafting?clientCraftTicks+1:Math.min(50,Math.max(0,clientCraftTicks-2));

    }
    public void getSurroundings(){
        pedestals.clear();var stabilisers=new ArrayList<BlockPos>();
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){
            boolean skip=false;
            for(int y=-5;y<=10;y++){
                if(x==0&&z==0)continue;var p=worldPosition.offset(x,-y,z);if(!level.hasChunkAt(p))continue;
                if(!skip&&y>0&&Math.abs(x)<=8&&Math.abs(z)<=8&&pedestal(p)!=null){pedestals.add(p);skip=true;continue;}
                if(stabilises(p))stabilisers.add(p);
            }
        }
        symmetry=0;for(var p:pedestals){var first=pedestal(p);if(first==null)continue;symmetry+=2;if(!first.isEmpty())symmetry++;var mirror=pedestal(new BlockPos(worldPosition.getX()*2-p.getX(),p.getY(),worldPosition.getZ()*2-p.getZ()));if(mirror==null)continue;symmetry-=2;if(!first.isEmpty()&&!mirror.isEmpty())symmetry--;}
        pedestalCount=pedestals.size();pedestalPenalty=symmetry;stabilizerCount=stabilisers.size();float stabilising=0;for(var p:stabilisers){stabilising+=.1f;var mirror=new BlockPos(worldPosition.getX()*2-p.getX(),p.getY(),worldPosition.getZ()*2-p.getZ());if(level.hasChunkAt(mirror)&&stabilises(mirror))stabilising-=.2f;}
        stabilization=-stabilising;symmetry=(int)(symmetry+stabilising);
    }
    private boolean stabilises(BlockPos p){var block=level.getBlockState(p).getBlock();return block instanceof AbstractSkullBlock||block instanceof IInfusionStabiliser stabiliser&&stabiliser.canStabaliseInfusion(level,p);}
    public void craftCycle(){
        var centre=pedestal(worldPosition.below(2));boolean valid=centre!=null&&!centre.isEmpty()&&ItemStack.isSameItemSameComponents(centre.getItem(0),originalInput);
        if(!valid||instability>0&&level.random.nextInt(500)<=instability){instabilityEvent(level.random.nextInt(21));if(valid){sync();return;}}
        if(!valid){crafting=false;instability=0;remainingEssentia.clear();remainingIngredients.clear();output=ItemStack.EMPTY;sound(R2Sounds.CRAFT_FAIL.get(),1,.6f);sync();return;}
        if(remainingExperience>0){
            for(var target:level.getEntitiesOfClass(ServerPlayer.class,new AABB(worldPosition).inflate(10))){
                if(target.experienceLevel<=0)continue;target.giveExperienceLevels(-1);remainingExperience--;target.hurt(level.damageSources().magic(),level.random.nextInt(2));
                xpCooldown=20;level.playSound(null,target.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.PLAYERS,1,2+level.random.nextFloat()*.4f);sync();return;
            }
            if(!remainingEssentia.isEmpty()&&level.random.nextInt(3)==0){var tags=new ArrayList<>(remainingEssentia.keySet());remainingEssentia.merge(tags.get(level.random.nextInt(tags.size())),1,Integer::sum);if(level.random.nextInt(Math.max(1,50-recipeInstability*2))==0)instability=Math.min(25,instability+1);sync();}return;
        }
        if(remainingEssentia.values().stream().anyMatch(n->n>0)){
            for(var tag:remainingEssentia.keySet())if(remainingEssentia.get(tag)>0){var a=Aspect.getAspect(tag);var source=sources.drain((ServerLevel)level,worldPosition,a,12);if(source!=null){remainingEssentia.put(tag,remainingEssentia.get(tag)-1);effectSource=source;effectColor=a.getColor();effectKind=0;effectItem=ItemStack.EMPTY;essentiaFlightTicks=Math.max(essentiaFlightTicks,thaumcraft.effects.EssentiaFlow.settlingTicks(source.getCenter(),worldPosition.getCenter().add(0,-1,0),level.getGameTime()));effectSerial++;sync();return;}if(level.random.nextInt(Math.max(1,100-recipeInstability*3))==0)instability=Math.min(25,instability+1);}
            checkSurroundings=true;sync();return;
        }
        if(essentiaFlightTicks>0)return;
        if(!remainingIngredients.isEmpty()){
            for(int i=0;i<remainingIngredients.size();i++){
                for(var p:pedestals){var tile=pedestal(p);if(tile==null||tile.isEmpty()||!remainingIngredients.get(i).test(tile.getItem(0)))continue;
                    if(itemCount==0){itemCount=5;effectSource=p;effectColor=0xFFFFFF;effectKind=1;effectItem=tile.getItem(0).copyWithCount(1);effectSerial++;sync();}
                    else if(itemCount--<=1){var remainder=tile.getItem(0).getCraftingRemainingItem();tile.setItem(0,remainder);remainingIngredients.remove(i);sync();}else setChanged();return;
                }
                if(!remainingEssentia.isEmpty()&&level.random.nextInt(1+i)==0){var tags=new ArrayList<>(remainingEssentia.keySet());var a=tags.get(level.random.nextInt(tags.size()));remainingEssentia.merge(a,1,Integer::sum);if(level.random.nextInt(Math.max(1,50-recipeInstability*2))==0)instability=Math.min(25,instability+1);sync();}
            }return;
        }
        centre.setInfusionResult(output.copy());var player=owner==null?null:level.getServer().getPlayerList().getPlayer(owner);if(player!=null){output.onCraftedBy(level,player,1);thaumcraft.knowledge.WarpService.crafted(player,output);}output=ItemStack.EMPTY;originalInput=ItemStack.EMPTY;crafting=false;instability=0;remainingEssentia.clear();sound(R2Sounds.CRAFT_FINISH.get(),1,1);sync();
    }
    public void instabilityEvent(int event){
        switch(event){
            case 0,2,10,13 -> eject(0);case 6,17 -> eject(1);case 1,11 -> eject(2);case 19 -> eject(3);case 7 -> eject(4);case 4,15 -> eject(5);
            case 3,8,14 -> zap(false);case 12 -> zap(true);case 5,16 -> harm(false);case 18 -> harm(true);
            case 9 -> level.explode(null,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,1.5f+level.random.nextFloat(),Level.ExplosionInteraction.NONE);
            case 20 -> {if(thaumcraft.config.ThaumcraftConfig.WUSS_MODE.get())break;var targets=level.getEntitiesOfClass(ServerPlayer.class,new AABB(worldPosition).inflate(10));if(!targets.isEmpty()){var player=targets.get(level.random.nextInt(targets.size()));boolean sticky=level.random.nextFloat()<.25;int amount=sticky?1:1+level.random.nextInt(5);KnowledgeService.mutate(player,k->{k.setWarp(k.permanentWarp(),k.stickyWarp()+(sticky?amount:0),k.temporaryWarp()+(sticky?0:amount),k.totalWarp()+amount);return true;});}}
            default -> throw new IllegalArgumentException("Invalid infusion instability event");
        }
    }
    private void eject(int type){for(int attempt=0;attempt<50&&!pedestals.isEmpty();attempt++){var p=pedestals.get(level.random.nextInt(pedestals.size()));var tile=pedestal(p);if(tile==null||tile.isEmpty())continue;var item=tile.removeItemNoUpdate(0);if(type<3||type==5)Block.popResource(level,p,item);tile.changed();if(type==1||type==3){FluxBlock.setUnits(level,p.above(),R2Content.FLUX_GOO.get(),8);level.playSound(null,p,SoundEvents.GENERIC_SWIM,SoundSource.BLOCKS,.3f,1);}else if(type==2||type==4){FluxBlock.setUnits(level,p.above(),R2Content.FLUX_GAS.get(),8);level.playSound(null,p,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.3f,1);}else if(type==5)level.explode(null,p.getX()+.5,p.getY()+.5,p.getZ()+.5,1,Level.ExplosionInteraction.NONE);effectSource=p;effectColor=0xFFFFFF;effectKind=2;effectItem=item.copyWithCount(1);effectSerial++;discharge(p.getCenter().add(0,1,0));sync();return;}}
    private void zap(boolean all){for(var target:level.getEntitiesOfClass(LivingEntity.class,new AABB(worldPosition).inflate(10))){discharge(target.position().add(0,target.getBbHeight()/2,0));target.hurt(level.damageSources().magic(),4+level.random.nextInt(4));if(!all)break;}}
    private void discharge(net.minecraft.world.phys.Vec3 target){net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear((ServerLevel)level,null,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,32,new thaumcraft.network.InfusionZapPayload(worldPosition.getCenter(),target));}
    private void harm(boolean all){for(var target:level.getEntitiesOfClass(LivingEntity.class,new AABB(worldPosition).inflate(10))){boolean poison=level.random.nextBoolean();target.addEffect(new MobEffectInstance(poison?TaintEffects.FLUX_TAINT:TaintEffects.VIS_EXHAUST,poison?120:2400,0));if(!all)break;}}
    private void sound(SoundEvent sound,float volume,float pitch){level.playSound(null,worldPosition,sound,SoundSource.BLOCKS,volume,pitch);}
    public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){
        super.saveAdditional(t,r);t.putInt("InfusionFormat",1);t.putBoolean("Active",active);t.putBoolean("Crafting",crafting);t.putInt("Symmetry",symmetry);t.putInt("PedestalCount",pedestalCount);t.putInt("StabilizerCount",stabilizerCount);t.putInt("PedestalPenalty",pedestalPenalty);t.putFloat("Stabilization",stabilization);t.putInt("Instability",instability);t.putInt("RecipeInstability",recipeInstability);t.putInt("Count",count);t.putInt("ItemCount",itemCount);t.putInt("RemainingExperience",remainingExperience);t.putInt("XpCooldown",xpCooldown);t.putInt("EssentiaFlightTicks",essentiaFlightTicks);if(owner!=null)t.putUUID("Owner",owner);if(!originalInput.isEmpty())t.put("OriginalInput",originalInput.save(r));if(!output.isEmpty())t.put("Output",output.save(r));var costs=new CompoundTag();remainingEssentia.forEach(costs::putInt);t.put("RemainingEssentia",costs);var list=new ListTag();for(var ingredient:remainingIngredients){var entry=new CompoundTag();entry.put("Ingredient",Ingredient.CODEC_NONEMPTY.encodeStart(RegistryOps.create(NbtOps.INSTANCE,r),ingredient).getOrThrow());list.add(entry);}t.put("RemainingIngredients",list);if(effectSource!=null)t.putLong("EffectSource",effectSource.asLong());t.putInt("EffectColor",effectColor);t.putInt("EffectKind",effectKind);if(!effectItem.isEmpty())t.put("EffectItem",effectItem.save(r));t.putInt("EffectSerial",effectSerial);
    }
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
        super.loadAdditional(t,r);active=t.getBoolean("Active");crafting=t.getBoolean("Crafting");symmetry=t.getInt("Symmetry");pedestalCount=t.getInt("PedestalCount");stabilizerCount=t.getInt("StabilizerCount");pedestalPenalty=t.getInt("PedestalPenalty");stabilization=t.getFloat("Stabilization");instability=t.getInt("Instability");recipeInstability=Math.clamp(t.getInt("RecipeInstability"),0,25);count=t.getInt("Count");itemCount=Math.clamp(t.getInt("ItemCount"),0,5);remainingExperience=Math.clamp(t.getInt("RemainingExperience"),0,32767);xpCooldown=Math.clamp(t.getInt("XpCooldown"),0,20);essentiaFlightTicks=Math.clamp(t.getInt("EssentiaFlightTicks"),0,1100);owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;originalInput=t.contains("OriginalInput")?ItemStack.parseOptional(r,t.getCompound("OriginalInput")):ItemStack.EMPTY;output=t.contains("Output")?ItemStack.parseOptional(r,t.getCompound("Output")):ItemStack.EMPTY;remainingEssentia.clear();var costs=t.getCompound("RemainingEssentia");for(var tag:costs.getAllKeys()){int n=costs.getInt(tag);if(Aspect.getAspect(tag)==null||n<0||n>100000000)throw new IllegalArgumentException("Invalid saved ritual essentia");remainingEssentia.put(tag,n);}remainingIngredients.clear();var list=t.getList("RemainingIngredients",10);if(list.size()>4096)throw new IllegalArgumentException("Invalid saved ritual ingredients");for(var entry:list)remainingIngredients.add(Ingredient.CODEC_NONEMPTY.parse(RegistryOps.create(NbtOps.INSTANCE,r),((CompoundTag)entry).get("Ingredient")).getOrThrow());if(crafting&&(originalInput.isEmpty()||output.isEmpty()))throw new IllegalArgumentException("Incomplete saved ritual");effectSource=t.contains("EffectSource")?BlockPos.of(t.getLong("EffectSource")):null;effectColor=t.getInt("EffectColor");effectKind=t.getInt("EffectKind");effectItem=t.contains("EffectItem")?ItemStack.parseOptional(r,t.getCompound("EffectItem")):ItemStack.EMPTY;effectSerial=t.getInt("EffectSerial");checkSurroundings=true;sources.refresh();
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
