package reverie.util;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.mod.*;
import reverie.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import static arc.Core.*;

public final class Tasks{
    private static final ThreadLocal<Seq<Runnable>> tasks = ThreadLocal.withInitial(() -> new Seq<>(Runnable.class));
    private static final ForkJoinPool pool;

    private static volatile long mainThreadId = -1;
    private static final Object sentinel = new Object();
    private static final ThreadLocal<LockState> lockStates = ThreadLocal.withInitial(LockState::new);

    static{
        if(Api.level >= Api.commonForkJoinPool){
            pool = Reflect.invoke(ForkJoinPool.class, "commonPool");
        }else{
            pool = null;
        }
    }

    private Tasks(){
        throw new AssertionError();
    }

    /** Safety: Must be called from {@link Mod#Mod()} off the main thread. */
    public static void initMainThread(){
        blockingPostOrNow(() -> mainThreadId = Thread.currentThread().getId());
    }

    public static void postOrNow(Runnable run){
        if(Thread.currentThread().getId() == mainThreadId){
            run.run();
        }else{
            app.post(run);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T blockingPostOrNow(Prov<T> prov){
        var t = Thread.currentThread();
        if(t.getId() == mainThreadId){
            return prov.get();
        }else{
            record ThrowableWrapper(Throwable ex){
            }

            var state = lockStates.get();
            app.post(() -> {
                state.lock.lock();
                try{
                    try{
                        state.result = prov.get();
                    }catch(Throwable ex){
                        state.result = new ThrowableWrapper(ex);
                    }
                    state.condition.signal();
                }finally{
                    state.lock.unlock();
                }
            });

            state.lock.lock();
            try{
                while(state.result == sentinel) state.condition.await();

                if(state.result instanceof ThrowableWrapper w) throw new RuntimeException(w.ex);
                return (T)state.result;
            }catch(InterruptedException e){
                lockStates.remove();
                t.interrupt();
                throw new RuntimeException(e);
            }finally{
                state.result = sentinel;
                state.lock.unlock();
            }
        }
    }

    public static void scope(Cons<Cons<Runnable>> scope){
        class ScopeTask extends CountedCompleter<Void>{
            final Runnable[] tasks;
            final int start, end;
            final AtomicReference<Throwable> error;

            ScopeTask(CountedCompleter<?> completer, Runnable[] tasks, int start, int end, AtomicReference<Throwable> error){
                super(completer);
                this.tasks = tasks;
                this.start = start;
                this.end = end;
                this.error = error;
            }

            @Override
            public void compute(){
                if(end - start == 1){
                    try{
                        tasks[start].run();
                    }catch(Throwable t){
                        error.compareAndSet(null, t);
                    }finally{
                        propagateCompletion();
                    }
                    return;
                }

                int mid = (start + end) >>> 1;
                addToPendingCount(1);

                new ScopeTask(this, tasks, mid, end, error).fork();
                new ScopeTask(this, tasks, start, mid, error).compute();
            }
        }

        if(pool != null){
            var stack = tasks.get();
            int start = stack.size;

            try{
                scope.get(stack::add);
                if(stack.size == start) return;

                AtomicReference<Throwable> error = new AtomicReference<>(null);
                pool.invoke(new ScopeTask(null, stack.items, start, stack.size, error));

                var t = error.get();
                if(t != null) throw new RuntimeException(t);
            }finally{
                stack.setSize(start);
            }
        }else{
            scope.get(Runnable::run);
        }
    }

    private static class LockState{
        private final Lock lock;
        private final Condition condition;
        private Object result;

        private LockState(){
            lock = new ReentrantLock();
            condition = lock.newCondition();
            result = sentinel;
        }
    }
}
