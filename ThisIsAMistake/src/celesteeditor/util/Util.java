package celesteeditor.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Util {
	public static BufferedImage getImage(String path) {
		try {
			URL url = Util.class.getResource(path);
			return url != null ? ImageIO.read(url) : null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
