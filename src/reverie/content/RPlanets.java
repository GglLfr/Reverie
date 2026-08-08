package reverie.content;

import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import reverie.world.celestial.planets.*;
import reverie.world.celestial.planets.MultilevelPlanet.*;

public final class RPlanets{
    public static Planet tmp;

    private RPlanets(){
        throw new AssertionError();
    }

    public static void load(){
        tmp = new MultilevelPlanet("tmp", Planets.sun, 1f, new MultilevelSpec(() -> new ShaderSphereMesh(tmp, Shaders.unlitWhite, 2))){{
            meshLoader = () -> new MultilevelMesh();
        }};
    }
}
