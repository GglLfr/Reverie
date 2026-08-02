package reverie;

import arc.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import reverie.gen.*;
import reverie.graphics.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Reverie extends Mod{
    protected static LoadedMod mod;

    public Reverie(){
        if(graphics != null && !graphics.getGLVersion().atLeast(3, app.isDesktop() ? 3 : 0)){
            throw new UnsupportedOperationException("Reverie only runs with OpenGL 3.3 (on desktop) or OpenGL ES 3.0 (on android) and above!");
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
    }

    public static boolean isModEnabled(){
        return mod.enabled();
    }
}
