package reverie.world.celestial.planets;

import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import reverie.graphics.*;
import reverie.graphics.gl.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// TODO: Depth atmosphere shader can be removed once v9 lands.
public class BasePlanet extends Planet{
    public @Nullable RFrameBuffer buffer;

    public BasePlanet(String name, Planet parent, float radius){
        super(name, parent, radius);
    }

    public BasePlanet(String name, Planet parent, float radius, int sectorSize){
        super(name, parent, radius, sectorSize);
    }

    @Override
    public void load(){
        super.load();
        if(!headless && buffer == null){
            buffer = new RFrameBuffer(2, 2, true);
            buffer.getTexture().setFilter(TextureFilter.nearest);
        }
    }

    @Override
    public void drawAtmosphere(Mesh atmosphere, Camera3D cam){
        var shader = RShaders.depthAtmosphere;
        shader.camera = cam;
        shader.planet = this;
        shader.bind();
        shader.apply();
        atmosphere.render(shader, Gl.triangles);
    }

    public class AtmosphereMesh implements GenericMesh{
        protected final GenericMesh mesh;

        public AtmosphereMesh(GenericMesh mesh){
            this.mesh = mesh;
        }

        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            buffer.resize(params.viewW > 0 ? params.viewW : graphics.getWidth(), params.viewH > 0 ? params.viewH : graphics.getHeight());
            buffer.begin(Color.clear);
            mesh.render(params, projection, transform);
            buffer.end();

            var blit = RShaders.depthScreenspace;
            blit.buffer = buffer;
            Draw.blit(blit);
        }

        @Override
        public void dispose(){
            mesh.dispose();
        }

        @Override
        public boolean isDisposed(){
            return mesh.isDisposed();
        }
    }
}