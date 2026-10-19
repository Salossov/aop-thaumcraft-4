package thaumcraft.api.crafting;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.world.inventory.*;
/** Runtime menu identity without a compile dependency on the core's implementation class. */
public final class WorkbenchApi {
 private static Class<? extends AbstractContainerMenu> menuClass;
 private static Supplier<MenuType<?>> menuType;
 public static void install(Class<? extends AbstractContainerMenu> type,Supplier<MenuType<?>> menu){if(menuClass!=null)throw new IllegalStateException("Workbench API already installed");menuClass=Objects.requireNonNull(type);menuType=Objects.requireNonNull(menu);}
 public static Class<? extends AbstractContainerMenu> menuClass(){return Objects.requireNonNull(menuClass,"Core API not initialized");}
 public static MenuType<?> menuType(){return Objects.requireNonNull(menuType,"Core API not initialized").get();}
 private WorkbenchApi(){}
}
