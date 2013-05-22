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

import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class MyActivity extends Activity implements GLSurfaceView.Renderer {

    //从左到右
    private int[] partInfo, partInfo1;

    private GLSurfaceView surfaceView;

    private List<Mesh> meshes = new ArrayList<Mesh>();

    private float ratio;

    private float[] projectionMatrix = new float[16];

    private View targetView;

    private ViewGroup rootView;

    private long duration = 15000;

    private boolean startAnimation;

    private int width, height;

    private float w;

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
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        ratio = width / (float) height;
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -10f, 10f);

        this.width = width;
        this.height = height;

        //这么做是为了尽量不出小数，而且符合实际使用情况
        partInfo = new int[]{
                150, 200, 150, width - (150 + 200 + 150)
        };

        //放置各个“片”到左侧的偏移量
        partInfo1 = new int[partInfo.length];

        for (int i = 0, j = 0; i < partInfo1.length; i++) {
            partInfo1[i] = j;
            j += partInfo[i];
        }

        meshes.clear();

        for (int i = 0; i < partInfo.length; i++) {
            meshes.add(new Mesh(this));
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
        if (startAnimation) {
            for (Mesh mesh : meshes) {
                mesh.draw(projectionMatrix);
            }
        }
    }

    private void startAnimation() {
        w = ratio * 2 * (partInfo.length - 1) + partInfo[0] / (width / ratio / 2);
        startAnimation = true;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float factor = (Float) valueAnimator.getAnimatedValue();
                surfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < partInfo.length; i++) {
                            Mesh mesh = meshes.get(i);
                            float left = -ratio + partInfo1[i] / (width / ratio / 2);
                            float right = left + partInfo[i] / (width / ratio / 2);
                            float[] vertexes = new float[]{
                                    left, 1, 0,
                                    left, -1, 0,
                                    right, 1, 0,
                                    right, -1, 0
                            };
                            //坐标x轴左移的终点
                            float move = ratio * 2 * (partInfo.length - 1 - i) +
                                    partInfo[i] / (width / ratio / 2) +
                                    partInfo1[i] / (width / ratio / 2);

                            for (int j = 0; j < vertexes.length; j += 3) {
                                if (w * factor < move) {
                                    vertexes[j] = vertexes[j] - move + w * factor;
                                }
                            }
                            mesh.setVertexBuffer(vertexes);
                        }

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
                        , 80);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                targetView.setVisibility(View.VISIBLE);
                startAnimation = false;

                surfaceView.requestRender();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                targetView.setVisibility(View.VISIBLE);
                startAnimation = false;

                surfaceView.requestRender();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        //创建视图的截图
        rootView.setDrawingCacheEnabled(true);
        final Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());

        rootView.setDrawingCacheEnabled(false);

        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                //为每个Mash设置Texture
                for (int i = 0; i < partInfo.length; i++) {
                    Bitmap texture = Bitmap.createBitmap(bitmap, partInfo1[i], 0, partInfo[i],
                            bitmap.getHeight());
                    meshes.get(i).loadTexture(texture);
                    texture.recycle();
                }
                bitmap.recycle();
            }
        });

        animator.start();
    }
}
