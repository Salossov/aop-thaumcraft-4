package thaumcraft.api.addon;

import java.util.UUID;
import thaumcraft.api.knowledge.PlayerKnowledge;

/** A defensive knowledge snapshot, never a mutable authorization token. */
public final class KnowledgeChangedEvent extends net.neoforged.bus.api.Event {
    private final UUID player;private final PlayerKnowledge knowledge;private final boolean client;
    public KnowledgeChangedEvent(UUID player,PlayerKnowledge knowledge,boolean client){this.player=player;this.knowledge=knowledge.copy();this.client=client;}
    public UUID player(){return player;}public PlayerKnowledge knowledge(){return knowledge.copy();}public boolean client(){return client;}
}
