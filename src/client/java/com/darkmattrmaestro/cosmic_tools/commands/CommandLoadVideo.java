package com.darkmattrmaestro.cosmic_tools.commands;

import com.badlogic.gdx.utils.Array;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.items.ClientDisplayWand;
import com.darkmattrmaestro.cosmic_tools.utils.ChunkUtils;
import com.darkmattrmaestro.cosmic_tools.utils.Selection;
import com.darkmattrmaestro.cosmic_tools.utils.SoundUtils;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.chat.commands.Command;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.networking.packets.blocks.BlockReplacePacket;
import finalforeach.cosmicreach.world.Chunk;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;

public class CommandLoadVideo extends Command {
//    static {
//        OpenCV.loadShared();
//    }

    public static Array<Selection> failedLampsSelections = new Array<>();

    public void run(IChat chat) {
        super.run(chat);
        switch (this.getNumberOfArgs()) {
            case 1: {
                break;
            }
            case 2: { // /load_video fps file_location
                if (ClientDisplayWand.getTLPos() == null || ClientDisplayWand.getBRPos() == null) {
                    sendMsg("No lamps were selected to form the display!");
                    return;
                }
                double fps = Double.parseDouble(this.getNextArg());
                if (fps <= 0) {
                    sendMsg("FPS must be positive (and non-zero)!");
                    return;
                }
                String fileLocation = this.getNextArg();

                failedLampsSelections.clear();

                int maxFrames = ClientDisplayWand.getMaxNumFrames(10);
                sendMsg("The display's memory can fit " + maxFrames + " frames (" + (maxFrames / fps) + " seconds of footage).");
                Constants.LOGGER.info("The display's memory can fit {} frames ({} seconds of footage).", maxFrames, (maxFrames / fps));

                File file = new File(fileLocation);

                if (!file.exists() || !file.isFile()) {
                    sendMsg("Video file does not exist at " + fileLocation);
                    Constants.LOGGER.warn("Video file does not exist at {}", fileLocation);
                    return;
                }

//                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                OpenCV.loadShared();

                try {
                    VideoCapture vidCap = new VideoCapture(fileLocation);

                    if (!vidCap.isOpened()) {
                        sendMsg("Could not load video from " + fileLocation);
                        Constants.LOGGER.warn("Could not load video from {}", fileLocation);
                        SoundUtils.successFailSound(false);
                        return;
                    }
                    double originalT = 1 / vidCap.get(Videoio.CAP_PROP_FPS);
                    double newT = 1 / fps;

                    Mat lastBinaryFrame = new Mat();
                    Mat frame = new Mat();
                    int frameIndex = -1;
                    double currTime = newT;
                    while (vidCap.read(frame) && frameIndex < maxFrames - 1) {
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



                        String outputFilePath = "D:/Downloads/OutJavaMatTestPngs/output_image" + frameIndex + ".png";

                        // Save the Mat as a PNG image
                        boolean success = Imgcodecs.imwrite(outputFilePath, binaryFrame);

                        List<Chunk> chunksToUpdate = new ArrayList<>();
                        for (int x = 0; x < scaledFrame.cols(); x++) {
                            for (int y = 0; y < scaledFrame.rows(); y++) {
                                Constants.LOGGER.warn("x:{} y:{} binaryFrame {} - {}", x, y, binaryFrame, binaryFrame.get(y, x));
                                double avgColour = Arrays.stream(binaryFrame.get(y, x)).average().orElse(0);
                                double lastAvgColour = Arrays.stream(lastBinaryFrame.get(y, x)).average().orElse(0);

                                String powerValue = (avgColour > 0.5 != lastAvgColour > 0.5) ? "on" : "off";
                                Constants.LOGGER.warn("x:{} y:{} -> {}", x, y, powerValue);

                                BlockPosition switchPos = ClientDisplayWand.getSwitchAtIndex(x, y, frameIndex);
                                if (switchPos == null) {
                                    BlockPosition failedLampPos = ClientDisplayWand.getPixelLampPos(x, y);
                                    if (failedLampPos == null) {
                                        Constants.LOGGER.error("An error");
                                        continue;
                                    }
                                    failedLampsSelections.add(Selection.of(failedLampPos, failedLampPos));
                                    sendMsg("Lamp at " + failedLampPos + " has no connected laser switch!");
                                    Constants.LOGGER.error("Could not find laser switch bit for {}", failedLampPos);
                                    continue;
                                }
                                BlockState switchState = switchPos.getBlockState();
                                InGame.getLocalPlayer().getZone().setBlockState(switchState.getVariantWithParam("power", powerValue), switchPos.getGlobalX(), switchPos.getGlobalY(), switchPos.getGlobalZ());
//
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
                    sendMsg("Could not load video from " + fileLocation);
                    Constants.LOGGER.warn("Could not load video from {}", fileLocation);
                    throw new RuntimeException(e);
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
