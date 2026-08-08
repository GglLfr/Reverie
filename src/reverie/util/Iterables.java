package reverie.util;

public final class Iterables{
    private Iterables(){
        throw new AssertionError();
    }

    public static <T> int foldi(T[] collection, int init, IntFold<T> folder){
        for(var t : collection) init = folder.fold(init, t);
        return init;
    }

    public static <T> int foldi(Iterable<T> collection, int init, IntFold<T> folder){
        for(var t : collection) init = folder.fold(init, t);
        return init;
    }

    public interface IntFold<T>{
        int fold(int accum, T item);
    }
}
