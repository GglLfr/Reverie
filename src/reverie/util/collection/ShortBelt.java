package reverie.util.collection;

import java.util.concurrent.atomic.*;

import static reverie.util.Atomics.*;

public class ShortBelt{
    private static final int lockFlag = 1 << Integer.SIZE - 1;
    private static final int mask = ~lockFlag;

    /**
     * The total length of all fragments before the tail. This is used to offset the current total length to reside
     * within the tail fragment.
     */
    private int precedingLength;
    /**
     * The synchronization primitive, contains the current total length and an
     * {@linkplain ShortBelt#lockFlag acquisition bit}.
     */
    private final AtomicInteger length;
    /** The first fragment that contains elements starting from index 0. */
    private Segment head;
    /** The tail fragment that threads are currently modifying. */
    private Segment tail;

    public ShortBelt(){
        this(16);
    }

    public ShortBelt(int initialCapacity){
        head = tail = new Segment();
        head.data = new short[initialCapacity];

        precedingLength = 0;
        length = new AtomicInteger(0);
    }

    /**
     * Extends the vector by `additional` elements, invoking a closure with an uninitialized slice to it and the
     * starting element's absolute index in the vector.
     *
     * @param count    How many additional elements to add.
     * @param enqueuer Lambda acceptor for the target array.
     */
    public int enqueue(int count, Enqueuer enqueuer){
        int length = getOpaque(this.length) & mask;
        while(true){
            int newLength = length + count;
            if(((newLength & lockFlag) == lockFlag) || newLength < length){
                throw new OutOfMemoryError("Too many elements");
            }

            int actualLength;
            if((actualLength = compareExchangeAcquire(this.length, length, newLength | lockFlag)) != length){
                length = actualLength & mask;
                continue;
            }

            var data = tail.data;
            if(data.length >= newLength - precedingLength){
                // Immediately release the lock, because the fragment fits and the data slice we requested is guaranteed not to be aliased by this atomic store.
                setRelease(this.length, newLength);
                enqueuer.get(data, length - precedingLength);
            }else{
                // Try to allocate a new fragment and linking to it, releasing the lock as soon as possible.
                Segment next;
                try{
                    // First, allocate a new fragment that fits...
                    next = new Segment();
                    next.data = new short[Math.max(length, count)];
                }catch(Throwable t){
                    setRelease(this.length, length);
                    throw t;
                }

                int precedingLengthPrev = precedingLength;
                precedingLength = length;

                // ...then, write `precedingLength` to the current length and `tail` to the new pointer.
                var oldTail = tail;
                tail = next;

                // These fields aren't written within the lock because we know subsequent operations will not ever
                // access this fragment in particular, therefore eliminating mutable aliasing.
                oldTail.next = next;
                oldTail.length = length - precedingLengthPrev;

                setRelease(this.length, newLength);
                enqueuer.get(next.data, 0);
            }
            return length;
        }
    }

    /**
     * Flattens the vector (if it isn't flattened already), invokes a closure with an owned slice, and clears the
     * vector.
     *
     * @param cons Lambda acceptor for the flattened array.
     */
    public <T> T clear(Clearer<T> cons){
        int length = getOpaque(this.length) & mask;
        while(true){
            int actualLength;
            if((actualLength = compareExchangeAcquire(this.length, length, lockFlag)) != length){
                length = actualLength & mask;
                continue;
            }

            int precedingLength = this.precedingLength, current = length;
            this.precedingLength = 0;

            try{
                if(head == tail){
                    head.length = 0;
                    return cons.get(head.data, current);
                }else{
                    var newHead = new Segment();
                    newHead.data = new short[current];

                    var node = head;
                    head = tail = newHead;

                    int offset = 0;
                    for(; node != null; node = node.next){
                        int nodeLength = node.next == null ? current - precedingLength : node.length;
                        System.arraycopy(node.data, 0, newHead.data, offset, nodeLength);
                        offset += nodeLength;
                    }

                    return cons.get(newHead.data, offset);
                }
            }finally{
                setRelease(this.length, 0);
            }
        }
    }

    public interface Enqueuer{
        void get(short[] dst, int offset);
    }

    public interface Clearer<T>{
        T get(short[] dst, int length);
    }

    private static class Segment{
        /** Reference to the next fragment. If this is `null`, then {@link Segment#length} is `0` and therefore invalid. */
        private Segment next;
        /** How many initialized elements are contained within this fragment. */
        private int length;
        /** The actual backing memory for elements. */
        private short[] data;
    }
}
