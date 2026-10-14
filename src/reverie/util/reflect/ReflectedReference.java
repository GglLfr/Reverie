package reverie.util.reflect;

import java.lang.reflect.*;

@SuppressWarnings("unchecked")
final class ReflectedReference<C, T> implements ReferenceFieldHandle<C, T>{
    final Field field;

    ReflectedReference(Field field){
        this.field = field;
    }

    @Override
    public T get(C instance){
        try{
            return (T)field.get(instance);
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(C instance, T newValue){
        try{
            field.set(instance, newValue);
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }
}
