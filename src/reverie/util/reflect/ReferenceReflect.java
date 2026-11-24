package reverie.util.reflect;

import java.lang.reflect.*;

@SuppressWarnings("unchecked")
final class ReferenceReflect<C, T> implements ReferenceField<C, T>{
    final Field field;

    ReferenceReflect(Field field){
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
