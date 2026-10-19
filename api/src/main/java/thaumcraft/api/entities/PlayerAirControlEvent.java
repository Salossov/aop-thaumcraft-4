package thaumcraft.api.entities;

import java.util.Objects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

/** Result of Player.getFlyingSpeed on either logical side. Includes the ability-flight
 * branch; listeners must distinguish it from ordinary airborne movement themselves.
 */
public final class PlayerAirControlEvent extends Event {
    public static final IEventBus BUS=BusBuilder.builder().build();
    private final Player player;
    private final float originalSpeed;
    private float speed;
    public PlayerAirControlEvent(Player player,float speed) {this.player=Objects.requireNonNull(player);this.originalSpeed=speed;this.speed=speed;}
    public Player player() {return player;}
    public float originalSpeed() {return originalSpeed;}
    public float speed() {return speed;}
    public void speed(float value) {
        if(!Float.isFinite(value)||value<0)throw new IllegalArgumentException("Invalid air-control speed");
        speed=value;
    }
}
