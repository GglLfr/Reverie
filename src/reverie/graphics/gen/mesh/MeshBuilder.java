package reverie.graphics.gen.mesh;

import arc.func.*;
import arc.graphics.*;
import arc.struct.*;

public class MeshBuilder{
    public static final VertexAttribute emissiveAttr = new VertexAttribute(4, Gl.unsignedByte, true, "a_emissive");

    private final FloatSeq vertices = new FloatSeq();
    private final ShortSeq indices = new ShortSeq();

    private int pos2d = -1, pos3d = -1, normal = -1, diffuse = -1, emissive = -1;
    private final VertexAttribute[] attributes;
    private final int stride;

    public MeshBuilder(boolean is3D, boolean normal, boolean diffuse, boolean emissive){
        Seq<VertexAttribute> attributes = new Seq<>(4);
        int stride = 0;

        if(is3D){
            pos3d = stride;
            attributes.add(VertexAttribute.position3);
            stride += VertexAttribute.position3.size / Float.BYTES;
        }else{
            pos2d = stride;
            attributes.add(VertexAttribute.position);
            stride += VertexAttribute.position.size / Float.BYTES;
        }

        if(normal){
            this.normal = stride;
            attributes.add(VertexAttribute.packedNormal);
            stride += VertexAttribute.packedNormal.size / Float.BYTES;
        }

        if(diffuse){
            this.diffuse = stride;
            attributes.add(VertexAttribute.color);
            stride += VertexAttribute.color.size / Float.BYTES;
        }

        if(emissive){
            this.emissive = stride;
            attributes.add(emissiveAttr);
            stride += emissiveAttr.size / Float.BYTES;
        }

        this.attributes = attributes.toArray(VertexAttribute.class);
        this.stride = stride;
    }

    protected int incr(int index, Intc set){
        if(index == -1){
            throw new IllegalArgumentException("Missing component in MeshBuilder, ensure the necesarry flag is enabled in the constructor");
        }else{
            int expectedSize = (index / stride + 1) * stride;
            vertices.ensureCapacity(expectedSize - vertices.size);
            vertices.setSize(expectedSize);

            set.get(index + stride);
            return index;
        }
    }

    public void pos2d(float x, float y){
        int i = incr(pos2d, pos2d -> this.pos2d = pos2d);
        vertices.items[i] = x;
        vertices.items[i + 1] = y;
    }

    public void pos3d(float x, float y, float z){
        int i = incr(pos3d, pos3d -> this.pos3d = pos3d);
        vertices.items[i] = x;
        vertices.items[i + 1] = y;
        vertices.items[i + 2] = z;
    }

    public void normal(float nx, float ny, float nz){
        int i = incr(normal, normal -> this.normal = normal);
        vertices.items[i] = packNormals(nx, ny, nz);
    }

    public void diffuse(float col){
        int i = incr(diffuse, diffuse -> this.diffuse = diffuse);
        vertices.items[i] = col;
    }

    public void emissive(float col){
        int i = incr(emissive, emissive -> this.emissive = emissive);
        vertices.items[i] = col;
    }

    public void indices(short... indices){
        indices(indices, 0, indices.length);
    }

    public void indices(short[] indices, int offset, int length){
        this.indices.addAll(indices, offset, length);
    }

    public Mesh build(){
        var out = new Mesh(true, vertices.size / stride, indices.size, attributes);
        out.getVerticesBuffer().position(0).limit(vertices.size).put(0, vertices.items, 0, vertices.size);
        out.getIndicesBuffer().position(0).limit(indices.size).put(0, indices.items, 0, indices.size);
        return out;
    }

    public static float packNormals(float nx, float ny, float nz){
        int xs = nx < -1f / 512f ? 1 : 0;
        int ys = ny < -1f / 512f ? 1 : 0;
        int zs = nz < -1f / 512f ? 1 : 0;

        int vi = zs << 29 | ((int)(nz * 511 + (zs << 9)) & 511) << 20 |
                     ys << 19 | ((int)(ny * 511 + (ys << 9)) & 511) << 10 |
                     xs << 9 | ((int)(nx * 511 + (xs << 9)) & 511);

        return Float.intBitsToFloat(vi);
    }
}