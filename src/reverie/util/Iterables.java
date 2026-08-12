package reverie.util;

import arc.func.*;

import java.util.*;

public final class Iterables{
    private Iterables(){
        throw new AssertionError();
    }

    public static <S, T> T[] map(T[] dst, S[] src, Func<S, T> mapper){
        if(dst.length < src.length) dst = Arrays.copyOf(dst, src.length);
        for(int i = 0, len = src.length; i < len; i++) dst[i] = mapper.get(src[i]);
        return dst;
    }

    public static <T> int foldi(T[] collection, int init, IntFold<T> folder){
        for(var t : collection) init = folder.fold(init, t);
        return init;
    }

    public static <T> int foldi(Iterable<T> collection, int init, IntFold<T> folder){
        for(var t : collection) init = folder.fold(init, t);
        return init;
    }

    public static <T> void split(T[] array, int batch, Intc2 split){
        split(array, 0, array.length, batch, split);
    }

    public static <T> void split(T[] array, int start, int length, int batch, Intc2 split){
        int end = start + length;
        for(int i = start; i < end; i += batch){
            split.get(i, Math.min(i + batch, end));
        }
    }

    public interface IntFold<T>{
        int fold(int accum, T item);
    }
}
