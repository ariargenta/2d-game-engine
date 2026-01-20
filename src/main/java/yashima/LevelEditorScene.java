package yashima;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_TRIANGLES;
import static org.lwjgl.opengl.GL20.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindBuffer;
import static org.lwjgl.opengl.GL20.glBufferData;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glDrawElements;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {
    private String vertexShaderSrc = "#version 330 core\n" +
        "\n" +
        "layout (location = 0) in vec3 aPos;\n" +
        "layout (location = 1) in vec4 aColor;\n" +
        "\n" +
        "out vec4 fColor;\n" +
        "\n" +
        "void main() {\n" +
        "    fColor = aColor;\n" +
        "    gl_Position = vec4(aPos, 1.0);\n" +
        "}";

    private String fragmentShaderSrc = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main() {\n" +
            "    color = fColor;\n" +
            "}";

    private int vertexID;
    private int fragmentID;
    private int shaderProgram;

    private float[] vertexArray = {
        // x, y, z, r, g, b, a
        0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f       // Bottom Right 0
        , -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f     // Top Left 1
        , 0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f      // Top Right 2
        , -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,   // Bottom Left 3
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

    public LevelEditorScene() {}

    @Override
    public void init() {
        vertexID = glCreateShader(GL_VERTEX_SHADER);

        glShaderSource(vertexID, vertexShaderSrc);
        glCompileShader(vertexID);

        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);

        if(success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);

            System.out.println(
                "ERROR: 'defaultShader.glsl'\n\tVertex shader compilation failed."
            );

            System.out.println(glGetShaderInfoLog(vertexID, len));

            assert false : "";
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(fragmentID, fragmentShaderSrc);
        glCompileShader(fragmentID);

        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);

        if(success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);

            System.out.println(
                "ERROR: 'defaultShader.glsl'\n\tFragment shader compilation failed."
            );

            System.out.println(glGetShaderInfoLog(fragmentID, len));

            assert false : "";
        }

        shaderProgram = glCreateProgram();

        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);

        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);

        if(success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);

            System.out.println(
                "ERROR: 'defaultShader.glsl'\n\tLinking of shaders failed."
            );

            System.out.println(glGetProgramInfoLog(shaderProgram, len));

            assert false: "";
        }

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
        glUseProgram(shaderProgram);
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
        glUseProgram(0);
    }
}