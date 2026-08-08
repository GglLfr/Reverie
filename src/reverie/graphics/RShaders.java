package reverie.graphics;

import arc.files.*;
import reverie.graphics.shaders.*;

import static mindustry.Vars.*;

public final class RShaders{
    public static DepthScreenspaceShader depthScreenspace;
    public static DepthAtmosphereShader depthAtmosphere;

    private RShaders(){
        throw new AssertionError();
    }

    /** Loads the shaders. Client-side and main thread only! */
    public static void load(){
        depthScreenspace = new DepthScreenspaceShader();
        depthAtmosphere = new DepthAtmosphereShader();
    }

    public static Fi file(String name){
        return tree.get("shaders/reverie/" + name);
    }
}