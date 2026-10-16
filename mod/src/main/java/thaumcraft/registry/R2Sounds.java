package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Original sound names; the TC4 sound definitions and recordings are retained. */
public final class R2Sounds {
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,"thaumcraft");
    public static final DeferredHolder<SoundEvent,SoundEvent> WISP_LIVE=sound("wisplive"),WISP_DEAD=sound("wispdead"),ZAP=sound("zap");
    public static final DeferredHolder<SoundEvent,SoundEvent> ROOTS=sound("roots"),TENTACLE=sound("tentacle");
    public static final DeferredHolder<SoundEvent,SoundEvent> FLY=sound("fly");
    public static final DeferredHolder<SoundEvent,SoundEvent> WIND=sound("wind");
    public static final DeferredHolder<SoundEvent,SoundEvent> BRAIN=sound("brain"),JAR=sound("jar");
    public static final DeferredHolder<SoundEvent,SoundEvent> ICE=sound("ice"),SHOCK=sound("shock"),FIRE_LOOP=sound("fireloop"),WAND_FAIL=sound("wandfail"),RUMBLE=sound("rumble");
    public static final DeferredHolder<SoundEvent,SoundEvent> PUMP=sound("pump"),RUNIC_SHIELD_CHARGE=sound("runicshieldcharge"),MONOLITH=sound("monolith");
    public static final DeferredHolder<SoundEvent,SoundEvent> RUNIC_SHIELD_EFFECT=sound("runicshieldeffect");
    public static final DeferredHolder<SoundEvent,SoundEvent> CHANT=sound("chant"),PECH_IDLE=sound("pech_idle"),PECH_TRADE=sound("pech_trade"),PECH_DICE=sound("pech_dice"),PECH_HIT=sound("pech_hit"),PECH_DEATH=sound("pech_death"),PECH_CHARGE=sound("pech_charge");
    public static final DeferredHolder<SoundEvent,SoundEvent> GUARDIAN_IDLE=sound("egidle"),GUARDIAN_DEATH=sound("egdeath"),GUARDIAN_ATTACK=sound("egattack"),GUARDIAN_SCREECH=sound("egscreech");
    public static final DeferredHolder<SoundEvent,SoundEvent> JACOBS=sound("jacobs");
    public static final DeferredHolder<SoundEvent,SoundEvent> CRAFT_START=sound("craftstart"),CRAFT_FAIL=sound("craftfail"),CRAFT_FINISH=sound("wand"),EVIL_PORTAL=sound("evilportal");
    public static final DeferredHolder<SoundEvent,SoundEvent> ASPECT_CLAIM=sound("hhoff");
    public static final DeferredHolder<SoundEvent,SoundEvent> ASPECT_COMBINE=sound("hhon");
    public static final DeferredHolder<SoundEvent,SoundEvent> WRITE=sound("write"),ERASE=sound("erase");
    public static final DeferredHolder<SoundEvent,SoundEvent> PAGE=sound("page"),LEARN=sound("learn"),WHISPERS=sound("whispers");
    public static final DeferredHolder<SoundEvent,SoundEvent> CAMERA_CLACK=sound("cameraclack");
    public static final DeferredHolder<SoundEvent,SoundEvent> CAMERA_TICKS=sound("cameraticks");
    public static final DeferredHolder<SoundEvent,SoundEvent> GOLEM_IRON_SHOOT=sound("golemironshoot");
    public static final DeferredHolder<SoundEvent,SoundEvent> BUBBLE=sound("bubble");
    public static final DeferredHolder<SoundEvent,SoundEvent> KEY=sound("key"),DOOR_FAIL=sound("doorfail");
    public static final DeferredHolder<SoundEvent,SoundEvent> COINS=sound("coins"),URN_BREAK=sound("urnbreak");
    public static final DeferredHolder<SoundEvent,SoundEvent> CRYSTAL=sound("crystal"),CRAB_IDLE=sound("crabtalk"),CRAB_DEATH=sound("crabdeath"),CRAB_CLAW=sound("crabclaw"),GORE=sound("gore");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String name) { return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("thaumcraft",name))); }
    public static void register(IEventBus bus) { SOUNDS.register(bus); }
    private R2Sounds() {}
}
