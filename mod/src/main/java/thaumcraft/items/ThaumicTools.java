package thaumcraft.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.IRepairable;

public final class ThaumicTools {
    private static Item.Properties properties() { return new Item.Properties().rarity(Rarity.RARE); }
    private static void restore(ItemStack stack, Level level, Entity entity, R3ToolTier tier) {
        if (!level.isClientSide && (tier == R3ToolTier.VOID || tier == R3ToolTier.PRIMAL)
                && entity.tickCount % 20 == 0 && stack.isDamaged()) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    public static final class Axe extends AxeItem implements IRepairable {
        private final R3ToolTier tier;
        public Axe(R3ToolTier tier) { super(tier, properties().attributes(AxeItem.createAttributes(tier, 3, -3))); this.tier=tier; }
        @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){restore(s,l,e,tier);}
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            if(tier!=R3ToolTier.ELEMENTAL)return super.use(level,player,hand);
            player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 72000;}
        @Override public void onUseTick(Level level,LivingEntity user,ItemStack stack,int remaining){
            if(tier!=R3ToolTier.ELEMENTAL||!(user instanceof Player player))return;
            for(var item:level.getEntitiesOfClass(ItemEntity.class,player.getBoundingBox().inflate(10))){
                if(item instanceof thaumcraft.entities.FollowingItem following&&following.isFollowing())continue;
                var direction=new Vec3(item.getX()-player.getX(),item.getY()-player.getY()+player.getBbHeight()/2,item.getZ()-player.getZ());
                if(direction.lengthSqr()<1e-8)continue;var motion=item.getDeltaMovement().subtract(direction.normalize().scale(.3));
                item.setDeltaMovement(Math.clamp(motion.x,-.35,.35),Math.clamp(motion.y,-.35,.35),Math.clamp(motion.z,-.35,.35));item.hasImpulse=true;
                if(level.isClientSide)thaumcraft.client.ElementalToolEffects.gather(item);
            }
        }
        public boolean onBlockStartBreak(ItemStack stack,BlockPos pos,Player player){
            return tier==R3ToolTier.ELEMENTAL&&ToolActions.fellFurthestLog(stack,pos,player);
        }
    }

    public static final class Pickaxe extends PickaxeItem implements IRepairable {
        private final R3ToolTier tier;
        public Pickaxe(R3ToolTier tier){super(tier,properties().attributes(PickaxeItem.createAttributes(tier,2,-2.8f)));this.tier=tier;}
        @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){restore(s,l,e,tier);}
        @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity target){
            if(tier==R3ToolTier.ELEMENTAL&&!player.level().isClientSide&&(!(target instanceof Player)||player.getServer().isPvpAllowed()))target.igniteForSeconds(2);
            return super.onLeftClickEntity(stack,player,target);
        }
        @Override public InteractionResult useOn(UseOnContext context){
            if(tier!=R3ToolTier.ELEMENTAL)return super.useOn(context);
            if(context.getPlayer() instanceof ServerPlayer player){
                context.getItemInHand().hurtAndBreak(5,player,LivingEntity.getSlotForHand(context.getHand()));
                ToolActions.scanOres(player,context.getClickedPos());
                context.getLevel().playSound(null,context.getClickedPos(),thaumcraft.registry.R2Sounds.WAND_FAIL.get(),net.minecraft.sounds.SoundSource.PLAYERS,.2f,.2f+context.getLevel().random.nextFloat()*.2f);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
    }

    public static final class Shovel extends ShovelItem implements IRepairable {
        private final R3ToolTier tier;
        public Shovel(R3ToolTier tier){super(tier,properties().attributes(ShovelItem.createAttributes(tier,1,-3)));this.tier=tier;}
        @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){restore(s,l,e,tier);}
        public boolean onBlockStartBreak(ItemStack stack,BlockPos pos,Player player){
            return tier==R3ToolTier.ELEMENTAL&&ToolActions.minePlane(stack,pos,player,false);
        }
        @Override public InteractionResult useOn(UseOnContext context){
            if(tier!=R3ToolTier.ELEMENTAL||context.getPlayer()==null)return super.useOn(context);
            return ToolActions.placePlane(context);
        }
    }

    public static final class Hoe extends HoeItem implements IRepairable {
        private final R3ToolTier tier;
        public Hoe(R3ToolTier tier){super(tier,properties().attributes(HoeItem.createAttributes(tier,-tier.getAttackDamageBonus(),-1)));this.tier=tier;}
        @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){restore(s,l,e,tier);}
        @Override public InteractionResult useOn(UseOnContext context){
            if(tier!=R3ToolTier.ELEMENTAL||context.getPlayer()==null||context.getPlayer().isShiftKeyDown())return super.useOn(context);
            var level=context.getLevel();var origin=context.getClickedPos();boolean worked=false,tilled=false;
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
                var pos=origin.offset(x,0,z);var state=level.getBlockState(pos);
                if((state.is(Blocks.DIRT)||state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.DIRT_PATH))&&level.getBlockState(pos.above()).isAir()){
                    if(super.useOn(new UseOnContext(context.getPlayer(),context.getHand(),new net.minecraft.world.phys.BlockHitResult(pos.getCenter(),context.getClickedFace(),pos,false))).consumesAction()){
                        if(!level.isClientSide)net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk((net.minecraft.server.level.ServerLevel)level,new net.minecraft.world.level.ChunkPos(pos),new thaumcraft.network.BlockSparklePayload(pos,0x803200));
                        worked=true;tilled=true;
                    }
                }
            }
            if(!worked&&context.getPlayer() instanceof ServerPlayer player){
                var state=level.getBlockState(origin);
                if(state.getBlock() instanceof thaumcraft.blocks.MagicalSaplingBlock sapling){
                    boolean silver=state.is(thaumcraft.registry.R3Content.SILVERWOOD_SAPLING.get());int required=silver?150:20;
                    if(context.getItemInHand().getMaxDamage()-context.getItemInHand().getDamageValue()>=required&&level.mayInteract(player,origin)){
                        sapling.advanceTree((net.minecraft.server.level.ServerLevel)level,origin,state,level.random);context.getItemInHand().hurtAndBreak(silver?25:5,player,LivingEntity.getSlotForHand(context.getHand()));worked=true;
                    }
                }
                if(!worked&&level.mayInteract(player,origin)&&state.getBlock() instanceof BonemealableBlock plant&&plant.isValidBonemealTarget(level,origin,state)){
                    if(plant.isBonemealSuccess(level,level.random,origin,state))plant.performBonemeal((net.minecraft.server.level.ServerLevel)level,level.random,origin,state);
                    context.getItemInHand().hurtAndBreak(1,player,LivingEntity.getSlotForHand(context.getHand()));worked=true;
                }
                if(worked)net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk((net.minecraft.server.level.ServerLevel)level,new net.minecraft.world.level.ChunkPos(origin),new thaumcraft.network.BlockSparklePayload(origin,0x803200));
            }
            if(worked&&!tilled&&!level.isClientSide)level.playSound(null,origin,thaumcraft.registry.R2Sounds.CRAFT_FINISH.get(),net.minecraft.sounds.SoundSource.PLAYERS,.75f,.9f+level.random.nextFloat()*.2f);
            return worked?InteractionResult.sidedSuccess(level.isClientSide):InteractionResult.PASS;
        }
    }

    public static final class Sword extends SwordItem implements IRepairable {
        private final R3ToolTier tier;
        public Sword(R3ToolTier tier){super(tier,properties().attributes(SwordItem.createAttributes(tier,3,-2.4f)));this.tier=tier;}
        @Override public net.minecraft.world.phys.AABB getSweepHitBox(ItemStack stack,Player player,Entity target){return tier==R3ToolTier.ELEMENTAL?new net.minecraft.world.phys.AABB(0,0,0,0,0,0):super.getSweepHitBox(stack,player,target);}
        @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity hitter){if(tier==R3ToolTier.VOID&&!target.level().isClientSide&&(!(target instanceof Player)||!(hitter instanceof Player)||target.getServer().isPvpAllowed()))target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,60));return super.hurtEnemy(stack,target,hitter);}
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flags){super.appendHoverText(stack,context,lines,flags);if(tier==R3ToolTier.VOID)lines.add(net.minecraft.network.chat.Component.translatable("enchantment.special.sapless").withStyle(net.minecraft.ChatFormatting.GOLD));}
        @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){restore(s,l,e,tier);}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            if(tier!=R3ToolTier.ELEMENTAL)return super.use(level,player,hand);player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 72000;}
        @Override public void onUseTick(Level level,LivingEntity user,ItemStack stack,int remaining){
            if(tier!=R3ToolTier.ELEMENTAL||!(user instanceof Player player))return;var velocity=player.getDeltaMovement();
            if(level.isClientSide)thaumcraft.client.ElementalToolEffects.wind(player);
            if(velocity.y<0){velocity=new Vec3(velocity.x,velocity.y/1.2,velocity.z);player.fallDistance/=1.2f;}
            double y=velocity.y+.08;player.setDeltaMovement(velocity.x,y>.5?.2:y,velocity.z);player.hasImpulse=true;
            if(player instanceof ServerPlayer server&&server.connection!=null)((thaumcraft.mixin.FloatingCounterAccessor)server.connection).thaumcraft$resetFloatingTicks(0);
            for(var target:level.getEntities(player,player.getBoundingBox().inflate(2.5))){
                if(target instanceof Player||target==player.getVehicle()||!target.isAlive())continue;
                var delta=target.position().subtract(player.position());target.setDeltaMovement(target.getDeltaMovement().add(delta.scale(1/(2.5*(delta.length()+.1)))));target.hasImpulse=true;
            }
            if(!level.isClientSide&&(72000-remaining)%20==0){level.playSound(null,player.getX(),player.getY(),player.getZ(),thaumcraft.registry.R2Sounds.WIND.get(),net.minecraft.sounds.SoundSource.PLAYERS,.5f,.9f+level.random.nextFloat()*.2f);stack.hurtAndBreak(1,player,LivingEntity.getSlotForHand(player.getUsedItemHand()));}
        }
        @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity target){
            if(tier==R3ToolTier.ELEMENTAL)ToolActions.sweep(player,target);return super.onLeftClickEntity(stack,player,target);
        }
    }

    public static final class Crusher extends PickaxeItem implements IRepairable {
        public Crusher(){super(R3ToolTier.PRIMAL,properties().rarity(Rarity.EPIC).attributes(PickaxeItem.createAttributes(R3ToolTier.PRIMAL,3.5f,-2.8f)));}
        @Override public void inventoryTick(ItemStack s,Level l,Entity e,int slot,boolean selected){restore(s,l,e,R3ToolTier.PRIMAL);}
        public boolean onBlockStartBreak(ItemStack stack,BlockPos pos,Player player){return ToolActions.minePlane(stack,pos,player,true);}
        @Override public float getDestroySpeed(ItemStack stack,BlockState state){return state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL)?R3ToolTier.PRIMAL.getSpeed():super.getDestroySpeed(stack,state);}
    }

    private ThaumicTools(){}
}
