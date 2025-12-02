package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * The <code>CustomGameMath</code> class contains math utility functions that mostly center around Axis-Aligned Bounding-Box (AABB) and ray collision detection.
 */
public class CustomGameMath {
    /**
     * Ray-AABB intersection test using the slab method. See <a href="https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196">https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196</a>
     *
     * @param ray the ray that is projected
     * @param box the Axis-Aligned Bounding-Box that will be tested for collision
     * @param maxDist the maximum distance along the ray that the collision can occur
     * @return <code>true</code> if a collision occurs between the ray and AABB within the given maximum distance, else <code>false</code>
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

    /**
     * Ray-AABB intersection distance using the slab method. See <a href="https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196">https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196</a>
     *
     * @param ray the ray that is projected
     * @param box the Axis-Aligned Bounding-Box that will be tested for collision
     * @return the distance along the ray to the collision point, or -1 if no collision occurs
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
}