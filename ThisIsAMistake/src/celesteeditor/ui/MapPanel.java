package celesteeditor.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.text.TextAlignment;
import com.text.TextRenderer;

import celesteeditor.AtlasUnpacker;
import celesteeditor.Main;
import celesteeditor.data.Decal;
import celesteeditor.data.Entity;
import celesteeditor.data.Level;
import celesteeditor.data.ListLevelLayer;
import celesteeditor.data.TileLevelLayer;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.EntityConfig.VisualType;
import celesteeditor.ui.EditingPanel.EditPanel;
import celesteeditor.ui.autotiler.Autotiler.Behaviour;
import celesteeditor.util.Drawing;
import celesteeditor.util.TextureArea;
import celesteeditor.util.Util;

public class MapPanel implements GLEventListener {
	
	public enum LevelEdge {
		None, Left, Right, Top, Bottom
	}
		
	public Point offset = new Point(0, 0);
	
	private int zoom;
	
	private double actualZoom = 1;
	
	public boolean draggingEntity;
	
	public Entity selectedEntity;
	
	public Decal selectedDecal;
	
	public Thread mapRenderThread;
	
	public Lock lock = new ReentrantLock();
	
	// -1 = no node selected
	public int selectedNode = -1;
	
	public Level selectedLevel;
	
	public LevelEdge selectedEdge = LevelEdge.None;
	
	public int selectedEdgeOffset;
		
	public boolean firstDraw = true;
	
	public boolean redrawEverything = false;
	
	public boolean ctrlPressed;
	
	public boolean altPressed;
				
	public static String defaultEntityImgPath = "/assets/defaultentity.png";
	
	public static Texture defaultEntityTex;
	
	//getting the capabilities object of GL2 profile
	private final GLProfile profile;
    
	private final GLCapabilities capabilities;
    
	public final GLJPanel panel;
    
	private final GLU glu = new GLU();
    
	// TODO Clear on map reload to avoid taking up too much memory
    private HashMap<String, TextureRenderer> triggerTextCache = new HashMap<>();
	
	public MapPanel() {
		// OpenGL CAPABILITIES
        profile = GLProfile.get(GLProfile.GL2);
        capabilities = new GLCapabilities(profile);

        // CANVAS
        panel = new GLJPanel(capabilities);
        panel.addGLEventListener(this);

		
		MapMouseListener mouseListener = new MapMouseListener(this);
		panel.addMouseListener(mouseListener);
		panel.addMouseMotionListener(mouseListener);
		panel.addMouseWheelListener(mouseListener);
		
		panel.setBackground(new Color(61, 51, 51));
		
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, KeyEvent.CTRL_DOWN_MASK), "ctrlPressed");
		panel.getActionMap().put("ctrlPressed", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ctrlPressed = true;
			}});
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true), "ctrlReleased");
		panel.getActionMap().put("ctrlReleased", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ctrlPressed = false;
			}});
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, KeyEvent.ALT_DOWN_MASK), "altPressed");
		panel.getActionMap().put("altPressed", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				altPressed = true;
			}});
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), "altReleased");
		panel.getActionMap().put("altReleased", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				altPressed = false;
			}});
		
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false), "save");
		panel.getActionMap().put("save", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				try {
					Main.saveMap();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}});
		
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false), "open");
		panel.getActionMap().put("open", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				try {
					Main.openMap();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}});
		
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		panel.getActionMap().put("delete", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if(selectedEntity != null) {
					if(selectedNode < 0) {
						if(Main.entityConfig.containsKey(selectedEntity.name)) {
							selectedLevel.entities.items.remove(selectedEntity);
						} else {
							selectedLevel.triggers.items.remove(selectedEntity);
						}
						selectedEntity = null;
						selectedNode = -1;
					} else {
						selectedEntity.nodes.remove(selectedNode);
						selectedNode--;
					}
				} else if(selectedDecal != null) {
					selectedLevel.fgDecals.items.remove(selectedDecal);
					selectedLevel.bgDecals.items.remove(selectedDecal);
					selectedDecal = null;
				}
			}});
	}
	
	public void setZoom(int zoom) {
		this.zoom = zoom;
		actualZoom = getActualZoom(zoom);
	}
	
	public int getZoom() {
		return zoom;
	}
	
	public double getActualZoom() {
		return actualZoom;
	}
	
	public double getActualZoom(int zoomAmount) {
		if(zoomAmount < 0) {
			return Math.pow(1.01, -zoomAmount);
		} else if(zoom > 0) {
			return Math.pow(0.99, zoomAmount);
		}
		
		return 1;
	}
	
	private void createLevelFrameBuffer(GL2 gl, Level level) {
		if(level.frameBuffer == null) {
			level.frameBuffer = IntBuffer.allocate(1);
		}
		gl.glGenFramebuffers(1, level.frameBuffer);
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
		IntBuffer texID = IntBuffer.allocate(1);
		gl.glGenTextures(1, texID);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texID.get(0));
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, level.bounds.width, level.bounds.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
		level.roomTexture = new Texture(texID.get(0), GL.GL_TEXTURE_2D, level.bounds.width, level.bounds.height, level.bounds.width, level.bounds.height, false);
		level.roomTexture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		level.roomTexture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, level.roomTexture.getTextureObject(gl), 0);
		
		if(gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER) != GL.GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Error when initializing frame buffer");
		}
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
	}
	
	@Override
    public void display(GLAutoDrawable drawable) {
		final Rectangle orthoView = new Rectangle(0, 0, panel.getSize().width, panel.getSize().height);
        final GL2 gl = drawable.getGL().getGL2();
                
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        final int attribBits = GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL2.GL_TRANSFORM_BIT;
	    gl.glPushAttrib(attribBits);
	    gl.glDisable(GLLightingFunc.GL_LIGHTING);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(orthoView.x, orthoView.x + orthoView.width, orthoView.y, orthoView.y + orthoView.height);
	    
	    gl.glScaled(getActualZoom(), getActualZoom(), 1);
	    
	    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	    gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_MODULATE);
	    gl.glColor3f(1, 1, 1);
        
        if(Main.loadedMap != null) {
			for(Level level : Main.loadedMap.levels) {
				if(level.frameBuffer == null || level.roomTexture == null) {
					// Setup the framebuffer and texture for the room if necessary
					createLevelFrameBuffer(gl, level);
				}
				if(level != selectedLevel) {
					if(level.needsRedraw) {
						gl.glColor3f(1, 1, 1);
						// Save old viewing settings
						IntBuffer oldVP = IntBuffer.allocate(4);
						gl.glGetIntegerv(GL2.GL_VIEWPORT, oldVP);
						gl.glMatrixMode(GL2.GL_PROJECTION);
						FloatBuffer oldMat = FloatBuffer.allocate(16);
						gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
						
						// Setup to draw to the framebuffer
						gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));

						gl.glBindTexture(GL.GL_TEXTURE_2D, level.roomTexture.getTextureObject());
						gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, level.bounds.width, level.bounds.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
						Drawing.unbindTexture(gl, GL.GL_TEXTURE_2D);
						
						gl.glLoadIdentity();
						gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
						glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
						
						// Actually draw stuff
						drawTiles(gl, level, false);
						drawDecals(gl, level, false);
						drawEntities(gl, level);
						drawTiles(gl, level, true);
						drawDecals(gl, level, true);
						drawTriggers(gl, level);
						
						// Revert everything
						gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
						gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
						gl.glLoadMatrixf(oldMat);
						level.needsRedraw = false;
					}
			        Drawing.drawTexture(gl, level.roomTexture, offset.x + level.bounds.x, -offset.y - level.bounds.y - level.bounds.height,
			        		0, 0, level.bounds.width, level.bounds.height, 1);
				} else {
					level.needsRedraw = true;
				}
			}
			drawFillers(gl);
			drawTiles(gl, false);
			drawDecals(gl, false);
			drawEntities(gl);
			drawTiles(gl, true);
			drawDecals(gl, true);
			drawTriggers(gl);
			drawRooms(gl);
			
			FloatBuffer oldMat = FloatBuffer.allocate(16);
			gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
			if(selectedLevel != null) {
				gl.glTranslatef(selectedLevel.bounds.x + offset.x, -selectedLevel.bounds.y - offset.y - selectedLevel.bounds.height, 0);
				drawSelectionBox(gl);
			}
			gl.glLoadMatrixf(oldMat);
			
			firstDraw = false;
			gl.glFlush();
		}
    }

	private void drawFillers(GL2 gl) {
		gl.glColor3f(0, 200/255f, 0);
		gl.glBegin(GL2.GL_QUADS);
		for(Rectangle r : Main.loadedMap.filler) {
			Point pos = getOpenGLPosition(r, true);
			gl.glVertex2f(pos.x, pos.y);
			gl.glVertex2f(pos.x + r.width * 8, pos.y);
			gl.glVertex2f(pos.x + r.width * 8, pos.y + r.height * 8);
			gl.glVertex2f(pos.x, pos.y + r.height * 8);
		}
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glColor3f(0, 0, 0);
		gl.glBegin(GL2.GL_QUADS);
		for(Rectangle r : Main.loadedMap.filler) {
			Point pos = getOpenGLPosition(r, true);
			gl.glVertex2f(pos.x, pos.y);
			gl.glVertex2f(pos.x + r.width * 8, pos.y);
			gl.glVertex2f(pos.x + r.width * 8, pos.y + r.height * 8);
			gl.glVertex2f(pos.x, pos.y + r.height * 8);
		}
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public void drawTiles(GL2 gl, boolean fg) {
		gl.glColor3f(1, 1, 1);
		IntBuffer oldVP = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldVP);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		// I was using glPushMatrix and glPopMatrix here, rather than glGetFloatv and glLoadMatrix,
		// but for whatever reason the matrix changed after the two commands executed, but only if the window had been resized
		// No idea why that happened, I suspect an issue with JOGL rather than OpenGL
		FloatBuffer oldMat = FloatBuffer.allocate(16);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
		for(Level level : Main.loadedMap.levels) {
			if(level != selectedLevel && (firstDraw || redrawEverything)) {
				gl.glLoadIdentity();
				gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
				glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
				drawTiles(gl, level, fg);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glLoadMatrixf(oldMat);
		if(selectedLevel != null) {
			gl.glTranslatef(selectedLevel.bounds.x + offset.x, -selectedLevel.bounds.y - offset.y - selectedLevel.bounds.height, 0);
			drawTiles(gl, selectedLevel, fg);
		}
		gl.glLoadMatrixf(oldMat);
	}
	
	public void drawTiles(GL2 gl, Level level, boolean fg) {
		Main.bgAutotiler.rand = new Random(level.name.hashCode());
		Main.fgAutotiler.rand = new Random(level.name.hashCode());
		
		TileLevelLayer tiles = fg ? level.solids : level.bg;
		tiles.tileImgs = (fg ? Main.fgAutotiler : Main.bgAutotiler).generateMap(tiles, false).tileImg;
		for(int i = 0; i < tiles.tileImgs.length; i++) {
			for(int j = 0; j < tiles.tileImgs[i].length; j++) {
				if(tiles.tileImgs[i][j] != null) {
					Drawing.drawTexture(gl, tiles.tileImgs[i][j].texture, j * 8, level.bounds.height - i * 8 - 8,
							tiles.tileImgs[i][j].area.x, tiles.tileImgs[i][j].area.y, tiles.tileImgs[i][j].area.width, tiles.tileImgs[i][j].area.height, 1);
				}
			}
		}
	}
	
	public void drawDecals(GL2 gl, boolean fg) {
		gl.glColor3f(1, 1, 1);
		IntBuffer oldVP = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldVP);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		// I was using glPushMatrix and glPopMatrix here, rather than glGetFloatv and glLoadMatrix,
		// but for whatever reason the matrix changed after the two commands executed, but only if the window had been resized
		// No idea why that happened, I suspect an issue with JOGL rather than OpenGL
		FloatBuffer oldMat = FloatBuffer.allocate(16);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
		for(Level level : Main.loadedMap.levels) {
			if(level != selectedLevel && (firstDraw || redrawEverything)) {
				gl.glLoadIdentity();
				gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
				glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
				drawDecals(gl, level, fg);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glLoadMatrixf(oldMat);
		if(selectedLevel != null) {
			gl.glTranslatef(selectedLevel.bounds.x + offset.x, -selectedLevel.bounds.y - offset.y - selectedLevel.bounds.height, 0);
			drawDecals(gl, selectedLevel, fg);
		}
		gl.glLoadMatrixf(oldMat);
	}
	
	public void drawDecals(GL2 gl, Level level, boolean fg) {
		ListLevelLayer decals = (fg ? level.fgDecals : level.bgDecals);
		if(decals != null) {
			for(int i = 0; i < decals.items.size(); i++) {
				Decal d = (Decal)decals.items.get(i);
				TextureArea texArea = d.getTextureArea();
				
				// Convert from coordinates for the center of the image (with padding), to coordinates for the bottom left of the image (without padding) 
				Point p = new Point(d.x + texArea.offsetX * d.scaleX - texArea.width / 2 * d.scaleX, level.bounds.height - (d.y + texArea.offsetY * d.scaleY + texArea.area.height * d.scaleY - texArea.height / 2 * d.scaleY));
				Drawing.drawTexture(gl, texArea.texture, p.x, p.y,
		        		texArea.area.x, texArea.area.y, texArea.area.width, texArea.area.height, d.scaleX, d.scaleY);
			}
		}
	}
	
	public void drawEntities(GL2 gl) {
		gl.glColor3f(1, 1, 1);
		IntBuffer oldVP = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldVP);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		// I was using glPushMatrix and glPopMatrix here, rather than glGetFloatv and glLoadMatrix,
		// but for whatever reason the matrix changed after the two commands executed, but only if the window had been resized
		// No idea why that happened, I suspect an issue with JOGL rather than OpenGL
		FloatBuffer oldMat = FloatBuffer.allocate(16);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
		for(Level level : Main.loadedMap.levels) {
			if(level != selectedLevel && (firstDraw || redrawEverything)) {
				gl.glLoadIdentity();
				gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
				glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
				drawEntities(gl, level);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glLoadMatrixf(oldMat);
		if(selectedLevel != null) {
			gl.glTranslatef(selectedLevel.bounds.x + offset.x, -selectedLevel.bounds.y - offset.y - selectedLevel.bounds.height, 0);
			drawEntities(gl, selectedLevel);
		}
		gl.glLoadMatrixf(oldMat);
	}
	
	public void drawEntities(GL2 gl, Level level) {
		for(int i = 0; i < level.entities.items.size(); i++) {
			Entity e = (Entity)level.entities.items.get(i);
			EntityConfig ec = Main.entityConfig.get(e.name);
			if(ec == null) {
				ec = new EntityConfig();
				ec.name = e.name;
				ec.setTexture(defaultEntityImgPath);
				Main.entityConfig.put(e.name, ec);
			}
			
			drawEntity(gl, level, e, ec);
			
			if(selectedEntity == e && e.nodes.size() != 0) {
				gl.glColor4f(1, 1, 1, 0.6f);
				for(Point n : e.nodes) {
					drawEntity(gl, level, n.x, n.y, e, ec, 0.6f);
				}
				gl.glColor4f(1, 1, 1, 0);
			}
		}
	}
	
	private void drawEntity(GL2 gl, Level level, Entity e, EntityConfig ec) {
		drawEntity(gl, level, e.x, e.y, e, ec, 0.6f);
	}
	
	private void drawEntity(GL2 gl, Level level, int x, int y, Entity e, EntityConfig ec, float alpha) {
		if(ec.visualType == VisualType.Image) {
			TextureArea texArea = ec.getTextureArea();
			Point p = new Point(e.x - ec.imgOffsetX + texArea.offsetX, level.bounds.height - (e.y - ec.imgOffsetY + texArea.offsetY + texArea.area.height));
			Drawing.drawTexture(gl, texArea.texture, p.x, p.y,
	        		texArea.area.x, texArea.area.y, texArea.area.width, texArea.area.height, 1);
		} else if(ec.visualType == VisualType.Box) {
			Rectangle eBounds = new Rectangle(e.x, level.bounds.height - e.y, (int)e.getProperty("width").value, (int)e.getProperty("height").value);
			gl.glColor4f(ec.fillColor.getRed()/255f, ec.fillColor.getGreen()/255f, ec.fillColor.getBlue()/255f, alpha);
			
			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2f(eBounds.x, eBounds.y);
			gl.glVertex2f(eBounds.x + eBounds.width, eBounds.y);
			gl.glVertex2f(eBounds.x + eBounds.width, eBounds.y - eBounds.height);
			gl.glVertex2f(eBounds.x, eBounds.y - eBounds.height);
			gl.glEnd();
			
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			gl.glColor4f(ec.borderColor.getRed()/255f, ec.borderColor.getGreen()/255f, ec.borderColor.getBlue()/255f, alpha);
			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2f(eBounds.x, eBounds.y);
			gl.glVertex2f(eBounds.x + eBounds.width, eBounds.y);
			gl.glVertex2f(eBounds.x + eBounds.width, eBounds.y - eBounds.height);
			gl.glVertex2f(eBounds.x, eBounds.y - eBounds.height);
			gl.glEnd();
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
			gl.glColor4f(1, 1, 1, alpha);
		} else if(ec.visualType == VisualType.ImageBox) {
			int width = (int)e.getPropertyValue("width", 8);
			int height = (int)e.getPropertyValue("height", 8);
			
			TextureArea texArea = ec.getTextureArea();
			int repX = Math.round(width / (float) texArea.width);
			int repY = Math.round(height / (float) texArea.height);
			
			Point p = new Point(e.x - ec.imgOffsetX + texArea.offsetX, level.bounds.height - (e.y - ec.imgOffsetY + texArea.offsetY + texArea.area.height));
			for(int j = 0; j < repY; j++) {
				for(int k = 0; k < repX; k++) {
					Drawing.drawTexture(gl, texArea.texture, p.x + k * texArea.width, p.y - j * texArea.height,
			        		texArea.area.x, texArea.area.y, texArea.area.width, texArea.area.height, 1);
				}
			}
		}
	}
	
	public void drawTriggers(GL2 gl) {
		gl.glColor3f(1, 1, 1);
		IntBuffer oldVP = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldVP);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		// I was using glPushMatrix and glPopMatrix here, rather than glGetFloatv and glLoadMatrix,
		// but for whatever reason the matrix changed after the two commands executed, but only if the window had been resized
		// No idea why that happened, I suspect an issue with JOGL rather than OpenGL
		FloatBuffer oldMat = FloatBuffer.allocate(16);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
		for(Level level : Main.loadedMap.levels) {
			if(level != selectedLevel && (firstDraw || redrawEverything)) {
				gl.glLoadIdentity();
				gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
				glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
				drawTriggers(gl, level);
				gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glLoadMatrixf(oldMat);
		if(selectedLevel != null) {
			gl.glTranslatef(selectedLevel.bounds.x + offset.x, -selectedLevel.bounds.y - offset.y - selectedLevel.bounds.height, 0);
			drawTriggers(gl, selectedLevel);
		}
		gl.glLoadMatrixf(oldMat);
	}
	
	public void drawTriggers(GL2 gl, Level level) {
		for(int i = 0; i < level.triggers.items.size(); i++) {
			Entity e = (Entity)level.triggers.items.get(i);
			drawTrigger(gl, level, e);
			
			if(selectedEntity == e && e.nodes.size() != 0) {
				for(Point n : e.nodes) {
					drawTrigger(gl, level, n.x, n.y, e, 0.6f);
				}
			}
		}
	}
	
	private void drawTrigger(GL2 gl, Level level, Entity e) {
		drawTrigger(gl, level, e.x, e.y, e, 1);
	}
	
	private void drawTrigger(GL2 gl, Level level, int x, int y, Entity e, float alpha) {
		Font font = new Font("Dialog", Font.PLAIN, 9);
		Rectangle triggerBounds = new Rectangle(x, level.bounds.height - y, (int)e.getProperty("width").value, (int)e.getProperty("height").value);
		gl.glColor4f(200/255f, 0, 0, 100/255f * alpha);
		
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y - triggerBounds.height);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y - triggerBounds.height);
		gl.glEnd();
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		FloatBuffer oldMat = FloatBuffer.allocate(16);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
		
		String cacheCode = e.name + ":" + triggerBounds.width + ":" + triggerBounds.height;
		if(!triggerTextCache.containsKey(cacheCode)) {
			TextureRenderer cache = new TextureRenderer(triggerBounds.width, triggerBounds.height, true);
			Rectangle textBounds = new Rectangle(0, 0, triggerBounds.width, triggerBounds.height);
			Graphics2D g2d = cache.createGraphics();
			TextRenderer.drawString(g2d, e.name, font, Color.black, textBounds, TextAlignment.MIDDLE);
			triggerTextCache.put(cacheCode, cache);
		}
		
		Texture tex = triggerTextCache.get(cacheCode).getTexture();
		gl.glColor4f(1, 1, 1, alpha);
		Drawing.drawTexture(gl, tex, triggerBounds.x, triggerBounds.y - triggerBounds.height,
				0, 0, tex.getWidth(), tex.getHeight(), 1);
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(oldMat);
		
		gl.glColor4f(.25f, .25f, .25f, alpha); // Color.darkGray
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y - triggerBounds.height);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y - triggerBounds.height);
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		gl.glColor4f(1, 1, 1, 1);
	}
	
	public void drawRooms(GL2 gl) {
		gl.glColor4f(0, 0, 0, 120/255f);
		gl.glBegin(GL2.GL_QUADS);
		for(Level level : Main.loadedMap.levels) {
			if(level != selectedLevel) {
				Point pos = getOpenGLPosition(level.bounds);
				gl.glVertex2f(pos.x, pos.y);
				gl.glVertex2f(pos.x + level.bounds.width, pos.y);
				gl.glVertex2f(pos.x + level.bounds.width, pos.y + level.bounds.height);
				gl.glVertex2f(pos.x, pos.y + level.bounds.height);
			}
		}
		gl.glEnd();
		gl.glColor3f(1, 1, 1);
		if(selectedLevel != null && Main.editingPanel.tiles.selectedTileTool != null && Main.editingPanel.getCurrentPanel() == EditPanel.Tiles && Main.editingPanel.tiles.selectedTiletype != null && !Main.editingPanel.tiles.selectedTiletype.name.equalsIgnoreCase("air")) {
			// TODO draw entity preview
			// Draw brush preview
			char[][] tileOverlay = Main.editingPanel.tiles.selectedTileTool.getTileOverlay(Main.editingPanel.tiles.selectedTiletype.ID);
			Point tileOverlayPos = Main.editingPanel.tiles.selectedTileTool.getTileOverlayPos();
			if(tileOverlay != null && tileOverlayPos != null) {
				FloatBuffer oldMat = FloatBuffer.allocate(16);
				gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, oldMat);
				gl.glTranslatef(selectedLevel.bounds.x + offset.x, -selectedLevel.bounds.y - offset.y - selectedLevel.bounds.height, 0);
				
				TileLevelLayer layer = new TileLevelLayer(tileOverlay.length == 0 ? 0 : tileOverlay[0].length, tileOverlay.length);
				layer.tileMap = tileOverlay;
				TextureArea[][] overlayImages = (Main.editingPanel.tiles.selectedFg ? Main.fgAutotiler : Main.bgAutotiler).generateMap(layer, new Behaviour()).tileImg;
				for(int i = 0; i < overlayImages.length; i++) {
					for(int j = 0; j < overlayImages[i].length; j++) {
						if(overlayImages[i][j] != null && i + tileOverlayPos.y >= 0 && j + tileOverlayPos.x >= 0 && i + tileOverlayPos.y < selectedLevel.bounds.height / 8 && j + tileOverlayPos.x < selectedLevel.bounds.width / 8) {
							Drawing.drawTexture(gl, overlayImages[i][j].texture, (j + tileOverlayPos.x) * 8, selectedLevel.bounds.height - (i + tileOverlayPos.y) * 8 - 8,
									overlayImages[i][j].area.x, overlayImages[i][j].area.y, overlayImages[i][j].area.width, overlayImages[i][j].area.height, 1);
						}
					}
				}
				Drawing.unbindTexture(gl, GL2.GL_TEXTURE_2D);
				gl.glLoadMatrixf(oldMat);
			}
		}
		gl.glColor3f(0, 0, 0);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glBegin(GL2.GL_QUADS);
		for(Level level : Main.loadedMap.levels) {
			Point pos = getOpenGLPosition(level.bounds);
			gl.glVertex2f(pos.x, pos.y);
			gl.glVertex2f(pos.x + level.bounds.width, pos.y);
			gl.glVertex2f(pos.x + level.bounds.width, pos.y + level.bounds.height);
			gl.glVertex2f(pos.x, pos.y + level.bounds.height);
		}
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		if(selectedLevel != null) {
			gl.glBegin(GL2.GL_LINES);
			Point pos = getOpenGLPosition(selectedLevel.bounds);
			switch(selectedEdge) {
			case Left:
				gl.glVertex2f(pos.x + selectedEdgeOffset * 8, pos.y);
				gl.glVertex2f(pos.x + selectedEdgeOffset * 8, pos.y + selectedLevel.bounds.height);
				break;
			case Right:
				gl.glVertex2f(pos.x + selectedLevel.bounds.width + selectedEdgeOffset * 8, pos.y);
				gl.glVertex2f(pos.x + selectedLevel.bounds.width + selectedEdgeOffset * 8, pos.y + selectedLevel.bounds.height);
				break;
			case Top:
				gl.glVertex2f(pos.x, pos.y + selectedLevel.bounds.height - selectedEdgeOffset * 8);
				gl.glVertex2f(pos.x + selectedLevel.bounds.width, pos.y + selectedLevel.bounds.height - selectedEdgeOffset * 8);
				break;
			case Bottom:
				gl.glVertex2f(pos.x, pos.y - selectedEdgeOffset * 8);
				gl.glVertex2f(pos.x + selectedLevel.bounds.width, pos.y - selectedEdgeOffset * 8);
				break;
			}
			gl.glEnd();
		}
	}
	
	public void drawSelectionBox(GL2 gl) {
		gl.glColor3f(1, 0, 0);
		gl.glLineWidth(4);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glBegin(GL2.GL_QUADS);
		if(selectedEntity != null) {
			Rectangle bounds = selectedEntity.getBounds(null, selectedNode);
			bounds.y = selectedLevel.bounds.height - bounds.y;
			
			gl.glVertex2f(bounds.x, bounds.y);
			gl.glVertex2f(bounds.x + bounds.width, bounds.y);
			gl.glVertex2f(bounds.x + bounds.width, bounds.y - bounds.height);
			gl.glVertex2f(bounds.x, bounds.y - bounds.height);
		} else if(selectedDecal != null) {
			int x = selectedDecal.x;
			int y = selectedLevel.bounds.height - selectedDecal.y;
			int width = selectedDecal.getTextureArea().width;
			int height = selectedDecal.getTextureArea().height;
			
			Rectangle bounds = new Rectangle(x - width / 2 * Math.abs(selectedDecal.scaleX), y - height / 2 * Math.abs(selectedDecal.scaleY), width, height);
			
			gl.glVertex2f(bounds.x, bounds.y);
			gl.glVertex2f(bounds.x + bounds.width, bounds.y);
			gl.glVertex2f(bounds.x + bounds.width, bounds.y + bounds.height);
			gl.glVertex2f(bounds.x, bounds.y + bounds.height);
		}
		gl.glEnd();
		gl.glColor3f(1, 1, 1);
		gl.glLineWidth(1);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public Point getOpenGLPosition(Rectangle rect) {
		return getOpenGLPosition(rect, false);
	}
	
	public Point getOpenGLPosition(Rectangle rect, boolean tilePos) {
		if(tilePos) {
			return new Point(offset.x + rect.x * 8, -offset.y - rect.y * 8 - rect.height * 8);
		}
		return new Point(offset.x + rect.x, -offset.y - rect.y - rect.height);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		defaultEntityTex = Util.getTexture(defaultEntityImgPath);
		AtlasUnpacker.loadAtlases();
		
		Decal.loadDecalsFromAtlas();
		Main.editingPanel.placements.refreshLists();
		
		try {
			Main.setupAutotilers();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Main.editingPanel.tiles.init();
		
		if(Main.loadedMap != null) {
			for(Level level : Main.loadedMap.levels) {
				// Setup the framebuffer and texture for the room if necessary
				createLevelFrameBuffer(gl, level);
			}
		}
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0.24f, 0.2f, 0.2f, 0f);
		gl.glClearDepth(1.0f);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		gl.glEnable(GL2.GL_TEXTURE_2D);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		
		if(height <= 0)
			height = 1;
		glViewportDPIAware(gl, 0, 0, width, height);
////		final float h = (float) width / (float) height;
//		gl.glMatrixMode(GL2.GL_PROJECTION);
//		gl.glLoadIdentity();
//		gl.glOrthof(-1, 1, -1, 1, 5, 100);
////		glu.gluPerspective(45.0f, h, 1.0, 20.0);
//		gl.glMatrixMode(GL2.GL_MODELVIEW);
//		gl.glLoadIdentity();
	}
	
	private void glViewportDPIAware(GL2 gl, int x, int y, int width, int height) {
		double dpiScalingFactor = ((Graphics2D)panel.getGraphics()).getTransform().getScaleX();
		width = (int) (width * dpiScalingFactor);
		height = (int) (height * dpiScalingFactor);
		gl.glViewport(x, y, width, height);
	}
	
}
