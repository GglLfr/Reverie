package reverie.util;

import java.util.concurrent.atomic.*;

import static reverie.Reverie.*;

public final class Atomics{
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

    public static void setRelease(AtomicInteger integer, int value){
        if(hasMemoryOrder){
            WithMemoryOrder.setRelease(integer, value);
        }else{
            integer.setRelease(value);
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

        private static void setRelease(AtomicInteger integer, int value){
            integer.setRelease(value);
        }

        private static int compareExchangeAcquire(AtomicInteger integer, int expected, int next){
            return integer.compareAndExchangeAcquire(expected, next);
        }
    }
}