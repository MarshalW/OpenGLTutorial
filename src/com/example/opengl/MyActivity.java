package com.example.opengl;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class MyActivity extends Activity implements GLSurfaceView.Renderer {

    private ViewGroup rootView;

    private GLSurfaceView surfaceView;

    private float ratio;

    private int width, height;

    private Mesh mesh;

    //投影矩阵
    private float[] projectionMatrix = new float[16];

    //模型矩阵
    private float[] modelMatrix = new float[16];

    //视图矩阵
    private float[] viewMatrix = new float[16];

    //模型视图投影矩阵
    private float[] mvpMatrix = new float[16];

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        rootView = (ViewGroup) findViewById(R.id.rootView);

        surfaceView = new GLSurfaceView(this);
        surfaceView.setEGLContextClientVersion(2);

        //设置背景透明
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        rootView.addView(surfaceView);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0, 0, 0, 0);

        //照相机位置
        float eyeX = 0.0f;
        float eyeY = 0.0f;
        float eyeZ = 7;

        //照相机拍照方向
        float lookX = 0.0f;
        float lookY = 0.0f;
        float lookZ = -1.0f;

        //照相机的垂直方向
        float upX = 0.0f;
        float upY = 1.0f;
        float upZ = 0.0f;

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        Bitmap texture = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ta));
        mesh = new Mesh(this);
        mesh.loadTexture(texture);
        texture.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.width = width;
        this.height = height;
        glViewport(0, 0, width, height);
        ratio = width / (float) height;

        float left = -ratio;
        float right = ratio;
        float top = 1;
        float bottom = -1;
        float near = 7;
        float far = 100;
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);

        mesh.setVertexBuffer(new float[]{
                -ratio, 1, 0,
                -ratio, -1, 0,
                ratio, 1, 0,
                ratio, -1, 0
        });
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

        mesh.draw(mvpMatrix);
    }
}
