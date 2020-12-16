package celesteeditor.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

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
	
	public static Texture getTexture(String path) {
		try {
			URL url = Util.class.getResource(path);
			return url != null ? TextureIO.newTexture(url, false, FilenameUtils.getExtension(path)) : null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
