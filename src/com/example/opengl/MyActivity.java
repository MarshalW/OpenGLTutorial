package com.example.opengl;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView surfaceView = new GLSurfaceView(this);
        surfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
                //TODO 创建时调用
            }

            @Override
            public void onSurfaceChanged(GL10 gl10, int width, int height) {
                //TODO 界面变化时（比如屏幕从竖屏到横屏）调用
            }

            @Override
            public void onDrawFrame(GL10 gl10) {
                //TODO 绘制一次界面（帧）
            }
        });
    }
}
