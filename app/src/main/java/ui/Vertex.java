package ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by roland on 18.08.2016.
 */

public class Vertex
{
    public FloatBuffer buffer; // buffer holding the vertices
    public float vertex[];
    public Vertex (float[] vertex)
    {
        this.vertex = vertex;
        this.prepare ();
    }
    private void prepare ()
    {
        // a float has 4 bytes so we allocate for each coordinate 4 bytes
        ByteBuffer factory = ByteBuffer.allocateDirect (vertex.length * 4);
        factory.order (ByteOrder.nativeOrder ());
        // allocates the memory from the byte buffer
        buffer = factory.asFloatBuffer ();

        // fill the vertexBuffer with the vertices
        buffer.put (vertex);
        // set the cursor position to the beginning of the buffer
        buffer.position (0);
    }
}
