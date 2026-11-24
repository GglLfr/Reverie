package reverie.util.reflect;

import reverie.*;

public interface ReferenceField<C, T>{
    T get(C instance);

    void set(C instance, T newValue);

    static <C, T> ReferenceField<C, T> of(Class<C> containingClass, String fieldName){
        try{
            var field = containingClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            if(Api.level >= Api.methodHandle) return new ReferenceHandle<>(field);
            else return new ReferenceReflect<>(field);
        }catch(NoSuchFieldException e){
            throw new RuntimeException(e);
        }
    }
}
