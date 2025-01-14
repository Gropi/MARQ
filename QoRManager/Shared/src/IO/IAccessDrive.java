package IO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IAccessDrive {
    List<String> listFilesUsingFilesList(String dir) throws IOException;

    void writeImageToDisk(BufferedImage image, File file, String format) throws IOException;

    String getContentFromFileAsString(String location) throws IOException;
}
