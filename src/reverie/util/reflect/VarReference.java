package reverie.util.reflect;

import java.lang.invoke.*;
import java.lang.reflect.*;

@SuppressWarnings("unchecked")
final class VarReference<C, T> implements ReferenceFieldHandle<C, T>{
    final MethodHandle getter, setter;

    VarReference(Field field){
        try{
            var lookup = MethodHandles.lookup();

            var getter = lookup.unreflectGetter(field);
            if(Modifier.isStatic(field.getModifiers())) getter = MethodHandles.dropArguments(getter, 0, Object.class);
            this.getter = getter.asType(MethodType.methodType(Object.class, Object.class));

            if(!Modifier.isFinal(field.getModifiers())){
                var setter = lookup.unreflectSetter(field);
                if(Modifier.isStatic(field.getModifiers()))
                    setter = MethodHandles.dropArguments(setter, 0, Object.class);
                this.setter = setter.asType(MethodType.methodType(void.class, Object.class, Object.class));
            }else{
                this.setter = null;
            }
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public T get(C instance){
        try{
            return (T)getter.invokeExact(instance);
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(C instance, T newValue){
        try{
            setter.invokeExact(instance, newValue);
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
    }
}
