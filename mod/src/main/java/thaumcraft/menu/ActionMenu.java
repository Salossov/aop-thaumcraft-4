package thaumcraft.menu;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.network.ResearchActionPayload;

public interface ActionMenu {
    UUID token();
    boolean accept(ServerPlayer player,ResearchActionPayload request);
}
