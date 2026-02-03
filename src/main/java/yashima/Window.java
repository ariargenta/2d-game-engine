package yashima;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private int width;
    private int height;
    private final String title;
    private static Window window = null;
    private long glfwWindow;
    private ImGuiLayer imguiLayer;
    public float r;
    public float g;
    public float b;
    private final float a;
    private final boolean fadeToBlack = false;
    private static Scene currentScene;

    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "Yashima Engine";

        r = 1;
        g = 1;
        b = 1;
        a = 1;
    }

    public static void changeScene(int newScene) {
        switch(newScene) {
            case 0:
                currentScene = new LevelEditorScene();

                currentScene.init();
                currentScene.start();

                break;
            case 1:
                currentScene = new LevelScene();

                currentScene.init();
                currentScene.start();

                break;
            default:
                assert false : "Unknown scene '" + newScene + "'";

                break;
        }
    }

    public static Window get() {
        if(Window.window == null) {
            Window.window = new Window();
        }

        return Window.window;
    }

    public static Scene getScene() {
        get();
        return currentScene;
    }

    public void run() {
        System.out.println("LWJGL " + Version.getVersion() + " initialized");

        init();
        loop();

        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        glfwWindow = glfwCreateWindow(
                this.width, this.height, this.title, NULL, NULL
        );

        if(glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create GLFW window");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);

        glfwSetMouseButtonCallback(
                glfwWindow, MouseListener::mouseButtonCallback
        );

        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        glfwSetWindowSizeCallback(
            glfwWindow
            , (w, newWidth, newHeight) -> {
                Window.setWidth(newWidth);
                Window.setHeight(newHeight);
            }
        );

        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);

        /*This line is critical for LWJGL interoperation with GLFW's
        * OpenGL context, or any context that is managed externally.
        * LWJGL detects the context that is current in the current thread,
        * creates the GLCapabilities instance and makes the OpenGL
        * bindings available for use.*/
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        this.imguiLayer = new ImGuiLayer(glfwWindow);

        this.imguiLayer.initImGui();

        Window.changeScene(0);
    }

    public void loop() {
        float beginTime = (float)glfwGetTime();
        float endTime;
        float dt = -1.0f;

        while(!glfwWindowShouldClose(glfwWindow)) {
            glfwPollEvents();
            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT);

            if(dt >= 0) {
                currentScene.update(dt);
            }

            this.imguiLayer.update(dt);

            glfwSwapBuffers(glfwWindow);

            endTime = (float)glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }

    public static int getWidth() {
        return get().width;
    }

    public static int getHeight() {
        return get().height;
    }

    public static void setWidth(int newWidth) {
        get().width = newWidth;
    }

    public static void setHeight(int newHeight) {
        get().height = newHeight;
    }
}