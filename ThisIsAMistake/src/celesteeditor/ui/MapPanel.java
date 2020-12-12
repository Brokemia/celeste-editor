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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.opengl.util.texture.TextureIO;
import com.text.TextAlignment;
import com.text.TextRenderer;

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
	
	public boolean redrawEverything = true;
	
	public boolean ctrlPressed;
	
	public boolean altPressed;
				
	public static String defaultEntityImgPath;
		
	public static BufferedImage defaultEntityImg;
	
	//getting the capabilities object of GL2 profile
    final private GLProfile profile;
    
    final private GLCapabilities capabilities;
    
    final public GLCanvas glcanvas;
    
    final private GLU glu = new GLU();
	
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
        glcanvas.setSize(200, 200);

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
	
	@Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        
		 /*gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);     // Clear The Screen And The Depth Buffer
		    gl.glLoadIdentity();                       // Reset The View
		    gl.glTranslatef(0f, 0f, -5.0f);
		    gl.glRotatef(0, 1.0f, 0.0f, 0.0f);
		    gl.glRotatef(0, 0.0f, 1.0f, 0.0f);
		    gl.glRotatef(0, 0.0f, 0.0f, 1.0f);
		    try {
				gl.glBindTexture(GL2.GL_TEXTURE_2D, TextureIO.newTexture(new File("./bin/assets/add.png"), true).getTextureObject(gl));
			} catch (GLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		    gl.glFlush();*/
        
        //renderingComplete = false;
        
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -2);
        TextureRenderer renderer = new TextureRenderer(200, 200, true);
        //gl.glBindTexture(GL2.GL_TEXTURE_2D, renderer.getTexture().getTextureObject(gl));
        paintComponent(renderer.createGraphics());
        renderer.beginOrthoRendering(200, 200);
        renderer.drawOrthoRect(0, 0);
        renderer.endOrthoRendering();
        gl.glFlush();
        //gl.glBindTexture(GL2.GL_TEXTURE_2D, renderer.getTexture().getTextureObject(gl));
//        try {
//			gl.glBindTexture(GL2.GL_TEXTURE_2D, TextureIO.newTexture(new File("./bin/assets/add.png"), true).getTextureObject(gl));
//		} catch (GLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		gl.glBegin(GL2.GL_QUADS);
//		
//		gl.glTexCoord2f(0, 1); gl.glVertex2f(-1, -1);//gl.glVertex2f(level.bounds.x, level.bounds.y);
//		gl.glTexCoord2f(1, 1); gl.glVertex2f(1, -1);//gl.glVertex2f(level.bounds.x - level.bounds.width, level.bounds.y);
//		gl.glTexCoord2f(1, 0); gl.glVertex2f(1, 1);//gl.glVertex2f(level.bounds.x - level.bounds.width, level.bounds.y - level.bounds.height);
//		gl.glTexCoord2f(0, 0); gl.glVertex2f(-1, 1);//gl.glVertex2f(level.bounds.x, level.bounds.y - level.bounds.height);
//		
//		gl.glEnd();
//		gl.glFlush();
//        Main.loadedMap.levels.get(0).roomCanvas.beginOrthoRendering(100, 100);
//        Main.loadedMap.levels.get(0).roomCanvas.drawOrthoRect(0, 100);
//        Main.loadedMap.levels.get(0).roomCanvas.endOrthoRendering();
        //gl.glTranslated(offset.x, -offset.y, 0);
        //gl.glScaled(actualZoom, actualZoom, 1);
        
        /*if(Main.loadedMap != null) {
			//lock.lock();
			for(Level level : Main.loadedMap.levels) {
				if(level.roomCanvas == null) {
					level.roomCanvas = new TextureRenderer(level.bounds.width, level.bounds.height, true);//new BufferedImage(level.bounds.width, level.bounds.height, BufferedImage.TYPE_INT_ARGB);
				}
				//g.drawImage(level.roomCanvas, level.bounds.x, level.bounds.y, null);
				//gl.glBindTexture(GL2.GL_TEXTURE_2D, level.roomCanvas.getTexture().getTextureObject(gl));
//				gl.glBegin(GL2.GL_QUADS);
//				
//				gl.glTexCoord2f(0, 0); gl.glVertex2f(-1, -1);//gl.glVertex2f(level.bounds.x, level.bounds.y);
//				gl.glTexCoord2f(1, 0); gl.glVertex2f(1, -1);//gl.glVertex2f(level.bounds.x - level.bounds.width, level.bounds.y);
//				gl.glTexCoord2f(1, 1); gl.glVertex2f(1, 1);//gl.glVertex2f(level.bounds.x - level.bounds.width, level.bounds.y - level.bounds.height);
//				gl.glTexCoord2f(0, 1); gl.glVertex2f(-1, 1);//gl.glVertex2f(level.bounds.x, level.bounds.y - level.bounds.height);
//				
//				gl.glEnd();
//				gl.glFlush();
//			    gl.glTranslatef(0f, 0f, -5.0f);
//			    gl.glRotatef(0, 1.0f, 0.0f, 0.0f);
//			    gl.glRotatef(0, 0.0f, 1.0f, 0.0f);
//			    gl.glRotatef(0, 0.0f, 0.0f, 1.0f);
//			    try {
//				gl.glBindTexture(GL2.GL_TEXTURE_2D, TextureIO.newTexture(new File("./bin/assets/add.png"), true).getTextureObject(gl));
//			} catch (GLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			    level.roomCanvas.beginOrthoRendering(100, 100);
			    level.roomCanvas.drawOrthoRect(0, 100);
			    level.roomCanvas.endOrthoRendering();
			    
//			    level.roomCanvas.beginOrthoRendering(1000, 1000);
//			    level.roomCanvas.markDirty(0, 0, 100, 100);
//			level.roomCanvas.drawOrthoRect(0, 0);
				//gl.glBindTexture(GL2.GL_TEXTURE_2D, level.roomCanvas.getTexture().getTextureObject(gl));
			
				
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
		      return;
			}
			
//			if(mapRenderThread == null) {
//				mapRenderThread = new Thread(new MapRenderThread());
//				mapRenderThread.start();
//			}
//			drawFillers(g);
//			drawTiles(g, false);
//			drawDecals(g, false);
//			drawEntities(g);
//			drawTiles(g, true);
//			drawDecals(g, true);
//			drawTriggers(g);
//			drawRooms(g);
//			drawSelectionBox(g);
			
			firstDraw = false;
			//lock.unlock();
		}
		
		renderingComplete = true;

        //drawing the base
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(-0.50f, -0.50f, 0);
        gl.glVertex3f(0.50f, -0.50f, 0);
        gl.glEnd();

        //drawing the right edge
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, 0.50f, 0);
        gl.glVertex3f(-0.50f, -0.50f, 0);
        gl.glEnd();

        //drawing the lft edge
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, 0.50f, 0);
        gl.glVertex3f(0.50f, -0.50f, 0);
        gl.glEnd();*/
        
        
    }

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		
		if(Main.loadedMap != null) {
			for(Level level : Main.loadedMap.levels) {
				if(level.roomCanvas == null) {
					level.roomCanvas = new TextureRenderer(level.bounds.width, level.bounds.height, true);//new BufferedImage(level.bounds.width, level.bounds.height, BufferedImage.TYPE_INT_ARGB);
				}
			}
		}
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1f);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		repaint();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		 
		if(height <=0)
			height =1;
		final float h = (float) width / (float) height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, h, 1.0, 20.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		renderingComplete = false;
		
		((Graphics2D)g).scale(actualZoom, actualZoom);
		g.translate(offset.x, offset.y);
		
		if(Main.loadedMap != null) {
			lock.lock();
			for(Level level : Main.loadedMap.levels) {
				if(level.roomCanvas == null) {
					//level.roomCanvas = new TextureRenderer(level.bounds.width, level.bounds.height, true);//new BufferedImage(level.bounds.width, level.bounds.height, BufferedImage.TYPE_INT_ARGB);
					return;
				}
				g.drawImage(level.roomCanvas.getImage(), level.bounds.x, level.bounds.y, null);
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
		
		renderingComplete = true;
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
					Graphics imgG = level.roomCanvas.createGraphics();
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
					Graphics imgG = level.roomCanvas.createGraphics();
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
					Graphics2D imgG = level.roomCanvas.createGraphics();
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
					Graphics2D imgG = level.roomCanvas.createGraphics();
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
