package reverie.util;

import arc.struct.*;
import arc.util.pooling.*;

import java.util.*;
import java.util.concurrent.*;

public final class Temporaries{
    public static final Pool<BoolSeq> boolArrayPool = new ConcurrentPool<>(){
        @Override
        protected BoolSeq newObject(){
            return new BoolSeq(true, 0);
        }

        @Override
        protected void reset(BoolSeq object){
            object.clear();
        }
    };

    public static final Pool<FloatSeq> floatArrayPool = new ConcurrentPool<>(){
        @Override
        protected FloatSeq newObject(){
            return new FloatSeq(true, 0);
        }

        @Override
        protected void reset(FloatSeq object){
            object.clear();
        }
    };

    private Temporaries(){
        throw new AssertionError();
    }

    public static abstract class ConcurrentPool<T> extends Pool<T>{
        private final LinkedBlockingDeque<T> free = new LinkedBlockingDeque<>();

        public ConcurrentPool(){
            super(0, Integer.MAX_VALUE);
        }

        @Override
        public T obtain(){
            var next = free.poll();
            return next == null ? newObject() : next;
        }

        @Override
        public void free(T object){
            Objects.requireNonNull(object, "Freed object may not be null and must have come from `obtain()`");

            reset(object);
            free.add(object);
        }

        @Override
        public void freeAll(Seq<T> objects){
            for(int i = 0, len = objects.size; i < len; i++){
                var object = objects.items[i];
                if(object == null) continue;

                reset(object);
                free.add(object);
            }
        }

        @Override
        public void clear(){
            free.clear();
        }

        @Override
        public int getFree(){
            return free.size();
        }
    }
}
