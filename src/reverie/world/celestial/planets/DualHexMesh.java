package reverie.world.celestial.planets;

import arc.graphics.gl.*;
import arc.math.geom.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import reverie.graphics.gen.mesh.*;

public class DualHexMesh extends PlanetMesh{
    public DualHexMesh(Planet planet, HexMesher outward, HexMesher inward, int divisions, float intensity, Shader shader){
        super(planet, MeshBuilder3D.buildHexDual(outward, inward, divisions, planet.radius, intensity), shader);
    }

    @Override
    public void preRender(PlanetParams params){
        Shaders.planet.planet = planet;
        Shaders.planet.emissive = planet.generator != null && planet.generator.isEmissive();
        Shaders.planet.lightDir.set(planet.solarSystem.position).sub(planet.position).rotate(Vec3.Y, planet.getRotation()).nor();
        Shaders.planet.ambientColor.set(planet.solarSystem.lightColor);
    }
}
