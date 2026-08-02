package reverie.graphics;

import arc.files.*;
import arc.graphics.gl.*;
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
        String prevVert = Shader.prependVertexCode, prevFrag = Shader.prependFragmentCode;
        Shader.prependVertexCode = Shader.prependFragmentCode = "";

        depthScreenspace = new DepthScreenspaceShader();
        depthAtmosphere = new DepthAtmosphereShader();

        Shader.prependVertexCode = prevVert;
        Shader.prependFragmentCode = prevFrag;
    }

    public static Fi file(String name){
        return tree.get("shaders/reverie/" + name);
    }
}