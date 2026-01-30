package yashima;

import imgui.*;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiMouseCursor;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.io.InputStream;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_4;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_5;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

public class ImGuiLayer {
    private long glfwWindow;
    private final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();

    public ImGuiLayer(long glfwWindow) {
        this.glfwWindow = glfwWindow;
    }

    public void initImGui() {
        ImGui.createContext();

        final ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setBackendFlags(ImGuiBackendFlags.HasMouseCursors);
        io.setBackendPlatformName("imgui_java_impl_glfw");

        glfwSetKeyCallback(
            glfwWindow
            , (w, key, scancode, action, mods) -> {
                if(action == GLFW_PRESS) {
                    io.addKeyEvent(key, true);
                }
                else if(action == GLFW_RELEASE) {
                    io.addKeyEvent(key, false);
                }

                io.setKeyCtrl(
                    io.getKeysData()[GLFW_KEY_LEFT_CONTROL].getDown()
                    || io.getKeysData()[GLFW_KEY_RIGHT_CONTROL].getDown()
                );

                io.setKeyShift(
                    io.getKeysData()[GLFW_KEY_LEFT_SHIFT].getDown()
                    || io.getKeysData()[GLFW_KEY_RIGHT_SHIFT].getDown()
                );

                io.setKeyAlt(
                    io.getKeysData()[GLFW_KEY_LEFT_ALT].getDown()
                    || io.getKeysData()[GLFW_KEY_RIGHT_ALT].getDown()
                );

                io.setKeySuper(
                    io.getKeysData()[GLFW_KEY_LEFT_SUPER].getDown()
                    || io.getKeysData()[GLFW_KEY_RIGHT_SUPER].getDown()
                );
            }
        );

        glfwSetCharCallback(glfwWindow, (w, c) -> {
            if(c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c);
            }
        });

        glfwSetMouseButtonCallback(
            glfwWindow
            , (w, button, action, mods) -> {
                final boolean[] mouseDown = new boolean[5];

                mouseDown[0] =
                    (button == GLFW_MOUSE_BUTTON_1)
                    && (action != GLFW_RELEASE);

                mouseDown[1] =
                    (button == GLFW_MOUSE_BUTTON_2)
                    && (action != GLFW_RELEASE);

                mouseDown[2] =
                    (button == GLFW_MOUSE_BUTTON_3)
                    && (action != GLFW_RELEASE);

                mouseDown[3] =
                    (button == GLFW_MOUSE_BUTTON_4)
                    && (action != GLFW_RELEASE);

                mouseDown[4] =
                    (button == GLFW_MOUSE_BUTTON_5)
                    && (action != GLFW_RELEASE);

                io.setMouseDown(mouseDown);

                if(!io.getWantCaptureMouse() && mouseDown[1]) {
                    ImGui.setWindowFocus(null);
                }
            }
        );

        glfwSetScrollCallback(
            glfwWindow
            , (w, xOffset, yOffset) -> {
                io.setMouseWheel(io.getMouseWheelH() + (float) xOffset);
                io.setMouseWheel(io.getMouseWheel() + (float) yOffset);
            }
        );

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(glfwWindow, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                final String clipboardString =
                    glfwGetClipboardString(glfwWindow);

                if(clipboardString != null) {
                    return clipboardString;
                }
                else {
                    return "";
                }
            }
        });

        final ImFontAtlas fontAtlas = io.getFonts();
        final ImFontConfig fontConfig = new ImFontConfig();

        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesCyrillic());
        fontAtlas.addFontDefault();
        fontAtlas.build();
        imGuiGlfw.init(glfwWindow, false);
        imGuiGl3.init("#version 330 core");
    }

    public void update(float dt) {
        startFrame(dt);

        ImGui.newFrame();
        ImGui.showDemoWindow();
        ImGui.render();
        endFrame();
    }

    private void startFrame(final float deltaTime) {
        float[] winWidth = {Window.getWidth()};
        float[] winHeight = {Window.getHeight()};
        double[] mousePosX = {0.0};
        double[] mousePosY = {0.0};

        glfwGetCursorPos(glfwWindow, mousePosX, mousePosY);

        final ImGuiIO io = ImGui.getIO();

        io.setDisplaySize(winWidth[0], winHeight[0]);
        io.setDisplayFramebufferScale(1f, 1f);
        io.setMousePos((float)mousePosX[0], (float)mousePosY[0]);
        io.setDeltaTime(deltaTime);

        final int imguiCursor = ImGui.getMouseCursor();

        glfwSetCursor(glfwWindow, mouseCursors[imguiCursor]);
        glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    private void endFrame() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void destroyImGui() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();

        ImGui.destroyContext();
    }
}