package com.example.opengl;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class MyActivity extends Activity implements GLSurfaceView.Renderer {

    private GLSurfaceView surfaceView;

    private Mesh mesh;

    private float ratio;

    private float[] projectionMatrix = new float[16];

    private View targetView;

    private ViewGroup rootView;

    private long duration = 1000;

    private boolean startAnimation;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        targetView = findViewById(R.id.targetView);
        rootView = (ViewGroup) findViewById(R.id.rootView);

        ImageView view = (ImageView) targetView;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimation();
            }
        });

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
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0, 0, 0, 0);
        mesh = new Mesh(this);
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
        if (startAnimation) {
            mesh.draw(projectionMatrix);
        }
    }

    private void startAnimation() {
        startAnimation = true;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                surfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mesh.setVertexBuffer(createVertexes());
                        surfaceView.requestRender();
                    }
                });
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //延时防止闪动
                targetView.getHandler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                targetView.setVisibility(View.INVISIBLE);
                            }
                        }
                        , 100);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                targetView.setVisibility(View.VISIBLE);
                startAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                targetView.setVisibility(View.VISIBLE);
                startAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        //创建视图的截图
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = rootView.getDrawingCache();
        final Bitmap texture = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / 2,
                bitmap.getHeight());

        rootView.setDrawingCacheEnabled(false);

        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mesh.loadTexture(texture);
                texture.recycle();
            }
        });

        animator.start();
    }

    private float[] createVertexes() {
        return new float[]{
                -ratio, 1, 0,
                -ratio, -1, 0,
                0, 1, 0,
                0, -1, 0
        };
    }

}
