package celesteeditor.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.text.TextAlignment;

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

public class MapPanel extends JPanel implements GLEventListener {
	
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
	
	public boolean renderingComplete;
	
	public boolean firstDraw = true;
	
	public boolean redrawEverything = false;
	
	public boolean ctrlPressed;
	
	public boolean altPressed;
				
	public static String defaultEntityImgPath;
		
	public static BufferedImage defaultEntityImg;
	
	public static Texture defaultEntityTex;
	
	//getting the capabilities object of GL2 profile
	private final GLProfile profile;
    
	private final GLCapabilities capabilities;
    
	public final GLCanvas glcanvas;
    
	private final GLU glu = new GLU();
    
    private TextRenderer textRenderer;
	
	static {
		defaultEntityImgPath = "/assets/defaultentity.png";
		defaultEntityImg = Util.getImage(defaultEntityImgPath);
	}
	
	public MapPanel() {
		// OpenGL CAPABILITIES
        profile = GLProfile.get(GLProfile.GL2);
        capabilities = new GLCapabilities(profile);

        // CANVAS
        glcanvas = new GLCanvas(capabilities);
        glcanvas.addGLEventListener(this);
        glcanvas.setSize(400, 400);

        add(glcanvas);
		
		MapMouseListener mouseListener = new MapMouseListener(this);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);
		
		setBackground(new Color(61, 51, 51));
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, KeyEvent.CTRL_DOWN_MASK), "ctrlPressed");
		getActionMap().put("ctrlPressed", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ctrlPressed = true;
			}});
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true), "ctrlReleased");
		getActionMap().put("ctrlReleased", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ctrlPressed = false;
			}});
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, KeyEvent.ALT_DOWN_MASK), "altPressed");
		getActionMap().put("altPressed", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				altPressed = true;
			}});
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), "altReleased");
		getActionMap().put("altReleased", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				altPressed = false;
			}});
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false), "save");
		getActionMap().put("save", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				try {
					Main.saveMap();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}});
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false), "open");
		getActionMap().put("open", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				try {
					Main.openMap();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}});
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		getActionMap().put("delete", new AbstractAction() {
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
		final Rectangle orthoView = new Rectangle(0, 0, 800, 800);
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
	    glu.gluOrtho2D(orthoView.x, orthoView.x + orthoView.width, orthoView.y, orthoView.y + orthoView.height);
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
		        Drawing.drawTexture(gl, level.roomTexture, offset.x + level.bounds.x, -offset.y - level.bounds.y - level.bounds.height,
		        		0, 0, level.roomTexture.getWidth(), level.roomTexture.getHeight(), (float)actualZoom);
			}
			
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
			drawFillers(gl);
			drawTiles(gl, false);
			drawDecals(gl, false);
			drawEntities(gl);
			drawTiles(gl, true);
			drawDecals(gl, true);
			drawTriggers(gl);
			drawRooms(gl);
//			drawSelectionBox(g);
			
			firstDraw = false;
			gl.glFlush();
		}
		
		//renderingComplete = true;
        
        
    }

	private void drawFillers(GL2 gl) {
		gl.glColor3f(0, 200/255f, 0);
		gl.glBegin(GL2.GL_QUADS);
		for(Rectangle r : Main.loadedMap.filler) {
			Point pos = getOpenGLPosition(r, true);
			gl.glVertex2f(pos.x, pos.y);
			gl.glVertex2f(pos.x + r.width * 8 * (float)actualZoom, pos.y);
			gl.glVertex2f(pos.x + r.width * 8 * (float)actualZoom, pos.y + r.height * 8 * (float)actualZoom);
			gl.glVertex2f(pos.x, pos.y + r.height * 8 * (float)actualZoom);
		}
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
		gl.glColor3f(0, 0, 0);
		gl.glBegin(GL2.GL_QUADS);
		for(Rectangle r : Main.loadedMap.filler) {
			Point pos = getOpenGLPosition(r, true);
			gl.glVertex2f(pos.x, pos.y);
			gl.glVertex2f(pos.x + r.width * 8 * (float)actualZoom, pos.y);
			gl.glVertex2f(pos.x + r.width * 8 * (float)actualZoom, pos.y + r.height * 8 * (float)actualZoom);
			gl.glVertex2f(pos.x, pos.y + r.height * 8 * (float)actualZoom);
		}
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
	}
	
	public void drawTiles(GL2 gl, boolean fg) {
		gl.glColor3f(1, 1, 1);
		IntBuffer oldVP = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldVP);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					
					gl.glLoadIdentity();
					gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
					glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
					drawTiles(gl, level, fg);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
				}
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glPopMatrix();
		// TODO Draw the selected level every frame
	}
	
	public void drawTiles(GL2 gl, Level level, boolean fg) {
		Main.bgAutotiler.rand = new Random(level.name.hashCode());
		Main.fgAutotiler.rand = new Random(level.name.hashCode());
		
		TileLevelLayer tiles = fg ? level.solids : level.bg;
		if(tiles.img == null) {
			tiles.img = new BufferedImage(level.bounds.width, level.bounds.height, BufferedImage.TYPE_INT_ARGB);
			Graphics imgG = tiles.img.createGraphics();
			tiles.tileImgs = (fg ? Main.fgAutotiler : Main.bgAutotiler).generateMap(tiles, true).tileImg;
			for(int i = 0; i < tiles.tileImgs.length; i++) {
				for(int j = 0; j < tiles.tileImgs[i].length; j++) {
					imgG.drawImage(tiles.tileImgs[i][j], j * 8, i * 8, null);
				}
			}
		}
		
		TextureRenderer texRender = new TextureRenderer(level.bounds.width, level.bounds.height, true);
		texRender.createGraphics().drawImage(tiles.img, 0, 0, null);
		Drawing.drawTexture(gl, texRender.getTexture(), 0, 0,
        		0, 0, level.bounds.width, level.bounds.height, 1);
	}
	
	public void drawDecals(GL2 gl, boolean fg) {
		gl.glColor3f(1, 1, 1);
		IntBuffer oldVP = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldVP);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					
					gl.glLoadIdentity();
					gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
					glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
					drawDecals(gl, level, fg);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
				}
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glPopMatrix();
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
		gl.glPushMatrix();
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					gl.glLoadIdentity();
					gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
					glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
					drawEntities(gl, level);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
				}
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glPopMatrix();
	}
	
	public void drawEntities(GL2 gl, Level level) {
		for(int i = 0; i < level.entities.items.size(); i++) {
			Entity e = (Entity)level.entities.items.get(i);
			EntityConfig ec = Main.entityConfig.get(e.name);
			if(ec == null) {
				ec = new EntityConfig();
				ec.name = e.name;
				ec.setImage(defaultEntityImg);
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
			
			gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
			gl.glColor4f(ec.borderColor.getRed()/255f, ec.borderColor.getGreen()/255f, ec.borderColor.getBlue()/255f, alpha);
			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2f(eBounds.x, eBounds.y);
			gl.glVertex2f(eBounds.x + eBounds.width, eBounds.y);
			gl.glVertex2f(eBounds.x + eBounds.width, eBounds.y - eBounds.height);
			gl.glVertex2f(eBounds.x, eBounds.y - eBounds.height);
			gl.glEnd();
			gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
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
		gl.glPushMatrix();
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					gl.glLoadIdentity();
					gl.glViewport(0, 0, level.bounds.width, level.bounds.height);
					glu.gluOrtho2D(0, level.bounds.width, 0, level.bounds.height);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, level.frameBuffer.get(0));
					drawTriggers(gl, level);
					gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
				}
			}
		}
		gl.glViewport(oldVP.get(), oldVP.get(), oldVP.get(), oldVP.get());
		gl.glPopMatrix();
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
		Rectangle triggerBounds = new Rectangle(x, level.bounds.height - y, (int)e.getProperty("width").value, (int)e.getProperty("height").value);
		gl.glColor4f(200/255f, 0, 0, 100/255f * alpha);
		
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y - triggerBounds.height);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y - triggerBounds.height);
		gl.glEnd();
		
		textRenderer.beginRendering(glcanvas.getWidth(), glcanvas.getHeight());
		textRenderer.setColor(0, 0, 0, alpha);
		Rectangle2D textBounds = textRenderer.getBounds(e.name);
		textRenderer.draw(e.name, (int)(triggerBounds.x + triggerBounds.width / 2 - textBounds.getWidth() / 2), (int)(triggerBounds.y + triggerBounds.height / 2 - textBounds.getHeight() / 2));
		textRenderer.endRendering();
		//TextRenderer.drawString(g2d, e.name, font, g2d.getColor(), triggerBounds, TextAlignment.MIDDLE);
		
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
		gl.glColor4f(.25f, .25f, .25f, alpha); // Color.darkGray
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y);
		gl.glVertex2f(triggerBounds.x + triggerBounds.width, triggerBounds.y - triggerBounds.height);
		gl.glVertex2f(triggerBounds.x, triggerBounds.y - triggerBounds.height);
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glColor4f(1, 1, 1, 1);
	}
	
	public void drawRooms(GL2 gl) {
		gl.glColor4f(0, 0, 0, 120/255f);
		gl.glBegin(GL2.GL_QUADS);
		for(Level level : Main.loadedMap.levels) {
			if(level != selectedLevel) {
				Point pos = getOpenGLPosition(level.bounds);
				gl.glVertex2f(pos.x, pos.y);
				gl.glVertex2f(pos.x + level.bounds.width * (float)actualZoom, pos.y);
				gl.glVertex2f(pos.x + level.bounds.width * (float)actualZoom, pos.y + level.bounds.height * (float)actualZoom);
				gl.glVertex2f(pos.x, pos.y + level.bounds.height * (float)actualZoom);
			} else if(Main.editingPanel.tiles.selectedTileTool != null && Main.editingPanel.getCurrentPanel() == EditPanel.Tiles && Main.editingPanel.tiles.selectedTiletype != null && !Main.editingPanel.tiles.selectedTiletype.name.equalsIgnoreCase("air")) {
				// TODO draw entity preview
				// Draw brush preview
				/*char[][] tileOverlay = Main.editingPanel.tiles.selectedTileTool.getTileOverlay(Main.editingPanel.tiles.selectedTiletype.ID);
				Point tileOverlayPos = Main.editingPanel.tiles.selectedTileTool.getTileOverlayPos();
				if(tileOverlay != null && tileOverlayPos != null) {
					TileLevelLayer layer = new TileLevelLayer(tileOverlay.length == 0 ? 0 : tileOverlay[0].length, tileOverlay.length);
					layer.tileMap = tileOverlay;
					BufferedImage[][] overlayImages = (Main.editingPanel.tiles.selectedFg ? Main.fgAutotiler : Main.bgAutotiler).generateMap(layer, new Behaviour()).tileImg;
					for(int i = 0; i < overlayImages.length; i++) {
						for(int j = 0; j < overlayImages[i].length; j++) {
							if(overlayImages[i][j] != null && i + tileOverlayPos.y >= 0 && j + tileOverlayPos.x >= 0 && i + tileOverlayPos.y < level.bounds.height / 8 && j + tileOverlayPos.x < level.bounds.width / 8) {
								g.drawImage(overlayImages[i][j], level.bounds.x + (j + tileOverlayPos.x) * 8, level.bounds.y + (i + tileOverlayPos.y) * 8, 8, 8, null);
							}
						}
					}
					g.setColor(Color.black);
				}*/
			}
		}
		gl.glEnd();
		gl.glColor3f(0, 0, 0);
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
		gl.glBegin(GL2.GL_QUADS);
		for(Level level : Main.loadedMap.levels) {
			Point pos = getOpenGLPosition(level.bounds);
			gl.glVertex2f(pos.x, pos.y);
			gl.glVertex2f(pos.x + level.bounds.width * (float)actualZoom, pos.y);
			gl.glVertex2f(pos.x + level.bounds.width * (float)actualZoom, pos.y + level.bounds.height * (float)actualZoom);
			gl.glVertex2f(pos.x, pos.y + level.bounds.height * (float)actualZoom);
		}
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
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
				gl.glVertex2f(pos.x, pos.y + selectedEdgeOffset * 8);
				gl.glVertex2f(pos.x + selectedLevel.bounds.width, pos.y + selectedEdgeOffset * 8);
				break;
			case Bottom:
				gl.glVertex2f(pos.x, pos.y + selectedLevel.bounds.height + selectedEdgeOffset * 8);
				gl.glVertex2f(pos.x + selectedLevel.bounds.width, pos.y + selectedLevel.bounds.height + selectedEdgeOffset * 8);
				break;
			}
			gl.glEnd();
		}
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
		Font font = new Font("Dialog", Font.PLAIN, 9);
		System.out.println(font);
		textRenderer = new TextRenderer(font);
		defaultEntityTex = Util.getTexture(defaultEntityImgPath);
		AtlasUnpacker.loadAtlases();
		
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
		
		repaint();
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
		double dpiScalingFactor = ((Graphics2D)getGraphics()).getTransform().getScaleX();
		width = (int) (width * dpiScalingFactor);
		height = (int) (height * dpiScalingFactor);
		gl.glViewport(x, y, width, height);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		/*renderingComplete = false;
		
		((Graphics2D)g).scale(actualZoom, actualZoom);
		g.translate(offset.x, offset.y);
		
		if(Main.loadedMap != null) {
			lock.lock();
			for(Level level : Main.loadedMap.levels) {
				if(level.frameBuffer == null) {
					//level.roomCanvas = new TextureRenderer(level.bounds.width, level.bounds.height, true);//new BufferedImage(level.bounds.width, level.bounds.height, BufferedImage.TYPE_INT_ARGB);
					return;
				}
				g.drawImage(level.frameBuffer.getImage(), level.bounds.x, level.bounds.y, null);
			}
			
			if(mapRenderThread == null) {
				mapRenderThread = new Thread(new MapRenderThread());
				mapRenderThread.start();
			}
			long start = System.currentTimeMillis();
			drawFillers(g);
			//System.out.println("Fillers: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawTiles(g, false);
			//System.out.println("Bg Tiles: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawDecals(g, false);
			//System.out.println("Bg Decals: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawEntities(g);
			//System.out.println("Entities: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawTiles(g, true);
			//System.out.println("Fg Tiles: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawDecals(g, true);
			//System.out.println("Fg Decals: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawTriggers(g);
			//System.out.println("Triggers: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawRooms(g);
			//System.out.println("Rooms: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			drawSelectionBox(g);
			//System.out.println("Selection Box: " + (System.currentTimeMillis() - start));
			
			firstDraw = false;
			lock.unlock();
		}
		
		renderingComplete = true;*/
	}
	
	public void drawFillers(Graphics g) {
		Color fillColor = new Color(0, 200, 0);
		for(Rectangle r : Main.loadedMap.filler) {
			g.setColor(fillColor);
			g.fillRect(r.x * 8, r.y * 8, r.width * 8, r.height * 8);
			g.setColor(Color.black);
			g.drawRect(r.x * 8, r.y * 8, r.width * 8, r.height * 8);
		}
	}
	
	public void drawTiles(Graphics g, boolean fg) {
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					Graphics imgG = level.frameBuffer.createGraphics();
					imgG.translate(-level.bounds.x, -level.bounds.y);
					drawTiles(imgG, level, fg);
				}
			}
		}
	}
	
	public void drawTiles(Graphics g, Level level, boolean fg) {
		Main.bgAutotiler.rand = new Random(level.name.hashCode());
		Main.fgAutotiler.rand = new Random(level.name.hashCode());
		
		TileLevelLayer tiles = fg ? level.solids : level.bg;
		//if(tiles.img == null) {
			tiles.img = new BufferedImage(level.bounds.width, level.bounds.height, BufferedImage.TYPE_INT_ARGB);
			Graphics imgG = tiles.img.createGraphics();
			tiles.tileImgs = (fg ? Main.fgAutotiler : Main.bgAutotiler).generateMap(tiles, true).tileImg;
			for(int i = 0; i < tiles.tileImgs.length; i++) {
				for(int j = 0; j < tiles.tileImgs[i].length; j++) {
					g.drawImage(tiles.tileImgs[i][j], level.bounds.x + j * 8, level.bounds.y + i * 8, null);
				}
			}
		//}
		
		//g.drawImage(tiles.img, level.bounds.x, level.bounds.y, null);
	}
	
	public void drawDecals(Graphics g, boolean fg) {
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					Graphics imgG = level.frameBuffer.createGraphics();
					imgG.translate(-level.bounds.x, -level.bounds.y);
					drawDecals(imgG, level, fg);
				}
			}
		}
	}
	
	public void drawDecals(Graphics g, Level level, boolean fg) {
		ListLevelLayer decals = (fg ? level.fgDecals : level.bgDecals);
		if(decals != null) {
			for(int i = 0; i < decals.items.size(); i++) {
				Decal d = (Decal)decals.items.get(i);
				BufferedImage img = d.getImage();
				
				g.drawImage(img, d.x + level.bounds.x - img.getWidth() / 2 * d.scaleX, d.y + level.bounds.y - img.getHeight() / 2 * d.scaleY, img.getWidth() * d.scaleX, img.getHeight() * d.scaleY, null);
			}
		}
	}
	
	public void drawEntities(Graphics g) {
		AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
		
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					Graphics2D imgG = level.frameBuffer.createGraphics();
					imgG.translate(-level.bounds.x, -level.bounds.y);
					drawEntities(imgG, level, alpha);
				}
			}
		}
	}
	
	public void drawEntities(Graphics2D g2d, Level level, AlphaComposite alpha) {
		for(int i = 0; i < level.entities.items.size(); i++) {
			Entity e = (Entity)level.entities.items.get(i);
			EntityConfig ec = Main.entityConfig.get(e.name);
			if(ec == null) {
				ec = new EntityConfig();
				ec.name = e.name;
				ec.setImage(defaultEntityImg);
				Main.entityConfig.put(e.name, ec);
			}
			
			Rectangle eBounds = null;
			if(ec.visualType == VisualType.Image) {
				g2d.drawImage(ec.getImage(), e.x + level.bounds.x - ec.imgOffsetX, e.y + level.bounds.y - ec.imgOffsetY, null);
			} else if(ec.visualType == VisualType.Box) {
				eBounds = new Rectangle(e.x + level.bounds.x, e.y + level.bounds.y, (int)e.getProperty("width").value, (int)e.getProperty("height").value);
				g2d.setColor(ec.fillColor);
				g2d.fillRect(eBounds.x, eBounds.y, eBounds.width, eBounds.height);
				g2d.setColor(ec.borderColor);
				g2d.drawRect(eBounds.x, eBounds.y, eBounds.width, eBounds.height);
			} else if(ec.visualType == VisualType.ImageBox) {
				int width = (int)e.getPropertyValue("width", 8);
				int height = (int)e.getPropertyValue("height", 8);
				BufferedImage img = ec.getImage();
				int repX = Math.round(width / (float) img.getWidth());
				int repY = Math.round(height / (float) img.getHeight());
				
				for(int j = 0; j < repY; j++) {
					for(int k = 0; k < repX; k++) {
						g2d.drawImage(ec.getImage(), e.x + level.bounds.x + k * img.getWidth() - ec.imgOffsetX, e.y + level.bounds.y + j * img.getHeight() - ec.imgOffsetY, null);
					}
				}
			}
			
			if(selectedEntity == e && e.nodes.size() != 0) {
				Composite c = g2d.getComposite();
				g2d.setComposite(alpha);
				for(Point n : e.nodes) {
					if(ec.visualType == VisualType.Image) {
						g2d.drawImage(ec.getImage(), n.x + level.bounds.x - ec.imgOffsetX, n.y + level.bounds.y - ec.imgOffsetY, null);
					} else if(ec.visualType == VisualType.Box) {
						g2d.setColor(ec.fillColor);
						g2d.fillRect(n.x + level.bounds.x, n.y + level.bounds.y, eBounds.width, eBounds.height);
						g2d.setColor(ec.borderColor);
						g2d.drawRect(n.x + level.bounds.x, n.y + level.bounds.y, eBounds.width, eBounds.height);
					} else if(ec.visualType == VisualType.ImageBox) {
						int width = (int)e.getProperty("width").value;
						int height = (int)e.getProperty("height").value;
						BufferedImage img = ec.getImage();
						int repX = Math.round(width / (float) img.getWidth());
						int repY = Math.round(height / (float) img.getHeight());
						
						for(int j = 0; j < repY; j++) {
							for(int k = 0; k < repX; k++) {
								g2d.drawImage(ec.getImage(), e.x + level.bounds.x + k * img.getWidth() - ec.imgOffsetX, e.y + level.bounds.y + j * img.getHeight() - ec.imgOffsetY, null);
							}
						}
					}
				}
				g2d.setComposite(c);
			}
		}
	}
	
	public void drawTriggers(Graphics g) {
		AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
		
		if(firstDraw || redrawEverything) {
			for(Level level : Main.loadedMap.levels) {
				if(level != selectedLevel) {
					Graphics2D imgG = level.frameBuffer.createGraphics();
					imgG.translate(-level.bounds.x, -level.bounds.y);
					drawTriggers(imgG, level, alpha);
				}
			}
		}
	}
	
	public void drawTriggers(Graphics2D g2d, Level level, AlphaComposite alpha) {
		Font font = new Font(getFont().getName(), getFont().getStyle(), getFont().getSize() * 3 / 4);
		for(int i = 0; i < level.triggers.items.size(); i++) {
			Entity e = (Entity)level.triggers.items.get(i);
			Rectangle triggerBounds = new Rectangle(e.x + level.bounds.x, e.y + level.bounds.y, e.getPropertyValue("width", 8), e.getPropertyValue("height", 8));
			g2d.setColor(new Color(200, 0, 0, 100));
			g2d.fillRect(triggerBounds.x, triggerBounds.y, triggerBounds.width, triggerBounds.height);
			g2d.setColor(Color.black);
			
			TextRenderer.drawString(g2d, e.name, font, g2d.getColor(), triggerBounds, TextAlignment.MIDDLE);
			g2d.setColor(Color.darkGray);
			g2d.drawRect(triggerBounds.x, triggerBounds.y, triggerBounds.width, triggerBounds.height);
			
			if(selectedEntity == e && e.nodes.size() != 0) {
				Composite c = g2d.getComposite();
				g2d.setComposite(alpha);
				for(Point n : e.nodes) {
					g2d.setColor(new Color(200, 0, 0, 100));
					g2d.fillRect(n.x + level.bounds.x, n.y + level.bounds.y, triggerBounds.width, triggerBounds.height);
					g2d.setColor(Color.darkGray);
					g2d.drawRect(n.x + level.bounds.x, n.y + level.bounds.y, triggerBounds.width, triggerBounds.height);
				}
				g2d.setComposite(c);
			}
		}
	}
	
	public void drawRooms(Graphics g) {
		g.setColor(Color.black);
		Color darkColor = new Color(0, 0, 0, 120);
		for(Level level : Main.loadedMap.levels) {
			if(level != selectedLevel) {
				g.setColor(darkColor);
				g.fillRect(level.bounds.x, level.bounds.y, level.bounds.width, level.bounds.height);
				g.setColor(Color.black);
			} else if(Main.editingPanel.tiles.selectedTileTool != null && Main.editingPanel.getCurrentPanel() == EditPanel.Tiles && Main.editingPanel.tiles.selectedTiletype != null && !Main.editingPanel.tiles.selectedTiletype.name.equalsIgnoreCase("air")) {
				// TODO draw entity preview
				// Draw brush preview
				char[][] tileOverlay = Main.editingPanel.tiles.selectedTileTool.getTileOverlay(Main.editingPanel.tiles.selectedTiletype.ID);
				Point tileOverlayPos = Main.editingPanel.tiles.selectedTileTool.getTileOverlayPos();
				if(tileOverlay != null && tileOverlayPos != null) {
					TileLevelLayer layer = new TileLevelLayer(tileOverlay.length == 0 ? 0 : tileOverlay[0].length, tileOverlay.length);
					layer.tileMap = tileOverlay;
					BufferedImage[][] overlayImages = (Main.editingPanel.tiles.selectedFg ? Main.fgAutotiler : Main.bgAutotiler).generateMap(layer, new Behaviour()).tileImg;
					for(int i = 0; i < overlayImages.length; i++) {
						for(int j = 0; j < overlayImages[i].length; j++) {
							if(overlayImages[i][j] != null && i + tileOverlayPos.y >= 0 && j + tileOverlayPos.x >= 0 && i + tileOverlayPos.y < level.bounds.height / 8 && j + tileOverlayPos.x < level.bounds.width / 8) {
								g.drawImage(overlayImages[i][j], level.bounds.x + (j + tileOverlayPos.x) * 8, level.bounds.y + (i + tileOverlayPos.y) * 8, 8, 8, null);
							}
						}
					}
					g.setColor(Color.black);
				}
			}
			g.drawRect(level.bounds.x, level.bounds.y, level.bounds.width, level.bounds.height);
			if(selectedLevel == level) {
				switch(selectedEdge) {
				case Left:
					g.drawLine(level.bounds.x + selectedEdgeOffset * 8, level.bounds.y, level.bounds.x + selectedEdgeOffset * 8, level.bounds.y + level.bounds.height);
					break;
				case Right:
					g.drawLine(level.bounds.x + level.bounds.width + selectedEdgeOffset * 8, level.bounds.y, level.bounds.x + level.bounds.width + selectedEdgeOffset * 8, level.bounds.y + level.bounds.height);
					break;
				case Top:
					g.drawLine(level.bounds.x, level.bounds.y + selectedEdgeOffset * 8, level.bounds.x + level.bounds.width, level.bounds.y + selectedEdgeOffset * 8);
					break;
				case Bottom:
					g.drawLine(level.bounds.x, level.bounds.y + level.bounds.height + selectedEdgeOffset * 8, level.bounds.x + level.bounds.width, level.bounds.y + level.bounds.height + selectedEdgeOffset * 8);
					break;
				}
			}
		}
	}
	
	public void drawSelectionBox(Graphics g) {
		
		if(selectedEntity != null) {
			Graphics2D g2d = (Graphics2D) g;
			Stroke origStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(2));
			g2d.setColor(Color.red);
			Rectangle bounds = selectedEntity.getBounds(selectedLevel, selectedNode);
			
			g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
			
			g2d.setStroke(origStroke);
		} else if(selectedDecal != null) {
			Graphics2D g2d = (Graphics2D) g;
			Stroke origStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(2));
			g2d.setColor(Color.red);
			int x = selectedDecal.x;
			int y = selectedDecal.y;
			int width = selectedDecal.getImage().getWidth();
			int height = selectedDecal.getImage().getHeight();
			g2d.drawRect(x + selectedLevel.bounds.x - width / 2 * Math.abs(selectedDecal.scaleX), y + selectedLevel.bounds.y - height / 2 * Math.abs(selectedDecal.scaleY), width, height);
			
			g2d.setStroke(origStroke);
		}
	}
	
}
