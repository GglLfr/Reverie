package reverie.util.reflect;

import reverie.*;

public interface ReferenceFieldHandle<C, T>{
    T get(C instance);

    void set(C instance, T newValue);

    static <C, T> ReferenceFieldHandle<C, T> of(Class<C> containingClass, String fieldName){
        try{
            var field = containingClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            if(Api.level >= Api.methodHandle) return new VarReference<>(field);
            else return new ReflectedReference<>(field);
        }catch(NoSuchFieldException e){
            throw new RuntimeException(e);
        }
    }
}
