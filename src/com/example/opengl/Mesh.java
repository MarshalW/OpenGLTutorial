package com.example.opengl;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-5-16
 * Time: 下午3:51
 * To change this template use File | Settings | File Templates.
 */
public class Mesh {

    private Shader shader;

    private FloatBuffer vertexBuffer;

    public Mesh(Context context) {
        shader = new Shader();
        shader.setProgram(context, R.raw.vertexes, R.raw.fragment);

        vertexBuffer = ByteBuffer.allocateDirect(3 * 4 * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public void draw(float[] projectionMatrix) {
        setVertexBuffer();

        this.shader.useProgram();

        glUniformMatrix4fv(shader.getHandle("projectionMatrix"), 1, false, projectionMatrix, 0);

        //获取shader的aPosition变量“指针”
        int aPosition = this.shader.getHandle("aPosition");
        //给Shader中aPosition变量赋值（顶点缓冲）
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false,
                3 * 4, vertexBuffer);
        glEnableVertexAttribArray(aPosition);

        //绘制三角形
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    /**
     * 设置顶点缓冲
     */
    private void setVertexBuffer() {
        vertexBuffer.clear();
        vertexBuffer.put(createTriangleVertexes());
        vertexBuffer.position(0);
    }

    /**
     * 创建一个三角形的各顶点坐标
     *
     * @return
     */
    private float[] createTriangleVertexes() {
        return new float[]{
                -0.5f, 0.5f, 0,
                -0.5f, -0.5f, 0,
                0.5f, 0.5f, 0,
                0.5f, -0.5f,0
        };
    }
}
