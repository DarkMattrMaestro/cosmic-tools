package com.darkmattrmaestro.cosmic_tools.imgui_windows;

import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.utils.SoundUtils;
import dev.puzzleshq.puzzleloader.loader.launch.Piece;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.gamestates.PauseMenu;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import org.tympanic.imgui_integration.imgui.ImGuiManager;
import org.tympanic.imgui_integration.imgui.ImGuiWindow;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.darkmattrmaestro.cosmic_tools.commands.CommandLoadVideo.loadVideo;
import static com.darkmattrmaestro.cosmic_tools.utils.DependencyUtils.downloadOpenCV;

public class LoadVideoWindow extends ImGuiWindow {
    private final ImBoolean SHOW = new ImBoolean(true);
    private final ImFloat FPS = new ImFloat(5);
    private final ImString FILENAME = new ImString();

    private final ImString ERROR_MSG = new ImString();

    private boolean opencv_dirty = true;
    private boolean found_opencv = false;

    @Override
    public void init() {
        this.renderIn(ChatMenu.class);
        this.renderIn(InGame.class);
        this.renderIn(PauseMenu.class);
    }

    private void selectVideoFileDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
        chooser.setFileFilter(new FileFilter() {
            public String getDescription() {
                return "Video file";
            }

            public boolean accept(File f) {
                return true;
            }
        });
        (new Thread(() -> {
            int result = chooser.showOpenDialog(null);
            if (result == 0) {
                File file = chooser.getSelectedFile();
                FILENAME.set(file.getAbsolutePath());
            }
        })).start();
    }

    @Override
    public void render() {
        if (SHOW.get()) {
            // Check if OpenCV is installed
            if (this.opencv_dirty) {
                this.opencv_dirty = false;

                AtomicBoolean foundOpenCV = new AtomicBoolean(false);
                Piece.classLoader.sources.forEach((URL url) -> {
                    if (url.getFile().toLowerCase().contains("opencv-4.9.0-0")) {
                        foundOpenCV.set(true);
                    }
                });

                this.found_opencv = foundOpenCV.get();
            }

            ImGuiStyle style = ImGui.getStyle();

            // Start window
            if (ImGui.begin("Load Video", SHOW, ImGuiWindowFlags.AlwaysAutoResize)) {
                // OpenCV check and download prompt
                if (!found_opencv) {
                    ImGui.textColored(ImGui.getColorU32(1.0f, 0.64f, 0.64f, 1.0f), "OpenCV is required to load videos!");
                    if (ImGui.button("Download OpenCV")) {
                        downloadOpenCV();
                        this.opencv_dirty = true;
                    }
                    if (ImGui.button("Check again")) {
                        this.opencv_dirty = true;
                    }

                    ImGui.end();
                    return;
                }

                // FPS
                ImGui.setNextItemWidth(style.getFramePaddingX()*2 + ImGui.calcTextSizeX(String.format("%.3f", FPS.get())));
                ImGui.inputFloat("Display's FPS", FPS);
                // Video file selection
                ImGui.setNextItemWidth(style.getFramePaddingX()*2 + Math.max(
                        ImGui.calcTextSizeX("Video Filename"),
                        ImGui.calcTextSizeX(FILENAME.get())
                ));
                ImGui.inputTextWithHint("##2", "Video Filename", FILENAME);
                if (ImGui.isItemHovered()) {
                    ImGui.beginTooltip();
                    ImGui.text("""
                        The filename can be:
                        - name of video file (eg. video.avi);
                        - or image sequence (eg. img_%02d.jpg, which will read samples like img_00.jpg, img_01.jpg, img_02.jpg, ...);
                        - or URL of video stream (eg. protocol://host:port/script_name?script_params|auth).""");
                    ImGui.endTooltip();
                }
                ImGui.sameLine();
                if (ImGui.arrowButton("Open File Explorer", 2)) {
                    selectVideoFileDialog();
                }

                ImGui.spacing();

                if (ImGui.button("Load Video")) {
                    String res = loadVideo(FPS.get(), FILENAME.get());
                    SoundUtils.successFailSound(res != null);
                    if (res != null) {
                        Constants.LOGGER.error(res);
                        ERROR_MSG.set(res);
                        ImGui.openPopup("Error");
                    }
                }

                // Error popup
                if (ImGui.beginPopupModal("Error", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoSavedSettings | ImGuiWindowFlags.NoMove)) {
                    ImGui.text(ERROR_MSG.get());
                    String buttonMSG = "Close";
                    float size = ImGui.calcTextSizeX(buttonMSG) + style.getFramePaddingX() * 2;
                    float avail = ImGui.getContentRegionAvailX();
                    float off = (avail - size) * 0.5f;
                    if (off > 0.0f) { ImGui.setCursorPosX(ImGui.getCursorPosX() + off); }
                    if (ImGui.button(buttonMSG)) {
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.endPopup();
                }

                ImGui.end();
            }
        } else {
            ImGuiManager.INSTANCE.closeWindow(this);
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void dispose() {
        Constants.LOGGER.info("dispose window");
    }
}