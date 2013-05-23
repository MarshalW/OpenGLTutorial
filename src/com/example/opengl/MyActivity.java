package com.example.opengl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MyActivity extends Activity {

    private View targetView;

    private ViewGroup rootView;

    private SegmentViewController controller;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        targetView = findViewById(R.id.targetView);
        rootView = (ViewGroup) findViewById(R.id.rootView);

        controller = new SegmentViewController(this, rootView, targetView);
        controller.setDuration(1000);
        controller.setAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Toast.makeText(MyActivity.this, "动画完成", Toast.LENGTH_SHORT).show();
            }
        });

        //为了测试，获取屏幕的宽度，正式情况下应该是动态获取界面宽度
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        final int displayWidth = point.x;

        targetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.setSegmentsData(new int[]{
                        150, 200, 150, displayWidth - (150 + 200 + 150)
                });
                controller.startAnimation();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.onResume();
    }

    @Override
    protected void onPause() {
        controller.onPause();
        super.onPause();
    }
}
