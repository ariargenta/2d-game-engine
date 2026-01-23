package yashima;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import renderer.Shader;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_TRIANGLES;
import static org.lwjgl.opengl.GL20.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL20.glBindBuffer;
import static org.lwjgl.opengl.GL20.glBufferData;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glDrawElements;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {
    private int vertexID;
    private int fragmentID;
    private int shaderProgram;

    private float[] vertexArray = {
        // x, y, z, r, g, b, a
        100.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f       // Bottom Right 0
        , 0.5f, 100.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f     // Top Left 1
        , 100.5f, 100.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f      // Top Right 2
        , 0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,   // Bottom Left 3
    };

    /**
     * Constraints: Counter-Clockwise order
     */
    private int[] elementArray = {
        2, 1, 0     // Top Right triangle
        , 0 , 1, 3  // Bottom Left triangle
    };

    private int vaoID;
    private int vboID;
    private int eboID;
    private Shader defaultShader;

    public LevelEditorScene() {}

    @Override
    public void init() {
        this.camera = new Camera(new Vector2f());

        defaultShader = new Shader("assets/shaders/default.glsl");

        defaultShader.compile();

        vaoID = glGenVertexArrays();

        glBindVertexArray(vaoID);

        FloatBuffer vertexBuffer =
            BufferUtils.createFloatBuffer(vertexArray.length);

        vertexBuffer.put(vertexArray).flip();

        vboID = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        IntBuffer elementBuffer =
            BufferUtils.createIntBuffer(elementArray.length);

        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        int positionsSize = 3;
        int colorSize = 4;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

        glVertexAttribPointer(
            0
            , positionsSize
            , GL_FLOAT
            , false
            , vertexSizeBytes
            , 0
        );

        glEnableVertexAttribArray(0);

        glVertexAttribPointer(
            1
            , colorSize
            , GL_FLOAT
            , false
            , vertexSizeBytes
            , positionsSize * floatSizeBytes
        );

        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
        camera.position.x -= dt * 50.0f;
        camera.position.y -= dt * 20.0f;

        defaultShader.use();

        defaultShader.uploadMat4f(
            "uProjection"
            , camera.getProjectionMatrix()
        );

        defaultShader.uploadMat4f(
            "uView"
            , camera.getViewMatrix()
        );

        defaultShader.uploadFloat("uTime", Time.getTime());

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(
            GL_TRIANGLES
            , elementArray.length
            , GL_UNSIGNED_INT
            , 0
        );

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        defaultShader.detach();
    }
}