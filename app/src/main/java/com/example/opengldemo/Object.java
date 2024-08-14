package com.example.opengldemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

import java.io.InputStreamReader;

public class Object {
    public final int numVertices;
    public final float[] verticesArray;
    public final float[] Ka_color = new float[4];
    public final float[] Kd_color = new float[4];
    public final float[] Ks_color = new float[4];
    public float Ns;
    public String texture_file;
    public final String VERTEX = "v";
    public final String NORM = "vn";
    public final String TEXTURE = "vt";
    public final String FACE = "f";
    public final String MTLLIB = "mtllib";
    private final FloatBuffer vertices;
    private final int mBytesPerFloat = 4;
    private final int mPositionOffset = 0;
    private final int mNormOffset = 3;
    private final int mTexCoordOffset = 6;
    private final int mPositionDataSize = 3;
    private final int mNormSize = 3;
    private final int mTexCoordSize = 2;
    private final int mStrideBytes = (mPositionDataSize + mNormSize + mTexCoordSize) * mBytesPerFloat;

    public Object(Context context, String file, Renderer render) {
//        Log.d("readObj", "readObj constrictor");
        Vector<Float> verticesVector = new Vector<Float>();
        Vector<Float> verNormalsVector = new Vector<Float>();
        Vector<Float> verTexturesVector = new Vector<Float>();
        Vector<String> facesVector = new Vector<>();
        BufferedReader reader = null;
        String mtllib = null;

        try {
            InputStreamReader in = new InputStreamReader(context.getAssets().open(file));
            reader = new BufferedReader(in);

            String line;

            while ((line = reader.readLine()) != null) {
                String[] ss = line.split(" ");
                if (ss[0].equals(MTLLIB)) {
                    mtllib = ss[1];
                } else if (ss[0].equals(VERTEX)) {
                    verticesVector.add(Float.valueOf(ss[1]));
                    verticesVector.add(Float.valueOf(ss[2]));
                    verticesVector.add(Float.valueOf(ss[3]));
                } else if (ss[0].equals(NORM)) {
//                    countNorm++;
                    verNormalsVector.add(Float.valueOf(ss[1]));
                    verNormalsVector.add(Float.valueOf(ss[2]));
                    verNormalsVector.add(Float.valueOf(ss[3]));
                } else if (ss[0].equals(TEXTURE)) {
                    verTexturesVector.add(Float.valueOf(ss[1]));
                    verTexturesVector.add(Float.valueOf(ss[2]));
//                    verTexturesVector.add(Float.valueOf(ss[3]));
                } else if (ss[0].equals(FACE)) {
                    facesVector.add(ss[1]);
                    facesVector.add(ss[2]);
                    facesVector.add(ss[3]);
                    if (ss.length >= 5) {
                        facesVector.add(ss[1]);
                        facesVector.add(ss[3]);
                        facesVector.add(ss[4]);
                        if (ss.length == 6) {
                            facesVector.add(ss[1]);
                            facesVector.add(ss[4]);
                            facesVector.add(ss[5]);
                        }
                    }
                }

            }
            Log.d("Constructor", "end read");
        } catch (IOException e) {
            Log.d("IO", "Cannot read file");
        } finally {
            if (reader != null) {
                try {
                    reader.close();

                } catch (IOException e) {
                    Log.d("IO", "Cannot read file");
                }
            }
        }

        this.numVertices = facesVector.size();
        this.verticesArray = new float[numVertices * 8];
        boolean hasTexturesArr[] = new boolean[numVertices];
        for (int i = 0; i < numVertices; i++) {
            String faceVertex = facesVector.get(i);
            String[] eles = faceVertex.split("/");
            boolean hasNormal = false, hasTexture = false;
            this.verticesArray[i * 8] = verticesVector.get((Integer.valueOf(eles[0]) - 1) * 3);
            this.verticesArray[i * 8 + 1] = verticesVector.get((Integer.valueOf(eles[0]) - 1) * 3 + 1);
            this.verticesArray[i * 8 + 2] = verticesVector.get((Integer.valueOf(eles[0]) - 1) * 3 + 2);

            if (eles.length == 2) {
                hasTexture = true;
            }
            if (eles.length == 3) {
                hasNormal = true;
                if (!eles[1].isEmpty()) {
                    hasTexture = true;
                }
            }

//                Log.d("ReadObj", "hasNormal=" + hasNormal + "; hasTexture=" + hasTexture + "; eles.length=" + eles.length);
//                Log.d("ReadObj", "Begin normal");
            hasTexturesArr[i] = hasTexture;
            if (hasNormal) {
//                    Log.d("ReadObj", "1");
                this.verticesArray[i * 8 + 3] = verNormalsVector.get((Integer.valueOf(eles[2]) - 1) * 3);
                this.verticesArray[i * 8 + 4] = verNormalsVector.get((Integer.valueOf(eles[2]) - 1) * 3 + 1);
                this.verticesArray[i * 8 + 5] = verNormalsVector.get((Integer.valueOf(eles[2]) - 1) * 3 + 2);

            } else {
                Log.d("ReadObj", "No normals"); //calculate based on order of vertices
                //Calculate normals
            }
            if (hasTexture) {
                float tx = verTexturesVector.get((Integer.valueOf(eles[1]) - 1) * 2);
                float ty = verTexturesVector.get((Integer.valueOf(eles[1]) - 1) * 2 + 1);
                if (ty > 1) {
                    ty = ty % 1;
                }
                ty = 1 - ty;
                ty = ty % 1;
                this.verticesArray[i * 8 + 6] = tx;
                this.verticesArray[i * 8 + 7] = ty;
            } else {
                this.verticesArray[i * 8 + 6] = 1;
                this.verticesArray[i * 8 + 7] = 1;
            }
            Log.d("ReadObj", "Num Data = " + this.verticesArray.length);
        }
        this.vertices = ByteBuffer.allocateDirect(verticesArray.length * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertices.put(verticesArray).position(0);

        //Read material
        try {
            InputStreamReader mtlIn = new InputStreamReader(context.getAssets().open(mtllib));
            reader = new BufferedReader(mtlIn);
            String line;
            while ((line = reader.readLine()) != null) {
                String ss[] = line.split(" ");
                if (ss[0].equals("Ka")) {
                    Ka_color[0] = Float.valueOf(ss[1]);
                    Ka_color[1] = Float.valueOf(ss[2]);
                    Ka_color[2] = Float.valueOf(ss[3]);
                }
                if (ss[0].equals("Kd")) {
                    Kd_color[0] = Float.valueOf(ss[1]);
                    Kd_color[1] = Float.valueOf(ss[2]);
                    Kd_color[2] = Float.valueOf(ss[3]);
                }
                if (ss[0].equals("Ks")) {
                    Ks_color[0] = Float.valueOf(ss[1]);
                    Ks_color[1] = Float.valueOf(ss[2]);
                    Ks_color[2] = Float.valueOf(ss[3]);
                }
                if (ss[0].equals("Tr")) {
                    float alpha = Float.valueOf(ss[1]);
                    Ka_color[3] = alpha;
                    Kd_color[3] = alpha;
                    Ks_color[3] = alpha;
                }
                if (ss[0].equals("Ns")) {
                    Ns = Float.valueOf(ss[1]);
                }
                if (ss[0].equals("map_Kd")) {
                    texture_file = ss[1];
                }
            }
        } catch (IOException e) {
            Log.d("IO", "Cannot read material file");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d("IO", "Cannot read material file");
                }
            }
        }
    }
}



