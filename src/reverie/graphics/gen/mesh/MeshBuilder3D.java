package reverie.graphics.gen.mesh;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.maps.generators.*;
import reverie.util.*;

import static reverie.Reverie.*;
import static reverie.util.Temporaries.*;

public final class MeshBuilder3D{
    private MeshBuilder3D(){
        throw new AssertionError();
    }

    public static Mesh buildHexDual(HexMesher outward, HexMesher inward, int divisions, float radius, float intensity){
        Timers.mark();

        var grid = PlanetGrid.create(divisions);
        if(outward instanceof PlanetGenerator gen){
            gen.seed = gen.baseSeed;
        }
        if(inward instanceof PlanetGenerator gen){
            gen.seed = gen.baseSeed;
        }

        boolean emitOutward = outward.isEmissive(), emitInward = inward.isEmissive();
        var builder = new MeshBuilder(true, true, true, emitOutward || emitInward);

        var skips = boolArrayPool.obtain();
        skips.setSize(grid.tiles.length);

        var heights = floatArrayPool.obtain();
        heights.setSize(grid.corners.length * 2);

        // [outward, ..] + [inward, ..]
        for(var c : grid.corners){
            heights.items[c.id] = (1f + outward.getHeight(c.v) * intensity) * radius;
            heights.items[c.id + grid.corners.length] = (1f - inward.getHeight(c.v) * intensity) * radius;
        }

        int vertexCount = 0;
        for(var t : grid.tiles){
            if(!(skips.items[t.id] = outward.getHeight(t.v) < -inward.getHeight(t.v))){
                vertexCount += t.corners.length * 2;
            }else{
                for(var c : t.corners){
                    float outHeight = heights.items[c.id], inHeight = heights.items[grid.corners.length + c.id];
                    heights.items[c.id] = heights.items[grid.corners.length + c.id] = (outHeight + inHeight) / 2f;
                }
            }
        }

        int vertexCountFinal = vertexCount;
        Tasks.scope(scope -> Iterables.split(grid.tiles, 1024, (start, end) -> scope.get(() -> {
            var indices = vertexCountFinal < 65536 ? new short[3] : null;

            var col = new Color();
            var nor = new Vec3();
            for(int tileIndex = start; tileIndex < end; tileIndex++){
                var tile = grid.tiles[tileIndex];
                // TODO: `skip(Vec3)` is unsupported here; instead, skipping is only done when the two curves intersect.
                //       Should it be supported? I have no such usecases...
                if(skips.items[tile.id]) continue;

                var c = tile.corners;
                for(boolean isOutward : Mathf.booleans){
                    int cOffset = isOutward ? 0 : grid.corners.length;

                    Corner c1 = c[0], c2 = c[isOutward ? 2 : 4], c3 = c[isOutward ? 4 : 2];
                    float h1 = heights.items[cOffset + c1.id], h2 = heights.items[cOffset + c2.id], h3 = heights.items[cOffset + c3.id];
                    Vec3 v1 = c1.v, v2 = c2.v, v3 = c3.v;

                    normal(
                        v1.x * h1, v1.y * h1, v1.z * h1,
                        v2.x * h2, v2.y * h2, v2.z * h2,
                        v3.x * h3, v3.y * h3, v3.z * h3,
                        nor
                    );

                    (isOutward ? outward : inward).getColor(tile.v, col.set(1f, 1f, 1f, 1f));

                    float color = col.toFloatBits();
                    float emissive;
                    if(isOutward ? emitOutward : emitInward){
                        (isOutward ? outward : inward).getEmissiveColor(tile.v, col.set(0f, 0f, 0f, 0f));
                        emissive = col.toFloatBits();
                    }else{
                        emissive = 0f;
                    }

                    if(indices != null){
                        int position = builder.vertex(c.length, (verts, off) -> {
                            for(var corner : c){
                                float height = heights.items[cOffset + corner.id];

                                builder.pos3d(verts, off, corner.v.x * height, corner.v.y * height, corner.v.z * height);
                                builder.normal(verts, off, nor.x, nor.y, nor.z);
                                builder.diffuse(verts, off, color);
                                if(emitOutward || emitInward) builder.emissive(verts, off, emissive);

                                off += builder.stride;
                            }
                        });

                        for(int i = 0; i < c.length - 2; i++){
                            indices[0] = (short)(position);
                            indices[1] = (short)(position + i + (isOutward ? 1 : 2));
                            indices[2] = (short)(position + i + (isOutward ? 2 : 1));
                            builder.indices(indices);
                        }
                    }else{
                        for(int i = 0; i < c.length - 2; i++){
                            float
                                ca = heights.items[cOffset + c[0].id],
                                cb = heights.items[cOffset + c[i + (isOutward ? 1 : 2)].id],
                                cc = heights.items[cOffset + c[i + (isOutward ? 2 : 1)].id];

                            int index = i;
                            builder.vertex(3, (verts, off) -> {
                                builder.pos3d(verts, off, c[0].v.x * ca, c[0].v.y * ca, c[0].v.z * ca);
                                builder.normal(verts, off, nor.x, nor.y, nor.z);
                                builder.diffuse(verts, off, color);
                                if(emitOutward || emitInward) builder.emissive(verts, off, emissive);

                                off += builder.stride;
                                builder.pos3d(verts, off, c[index + (isOutward ? 1 : 2)].v.x * cb, c[index + (isOutward ? 1 : 2)].v.y * cb, c[index + (isOutward ? 1 : 2)].v.z * cb);
                                builder.normal(verts, off, nor.x, nor.y, nor.z);
                                builder.diffuse(verts, off, color);
                                if(emitOutward || emitInward) builder.emissive(verts, off, emissive);

                                off += builder.stride;
                                builder.pos3d(verts, off, c[index + (isOutward ? 2 : 1)].v.x * cc, c[index + (isOutward ? 2 : 1)].v.y * cc, c[index + (isOutward ? 2 : 1)].v.z * cc);
                                builder.normal(verts, off, nor.x, nor.y, nor.z);
                                builder.diffuse(verts, off, color);
                                if(emitOutward || emitInward) builder.emissive(verts, off, emissive);
                            });
                        }
                    }
                }
            }
        })));

        floatArrayPool.free(heights);
        boolArrayPool.free(skips);
        Log.debug("@ Took @ms to build dual hex mesh.", logTag, Timers.elapsed());

        return builder.build();
    }

    private static void normal(float v1x, float v1y, float v1z, float v2x, float v2y, float v2z, float v3x, float v3y, float v3z, Vec3 out){
        float
            x = v2x - v1x,
            y = v2y - v1y,
            z = v2z - v1z,

            vx = v3x - v1x,
            vy = v3y - v1y,
            vz = v3z - v1z,

            cx = y * vz - z * vy,
            cy = z * vx - x * vz,
            cz = x * vy - y * vx;

        out.set(cx, cy, cz).nor();
    }
}
