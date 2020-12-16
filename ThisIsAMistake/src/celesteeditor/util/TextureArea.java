package celesteeditor.util;

import java.awt.Rectangle;

import com.jogamp.opengl.util.texture.Texture;

public class TextureArea {
	
	public Texture texture;
	
	public Rectangle area;
	
	public int width, height, offsetX, offsetY;
	
	public TextureArea() {}
	
	public TextureArea(Texture tex, Rectangle rect) {
		this(tex, rect, rect.width, rect.height, 0, 0);
	}
	
	public TextureArea(Texture tex, Rectangle rect, int w, int h, int offsetX, int offsetY) {
		texture = tex;
		area = rect;
		width = w;
		height = h;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
}
