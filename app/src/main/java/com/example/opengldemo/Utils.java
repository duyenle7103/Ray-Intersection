package com.example.opengldemo;

public class Utils {
    public static float len(float[] vector) {
        float sumSquare = 0;
        for (int i = 0; i < vector.length; i++) {
            sumSquare += vector[i] * vector[i];
        }
        return (float) Math.sqrt(sumSquare);
    }

    public static float dotProduct(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new RuntimeException("Different vector length in dot product calculation");
        }
        float result = 0;
        for (int i = 0; i < vector1.length; i++) {
            result += vector1[i] * vector2[i];
        }
        return result;
    }

    public static void normalize(float[] vector) {
        float vectorLength = len(vector);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= vectorLength;
        }
    }

    public static float[] crossProduct(float[] vector1, float[] vector2) {
        float[] crossProduct = new float[]{
                vector1[1] * vector2[2] - vector1[2] * vector2[1],
                vector1[2] * vector2[0] - vector1[0] * vector2[2],
                vector1[0] * vector2[1] - vector1[1] * vector2[0]
        };
        return crossProduct;
    }
}