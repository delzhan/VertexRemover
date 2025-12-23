package com.cgvsu.math;

import java.util.Objects;

public class Vector3f {
    private static final float EPSILON = 1e-7f;
    private final float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3f other = (Vector3f) obj;
        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON &&
                Math.abs(z - other.z) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Float.hashCode(x),
                Float.hashCode(y),
                Float.hashCode(z)
        );
    }

    public boolean equals(Vector3f other) {
        if (other == null) return false;
        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON &&
                Math.abs(z - other.z) < EPSILON;
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.6f, %.6f, %.6f)", x, y, z);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    // Векторное вычитание: this - other
    public Vector3f subtract(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    // Векторное произведение: this x other
    public Vector3f cross(Vector3f other) {
        float newX = this.y * other.z - this.z * other.y;
        float newY = this.z * other.x - this.x * other.z;
        float newZ = this.x * other.y - this.y * other.x;
        return new Vector3f(newX, newY, newZ);
    }

    // Нормализация вектора (приведение к длине 1)
    public Vector3f normalize() {
        float length = (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        // Избегаем деления на ноль
        if (Math.abs(length) < EPSILON) {
            return new Vector3f(0, 0, 0);
        }
        return new Vector3f(this.x / length, this.y / length, this.z / length);
    }

    // Сложение векторов: this + other
    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    // Метод для удобной записи в OBJ-файл (формат "x y z")
    public String toObjString() {
        // Используем 6 знаков после запятой, как в стандартных OBJ-файлах
        return String.format("%.6f %.6f %.6f", x, y, z);
    }
}