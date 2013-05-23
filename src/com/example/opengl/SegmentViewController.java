package com.example.opengl;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-5-22
 * Time: 下午4:50
 * To change this template use File | Settings | File Templates.
 */
public class SegmentViewController implements GLSurfaceView.Renderer {

    private final static int DELAY=120;

    private GLSurfaceView surfaceView;

    private ViewGroup rootView;

    private View targetView;

    private float ratio;

    private int width, height;

    private Segment[] segments;

    private float[] projectionMatrix = new float[16];

    private ValueAnimator animator;

    private List<Mesh> meshes = new ArrayList<Mesh>();

    private long duration = 500;

    private Animator.AnimatorListener animatorListener;

    public SegmentViewController(Context context, ViewGroup rootView, View targetView) {
        this.rootView = rootView;
        this.targetView = targetView;

        surfaceView = new GLSurfaceView(context);
        surfaceView.setEGLContextClientVersion(2);

        //设置背景透明
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        rootView.addView(surfaceView);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.animatorListener = animatorListener;
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
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
        Log.d("tutorial", "animator is null? " + (animator == null) + ", segments.length: " + segments);
        if (animator != null) {
            for (int i = 0; i < segments.length; i++) {
                meshes.get(i).draw(projectionMatrix);
            }
        }
    }

    public void startAnimation() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(duration);

            //帧的更新
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    final float factor = (Float) valueAnimator.getAnimatedValue();
                    surfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            float maxLeft = segments[0].startMove;
                            for (int i = 0; i < segments.length; i++) {
                                Mesh mesh = meshes.get(i);
                                mesh.setVertexBuffer(segments[i].getVertexes(factor, maxLeft));
                            }
                            surfaceView.requestRender();
                        }
                    });
                }
            });

            //动画起止的处理
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    //延时防止闪动
                    rootView.getHandler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    targetView.setVisibility(View.INVISIBLE);
                                }
                            }
                            , DELAY);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    targetView.setVisibility(View.VISIBLE);
                    SegmentViewController.this.animator = null;
                    targetView.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            surfaceView.requestRender();
                        }
                    }, DELAY);

                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    targetView.setVisibility(View.VISIBLE);
                    SegmentViewController.this.animator = null;
                    surfaceView.requestRender();
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            if (animatorListener != null) {
                animator.addListener(animatorListener);
            }

            //创建视图的截图
            rootView.setDrawingCacheEnabled(true);
            final Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());

            rootView.setDrawingCacheEnabled(false);

            surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    //为每个Mash设置Texture
                    for (int i = 0; i < segments.length; i++) {
                        Bitmap texture = Bitmap.createBitmap(bitmap, segments[i].leftPx, 0, segments[i].widthPx,
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

    public void setSegmentsData(int[] segmentsWidthData) {
        segments = new Segment[segmentsWidthData.length];
        int offset = 0;
        for (int i = 0; i < segmentsWidthData.length; i++) {
            segments[i] = new Segment();
            segments[i].widthPx = segmentsWidthData[i];
            segments[i].leftPx = offset;
            segments[i].width = segmentsWidthData[i] / (width / ratio / 2);
            float _offset = offset / (width / ratio / 2);
            segments[i].left = -ratio + _offset;
            segments[i].startMove = ratio * 2 * (segmentsWidthData.length - 1 - i) +
                    segments[i].width + _offset;
            offset += segmentsWidthData[i];
        }

        //mesh的创建是有成本的，需要编译和加载shader，另外，创建texture指针，因此尽量复用
        if (meshes.size() < segments.length) {
            surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    int newInstanceCount = segments.length - meshes.size();
                    for (int i = 0; i < newInstanceCount; i++) {
                        meshes.add(new Mesh(surfaceView.getContext()));
                    }
                }
            });
        }
    }

    public void onResume() {
        if (surfaceView != null) {
            surfaceView.onResume();
        }
    }

    public void onPause() {
        if (surfaceView != null) {
            surfaceView.onPause();
        }
    }

    class Segment {
        int leftPx, widthPx;

        float left;
        float width;
        float startMove;

        float[] getVertexes(float factor, float maxLeft) {
            float[] vertexes = new float[]{
                    left, 1, 0,
                    left, -1, 0,
                    left + width, 1, 0,
                    left + width, -1, 0
            };

            if (maxLeft * factor < startMove) {
                for (int i = 0; i < vertexes.length; i += 3) {
                    vertexes[i] = vertexes[i] - startMove + maxLeft * factor;
                }
            }

            return vertexes;
        }
    }
}
