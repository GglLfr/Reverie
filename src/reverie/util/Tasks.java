package reverie.util;

import arc.func.*;
import arc.struct.*;
import arc.util.*;

import java.util.concurrent.*;

import static arc.Core.*;

public final class Tasks{
    private static final ThreadLocal<Seq<Runnable>> tasks = ThreadLocal.withInitial(() -> new Seq<>(Runnable.class));
    private static final ForkJoinPool pool;

    static{
        if(app.isDesktop() || app.isAndroid() && app.getVersion() >= 24){
            pool = Reflect.invoke(ForkJoinPool.class, "commonPool");
        }else{
            pool = null;
        }
    }

    private Tasks(){
        throw new AssertionError();
    }

    public static void scope(Cons<Cons<Runnable>> scope){
        class ScopeTask extends CountedCompleter<Void>{
            final Runnable[] tasks;
            final int start, end;

            ScopeTask(CountedCompleter<?> completer, Runnable[] tasks, int start, int end){
                super(completer);
                this.tasks = tasks;
                this.start = start;
                this.end = end;
            }

            @Override
            public void compute(){
                if(end - start == 1){
                    tasks[start].run();
                    propagateCompletion();
                    return;
                }

                int mid = (start + end) >>> 1;
                addToPendingCount(1);

                new ScopeTask(this, tasks, mid, end).fork();
                new ScopeTask(this, tasks, start, mid).compute();
            }
        }

        if(pool != null){
            var stack = tasks.get();
            int start = stack.size;

            scope.get(stack::add);
            if(stack.size == start) return;

            try{
                pool.invoke(new ScopeTask(null, stack.items, start, stack.size));
            }finally{
                stack.setSize(start);
            }
        }else{
            scope.get(Runnable::run);
        }
    }
}
