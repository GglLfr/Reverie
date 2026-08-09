package reverie.content;

import mindustry.type.*;
import reverie.content.planet.*;

public final class RPlanets{
    public static Planet dayspring;

    private RPlanets(){
        throw new AssertionError();
    }

    public static void load(){
        dayspring = new DayspringPlanet("dayspring");
    }
}
