package thaumcraft.client;

import net.minecraft.client.Minecraft;
import thaumcraft.network.NodeZapPayload;

public final class NodeExchangeEffects {
    public static void accept(NodeZapPayload packet){var level=Minecraft.getInstance().level;if(level==null)return;LegacyLightningParticle.node(packet.from(),packet.to());level.playLocalSound(packet.from().x,packet.from().y,packet.from().z,thaumcraft.registry.R2Sounds.ZAP.get(),net.minecraft.sounds.SoundSource.BLOCKS,.1f,1+level.random.nextFloat()*.2f,false);}
    private NodeExchangeEffects(){}
}
