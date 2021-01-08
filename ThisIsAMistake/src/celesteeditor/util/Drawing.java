package celesteeditor.util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

public class Drawing {
	
	private static Texture boundTex;
	
	public static void drawTexture(GL2 gl, Texture tex, final float x, final float y,
            final int texturex, final int texturey,
            final int width, final int height,
            final float scaleFactor) throws GLException {
		drawTexture(gl, tex, x, y, texturex, texturey, width, height, scaleFactor, scaleFactor);
	}
	
	public static void drawTexture(GL2 gl, Texture tex, final float x, final float y,
            final int texturex, final int texturey,
            final int width, final int height,
            final float scaleX, final float scaleY) throws GLException {
		tex.enable(gl);
		if(tex != boundTex) {
			tex.bind(gl);
			boundTex = tex;
		}
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		final TextureCoords coords = tex.getSubImageTexCoords(texturex, texturey,
		                                           texturex + width,
		                                           texturey + height);
		gl.glBegin(GL2GL3.GL_QUADS);
		gl.glTexCoord2f(coords.left(), coords.bottom());
		gl.glVertex2f(x, y);
		gl.glTexCoord2f(coords.right(), coords.bottom());
		gl.glVertex2f(x + width * scaleX, y);
		gl.glTexCoord2f(coords.right(), coords.top());
		gl.glVertex2f(x + width * scaleX, y + height * scaleY);
		gl.glTexCoord2f(coords.left(), coords.top());
		gl.glVertex2f(x, y + height * scaleY);
		gl.glEnd();
		gl.glBindTexture(tex.getTarget(), 0);
	}
	
	public static void unbindTexture(GL2 gl, int target) {
		boundTex = null;
		gl.glBindTexture(target, 0);
	}
}
