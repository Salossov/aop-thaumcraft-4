package thaumcraft.production;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.*;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.fluids.*;
import thaumcraft.registry.R4Content;

/** TC4's sixteen-cell search budget, five centivis per quantum and four-charge conversion. */
public final class FluxScrubberBlockEntity extends BlockEntity implements IEssentiaTransport {
    private final List<BlockPos> search=new ArrayList<>();
    private int cursor,charges,power,essentia;
    public int animationTicks;
    public FluxScrubberBlockEntity(BlockPos p,BlockState s){super(R4Content.SCRUBBER_ENTITY.get(),p,s);}
    public void tick(){
        animationTicks++;if(!(level instanceof ServerLevel server))return;
        boolean changed=false;
        if(charges>=4){charges-=4;if(server.random.nextInt(4)==0)essentia=Math.min(4,essentia+1);changed=true;}
        if(power<5){int drained=VisNetHandler.drainVis(server,worldPosition,Aspects.AIR,10);if(drained>0){power+=drained;changed=true;}}
        if(power>=5&&scrub(server))changed=true;
        if(changed)sync();
    }
    private boolean scrub(ServerLevel server){
        if(cursor>=search.size()){
            search.clear();for(int x=-16;x<=16;x++)for(int y=-16;y<=16;y++)for(int z=-16;z<=16;z++)search.add(worldPosition.offset(x,y,z));
            Collections.shuffle(search,new Random(server.random.nextLong()));cursor=0;
        }
        for(int i=0;i<16&&cursor<search.size();i++){
            var pos=search.get(cursor++);if(worldPosition.distSqr(pos)>=256||server.isOutsideBuildHeight(pos)||!server.hasChunkAt(pos))continue;
            var state=server.getBlockState(pos);if(!(state.getBlock() instanceof FluxBlock flux))continue;
            FluxBlock.setUnits(server,pos,flux,state.getValue(FiniteFluxFluid.QUANTA)-1);power-=5;charges++;
            PacketDistributor.sendToPlayersTrackingChunk(server,server.getChunkAt(pos).getPos(),new thaumcraft.network.FluxScrubPayload(pos));return true;
        }
        return false;
    }
    public int storedPower(){return power;}
    public int storedCharges(){return charges;}
    public void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    private Direction port(){return getBlockState().getValue(FluxScrubberBlock.FACING);}
    @Override public boolean isConnectable(Direction side){return side==port();}
    @Override public boolean canInputFrom(Direction side){return false;}
    @Override public boolean canOutputTo(Direction side){return side==port();}
    @Override public void setSuction(Aspect aspect,int amount){}
    @Override public Aspect getSuctionType(Direction side){return null;}
    @Override public int getSuctionAmount(Direction side){return 0;}
    @Override public int getMinimumSuction(){return 0;}
    @Override public boolean renderExtendedTube(){return false;}
    @Override public Aspect getEssentiaType(Direction side){return essentia>0?Aspects.MAGIC:null;}
    @Override public int getEssentiaAmount(Direction side){return essentia;}
    @Override public int addEssentia(Aspect a,int amount,Direction side){return 0;}
    @Override public int takeEssentia(Aspect a,int amount,Direction side){if(a!=Aspects.MAGIC||amount<=0||!canOutputTo(side))return 0;int taken=Math.min(amount,essentia);if(taken>0){essentia-=taken;sync();}return taken;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider r){super.saveAdditional(tag,r);tag.putInt("charges",charges);tag.putInt("power",power);tag.putInt("essentia",essentia);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider r){super.loadAdditional(tag,r);charges=Math.max(0,tag.getInt("charges"));power=Math.max(0,Math.min(14,tag.getInt("power")));essentia=Math.max(0,Math.min(4,tag.getInt("essentia")));}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
