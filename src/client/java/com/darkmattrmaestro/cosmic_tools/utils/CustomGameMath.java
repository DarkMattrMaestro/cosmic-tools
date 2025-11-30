package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.collision.Segment;

public class CustomGameMath {
    public static BoundingBox expandAABB(BoundingBox bb, float radius) {
        return new BoundingBox(
                bb.min.sub(radius),
                bb.max.add(radius)
        );
    }

    /***
     * Ray-AABB intersection test using the slab method. See <a href="https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196">https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196</a>
     *
     * @param ray
     * @param box
     * @return
     */
    public static boolean rayAABBTest(Ray ray, BoundingBox box, double maxDist) {
        Vector3 inv_dir = new Vector3(
                1 / ray.direction.x,
                1 / ray.direction.y,
                1 / ray.direction.z
        );

        double tx1 = (box.min.x - ray.origin.x)*inv_dir.x;
        double tx2 = (box.max.x - ray.origin.x)*inv_dir.x;

        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);

        double ty1 = (box.min.y - ray.origin.y)*inv_dir.y;
        double ty2 = (box.max.y - ray.origin.y)*inv_dir.y;

        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));

        double tz1 = (box.min.z - ray.origin.z)*inv_dir.z;
        double tz2 = (box.max.z - ray.origin.z)*inv_dir.z;

        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));

        return tmax >= Math.max(0.0, tmin) && tmin < maxDist;
    }

    /***
     * Ray-AABB intersection test using the slab method. See <a href="https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196">https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196</a>
     *
     * @param ray
     * @param box
     * @return
     */
    public static double rayAABBTest(Ray ray, BoundingBox box) {
        Vector3 inv_dir = new Vector3(
                1 / ray.direction.x,
                1 / ray.direction.y,
                1 / ray.direction.z
        );

        double tx1 = (box.min.x - ray.origin.x)*inv_dir.x;
        double tx2 = (box.max.x - ray.origin.x)*inv_dir.x;

        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);

        double ty1 = (box.min.y - ray.origin.y)*inv_dir.y;
        double ty2 = (box.max.y - ray.origin.y)*inv_dir.y;

        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));

        double tz1 = (box.min.z - ray.origin.z)*inv_dir.z;
        double tz2 = (box.max.z - ray.origin.z)*inv_dir.z;

        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));

        if (tmax >= Math.max(0.0, tmin)) {
            return tmin;
        }
        return -1;
    }

    /***
     * Segment-AABB intersection test using the slab method. See <a href="https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196">https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196</a>
     *
     * @param segment
     * @param box
     * @return
     */
    public static boolean segmentAABBTest(Segment segment, BoundingBox box) {
//        Constants.LOGGER.info("&&&&&&&&& segmentAABBTest Called!");

        float length = segment.len();
        Vector3 dir = new Vector3(
                (segment.b.x - segment.a.x) / length,
                (segment.b.y - segment.a.y) / length,
                (segment.b.z - segment.a.z) / length
        );
        Vector3 inv_dir = new Vector3(
                1 / dir.x,
                1 / dir.y,
                1 / dir.z
        );

        double tx1 = (box.min.x - segment.a.x)*inv_dir.x;
        double tx2 = (box.max.x - segment.a.x)*inv_dir.x;

        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);

        double ty1 = (box.min.y - segment.a.y)*inv_dir.y;
        double ty2 = (box.max.y - segment.a.y)*inv_dir.y;

        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));

        double tz1 = (box.min.z - segment.a.z)*inv_dir.z;
        double tz2 = (box.max.z - segment.a.z)*inv_dir.z;

        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));

        return tmax >= Math.max(0.0, tmin) && tmin < segment.len();
    }

    /***
     * Ray-AABB intersection distance using a method from Stack Exchange. See <a href="https://gamedev.stackexchange.com/a/18459/197454">https://gamedev.stackexchange.com/a/18459/197454</a>
     *
     * @param r
     * @param box
     * @return Distance to point of collision, or -1 if no collision occurs
     */
    public static double segmentAABBCollisionDist(Ray r, BoundingBox box) {
        Vector3 dirfrac = new Vector3();
        // r.dir is unit direction vector of ray
        dirfrac.x = 1.0f / r.direction.x;
        dirfrac.y = 1.0f / r.direction.y;
        dirfrac.z = 1.0f / r.direction.z;
        // lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
        // r.org is origin of ray
        float t1 = (box.min.x - r.origin.x)*dirfrac.x;
        float t2 = (box.max.x - r.origin.x)*dirfrac.x;
        float t3 = (box.min.y - r.origin.y)*dirfrac.y;
        float t4 = (box.max.y - r.origin.y)*dirfrac.y;
        float t5 = (box.min.z - r.origin.z)*dirfrac.z;
        float t6 = (box.max.z - r.origin.z)*dirfrac.z;

        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // if tmax < 0, ray (line) is intersecting AABB, but the whole AABB is behind us
        if (tmax < 0)
        {
            return -1;
        }

        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax)
        {
            return -1;
        }

        return tmin;
    }
}