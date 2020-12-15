package test;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

public class Drawing {
	public static void drawTexture(GL2 gl, Texture tex, final float x, final float y,
            final int texturex, final int texturey,
            final int width, final int height,
            final float scaleFactor) throws GLException {
		tex.enable(gl);
		tex.bind(gl);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		final TextureCoords coords = tex.getSubImageTexCoords(texturex, texturey,
		                                           texturex + width,
		                                           texturey + height);
		gl.glBegin(GL2GL3.GL_QUADS);
		gl.glTexCoord2f(coords.left(), coords.bottom());
		gl.glVertex3f(x, y, 0);
		gl.glTexCoord2f(coords.right(), coords.bottom());
		gl.glVertex3f(x + width * scaleFactor, y, 0);
		gl.glTexCoord2f(coords.right(), coords.top());
		gl.glVertex3f(x + width * scaleFactor, y + height * scaleFactor, 0);
		gl.glTexCoord2f(coords.left(), coords.top());
		gl.glVertex3f(x, y + height * scaleFactor, 0);
		gl.glEnd();
	}
}
