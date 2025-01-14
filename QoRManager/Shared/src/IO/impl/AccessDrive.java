package IO.impl;

import IO.IAccessDrive;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class AccessDrive implements IAccessDrive {
    public List<String> listFilesUsingFilesList(String dir) throws IOException {
        try (var stream = Files.list(Paths.get(dir))) {
            return stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .toList();
        }
    }

    public void writeImageToDisk(BufferedImage image, File file, String format) throws IOException {
        var parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        ImageIO.write(image, format, file);
    }

    public String getContentFromFileAsString(String location) throws IOException {
        var path = Paths.get(location);
        var bytes = Files.readAllBytes(path);

        return new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
    }
}
