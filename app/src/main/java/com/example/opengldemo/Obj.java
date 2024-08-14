package com.example.opengldemo;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

public abstract class Obj {
    protected float[] mModelMatrix = new float[16];
    protected float[] mMVPMatrix = new float[16];
    protected float[] mMVMatrix = new float[16];
    protected float[] mInv_Trans_Matrix = new float[16];
    protected final int mBytesPerFloat = 4;
    protected final int mPositionDataSize = 3;
    protected final int mStrideBytes = mPositionDataSize * mBytesPerFloat;
    protected final int mPositionOffset = 0;

    public abstract void draw(Renderer renderer, float[] color);
}

class Ray extends Obj {
    public float x0;
    public float y0;
    public float z0;
    public float xd;
    public float yd;
    public float zd;

    public Ray(float x0, float y0, float z0, double xd, double yd, double zd) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        //Normalize
        double len = xd * xd + yd * yd + zd * zd;
        this.xd = (float) xd / ((float) Math.sqrt(len));
        this.yd = (float) yd / ((float) Math.sqrt(len));
        this.zd = (float) zd / ((float) Math.sqrt(len));
    }

    public float[] setModelMatrix(float[] translate, float[] rotate, float[] scale) {
        Log.d("Sphere", "Set Model Matrix");
        Matrix.setIdentityM(mModelMatrix, 0);
        if (scale != null) {
            Log.d("Sphere", "Tranlate");
            Matrix.scaleM(mModelMatrix, 0, scale[0], scale[1], scale[2]);
        }
        if (translate != null) {
            Log.d("Sphere", "T");
            Matrix.translateM(mModelMatrix, 0, translate[0], translate[1], translate[2]);
        }
        if (rotate != null) {
            Matrix.rotateM(mModelMatrix, 0, rotate[0], rotate[1], rotate[2], rotate[3]);
        }
        return mModelMatrix;
    }

    @Override
    public void draw(Renderer renderer, float[] color) {
        Log.d("Ray", "Draw");

        if (color == null) {
            color = new float[]{1, 1, 1, 1};
        }
        if (color.length != 4) {
            throw new RuntimeException("Invalid color");
        }
        Point origin = new Point(x0, y0, z0);
        Point end = new Point(x0 + xd * 100, y0 + yd * 100, z0 + zd * 100);
        Line line = new Line(origin, end);
        origin.mModelMatrix = this.mModelMatrix;
        origin.draw(renderer, color);
        line.mModelMatrix = this.mModelMatrix;
        line.draw(renderer, color);
    }
}

class Line extends Obj {
    public Point point1;
    public Point point2;
    static private FloatBuffer verticesBuffer;

    public Line(Point point1, Point point2) {
        this.point1 = point1;
        this.point2 = point2;
        verticesBuffer = ByteBuffer.allocateDirect(6 * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(new float[]{this.point1.x, this.point1.y, this.point1.z, this.point2.x, this.point2.y, this.point2.z});
    }

    public float[] setModelMatrix(float[] translate, float[] rotate, float[] scale) {
        Log.d("Sphere", "Set Model Matrix");
        Matrix.setIdentityM(mModelMatrix, 0);
        if (scale != null) {
            Log.d("Sphere", "Tranlate");
            Matrix.scaleM(mModelMatrix, 0, scale[0], scale[1], scale[2]);
        }
        if (translate != null) {
            Log.d("Sphere", "T");
            Matrix.translateM(mModelMatrix, 0, translate[0], translate[1], translate[2]);
        }
        if (rotate != null) {
            Matrix.rotateM(mModelMatrix, 0, rotate[0], rotate[1], rotate[2], rotate[3]);
        }
        return mModelMatrix;
    }

    @Override
    public void draw(Renderer renderer, float[] color) {
        Log.d("Line", "Draw line: (" + point1.x + "," + point1.y + "," + point1.z + "),(" + point2.x + "," + point2.y + "," + point2.z + ")");
        Log.d("Line", "Draw line: color = " + color[0] + "," + color[1] + "," + color[2]);
        if (color == null) {
            color = new float[]{1, 1, 1, 1};
        }
        if (color.length != 4) {
            throw new RuntimeException("Invalid color");
        }
        Log.d("Line", "Verticesbuffer capacity = " + verticesBuffer.capacity());
        verticesBuffer.position(mPositionOffset);
        GLES20.glEnableVertexAttribArray(renderer.mPositionHandle);
        GLES20.glVertexAttribPointer(renderer.mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, verticesBuffer);
        Matrix.multiplyMM(mMVMatrix, 0, renderer.mViewMatrix, 0, mModelMatrix, 0);
        Matrix.invertM(mInv_Trans_Matrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mInv_Trans_Matrix, 0, mInv_Trans_Matrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, renderer.mProjectionMatrix, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(renderer.mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform4fv(renderer.mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
    }
}

class Point extends Obj {
    public float x;
    public float y;
    public float z;
    FloatBuffer pointBuffer;

    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        pointBuffer = ByteBuffer.allocateDirect(3 * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pointBuffer.put(new float[]{this.x, this.y, this.z});
    }

    public Point(float[] coords) {
        if (coords.length != 3) {
            throw new RuntimeException("Invalid point coordinate");
        }
        this.x = coords[0];
        this.y = coords[1];
        this.z = coords[2];
        pointBuffer = ByteBuffer.allocateDirect(3 * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pointBuffer.put(new float[]{this.x, this.y, this.z});
    }

    public float distanceTo(Point other) {
        return (float) Math.sqrt((this.x - other.x) * (this.x - other.x) + (this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.z - other.z));
    }

    public float[] setModelMatrix(float[] translate, float[] rotate, float[] scale) {
        Log.d("Sphere", "Set Model Matrix");
        Matrix.setIdentityM(mModelMatrix, 0);
        if (scale != null) {
            Log.d("Sphere", "Tranlate");
            Matrix.scaleM(mModelMatrix, 0, scale[0], scale[1], scale[2]);
        }
        if (translate != null) {
            Log.d("Sphere", "T");
            Matrix.translateM(mModelMatrix, 0, translate[0], translate[1], translate[2]);
        }
        if (rotate != null) {
            Matrix.rotateM(mModelMatrix, 0, rotate[0], rotate[1], rotate[2], rotate[3]);
        }
        return mModelMatrix;
    }

    @Override
    public void draw(Renderer renderer, float[] color) {
        Log.d("Point", "Draw point:" + this.x + "," + this.y + "," + this.z);
        if (color == null) {
            color = new float[]{1, 1, 1, 1};
        }
        if (color.length != 4) {
            throw new RuntimeException("Invalid color");
        }
        pointBuffer.position(mPositionOffset);
        GLES20.glEnableVertexAttribArray(renderer.mPositionHandle);
        GLES20.glVertexAttribPointer(renderer.mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, pointBuffer);
        Matrix.multiplyMM(mMVMatrix, 0, renderer.mViewMatrix, 0, mModelMatrix, 0);
        Matrix.invertM(mInv_Trans_Matrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mInv_Trans_Matrix, 0, mInv_Trans_Matrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, renderer.mProjectionMatrix, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(renderer.mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform4fv(renderer.mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }
}

class Triangle extends Obj {
    static private FloatBuffer verticesBuffer;
    public float[] vertex1;
    public float[] vertex2;
    public float[] vertex3;
    public float[] normal1;
    public float[] normal2;
    public float[] normal3;
    final float MUL = 3.0f;
    //ax + by + cz + d;
    public float a;
    public float b;
    public float c;
    public float d;

    public Triangle(float[] vertex1, float[] vertex2, float[] vertex3) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.vertex3 = vertex3;
        verticesBuffer = ByteBuffer.allocateDirect(9 * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(this.vertex1);
        verticesBuffer.put(this.vertex2);
        verticesBuffer.put(this.vertex3);
        findPlaneEquation();
    }

    public Triangle(float[] vertex1, float[] vertex2, float[] vertex3, float[] norm1, float[] norm2, float[] norm3) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.vertex3 = vertex3;
        this.normal1 = norm1;
        this.normal2 = norm2;
        this.normal3 = norm3;
        Utils.normalize(this.normal1);
        Utils.normalize(this.normal2);
        Utils.normalize(this.normal3);
        verticesBuffer = ByteBuffer.allocateDirect(9 * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(this.vertex1);
        verticesBuffer.put(this.vertex2);
        verticesBuffer.put(this.vertex3);
        findPlaneEquation();
    }

    private void findPlaneEquation() {
        // Tính vector v1 và v2
        float[] v1 = {vertex2[0] - vertex1[0], vertex2[1] - vertex1[1], vertex2[2] - vertex1[2]};
        float[] v2 = {vertex3[0] - vertex1[0], vertex3[1] - vertex1[1], vertex3[2] - this.vertex1[2]};
        // Tính tích có hướng của v1 và v2 để tìm vector pháp tuyến
        a = v1[1] * v2[2] - v1[2] * v2[1];
        b = v1[2] * v2[0] - v1[0] * v2[2];
        c = v1[0] * v2[1] - v1[1] * v2[0];
        float len = (float) Math.sqrt(a * a + b * b + c * c);
        a = a / len;
        b = b / len;
        c = c / len;
        // Tính d
        d = -(a * vertex1[0] + b * vertex1[1] + c * vertex1[2]);
    }

    public float[] setModelMatrix(float[] translate, float[] rotate, float[] scale) {
        Matrix.setIdentityM(mModelMatrix, 0);
        if (scale != null) {
            Matrix.scaleM(mModelMatrix, 0, scale[0], scale[1], scale[2]);
        }
        if (translate != null) {
            Matrix.translateM(mModelMatrix, 0, translate[0], translate[1], translate[2]);
        }
        if (rotate != null) {
            Matrix.rotateM(mModelMatrix, 0, rotate[0], rotate[1], rotate[2], rotate[3]);
        }
        return mModelMatrix;
    }

    public void draw(Renderer renderer, float[] color) {
        Log.d("Triangle", verticesBuffer.capacity() + "");
        if (color == null) {
            color = new float[]{1, 1, 1, 1};
        }
        if (color.length != 4) {
            throw new RuntimeException("Invalid color");
        }
        verticesBuffer.position(mPositionOffset);
        GLES20.glEnableVertexAttribArray(renderer.mPositionHandle);
        GLES20.glVertexAttribPointer(renderer.mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, verticesBuffer);
        Matrix.multiplyMM(mMVMatrix, 0, renderer.mViewMatrix, 0, mModelMatrix, 0);
        Matrix.invertM(mInv_Trans_Matrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mInv_Trans_Matrix, 0, mInv_Trans_Matrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, renderer.mProjectionMatrix, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(renderer.mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform4fv(renderer.mColorHandle, 1, color, 0);
        Log.d("Triangle", "Test draw");
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        if (normal1 != null) {
            Log.d("Triangle", "Line1");
            Line line1 = new Line(new Point(vertex1), new Point(new float[]{vertex1[0] + this.normal1[0] * MUL, vertex1[1] + this.normal1[1] * MUL, vertex1[2] + this.normal1[2] * MUL}));
            line1.setModelMatrix(null, null, new float[]{0.2f, 0.2f, 0.2f});
            line1.draw(renderer, new float[]{1, 0, 0, 1});
            Log.d("Triangle", "Line1");
            Line line2 = new Line(new Point(vertex2), new Point(new float[]{vertex2[0] + this.normal2[0] * MUL, vertex2[1] + this.normal2[1] * MUL, vertex2[2] + this.normal2[2] * MUL}));
            line2.setModelMatrix(null, null, new float[]{0.2f, 0.2f, 0.2f});
            line2.draw(renderer, new float[]{1, 0, 0, 1});
            Log.d("Triangle", "Line1");
            Line line3 = new Line(new Point(vertex3), new Point(new float[]{vertex3[0] + this.normal3[0] * MUL, vertex3[1] + this.normal3[1] * MUL, vertex3[2] + this.normal3[2] * MUL}));
            line3.setModelMatrix(null, null, new float[]{0.2f, 0.2f, 0.2f});
            line3.draw(renderer, new float[]{1, 0, 0, 1});
        }
    }
}

class Sphere extends Obj {

    public float[] vertices;
    static private FloatBuffer verticesBuffer;
    public double radius;
    public double mStep;
    private static double DEG = Math.PI / 180;
    public float centerX;
    public float centerY;
    public float centerZ;
    int mPoints;

    public Sphere(float radius, double step, float x, float y, float z) {
        centerX = x;
        centerY = y;
        centerZ = z;
        this.radius = radius;
        this.mStep = step;
        mPoints = build();

    }

    public float[] setModelMatrix(float[] translate, float[] rotate, float[] scale) {
        Log.d("Sphere", "Set Model Matrix");
        Matrix.setIdentityM(mModelMatrix, 0);
        if (scale != null) {
            Log.d("Sphere", "Tranlate");
            Matrix.scaleM(mModelMatrix, 0, scale[0], scale[1], scale[2]);
        }
        if (translate != null) {
            Log.d("Sphere", "T");
            Matrix.translateM(mModelMatrix, 0, translate[0], translate[1], translate[2]);
        }
        if (rotate != null) {
            Matrix.rotateM(mModelMatrix, 0, rotate[0], rotate[1], rotate[2], rotate[3]);
        }
        return mModelMatrix;
    }

    private int build() {


        /**
         * x = p * sin(phi) * cos(theta)
         * y = p * sin(phi) * sin(theta)
         * z = p * cos(phi)
         */
        double dTheta = mStep * DEG;
        double dPhi = dTheta;
        int points = 0;
        Vector<Float> verticesVector = new Vector<Float>();

        for (double phi = -Math.PI; phi <= Math.PI; phi += dPhi) {
            //for each stage calculating the slices
            for (double theta = 0.0; theta <= (Math.PI * 2); theta += dTheta) {
                verticesVector.add((float) (radius * Math.sin(phi) * Math.cos(theta)) + centerX);
                verticesVector.add((float) (radius * Math.sin(phi) * Math.sin(theta)) + centerY);
                verticesVector.add((float) (radius * Math.cos(phi)) + centerZ);
                points++;

            }
        }
        vertices = new float[verticesVector.size()];
        for (int i = 0; i < verticesVector.size(); i++) {
            vertices[i] = verticesVector.get(i);
        }
        verticesBuffer = ByteBuffer.allocateDirect(vertices.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(vertices).position(0);
        Log.d("Sphere", "Build");
        return points;
    }

    @Override
    public void draw(Renderer renderer, float[] color) {
        if (color == null) {
            color = new float[]{1, 1, 1, 1};
        }
        if (color.length != 4) {
            throw new RuntimeException("Invalid color");
        }
        Log.d("Sphere", "Color = " + color[0] + "," + color[1] + "," + color[2]);
        Log.d("Sphere", "Draw");
        verticesBuffer.position(mPositionOffset);
        GLES20.glEnableVertexAttribArray(renderer.mPositionHandle);
        GLES20.glVertexAttribPointer(renderer.mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, verticesBuffer);
        Matrix.multiplyMM(mMVMatrix, 0, renderer.mViewMatrix, 0, mModelMatrix, 0);
        Matrix.invertM(mInv_Trans_Matrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mInv_Trans_Matrix, 0, mInv_Trans_Matrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, renderer.mProjectionMatrix, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(renderer.mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform4fv(renderer.mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, mPoints);
    }

}