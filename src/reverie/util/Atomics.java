package reverie.util;

import reverie.*;

import java.util.concurrent.atomic.*;

public final class Atomics{
    public static final boolean hasMemoryOrder = Api.level >= Api.memoryOrder;

    private Atomics(){
        throw new AssertionError();
    }

    public static int getOpaque(AtomicInteger integer){
        if(hasMemoryOrder){
            return WithMemoryOrder.getOpaque(integer);
        }else{
            return integer.get();
        }
    }

    public static <T> T getAcquire(AtomicReference<T> ref){
        if(hasMemoryOrder){
            return WithMemoryOrder.getAcquire(ref);
        }else{
            return ref.get();
        }
    }

    public static void setRelease(AtomicInteger integer, int value){
        if(hasMemoryOrder){
            WithMemoryOrder.setRelease(integer, value);
        }else{
            integer.lazySet(value);
        }
    }

    public static <T> void setRelease(AtomicReference<T> ref, T value){
        if(hasMemoryOrder){
            WithMemoryOrder.setRelease(ref, value);
        }else{
            ref.setRelease(value);
        }
    }

    public static int compareExchangeAcquire(AtomicInteger integer, int expected, int next){
        if(hasMemoryOrder){
            return WithMemoryOrder.compareExchangeAcquire(integer, expected, next);
        }else{
            int witness = integer.get();
            while(witness == expected){
                if(integer.compareAndSet(witness, next)) return witness;
                witness = integer.get();
            }
            return witness;
        }
    }

    private static class WithMemoryOrder{
        private static int getOpaque(AtomicInteger integer){
            return integer.getOpaque();
        }

        private static <T> T getAcquire(AtomicReference<T> ref){
            return ref.getAcquire();
        }

        private static void setRelease(AtomicInteger integer, int value){
            integer.setRelease(value);
        }

        private static <T> void setRelease(AtomicReference<T> ref, T value){
            ref.setRelease(value);
        }

        private static int compareExchangeAcquire(AtomicInteger integer, int expected, int next){
            return integer.compareAndExchangeAcquire(expected, next);
        }
    }
}