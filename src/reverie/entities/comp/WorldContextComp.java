package reverie.entities.comp;

import arc.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Time.*;
import ent.anno.Annotations.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.gen.*;
import reverie.gen.*;
import reverie.util.reflect.*;

@EntityComponent(base = true)
@EntityDef(value = WorldContextc.class, genIO = false)
abstract class WorldContextComp implements Drawc, Syncc{
    // class Events -> static final ObjectMap<Object, Seq<Cons<?>>> events
    private static final ReferenceFieldHandle<Events, ObjectMap<Object, Seq<Cons<?>>>> Events_events = ReferenceFieldHandle.of(Events.class, "events");
    private static final ReferenceFieldHandle<Time, Seq<DelayRun>>
        // class Time -> static Seq<DelayRuns> runs
        Time_runs = ReferenceFieldHandle.of(Time.class, "runs"),
    // class Time -> static Seq<DelayRuns> removal
    Time_removal = ReferenceFieldHandle.of(Time.class, "removal");

    private final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>(), tmp = new ObjectMap<>();
    private final Seq<DelayRun> runs = new Seq<>(), removal = new Seq<>();

    private final EntityGroup<Entityc> all = new EntityGroup<>(Entityc.class, false, false, (e, pos) -> {});
    private final EntityGroup<EffectState> effect = new EntityGroup<>(EffectState.class, false, false, (e, pos) -> {});
    private final EntityGroup<Player> player = new EntityGroup<>(Player.class, false, false, (e, pos) -> {});
    private final EntityGroup<Bullet> bullet = new EntityGroup<>(Bullet.class, false, false, (e, pos) -> {});
    private final EntityGroup<Unit> unit = new EntityGroup<>(Unit.class, false, false, (e, pos) -> {});
    private final EntityGroup<Building> build = new EntityGroup<>(Building.class, false, false, (e, pos) -> {});
    private final EntityGroup<Syncc> sync = new EntityGroup<>(Syncc.class, false, false, (e, pos) -> {});
    private final EntityGroup<Drawc> draw = new EntityGroup<>(Drawc.class, false, false, (e, pos) -> {});
    private final EntityGroup<WeatherState> weather = new EntityGroup<>(WeatherState.class, false, false, (e, pos) -> {});
    private final EntityGroup<PowerGraphUpdaterc> powerGraph = new EntityGroup<>(PowerGraphUpdaterc.class, false, false, (e, pos) -> {});

    private final World world = run(World::new);

    void run(Runnable run){
        run(() -> {
            run.run();
            return null;
        });
    }

    <T> T run(Prov<T> prov){
        var prevEvents = Events_events.get(null);
        tmp.set(prevEvents);
        prevEvents.set(events);

        Seq<DelayRun> prevRuns = Time_runs.get(null), prevRemoval = Time_removal.get(null);
        Time_runs.set(null, runs);
        Time_removal.set(null, removal);

        var prevAll = Groups.all;
        var prevEffect = Groups.effect;
        var prevPlayer = Groups.player;
        var prevBullet = Groups.bullet;
        var prevUnit = Groups.unit;
        var prevBuild = Groups.build;
        var prevSync = Groups.sync;
        var prevDraw = Groups.draw;
        var prevWeather = Groups.weather;
        var prevPowerGraph = Groups.powerGraph;

        Groups.all = all;
        Groups.effect = effect;
        Groups.player = player;
        Groups.bullet = bullet;
        Groups.unit = unit;
        Groups.build = build;
        Groups.sync = sync;
        Groups.draw = draw;
        Groups.weather = weather;
        Groups.powerGraph = powerGraph;

        var prevWorld = Vars.world;
        Vars.world = world;

        try{
            return prov.get();
        }finally{
            events.set(prevEvents);
            prevEvents.set(tmp);

            Time_runs.set(null, prevRuns);
            Time_removal.set(null, prevRemoval);

            Groups.all = prevAll;
            Groups.effect = prevEffect;
            Groups.player = prevPlayer;
            Groups.bullet = prevBullet;
            Groups.unit = prevUnit;
            Groups.build = prevBuild;
            Groups.sync = prevSync;
            Groups.draw = prevDraw;
            Groups.weather = prevWeather;
            Groups.powerGraph = prevPowerGraph;

            Vars.world = prevWorld;
        }
    }
}