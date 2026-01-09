package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.math.*;

import java.io.Serializable;

/**
 * A <code>Vector3</code> that uses integers instead of floating-point numbers.
 */
public class Vector3Int implements Serializable {
        public int x;
        public int y;
        public int z;

        public Vector3Int() {
        }

        public Vector3Int(int x, int y, int z) {
            this.set(x, y, z);
        }

        public Vector3Int(Vector3Int vector) {
            this.set(vector);
        }

        public Vector3Int(int[] values) {
            this.set(values[0], values[1], values[2]);
        }

        public Vector3Int(Vector3 vector) { this.set((int)vector.x, (int)vector.y, (int)vector.z); }

        public int[] toList() { return new int[]{this.x, this.y, this.z}; }

        public Vector3Int set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Vector3Int set(Vector3Int vector) {
            return this.set(vector.x, vector.y, vector.z);
        }

        public Vector3Int cpy() {
            return new Vector3Int(this);
        }

        public Vector3Int add(Vector3Int vector) {
            return this.add(vector.x, vector.y, vector.z);
        }

        public Vector3Int add(int[] arr) {
            return this.set(this.x + arr[0], this.y + arr[1], this.z + arr[2]);
        }

        public Vector3Int add(int x, int y, int z) {
            return this.set(this.x + x, this.y + y, this.z + z);
        }

        public Vector3Int add(int values) {
            return this.set(this.x + values, this.y + values, this.z + values);
        }

        public Vector3Int sub(Vector3Int a_vec) {
            return this.sub(a_vec.x, a_vec.y, a_vec.z);
        }

        public Vector3Int sub(int x, int y, int z) {
            return this.set(this.x - x, this.y - y, this.z - z);
        }

        public Vector3Int sub(int value) {
            return this.set(this.x - value, this.y - value, this.z - value);
        }

        public int len2() {
            return this.x*this.x + this.y*this.y + this.z*this.z;
        }

        public double len() {
            return Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
        }

        public Vector3 toVector3() {
            return new Vector3(this.x, this.y, this.z);
        }

        public String toString() {
            return "(" + this.x + "," + this.y + "," + this.z + ")";
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + this.x;
            result = 31 * result + this.y;
            result = 31 * result + this.z;
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                Vector3Int other = (Vector3Int) obj;
                if (this.x != other.x) {
                    return false;
                } else if (this.y != other.y) {
                    return false;
                } else {
                    return this.z == other.z;
                }
            }
        }
    }
