package reverie.graphics.gl;

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;

public class RFrameBuffer extends FrameBuffer{
    protected Format format;
    protected boolean hasDepth, hasStencil;

    public RFrameBuffer(){
        this(2, 2);
    }

    public RFrameBuffer(int width, int height){
        this(Format.rgba8888, width, height, false, false);
    }

    public RFrameBuffer(Format format, int width, int height){
        this(format, width, height, false, false);
    }

    public RFrameBuffer(Format format, int width, int height, boolean hasDepth){
        this(format, width, height, hasDepth, false);
    }

    public RFrameBuffer(int width, int height, boolean hasDepth){
        this(Format.rgba8888, width, height, hasDepth, false);
    }

    public RFrameBuffer(Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        create(format, width, height, hasDepth, hasStencil);
    }

    public @Nullable Texture getDepthTexture(){
        return hasDepth ? textureAttachments.get(1) : null;
    }

    public @Nullable Texture getStencilTexture(){
        return hasStencil ? textureAttachments.get(1) : null;
    }

    @Override
    protected void create(Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        width = Math.max(width, 2);
        height = Math.max(height, 2);
        this.format = format;

        var builder = new FrameBufferBuilder(width, height);
        builder.addBasicColorTextureAttachment(format);
        if(hasDepth && hasStencil){
            var spec = new FrameBufferTextureAttachmentSpec(GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8);
            Reflect.set(FrameBufferTextureAttachmentSpec.class, spec, "isDepth", true);
            Reflect.set(FrameBufferTextureAttachmentSpec.class, spec, "isStencil", true);
            Reflect.<Seq<FrameBufferTextureAttachmentSpec>>get(GLFrameBufferBuilder.class, builder, "textureAttachmentSpecs").add(spec);
        }else if(hasDepth){
            builder.addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT24, GL30.GL_UNSIGNED_INT);
        }else if(hasStencil){
            builder.addStencilTextureAttachment(GL30.GL_STENCIL_INDEX8, GL30.GL_UNSIGNED_BYTE);
        }

        this.hasDepth = hasDepth;
        this.hasStencil = hasStencil;
        bufferBuilder = builder;
        build();
    }

    @Override
    protected Texture createTexture(FrameBufferTextureAttachmentSpec spec){
        var result = super.createTexture(spec);
        if(!spec.isColorTexture()) result.setFilter(TextureFilter.nearest);

        return result;
    }

    @Override
    public void resize(int width, int height){
        width = Math.max(width, 2);
        height = Math.max(height, 2);
        if(width == getWidth() && height == getHeight()) return;

        TextureFilter min = getTexture().getMinFilter(), mag = getTexture().getMagFilter();
        dispose();

        var builder = new FrameBufferBuilder(width, height);
        builder.addBasicColorTextureAttachment(format);
        if(hasDepth && hasStencil){
            var spec = new FrameBufferTextureAttachmentSpec(GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8);
            Reflect.set(FrameBufferTextureAttachmentSpec.class, spec, "isDepth", true);
            Reflect.set(FrameBufferTextureAttachmentSpec.class, spec, "isStencil", true);
            Reflect.<Seq<FrameBufferTextureAttachmentSpec>>get(GLFrameBufferBuilder.class, builder, "textureAttachmentSpecs").add(spec);
        }else if(hasDepth){
            builder.addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT24, GL30.GL_UNSIGNED_INT);
        }else if(hasStencil){
            builder.addStencilTextureAttachment(GL30.GL_STENCIL_INDEX8, GL30.GL_UNSIGNED_BYTE);
        }

        bufferBuilder = builder;
        textureAttachments.clear();
        framebufferHandle = depthbufferHandle = stencilbufferHandle = depthStencilPackedBufferHandle = 0;
        hasDepthStencilPackedBuffer = isMRT = false;

        build();

        // Ignore filters for depth and stencil textures, as changing them in the first place is always a wrong choice.
        getTexture().setFilter(min, mag);
    }

    @Override
    public void begin(Color clearColor){
        begin();
        Gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        Gl.clearDepthf(1f);
        Gl.clear(Gl.colorBufferBit | (hasDepth ? Gl.depthBufferBit : 0) | (hasStencil ? Gl.stencilBufferBit : 0));
    }
}