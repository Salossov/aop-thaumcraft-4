package thaumcraft.api.client;

import java.util.Objects;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.*;
import thaumcraft.api.research.ResearchDefinition;

/** Client-thread key input while a research entry is open in the Thaumonomicon.
 * Subscribe to BUS. Cancellation consumes the key before ordinary book handling;
 * no event is posted for the category tree. Screen is the public vanilla parent
 * for dialogs; addons do not need the core's screen or menu implementation.
 */
public final class BookResearchKeyEvent extends Event implements ICancellableEvent {
    public static final IEventBus BUS=BusBuilder.builder().build();
    private final Screen screen;
    private final ResearchDefinition research;
    private final int keyCode,scanCode,modifiers;
    public BookResearchKeyEvent(Screen screen,ResearchDefinition research,int keyCode,int scanCode,int modifiers){
        this.screen=Objects.requireNonNull(screen);this.research=Objects.requireNonNull(research);
        this.keyCode=keyCode;this.scanCode=scanCode;this.modifiers=modifiers;
    }
    public Screen screen(){return screen;}
    public ResearchDefinition research(){return research;}
    public int keyCode(){return keyCode;}
    public int scanCode(){return scanCode;}
    public int modifiers(){return modifiers;}
}
