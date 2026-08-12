package reverie.util;

import arc.struct.*;
import arc.util.*;

public final class Timers{
    private static final ThreadLocal<LongSeq> marks = ThreadLocal.withInitial(() -> new LongSeq(true, 4));

    private Timers(){
        throw new AssertionError();
    }

    public static void mark(){
        marks.get().add(Time.millis());
    }

    public static long elapsed(){
        var seq = marks.get();
        return seq.isEmpty() ? -1 : (Time.millis() - seq.pop());
    }
}
