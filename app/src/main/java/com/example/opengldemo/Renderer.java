package com.example.opengldemo;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.util.Random;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Renderer implements GLSurfaceView.Renderer {
    Triangle triangle;
    Sphere sphere;
    Ray ray;

    private final int mBytesPerFloat = 4;

    public float[] mViewMatrix = new float[16];
    public float[] mProjectionMatrix = new float[16];

    private final int PARTITION = 10;
    private final float[] SCALE = new float[]{0.2f, 0.2f, 0.2f};

    /*
    mode = 0: Normal
    mode = 1: Triangle
    mode = 2: Sphere
     */
    int mode = 3;

    private final float MUL = 5.0f;
    private final float[] RED = new float[]{1, 0, 0, 1};
    private final float[] YELLOW = new float[]{1, 1, 0, 1};
    private final float[] GREEN = new float[]{0, 1, 0, 1};
    private final float[] BLUE = new float[]{0, 0, 1, 1};
    private final float[] WHITE = new float[]{1, 1, 1, 1};

    public int mMVPMatrixHandle;
    public int mPositionHandle;
    public int mColorHandle;
    private int mProgramHandle;

    private int mTexDataHandle;
    private Context context;
    private int numIntersection;
    Point intersection1;
    Point intersection2;
    boolean isFront1;
    boolean isFront2;
    boolean isInside = false;
    float[] interpolatedNormal;

    public Renderer(Context context) {
        Log.d("Renderer", "Constructor");

        this.context = context;
    }

    public float[] calculateSphereIntersection(Ray ray, Sphere sphere) {
//        Log.d("Renderer","Calculate sphere intersection");
        float b = 2 * (ray.xd * (ray.x0 - sphere.centerX) + ray.yd * (ray.y0 - sphere.centerY) + ray.zd * (ray.z0 - sphere.centerZ));
        float c = (float) ((ray.x0 - sphere.centerX) * (ray.x0 - sphere.centerX) + (ray.y0 - sphere.centerY) * (ray.y0 - sphere.centerY) + (ray.z0 - sphere.centerZ) * (ray.z0 - sphere.centerZ) - sphere.radius * sphere.radius);
        float delta = b * b - 4 * c;
//        Log.d("CalculateIntersection", "delta="+delta);
        if (delta < 0) {
            numIntersection = 0;
            return null;
        } else if (delta == 0) {
            float t0 = (float) (-b - Math.sqrt(delta) / 2);
            if (t0 <= 0) {
                numIntersection = 0;
                return null;
            } else {
                numIntersection = 1;
                return new float[]{t0};
            }
        } else {
            numIntersection = 2;
            float t0 = (float) (-b - Math.sqrt(delta)) / 2;
            float t1 = (float) (-b + Math.sqrt(delta)) / 2;
            if (t0 <= 0 && t1 <= 0) {
                numIntersection = 0;
                return null;
            } else if (t0 <= 0) {
                numIntersection = 1;
                return new float[]{t1};
            } else if (t1 <= 0) {
                numIntersection = 1;
                return new float[]{t0};
            } else {
                numIntersection = 2;
                return new float[]{t0, t1};
            }
        }
    }

    float[] calVector(float[] vertex1, float[] vertex2) {
        return new float[]{vertex2[0] - vertex1[0], vertex2[1] - vertex1[1], vertex2[2] - vertex1[2]};
    }

    public Ray[] genRay(Triangle triangle) {
        float[] vertex1 = triangle.vertex1;
        float[] vertex2 = triangle.vertex2;
        float[] vertex3 = triangle.vertex3;

        float[] vec12 = calVector(vertex1, vertex2);
        float[] vec23 = calVector(vertex2, vertex3);
        float[] vec13 = calVector(vertex1, vertex3);
        float[][] vec = new float[][]{vec12, vec23, vec13};

        float len12 = Utils.len(vec12);
        float len23 = Utils.len(vec23);
        float len13 = Utils.len(vec13);
        float[] len = new float[]{len12, len23, len13};

        float[][] vertexTestList = new float[PARTITION * 3][3];
        for (int offset = 0; offset < PARTITION * 3; offset += PARTITION) {
            for (int i = 0; i < PARTITION; ++i) {
                vertexTestList[offset + i][0] = vertex1[0] + vec[offset / PARTITION][0] * len[offset / PARTITION] * i / 10.0f;
                vertexTestList[offset + i][1] = vertex1[1] + vec[offset / PARTITION][0] * len[offset / PARTITION] * i / 10.0f;
                vertexTestList[offset + i][2] = vertex1[2] + vec[offset / PARTITION][0] * len[offset / PARTITION] * i / 10.0f;
            }
        }
        Ray[] rayList = new Ray[3 * PARTITION];
        Random random = new Random();
        for (int i = 0; i < 3 * PARTITION; ++i) {
            rayList[i] = new Ray(vertexTestList[i][0], vertexTestList[i][1], vertexTestList[i][2], random.nextDouble(), random.nextDouble(), random.nextDouble());
        }
//        Log.d("vertexTest",String.valueOf(vertexTestList[0][0]));
        return rayList;
    }

    public float[] calculateTriangleIntersection(Ray ray, Triangle triangle) {
        float dot = ray.xd * triangle.a + ray.yd * triangle.b + ray.zd * triangle.c;
        if (Math.abs(dot) < 1e-7) {
            numIntersection = 0;
            return null;
        }
        float t = -(triangle.a * ray.x0 + triangle.b * ray.y0 + triangle.c * ray.z0 + triangle.d) / (ray.xd * triangle.a + ray.yd * triangle.b + ray.zd * triangle.c);
        isFront1 = isRayIntersectingFront(triangle, ray);
        return new float[]{t};
    }

    public boolean isRayIntersectingFront(Triangle triangle, Ray ray) {
        float dotProduct = Utils.dotProduct(new float[]{triangle.a, triangle.b, triangle.c}, new float[]{ray.xd, ray.yd, ray.zd});
        return dotProduct < 0;
    }

    // Helper method to calculate barycentric coordinates of a point with respect to a triangle
    public float[] calculateBarycentricCoordinates(Point point, Triangle triangle) {
        float[] v0 = {triangle.vertex2[0] - triangle.vertex1[0], triangle.vertex2[1] - triangle.vertex1[1], triangle.vertex2[2] - triangle.vertex1[2]};
        float[] v1 = {triangle.vertex3[0] - triangle.vertex1[0], triangle.vertex3[1] - triangle.vertex1[1], triangle.vertex3[2] - triangle.vertex1[2]};
        float[] v2 = {point.x - triangle.vertex1[0], point.y - triangle.vertex1[1], point.z - triangle.vertex1[2]};

        float areaTriangle = crossProductLength(v0, v1) / 2.0f;
        float areaSub0 = crossProductLength(v1, v2) / 2.0f;
        float areaSub1 = crossProductLength(v2, v0) / 2.0f;
        float areaSub2 = areaTriangle - areaSub0 - areaSub1;

        float alpha = areaSub2 / areaTriangle;
        float beta = areaSub0 / areaTriangle;
        float gamma = areaSub1 / areaTriangle;

        return new float[]{alpha, beta, gamma};
    }

    public float[] getInterpolatedNormal(Point point, Triangle triangle) {
        float[] barycoords = calculateBarycentricCoordinates(point, triangle);
        float alpha = barycoords[0];
        float beta = barycoords[1];
        float gamma = barycoords[2];
        float x = alpha * (triangle.normal1[0] + triangle.normal2[0] + triangle.normal3[0]);
        float y = beta * (triangle.normal1[1] + triangle.normal2[1] + triangle.normal3[1]);
        float z = gamma * (triangle.normal1[2] + triangle.normal2[2] + triangle.normal3[2]);
        return new float[]{x, y, z};
    }

    private float crossProductLength(float[] vector1, float[] vector2) {
        float[] crossProduct = {
                vector1[1] * vector2[2] - vector1[2] * vector2[1],
                vector1[2] * vector2[0] - vector1[0] * vector2[2],
                vector1[0] * vector2[1] - vector1[1] * vector2[0]
        };
        return (float) Math.sqrt(crossProduct[0] * crossProduct[0] + crossProduct[1] * crossProduct[1] + crossProduct[2] * crossProduct[2]);
    }

    public boolean isPointInsideTriangle(Point point, Triangle triangle) {
        float[] v0 = {triangle.vertex2[0] - triangle.vertex1[0], triangle.vertex2[1] - triangle.vertex1[1], triangle.vertex2[2] - triangle.vertex1[2]};
        float[] v1 = {triangle.vertex3[0] - triangle.vertex1[0], triangle.vertex3[1] - triangle.vertex1[1], triangle.vertex3[2] - triangle.vertex1[2]};
        float[] v2 = {triangle.vertex3[0] - triangle.vertex2[0], triangle.vertex3[1] - triangle.vertex2[1], triangle.vertex3[2] - triangle.vertex2[2]};
        float[] v3 = {point.x - triangle.vertex1[0], point.y - triangle.vertex1[1], point.z - triangle.vertex1[2]};
        float[] v4 = {point.x - triangle.vertex2[0], point.y - triangle.vertex2[1], point.z - triangle.vertex2[2]};
        float areaTriangle = Utils.len(Utils.crossProduct(v0, v1)) / 2.0f;
        float areaSub0 = Utils.len(Utils.crossProduct(v0, v3)) / 2.0f;
        float areaSub1 = Utils.len(Utils.crossProduct(v1, v3)) / 2.0f;
        float areaSub2 = Utils.len(Utils.crossProduct(v2, v4)) / 2.0f;
        return areaSub0 + areaSub1 + areaSub2 == areaTriangle;
    }

    Line getNormLine(Point origin, float[] norm) {
        return new Line(origin, new Point(new float[]{origin.x + norm[0] * MUL, origin.y + norm[1] * MUL, origin.z + norm[2] * MUL}));
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        Log.d("On surfaceCreated", "Begin");
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;          \n"     // A constant representing the combined model/view/projection matrix
                + "attribute vec4 a_Position;       \n"     // Per-vertex position information we will pass in
                + "uniform vec4 a_Color;            \n"
                + "varying vec4 v_Color;            \n"     // This will be passed into the fragment shader.

                + "void main()                      \n"     // The entry point for our vertex shader.
                + "{                                \n"
//                + "   vec4 a_Color = vec4(1.0,1.0,1.0,0.5);        \n"
                + "   v_Color = a_Color;            \n"
                + "   gl_PointSize = 5.0;"
                + "   gl_Position = u_MVPMatrix     \n"
                + "               * a_Position;     \n"
                + "}                                \n";

        final String fragmentShader =
                "precision mediump float;           \n"        // Set the default precision to medium. We don't need as high of a
                + "varying vec4 v_Color;            \n"
                + "void main()                      \n"        // The entry point for our fragment shader.
                + "{                                \n"
                + "   gl_FragColor = v_Color;       \n"
                + "}                                \n";

        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);


            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }

        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
        mProgramHandle = GLES20.glCreateProgram();

        if (mProgramHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(mProgramHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(mProgramHandle, fragmentShaderHandle);

            // Bind attributes
            GLES20.glBindAttribLocation(mProgramHandle, 0, "a_Position");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(mProgramHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(mProgramHandle);
                mProgramHandle = 0;
            }
        }

        if (mProgramHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "a_Color");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");

        GLES20.glUseProgram(mProgramHandle);

        mTexDataHandle = TextureHelper.LoadTexture(R.drawable.texture, this.context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        Log.d("On surfaceChanged", "Begin");
        GLES20.glViewport(0, 0, width, height);
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 0.6f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        Log.d("Renderer", "OnDrawFrame");
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexDataHandle);
        Log.d("Renderer", "Begin draw ray");
        ray.setModelMatrix(null, null, SCALE);
        ray.draw(this, BLUE);
        Log.d("Renderer", "End draw ray");
        Log.d("Renderer", "Mode=" + mode);

        if (mode == 0) {
            Log.d("Renderer", "Draw mode = 0");
            Log.d("Renderer", "Draw normal");
            triangle.setModelMatrix(null, null, SCALE);
            triangle.draw(this, YELLOW);
            if (numIntersection == 1) {
                intersection1.setModelMatrix(null, null, SCALE);
                float[] color = RED;
                if (isInside) {
                    color = GREEN;
                    Line normLine = getNormLine(intersection1, interpolatedNormal);
                    normLine.setModelMatrix(null, null, SCALE);
                    normLine.draw(this, GREEN);
                }
                Log.d("Renderer", "Draw line 1");
                intersection1.draw(this, color);
            }

        }
        if (mode == 1) {
            triangle.setModelMatrix(null, null, SCALE);
            triangle.draw(this, YELLOW);
            if (numIntersection == 1) {
                intersection1.setModelMatrix(null, null, SCALE);
                float[] color = RED;
                Log.d("Renderer Test draw", "Is inside");
                if (isInside) {
                    color = GREEN;
                }
                Log.d("Intersection1", "coords" + intersection1.x + "," + intersection1.y + "," + intersection1.z);
                intersection1.draw(this, color);
                Point origin = new Point(ray.x0, ray.y0, ray.z0);
                Line line = new Line(origin, intersection1);
                line.setModelMatrix(null, null, SCALE);
                line.draw(this, BLUE);
            }

        }
        if (mode == 2) {
            float[] sphereModelMatrix = sphere.setModelMatrix(null, null, new float[]{0.2f, 0.2f, 0.2f});
//            Log.d("On draw frame", "draw");
            sphere.draw(this, YELLOW);
            Point origin = new Point(ray.x0, ray.y0, ray.z0);
//            Log.d("Distance", "" + origin.distanceTo(new Point(sphere.centerX, sphere.centerY, sphere.centerZ)));
            origin.setModelMatrix(null, null, SCALE);
            origin.draw(this, BLUE);
            if (numIntersection >= 1) {
//                Log.d("OndrawFrame", "numIntersection > = 1");
                intersection1.setModelMatrix(null, null, SCALE);
                float[] color = RED;
                if (isFront1) {
                    color = GREEN;
                }
                intersection1.draw(this, color);
                Line line1 = new Line(intersection1, origin);
                line1.setModelMatrix(null, null, SCALE);
                line1.draw(this, BLUE);
            }
            if (numIntersection == 2) {
//                Log.d("OndrawFrame", "numIntersection == 2");
                intersection2.setModelMatrix(null, null, SCALE);
                float[] color = RED;
                if (isFront2) {
                    color = GREEN;
                }
                intersection2.draw(this, color);
                Line line2 = new Line(intersection2, origin);
                line2.setModelMatrix(null, null, SCALE);
                line2.draw(this, BLUE);
            }
        }
    }

    public void setTriangleInput(float[] vertex1, float[] vertex2, float[] vertex3, float[] ray_origin, float[] ray_direction) {
        mode = 1;

        ray = new Ray(ray_origin[0], ray_origin[1], ray_origin[2], ray_direction[0], ray_direction[1], ray_direction[2]);
        ray.setModelMatrix(null, null, SCALE);
        ray.draw(this, BLUE);

        triangle = new Triangle(vertex1, vertex2, vertex3);

        float[] expectOutput = new float[PARTITION * 3];
        float[] t = calculateTriangleIntersection(ray, triangle);
        if (t != null) {
            Log.d("Mode = 1", "t = " + t[0]);
            if (t[0] < 0) {
                numIntersection = 0;
            } else {
                numIntersection = 1;
                float x = ray.x0 + t[0] * ray.xd;
                float y = ray.y0 + t[0] * ray.yd;
                float z = ray.z0 + t[0] * ray.zd;
                intersection1 = new Point(x, y, z);
                isInside = isPointInsideTriangle(intersection1, triangle);
                Log.d("Is inside", "" + isInside);
            }
        } else {
            numIntersection = 0;
        }
    }

    public void setSphereInput(float r, float[] center, float[] ray_origin, float[] ray_direction) {
        mode = 2;

        ray = new Ray(ray_origin[0], ray_origin[1], ray_origin[2], ray_direction[0], ray_direction[1], ray_direction[2]);
        ray.setModelMatrix(null, null, SCALE);
        ray.draw(this, BLUE);

        Log.d("Renderer", "Mode = 2");
        sphere = new Sphere(r, 5.0d, center[0], center[1], center[2]);
        float[] tArr = calculateSphereIntersection(ray, sphere);
        if (numIntersection == 1) {
//                Log.d("Renderer", "no.intersection = 1");
            float t = tArr[0];
            float x = ray.x0 + ray.xd * t;
            float y = ray.y0 + ray.yd * t;
            float z = ray.z0 + ray.zd * t;
            intersection1 = new Point(x, y, z);
            float[] normal = new float[]{x - sphere.centerX, y - sphere.centerY, z - sphere.centerZ};
            Utils.normalize(normal);
            isFront1 = Utils.dotProduct(normal, new float[]{ray.xd, ray.yd, ray.zd}) < 0;
//                Log.d("Renderer", "Cal dotproduct");
        } else if (numIntersection == 2) {
            float t1 = tArr[0];
            float t2 = tArr[1];
            float x1 = ray.x0 + ray.xd * t1;
            float y1 = ray.y0 + ray.yd * t1;
            float z1 = ray.z0 + ray.zd * t1;
            intersection1 = new Point(x1, y1, z1);
            float[] normal1 = new float[]{x1 - sphere.centerX, y1 - sphere.centerY, z1 - sphere.centerZ};
            Utils.normalize(normal1);
            isFront1 = Utils.dotProduct(normal1, new float[]{ray.xd, ray.yd, ray.zd}) < 0;

            float x2 = ray.x0 + ray.xd * t2;
            float y2 = ray.y0 + ray.yd * t2;
            float z2 = ray.z0 + ray.zd * t2;
            intersection2 = new Point(x2, y2, z2);
            float[] normal2 = new float[]{x2 - sphere.centerX, y2 - sphere.centerY, z2 - sphere.centerZ};
            Utils.normalize(normal2);
            isFront2 = Utils.dotProduct(normal2, new float[]{ray.xd, ray.yd, ray.zd}) < 0;
        }
    }

    public void setNormalInput(float[] vertex1, float[] vertex2, float[] vertex3, float[] normal1, float[] normal2, float[] normal3, float[] ray_origin, float[] ray_direction) {
        mode = 0;

        ray = new Ray(ray_origin[0], ray_origin[1], ray_origin[2], ray_direction[0], ray_direction[1], ray_direction[2]);
        ray.setModelMatrix(null, null, SCALE);
        ray.draw(this, BLUE);

        Log.d("Renderer", "Mode = 0");
        triangle = new Triangle(vertex1, vertex2, vertex3, normal1, normal2, normal3);
        float[] t = calculateTriangleIntersection(ray, triangle);
        if (t != null) {
            if (t[0] <= 0) {
                numIntersection = 0;
            } else {
                numIntersection = 1;
                float x = ray.x0 + t[0] * ray.xd;
                float y = ray.y0 + t[0] * ray.yd;
                float z = ray.z0 + t[0] * ray.zd;
//                Log.d("Intersection Triangle",  x + "," + y + "," + z);
                intersection1 = new Point(x, y, z);
                isInside = isPointInsideTriangle(intersection1, triangle);
                Log.d("Rederer", "Is inside:" + isInside);
                interpolatedNormal = getInterpolatedNormal(intersection1, triangle);
            }
        } else {
            numIntersection = 0;
        }
    }
}