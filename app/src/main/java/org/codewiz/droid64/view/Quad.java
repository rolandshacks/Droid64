package org.codewiz.droid64.view;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by roland on 18.08.2016.
 */

public class Quad
{

    private static final Vertex QUAD_COORDS = new Vertex (new float[]
        {
            1f,1f,0f,
            0f,1f,0f,
            1f,0f,0f,
            0f,0f,0f,
        });

    private static final Vertex TEXTURE_COORDS = new Vertex (new float[]
        {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
        });

    /** The draw method for the square with the GL context */
    public static void draw (GL10 gl, int image, float x, float y, float width, float height, Color color, boolean textureFiltering)
    {
        gl.glPushMatrix();

        gl.glTranslatef (x, y, 0.0f); //MOVE !!! 1f is size of figure if called after scaling, 1f is pixel if called before scaling
        gl.glScalef (width, height, 0.0f); // ADJUST SIZE !!!

        // bind the previously generated texture
        gl.glBindTexture(GL10.GL_TEXTURE_2D, image);

        // Point to our buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // set the colour for the square
        gl.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        if (textureFiltering) {
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        } else {
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        }


        // Point to our vertex buffer
        gl.glVertexPointer (3, GL10.GL_FLOAT, 0, QUAD_COORDS.buffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXTURE_COORDS.buffer);

        // Draw the vertices as triangle strip
        gl.glDrawArrays (GL10.GL_TRIANGLE_STRIP, 0, QUAD_COORDS.vertex.length / 3);

        // Disable the client state before leaving
        gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        gl.glPopMatrix();
    }

}
