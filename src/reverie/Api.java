package reverie;

public final class Api{
    public static final int level;

    public static final int
        commonForkJoinPool = 24,
    // Technically this should be 26, but there's no easy way to selectively dex polymorphic-signature calls.
    // So only enable method handles on Desktop.
    methodHandle = Integer.MAX_VALUE,
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
