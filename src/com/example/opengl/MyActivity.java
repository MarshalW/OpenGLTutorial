package com.example.opengl;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class MyActivity extends Activity implements GLSurfaceView.Renderer {

    private GLSurfaceView surfaceView;

    private Mesh mesh;

    private float ratio, factor;

    private float[] projectionMatrix = new float[16];

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        surfaceView = new GLSurfaceView(this);
        //设置使用opengl es2，默认是1.x
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(this);

        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(surfaceView);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        startAnimation();
                    }
                }
                , 2000);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0, 0, 0, 0);
        mesh = new Mesh(this);
        mesh.loadTexture(BitmapFactory.decodeResource(getResources(), R.drawable.h));
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        ratio = width / (float) height;
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -10f, 10f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
        mesh.draw(projectionMatrix);
    }

    public void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(-1, 1);
        animator.setDuration(1500);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float factor = (Float) valueAnimator.getAnimatedValue();
                surfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        MyActivity.this.factor = factor;
                        mesh.setVertexBuffer(createVertexes());
                        surfaceView.requestRender();
                    }
                });
            }
        });

        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
    }

    private float[] createVertexes() {
        float moveX = 0.2f*factor;
        float[] vertexes =
                new float[]{
                        -0.5f + moveX, 0.5f, 0,
                        -0.5f + moveX, -0.5f, 0,
                        0.5f + moveX, 0.5f, 0,
                        0.5f + moveX, -0.5f, 0
                };
        return vertexes;
    }
}
