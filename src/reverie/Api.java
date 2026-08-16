package reverie;

public final class Api{
    public static final int level;

    public static final int
        methodHandle = 26,
        varHandle = 33,
        memoryOrder = 33;

    static{
        int sdk;
        try{
            var build = Class.forName("android.os.Build");
            var version = build.getField("VERSION").get(null);
            sdk = (int)version.getClass().getField("SDK_INT").get(version);
        }catch(ClassNotFoundException | NoSuchFieldException e){
            sdk = Integer.MAX_VALUE;
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }
        level = sdk;
    }

    private Api(){
        throw new AssertionError();
    }
}
