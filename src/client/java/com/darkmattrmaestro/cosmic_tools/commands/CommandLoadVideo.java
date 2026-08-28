package com.darkmattrmaestro.cosmic_tools.commands;

import com.badlogic.gdx.utils.Array;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.imgui_windows.LoadVideoWindow;
import com.darkmattrmaestro.cosmic_tools.items.ClientDisplayWand;
import com.darkmattrmaestro.cosmic_tools.utils.ChunkUtils;
import com.darkmattrmaestro.cosmic_tools.utils.OptionalMods;
import com.darkmattrmaestro.cosmic_tools.utils.Selection;
import com.darkmattrmaestro.cosmic_tools.utils.SoundUtils;
import dev.puzzleshq.puzzleloader.loader.launch.Piece;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.chat.commands.Command;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.blocks.BlockReplacePacket;
import finalforeach.cosmicreach.world.Chunk;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.tympanic.imgui_integration.imgui.ImGuiManager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;
import static com.darkmattrmaestro.cosmic_tools.utils.DependencyUtils.downloadOpenCV;

public class CommandLoadVideo extends Command {

    public static Array<Selection> failedLampsSelections = new Array<>();

    public static String loadVideo(double fps, String fileLocation) {
        if (fps <= 0) { return "FPS must be positive (and non-zero)!"; }

        if (fileLocation.isBlank()) { return "No filepath was given!"; }

        AtomicBoolean foundOpenCV = new AtomicBoolean(false);
        Piece.classLoader.sources.forEach((URL url) -> {
            if (url.getFile().toLowerCase().contains("opencv-4.9.0-0")) { foundOpenCV.set(true); }
        });

        if (!foundOpenCV.get()) { return "OpenCV is required to load videos! Use the command `/load_video OpenCV` to download it, or download it manually from https://opencv.org/releases/"; }

        if (ClientDisplayWand.getTLPos() == null || ClientDisplayWand.getBRPos() == null) { return "No lamps were selected to form the display!"; }

        failedLampsSelections.clear();

        ClientDisplayWand.cacheAllSwitches();
        int maxFrames = ClientDisplayWand.getMaxNumFrames() - 1; // Subtract 1 to compensate for final black frame
        if (maxFrames < 1) { return "The display's memory cannot fit any frames (including final buffer frame)!"; }
        sendMsg("The display's memory can fit " + maxFrames + " frames (" + (maxFrames / fps) + " seconds of footage).");
        Constants.LOGGER.info("The display's memory can fit {} frames ({} seconds of footage).", maxFrames, (maxFrames / fps));

        File file = new File(fileLocation);

        if (!file.exists() || !file.isFile()) {
            return "Video file does not exist at `" + fileLocation + "`!";
        }

        OpenCV.loadShared();

        try {
            VideoCapture vidCap = new VideoCapture(fileLocation);

            if (!vidCap.isOpened()) {
                return "Could not load video from `" + fileLocation + "`!";
            }
            double originalT = 1 / vidCap.get(Videoio.CAP_PROP_FPS);
            double newT = 1 / fps;

            Mat lastBinaryFrame = new Mat();
            Mat frame = new Mat();
            int frameIndex = -1;
            double currTime = newT;
            while (vidCap.read(frame) && frameIndex < maxFrames) {
                // Frame-rate conversion
                currTime += originalT;
                if (currTime < newT) { continue; }
                currTime %= newT;
                frameIndex++;
                Constants.LOGGER.warn("Frame #" + frameIndex);

                Constants.LOGGER.warn("Frame captured: " + frame.size());

                // Scale to display size
                Mat scaledFrame = new Mat();
                Imgproc.resize(frame, scaledFrame, new Size(ClientDisplayWand.getWidthPixels(), ClientDisplayWand.getHeightPixels()));
                Constants.LOGGER.warn("Frame resized: " + scaledFrame.size());

                // Grayscale
                Mat grayFrame = new Mat();
                Imgproc.cvtColor(scaledFrame, grayFrame, Imgproc.COLOR_BGR2GRAY);

                // Black and white
                Mat binaryFrame = new Mat();
                double thresholdValue = 128;
                Imgproc.threshold(grayFrame, binaryFrame, thresholdValue, 255, Imgproc.THRESH_BINARY);
                if (frameIndex == 0) {
                    Imgproc.threshold(grayFrame, lastBinaryFrame, 10000, 255, Imgproc.THRESH_BINARY);
                }

                List<Chunk> chunksToUpdate = new ArrayList<>();
                for (int x = 0; x < scaledFrame.cols(); x++) {
                    for (int y = 0; y < scaledFrame.rows(); y++) {
                        Constants.LOGGER.info("x:{} y:{} binaryFrame {} - {}", x, y, binaryFrame, binaryFrame.get(y, x));
                        double avgColour;
                        if (frameIndex < maxFrames) {
                            avgColour = Arrays.stream(binaryFrame.get(y, x)).average().orElse(0);
                        } else {
                            avgColour = 0;
                        }
                        double lastAvgColour = Arrays.stream(lastBinaryFrame.get(y, x)).average().orElse(0);

                        String powerValue = (avgColour > 0.5 != lastAvgColour > 0.5) ? "on" : "off";
                        Constants.LOGGER.info("x:{} y:{} -> {}", x, y, powerValue);

                        BlockPosition switchPos = ClientDisplayWand.getSwitchAtIndex(x, y, frameIndex);
                        if (switchPos == null) {
                            BlockPosition failedLampPos = ClientDisplayWand.getPixelLampPos(x, y);
                            if (failedLampPos == null) {
                                Constants.LOGGER.warn("An error occurred, skipping lamp.");
                                continue;
                            }
                            failedLampsSelections.add(Selection.of(failedLampPos, failedLampPos));
                            sendMsg("Lamp at " + failedLampPos + " has no connected laser switch!");
                            Constants.LOGGER.warn("Could not find laser switch bit for {}", failedLampPos);
                            continue;
                        }
                        BlockState switchState = switchPos.getBlockState();
                        InGame.getLocalPlayer().getZone().setBlockState(switchState.getVariantWithParam("power", powerValue), switchPos.getGlobalX(), switchPos.getGlobalY(), switchPos.getGlobalZ());

                        Chunk c = InGame.getLocalPlayer().getZone().getChunkAtBlock(switchPos.getGlobalX(), switchPos.getGlobalY(), switchPos.getGlobalZ());
                        if (!chunksToUpdate.contains(c)) chunksToUpdate.add(c);

                        if (ClientNetworkManager.isConnected()) {
                            ClientNetworkManager.sendAsClient(new BlockReplacePacket(c.getZone(), switchState.getVariantWithParam("power", powerValue), switchPos));
                        }
                    }
                }
                ChunkUtils.remesh(chunksToUpdate, InGame.getLocalPlayer().getZone());
                binaryFrame.copyTo(lastBinaryFrame);
            }

            vidCap.release();
        } catch (Exception e) {
            return "Could not load video from `" + fileLocation + "`!";
        }

        return null;
    }

    public void run(IChat chat) {
        super.run(chat);

        switch (this.getNumberOfArgs()) {
            case 0: {
                if (!OptionalMods.hasImgui()) {
                    sendMsg("The Dear ImGui Integration Mod is required for this command! The mod can be found at https://crmods.org/mod/imgui-integration");
                    return;
                }

                ImGuiManager.INSTANCE.windows.add(new LoadVideoWindow());

                break;
            }
            case 1: { // /load_video opencv
                String param = this.getNextArg();
                if (param.equalsIgnoreCase("opencv")) {
                    downloadOpenCV();
                } else {
                    sendMsg("Invalid arguments!");
                }

                Constants.LOGGER.info("\nNow:");

                Piece.classLoader.sources.forEach((URL url) -> {
                    if (!url.getFile().toLowerCase().contains("opencv")) { return; }
                    Constants.LOGGER.warn(url.getFile());
                });

                return;
            }
            case 2: { // /load_video fps file_location
                double fps = Double.parseDouble(this.getNextArg());
                String fileLocation = this.getNextArg();

                String res = loadVideo(fps, fileLocation);
                SoundUtils.successFailSound(res != null);
                if (res != null) {
                    sendMsg(res);
                    Constants.LOGGER.error(res);
                }
                break;
            }
            default: {
                sendMsg("Incorrect number of arguments!");
                break;
            }
        }
    }

    public String getShortDescription() {
        return "Loads a video into the memory of a display, where each bit is represented by the state of a laser switch.";
    }
}
