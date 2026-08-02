package reverie.graphics.shaders;

import reverie.graphics.gl.*;

import static reverie.graphics.RShaders.*;

public class DepthScreenspaceShader extends RShader{
    public RFrameBuffer buffer;

    public DepthScreenspaceShader(){
        super(file("depth-screenspace.vert"), file("depth-screenspace.frag"));
    }

    @Override
    public void apply(){
        buffer.getTexture().bind(1);
        buffer.getDepthTexture().bind(0);

        setUniformi("u_color", 1);
        setUniformi("u_depth", 0);
    }
}