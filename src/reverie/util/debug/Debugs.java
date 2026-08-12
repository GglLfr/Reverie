package reverie.util.debug;

import arc.util.*;
import arc.util.Log.*;
import mindustry.ui.dialogs.*;

public final class Debugs{
    private Debugs(){
        throw new AssertionError();
    }

    public static void init(){
        Log.level = LogLevel.debug;
        PlanetDialog.debugSelect = true;
    }
}
