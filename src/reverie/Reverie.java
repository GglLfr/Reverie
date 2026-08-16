package reverie;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import reverie.content.*;
import reverie.gen.*;
import reverie.graphics.*;
import reverie.util.debug.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Reverie extends Mod{
    public static final String logTag = "[Reverie]";

    protected static LoadedMod mod;

    public Reverie(){
        if(graphics != null && !graphics.getGLVersion().atLeast(3, app.isDesktop() ? 3 : 0)){
            throw new UnsupportedOperationException("Reverie only runs with OpenGL 3.3 (on desktop) or OpenGL ES 3.0 (on android) and above!");
        }

        if(OS.hasEnvFlag("REVERIE_DEBUG")){
            Debugs.init();
        }

        Events.on(FileTreeInitEvent.class, e -> app.post(() -> {
            mod = mods.getMod(Reverie.class);
            if(isModEnabled()){
                RShaders.load();
            }
        }));
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();

        REnv.load();
        RPlanets.load();
    }

    public static boolean isModEnabled(){
        return mod.enabled();
    }
}
