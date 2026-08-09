package reverie.content.planet;

import arc.graphics.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.maps.generators.*;
import reverie.world.celestial.planets.*;

public class DayspringPlanet extends MultilevelPlanet{
    public DayspringPlanet(String name){
        super(name, Planets.sun, 1f);

        var generator = new DayspringGenerator();
        this.generator = generator;

        specs = new MultilevelSpec[]{
            new MultilevelSpec(() -> new AtmosphereMesh(new DualHexMesh(this, generator, generator.inward(), 6, 0.2f, Shaders.planet))),
        };
        meshLoader = MultilevelMesh::new;
    }

    public class DayspringGenerator extends PlanetGenerator{
        @Override
        public float getHeight(Vec3 position){
            return Simplex.noise4d(6d, 0.5d, 1.4d, position.x, position.y, position.z, 2.671954d);
        }

        @Override
        public void getColor(Vec3 position, Color out){
            out.set(Color.red);
        }

        public HexMesher inward(){
            return new HexMesher(){
                @Override
                public float getHeight(Vec3 position){
                    return Simplex.noise4d(6d, 0.5d, 1.4d, position.x, position.y, position.z, -3.813645d);
                }

                @Override
                public void getColor(Vec3 position, Color out){
                    out.set(Color.blue);
                }
            };
        }
    }
}
