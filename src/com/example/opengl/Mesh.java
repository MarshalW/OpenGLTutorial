package com.example.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-5-16
 * Time: 下午3:51
 * To change this template use File | Settings | File Templates.
 */
public class Mesh {

    private Shader shader;

    private FloatBuffer vertexBuffer, textureCoordBuffer;

    private int[] textureId;

    public Mesh(Context context) {
        shader = new Shader();
        shader.setProgram(context, R.raw.vertexes, R.raw.fragment);

        vertexBuffer = ByteBuffer.allocateDirect(3 * 4 * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer();

        textureCoordBuffer = ByteBuffer.allocateDirect(2 * 4 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureCoordBuffer.put(new float[]{
                0, 0,
                0, 1,
                1, 0,
                1, 1
        });
    }

    public void loadTexture(Bitmap texture) {
        Bitmap bitmap = Bitmap.createBitmap(texture);

        if (textureId == null) {
            //创建纹理指针
            textureId = new int[1];
            glGenTextures(1, textureId, 0);

            //绑定纹理
            glBindTexture(GL_TEXTURE_2D, textureId[0]);

            //设置纹理滤镜
            glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        }

        textureCoordBuffer.position(0);

        //设置纹理坐标
        int aTextureCoord = this.shader.getHandle("aTextureCoord");
        glVertexAttribPointer(aTextureCoord, 2, GL_FLOAT, false,
                0, textureCoordBuffer);
        glEnableVertexAttribArray(aTextureCoord);

        //加入纹理
        glEnable(GL_TEXTURE_2D);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        glDisable(GL_TEXTURE_2D);

        bitmap.recycle();
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

        //绑定纹理
        glBindTexture(GL_TEXTURE_2D, textureId[0]);

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
        return
                new float[]{
                        -0.5f, 0.5f, 0,
                        -0.5f, -0.5f, 0,
                        0.5f, 0.5f, 0,
                        0.5f, -0.5f, 0
                };
    }
}
