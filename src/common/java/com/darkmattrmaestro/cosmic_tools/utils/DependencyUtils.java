package com.darkmattrmaestro.cosmic_tools.utils;

import com.darkmattrmaestro.cosmic_tools.Constants;
import dev.puzzleshq.puzzleloader.loader.launch.Piece;
import dev.puzzleshq.puzzleloader.loader.util.ModFinder;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;

public class DependencyUtils {
    public static boolean downloadOpenCV() {
        AtomicBoolean foundOpenCV = new AtomicBoolean(false);
        Piece.classLoader.sources.forEach((URL url) -> {
            if (url.getFile().toLowerCase().contains("opencv-4.9.0-0")) {
                foundOpenCV.set(true);
                sendMsg("- Found: " + url.getFile());
            }
        });

        if (foundOpenCV.get()) {
            sendMsg("OpenCV 4.9.0-0 is already installed, no download is required.");
            return true;
        }

        sendMsg("Downloading OpenCV 4.9.0-0 to mods folder...");

        String url = "https://repo1.maven.org/maven2/org/openpnp/opencv/4.9.0-0/opencv-4.9.0-0.jar";
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        String filePath = ModFinder.getModFolder().toPath() + "/" + fileName;
        Path outPath = Paths.get(filePath);
        Constants.LOGGER.warn("outPath: {}", outPath);
        try(InputStream in = (new URI(url)).toURL().openStream()) {
            Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
            Piece.classLoader.addURL(outPath.toUri().toURL());
        } catch (Exception e) {
            sendMsg("Failed to download OpenCV!");
            return false;
        }

        return true;
    }
}
