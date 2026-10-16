package thaumcraft.network;

import java.util.UUID;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.knowledge.KnowledgeNbt;

/** Client-only mirror; never used to calculate or authorize rewards on the server. */
public final class ClientKnowledge {
    private static UUID owner,session;
    private static PlayerKnowledge state=new PlayerKnowledge();
    private static long snapshotSequence;
    private static thaumcraft.data.AspectDataSync.Definitions aspects=thaumcraft.data.AspectDataSync.Definitions.EMPTY;
    private static java.util.Map<String,thaumcraft.api.research.ResearchDefinition> definitions=java.util.Map.of();
    public static void accept(KnowledgePayload packet) {
        var snapshot=KnowledgeNbt.decode(packet.data());
        long sequence=packet.data().getLong("snapshotSequence");boolean same=packet.player().equals(owner)&&packet.session().equals(session);
        if(same&&(sequence<=snapshotSequence||snapshot.revision()<state.revision()))return;
        definitions=thaumcraft.data.ResearchDataSync.decode(packet.data());
        aspects=thaumcraft.data.AspectDataSync.decode(packet.data());owner=packet.player();session=packet.session();
        // The server sends one notification per committed gain. A snapshot is a
        // baseline (also sent on login/reload), not another reward event.
        state=snapshot;snapshotSequence=sequence;
    }
    public static PlayerKnowledge snapshot() { return state.copy(); }
    public static UUID session() { return session; }
    public static java.util.Map<String,thaumcraft.api.research.ResearchDefinition> definitions() { return definitions; }
    public static thaumcraft.data.AspectDataSync.Definitions aspects(){return aspects;}
    public static void clear() { owner=null;session=null;state=new PlayerKnowledge();definitions=java.util.Map.of();aspects=thaumcraft.data.AspectDataSync.Definitions.EMPTY;snapshotSequence=0; }
    private ClientKnowledge() {}
}
