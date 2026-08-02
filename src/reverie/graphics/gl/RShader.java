package reverie.graphics.gl;

import arc.files.*;
import arc.graphics.gl.*;

import static arc.Core.*;

public class RShader extends Shader{
    public RShader(Fi vert, Fi frag){
        super(vert, frag);
    }

    @Override
    protected String preprocess(String source, boolean fragment){
        if(source.contains("#ifdef GL_ES")){
            throw new IllegalArgumentException("Shader contains GL_ES specific code; this should be handled by the preprocessor. Code: \n```\n" + source + "\n```");
        }

        if(source.contains("#version")){
            throw new IllegalArgumentException("Shader contains explicit version requirement; this should be handled by the preprocessor. Code: \n```\n" + source + "\n```");
        }

        var version = app.isDesktop() ? "330 core" : "300 es";
        if(fragment){
            source = String.format(
                """
                    #version %s
                    #ifdef GL_ES
                        precision %s float;
                    #else
                        #define lowp
                        #define mediump
                        #define highp
                    #endif
                    
                    %s
                    """,
                version,
                (source.contains("#define HIGHP") && !source.contains("//#define HIGHP") ? "highp" : "mediump"),
                source
            );
        }else{
            source = String.format(
                """
                    #version %s
                    #ifndef GL_ES
                        #define lowp
                        #define mediump
                        #define highp
                    #endif
                    
                    %s
                    """,
                version,
                source
            );
        }

        return source;
    }
}