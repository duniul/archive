package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javafx.scene.media.Media;

/*
 * Handles the loading of image and sound resources.
 * The class uses 2 HashMaps to store resources that have
 * been previously loaded. It is also singleton to avoid
 * several instances being used at once.
 *
 * ADDITIONS FOR ASSIGNMENT 2:
 * - the getFont() method has been added.
 */

public class Loader {
    private static Loader instance = null;
    private HashMap<String, BufferedImage> imageResourceMap;
    private HashMap<String, Media> soundResourceMap;
    private HashMap<String, Font> fontResourceMap;

    protected Loader() {
    }

    public static Loader getInstance() {
        if (instance == null) {
            instance = new Loader();
            instance.imageResourceMap = new HashMap<String, BufferedImage>();
            instance.soundResourceMap = new HashMap<String, Media>();
            instance.fontResourceMap = new HashMap<String, Font>();
        }
        return instance;
    }

    // Returns an image specified by path.
    public BufferedImage getImage(String path) {
        if (imageResourceMap.containsKey(path)) {
            return imageResourceMap.get(path);
        } else {
            BufferedImage img = null;
            try {
                img = ImageIO.read(getClass().getResourceAsStream(path));
                imageResourceMap.put(path, img);
                return img;
            } catch (IOException e) {
                System.err.println("File " + path + " not found");
                return null;
            } catch (IllegalArgumentException iae) {
                System.err.println("File " + path + " not found");
                return null;
            }
        }
    }

    public String getText(String path, Charset encoding) {
        char[] buf = new char[2048];
        InputStream is = getClass().getResourceAsStream(path);
        Reader r = new InputStreamReader(is, encoding);
        StringBuilder s = new StringBuilder();
        while (true) {
            try {
                int n = r.read(buf);
                if (n < 0)
                    break;
                s.append(buf, 0, n);
            } catch (IOException ioe) {
                System.err.println("File " + path + " not found");
                return null;
            }
        }
        return s.toString();
    }

    // Returns a sound file specified by path.
    public Media getSound(String path) {
        if (soundResourceMap.containsKey(path)) {
            return soundResourceMap.get(path);
        } else {
            URL file = getClass().getResource(path);
            final Media media = new Media(file.toString());
            soundResourceMap.put(path, media);
            return media;
        }
    }

    // Returns a font file specified by path.
    public Font getFont(String path) {
        if (fontResourceMap.containsKey(path)) {
            return fontResourceMap.get(path);
        } else {
            InputStream is = getClass().getResourceAsStream(path);
            Font font = null;
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, is);
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fontResourceMap.put(path, font);
            return font;
        }
    }

    public void removeResource(String path) {
        if (imageResourceMap.containsKey(path)) {
            imageResourceMap.remove(path);
        } else if (soundResourceMap.containsKey(path)) {
            soundResourceMap.remove(path);
        }
    }
}
