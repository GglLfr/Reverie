package reverie.world.celestial.planets;

import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import reverie.graphics.*;
import reverie.graphics.gl.*;

import static arc.Core.*;
import static mindustry.Vars.*;

//TODO depth atmosphere shader can be removed once v9 lands
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

    public class AtmosphereHexMesh implements GenericMesh{
        protected Mesh mesh;

        public AtmosphereHexMesh(HexMesher mesher, int divisions){
            mesh = MeshBuilder.buildHex(mesher, divisions, radius, 0.2f);
        }

        public AtmosphereHexMesh(int divisions){
            this(generator, divisions);
        }

        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            buffer.resize(graphics.getWidth(), graphics.getHeight());
            buffer.begin(Color.clear);

            var shader = Shaders.planet;
            shader.planet = BasePlanet.this;
            shader.lightDir.set(solarSystem.position).sub(position).rotate(Vec3.Y, getRotation()).nor();
            shader.ambientColor.set(solarSystem.lightColor);
            shader.bind();
            shader.setUniformMatrix4("u_proj", renderer.planets.cam.combined.val);
            shader.setUniformMatrix4("u_trans", transform.val);
            shader.apply();
            mesh.render(shader, Gl.triangles);

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