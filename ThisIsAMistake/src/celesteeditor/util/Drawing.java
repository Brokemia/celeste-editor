package celesteeditor.util;

import java.awt.Image;

import com.jogamp.opengl.util.awt.TextureRenderer;

public class Drawing {
	public static TextureRenderer imageToTextureRenderer(Image img) {
		TextureRenderer renderer = new TextureRenderer(img.getWidth(null), img.getHeight(null), true);
		renderer.createGraphics().drawImage(img, 0, 0, null);
		
		return renderer;
	}
}
