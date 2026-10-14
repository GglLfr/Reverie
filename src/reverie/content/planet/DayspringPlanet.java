package reverie.content.planet;

import arc.graphics.*;
import arc.math.*;
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
            new MultilevelSpec(() -> new DualHexMesh(this, generator.outward(0), generator.inward(1), 6, radius, 0.2f, Shaders.planet)),
            new MultilevelSpec(() -> new DualHexMesh(this, generator.outward(2), generator.inward(3), 5, radius * 4f / 5f, 0.2f, Shaders.planet)),
            new MultilevelSpec(() -> new DualHexMesh(this, generator.outward(3), generator.inward(4), 4, radius * 3f / 5f, 0.2f, Shaders.planet)),
            new MultilevelSpec(() -> new DualHexMesh(this, generator.outward(5), generator.inward(6), 3, radius * 2f / 5f, 0.2f, Shaders.planet)),
        };
    }

    public class DayspringGenerator extends PlanetGenerator{
        public final HexMesher self = outward(baseSeed);

        @Override
        public float getHeight(Vec3 position){
            return self.getHeight(position);
        }

        @Override
        public void getColor(Vec3 position, Color out){
            self.getColor(position, out);
        }

        public HexMesher outward(int seed){
            return new HexMesher(){
                @Override
                public float getHeight(Vec3 position){
                    return Simplex.noise4d(6d, 0.5d, 1.4d, position.x, position.y, position.z, Mathf.randomSeedRange(seed, 3f));
                }

                @Override
                public void getColor(Vec3 position, Color out){
                    out.set(Color.red);
                }
            };
        }

        public HexMesher inward(int seed){
            return new HexMesher(){
                @Override
                public float getHeight(Vec3 position){
                    return Simplex.noise4d(6d, 0.5d, 1.4d, position.x, position.y, position.z, Mathf.randomSeedRange(seed, 3f));
                }

                @Override
                public void getColor(Vec3 position, Color out){
                    out.set(Color.blue);
                }
            };
        }
    }
}
