package test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Test implements GLEventListener {
	private static GraphicsEnvironment graphicsEnviorment;
	private static boolean isFullScreen = false;
	public static DisplayMode dm, dm_old;
	private static Dimension xgraphic;
	private static Point point = new Point(0, 0);
	private static JFrame frame;
	
	private GLU glu = new GLU();
	
	private float xrot, yrot,zrot;
	private int texture;
	
	@Override
	public void display(GLAutoDrawable drawable) {
		dispSquare(drawable);
	}
	
	private void dispRotatingCube(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		 gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);     // Clear The Screen And The Depth Buffer
		    gl.glLoadIdentity();                       // Reset The View
		    gl.glTranslatef(0f, 0f, -5.0f);
		    gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
		    gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);
		    gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);
		    gl.glBindTexture(GL2.GL_TEXTURE_2D, texture);
		    gl.glBegin(GL2.GL_QUADS);
	        // Front Face
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
	        // Back Face
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
	        // Top Face
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
	        // Bottom Face
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
	        // Right face
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
	        // Left Face
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
	      gl.glEnd();
		    gl.glFlush();
		    xrot+=.3f;
		    yrot+=.2f;
		    zrot+=.4;
	}
	
	private void dispSquare(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
        
        //renderingComplete = false;
        
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        final int attribBits = GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL2.GL_TRANSFORM_BIT;
	    gl.glPushAttrib(attribBits);
	    gl.glDisable(GLLightingFunc.GL_LIGHTING);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(0, 800, 0, 800);
	    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	    gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_MODULATE);
        
        //if(Main.loadedMap != null) {
			//lock.lock();
			//for(Level level : Main.loadedMap.levels) {
	    IntBuffer frameBuffer = IntBuffer.allocate(1);
	    gl.glGenFramebuffers(1, frameBuffer);
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, frameBuffer.get());
		IntBuffer texID = IntBuffer.allocate(1);
		gl.glGenTextures(1, texID);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texID.get(0));
		CharBuffer b = CharBuffer.allocate(100 * 100 * 4);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, 100, 100, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, b);
		Texture roomTex = new Texture(texID.get(0), GL.GL_TEXTURE_2D, 100, 100, 100, 100, false);
		roomTex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		roomTex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glFramebufferTextureEXT(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, roomTex.getTextureObject(gl), 0);
		gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT0);
		
		if(gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER) != GL.GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Error when initializing frame buffer");
		}
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
				Texture tex = null;
				try {
					tex = TextureIO.newTexture(new File("../ThisIsAMistake/bin/assets/add.png"), false);
				} catch (GLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        Drawing.drawTexture(gl, tex, 0 + 10, 20,
		        		0, 0, tex.getWidth(), tex.getHeight(), (float)1);
		        gl.glFlush();
			//}
			
//			if(mapRenderThread == null) {
//				mapRenderThread = new Thread(new MapRenderThread());
//				mapRenderThread.start();
//			}
		        
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2f(1, 1);
			gl.glVertex2f(50, 1);
			gl.glVertex2f(50, 50);
			gl.glVertex2f(1, 50);
			gl.glEnd();
			gl.glFlush();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {

		final GL2 gl = drawable.getGL().getGL2();
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		try{
			File im = new File("data/NeHE.png");
			Texture t = TextureIO.newTexture(im, true);
			texture= t.getTextureObject(gl);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		// TODO Auto-generated method stub
		final GL2 gl = drawable.getGL().getGL2();
		 
		if(height <=0)
			height =1;
		final float h = (float) width / (float) height;
		double dpiScalingFactor = ((Graphics2D)frame.getGraphics()).getTransform().getScaleX();
		width = (int) (width * dpiScalingFactor);
		height = (int) (height * dpiScalingFactor);
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, h, 1.0, 20.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// setUp open GL version 2
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);
		
		// The canvas 
		final GLCanvas glcanvas = new GLCanvas(capabilities);
		Test r = new Test();
		glcanvas.addGLEventListener(r);
		glcanvas.setSize(400, 400);
		
		final FPSAnimator animator = new FPSAnimator(glcanvas, 300,true );
		
		frame = new JFrame ("nehe: Lesson 6");
		
		frame.getContentPane().add(glcanvas);
		
		//Shutdown
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				if(animator.isStarted())
					animator.stop();
				System.exit(0);
			}
		});
		
		frame.setSize(frame.getContentPane().getPreferredSize());
		/**
		 * Centers the screen on start up
		 * 
		 */
		graphicsEnviorment = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] devices = graphicsEnviorment.getScreenDevices();

		dm_old = devices[0].getDisplayMode();
		dm = dm_old;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int windowX = Math.max(0, (screenSize.width - frame.getWidth()) / 2);
		int windowY = Math.max(0, (screenSize.height - frame.getHeight()) / 2);

		frame.setLocation(windowX, windowY);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		/*
		 * Time to add Button Control
		 */
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(0,0));
		frame.add(p, BorderLayout.SOUTH);
		
		keyBindings(p, frame, r);
		animator.start();
	}

	private static void keyBindings(JPanel p, final JFrame frame, final Test r) {
	
		ActionMap actionMap = p.getActionMap();
		InputMap inputMap = p.getInputMap();
		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "F1");
		actionMap.put("F1", new AbstractAction(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -6576101918414437189L;

			@Override
			public void actionPerformed(ActionEvent drawable) {
				// TODO Auto-generated method stub
				fullScreen(frame);
			}});
	}

	protected static void fullScreen(JFrame f) {
		// TODO Auto-generated method stub
		if(!isFullScreen){
			f.dispose();
			f.setUndecorated(true);
			f.setVisible(true);
			f.setResizable(false);
			xgraphic = f.getSize();
			point = f.getLocation();
			f.setLocation(0, 0);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			f.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
			isFullScreen=true;
		}else{
			f.dispose();
			f.setUndecorated(false);
			f.setResizable(true);
			f.setLocation(point);
			f.setSize(xgraphic);
			f.setVisible(true);
			
			isFullScreen =false;
		}
	


}

}