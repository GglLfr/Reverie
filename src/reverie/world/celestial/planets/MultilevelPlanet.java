package reverie.world.celestial.planets;

import arc.*;
import arc.func.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class MultilevelPlanet extends BasePlanet{
    public int selectedLevel;
    public MultilevelSpec[] specs = {};

    public MultilevelPlanet(String name, Planet parent, float radius){
        super(name, parent, radius, 0);
    }

    @Override
    public void load(){
        super.load();
        selectedLevel = settings.getInt(String.format("%s-selected-level", name), 0);

        Events.on(ClientLoadEvent.class, e -> ui.planet.shown(() -> {
            for(var topChild : ui.planet.getChildren()){
                if(!(topChild instanceof Stack stack)) continue;
                for(var stackChild : stack.getChildren()){
                    if(!(stackChild instanceof Table planetSelect)) continue;
                    for(var selectChild : planetSelect.getChildren()){
                        if(!(selectChild instanceof ScrollPane pane)) continue;

                        var widget = pane.getWidget();
                        if(!(widget instanceof Table starsTable)) continue;

                        for(var starChild : starsTable.getChildren()){
                            if(!(starChild instanceof Table planetTable)) continue;
                            for(var planetChild : planetTable.getChildren()){
                                if(!(planetChild instanceof TextButton planetButton)) continue;
                                if(!planetButton.getText().toString().equals(localizedName)) continue;

                                var cell = planetTable.getCell(planetButton);
                                cell.setElement(new Table(t -> {
                                    t.add(planetButton).growX().height(40f);
                                    t.row();

                                    for(int i = 0; i < specs.length; i++){
                                        int level = i;
                                        t.button(
                                                bundle.get(String.format("%s.%s-spec-%d.name", getContentType(), name, level)),
                                                Styles.flatTogglet,
                                                () -> {
                                                    ui.planet.viewPlanet(this, false);
                                                    settings.putInt(String.format("%s-selected-level", name), selectedLevel = level);
                                                }
                                            )
                                            .growX()
                                            .height(40f)
                                            .padLeft(40f)
                                            .update(tt -> tt.setChecked(ui.planet.state.planet == this && selectedLevel == level))
                                            .row();
                                    }
                                })).maxHeight(Float.POSITIVE_INFINITY);

                                Log.debug("[Reverie] Attached spec selection to UI dialog!");
                                return;
                            }
                        }
                    }
                }
            }
        }));
    }

    public class MultilevelMesh implements GenericMesh{
        private boolean disposed = false;
        private final GenericMesh[] meshes = new GenericMesh[specs.length];

        {
            long millis = Time.millis();
            for(int i = 0; i < specs.length; i++) meshes[i] = specs[i].mesh.get();
            Log.debug("[Reverie] Took @ms to generate planet mesh for @.", Time.millis() - millis, localizedName);
        }

        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            for(var mesh : meshes) mesh.render(params, projection, transform);
        }

        @Override
        public void dispose(){
            for(var mesh : meshes) mesh.dispose();
            disposed = true;
        }

        @Override
        public boolean isDisposed(){
            return disposed;
        }
    }

    public record MultilevelSpec(Prov<GenericMesh> mesh){
    }
}
