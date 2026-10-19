package thaumcraft.api.client;
/** Client entry point. The core asks the server to open a real, authorized book menu. */
public final class BookNavigation {
    private static java.util.function.Consumer<String> open=key->{};
    public static void install(java.util.function.Consumer<String> handler){open=java.util.Objects.requireNonNull(handler);}
    public static void open(String research){open.accept(research);}
    private BookNavigation(){}
}
