package reverie.content;

import env.*;

public final class REnv{
    public static int twilight;

    private REnv(){
        throw new AssertionError();
    }

    public static void load(){
        twilight = EnvAlloc.create("reverie-twilight");
    }
}
