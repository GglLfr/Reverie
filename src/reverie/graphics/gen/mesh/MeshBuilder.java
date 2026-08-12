package reverie.graphics.gen.mesh;

import arc.graphics.*;
import arc.struct.*;
import reverie.util.collection.*;
import reverie.util.collection.FloatBelt.*;

public class MeshBuilder{
    public static final VertexAttribute emissiveAttr = new VertexAttribute(4, Gl.unsignedByte, true, "a_emissive");

    private final FloatBelt vertices = new FloatBelt();
    private final ShortBelt indices = new ShortBelt();

    private int pos2d = -1, pos3d = -1, normal = -1, diffuse = -1, emissive = -1;
    private final VertexAttribute[] attributes;
    public final int stride;

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

    public int vertex(int count, Enqueuer enqueuer){
        return vertices.enqueue(count * stride, enqueuer) / stride;
    }

    protected int ensure(int index, String error){
        if(index == -1){
            throw new IllegalArgumentException(error);
        }else{
            return index;
        }
    }

    public void pos2d(float[] vertices, int offset, float x, float y){
        int i = offset + ensure(pos2d, "Missing 2D position component");
        vertices[i] = x;
        vertices[i + 1] = y;
    }

    public void pos3d(float[] vertices, int offset, float x, float y, float z){
        int i = offset + ensure(pos3d, "Missing 3D position component");
        vertices[i] = x;
        vertices[i + 1] = y;
        vertices[i + 2] = z;
    }

    public void normal(float[] vertices, int offset, float nx, float ny, float nz){
        int i = offset + ensure(normal, "Missing normal component");
        vertices[i] = packNormals(nx, ny, nz);
    }

    public void diffuse(float[] vertices, int offset, float col){
        int i = offset + ensure(diffuse, "Missing diffuse color component");
        vertices[i] = col;
    }

    public void emissive(float[] vertices, int offset, float col){
        int i = offset + ensure(emissive, "Missing emissive color component");
        vertices[i] = col;
    }

    public void indices(short... indices){
        indices(indices, 0, indices.length);
    }

    public void indices(short[] indices, int offset, int length){
        this.indices.enqueue(length, (slice, sliceOffset) -> System.arraycopy(indices, offset, slice, sliceOffset, length));
    }

    public Mesh build(){
        return vertices.clear((vertices, vertexLength) -> indices.clear((indices, indexLength) -> {
            var out = new Mesh(true, vertexLength / stride, indexLength, attributes);
            out.getVerticesBuffer().position(0).limit(vertexLength).put(0, vertices, 0, vertexLength);
            out.getIndicesBuffer().position(0).limit(indexLength).put(0, indices, 0, indexLength);
            return out;
        }));
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