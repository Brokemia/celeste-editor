package celesteeditor.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.text.TextAlignment;
import com.text.TextRenderer;

import celesteeditor.Main;
import celesteeditor.data.Decal;
import celesteeditor.data.Entity;
import celesteeditor.data.Level;
import celesteeditor.data.ListLevelLayer;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.Tiletype;
import celesteeditor.ui.EditingPanel.EditPanel;
import celesteeditor.editing.EntityConfig.VisualType;
import celesteeditor.util.Util;

public class MapPanel extends JPanel {
		
	public Point offset = new Point(0, 0);
	
	private int zoom;
	
	private double actualZoom = 1;
	
	public boolean draggingEntity;
	
	public Entity selectedEntity;
	
	public Decal selectedDecal;
	
	// -1 = no node selected
	public int selectedNode = -1;
	
	public Level selectedLevel;
	
	public boolean renderingComplete;
	
	public boolean ctrlPressed;
	
	public boolean altPressed;
				
	public static String defaultEntityImgPath;
		
	public static BufferedImage defaultEntityImg;
	
	static {
		defaultEntityImgPath = "/assets/defaultentity.png";
		defaultEntityImg = Util.getImage(defaultEntityImgPath);
	}
	
	public MapPanel() {
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
		TilesTab.fgTileTypes.add(new Tiletype("Air", true, (char)0, new Color(0, 0, 0, 0)));
		TilesTab.fgTileTypes.add(new Tiletype("Air", true, '0', new Color(0, 0, 0, 0)));
		TilesTab.bgTileTypes.add(new Tiletype("Air", false, (char)0, new Color(0, 0, 0, 0)));
		TilesTab.bgTileTypes.add(new Tiletype("Air", false, '0', new Color(0, 0, 0, 0)));
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
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		renderingComplete = false;
		
		((Graphics2D)g).scale(actualZoom, actualZoom);
		g.translate(offset.x, offset.y);
		
		drawFillers(g);
		drawTiles(g, false);
		drawDecals(g, false);
		drawEntities(g);
		drawTiles(g, true);
		drawDecals(g, true);
		drawTriggers(g);
		drawSelectionBox(g);
		drawRooms(g);
		
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
		ArrayList<Tiletype> tileTypes = fg ? TilesTab.fgTileTypes :  TilesTab.bgTileTypes;
		
		for(Level level : Main.loadedMap.levels) {
			char[][] tiles = (fg ? level.solids : level.bg).tileMap;
			for(int i = 0; i < tiles.length; i++) {
				for(int j = 0; j < tiles[i].length; j++) {
					char tile = tiles[i][j];
					Tiletype type = tileTypes.stream().filter((t) -> t.tile == tile).findFirst().orElse(null);
					g.setColor(type == null ? Color.pink : type.color);
					if(type == null) System.out.println((fg ? "Fore" : "Back") + "ground tile " + tile + " not found");
					g.fillRect(level.bounds.x + j * 8, level.bounds.y + i * 8, 8, 8);
				}
			}
		}
	}
	
	public void drawDecals(Graphics g, boolean fg) {
		for(Level level : Main.loadedMap.levels) {
			ListLevelLayer decals = (fg ? level.fgDecals : level.bgDecals);
			for(int i = 0; i < decals.items.size(); i++) {
				Decal d = (Decal)decals.items.get(i);
				BufferedImage img = d.getImage();
				
				g.drawImage(img, d.x + level.bounds.x - img.getWidth() / 2 * d.scaleX, d.y + level.bounds.y - img.getHeight() / 2 * d.scaleY, img.getWidth() * d.scaleX, img.getHeight() * d.scaleY, null);
			}
		}
	}
	
	public void drawEntities(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
		for(Level level : Main.loadedMap.levels) {
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
					g.drawImage(ec.getImage(), e.x + level.bounds.x - ec.imgOffsetX, e.y + level.bounds.y - ec.imgOffsetY, null);
				} else if(ec.visualType == VisualType.Box) {
					eBounds = new Rectangle(e.x + level.bounds.x, e.y + level.bounds.y, (int)e.getProperty("width").value, (int)e.getProperty("height").value);
					g.setColor(ec.fillColor);
					g.fillRect(eBounds.x, eBounds.y, eBounds.width, eBounds.height);
					g.setColor(ec.borderColor);
					g.drawRect(eBounds.x, eBounds.y, eBounds.width, eBounds.height);
				} else if(ec.visualType == VisualType.ImageBox) {
					int width = (int)e.getPropertyValue("width", 8);
					int height = (int)e.getPropertyValue("height", 8);
					BufferedImage img = ec.getImage();
					int repX = Math.round(width / (float) img.getWidth());
					int repY = Math.round(height / (float) img.getHeight());
					
					for(int j = 0; j < repY; j++) {
						for(int k = 0; k < repX; k++) {
							g.drawImage(ec.getImage(), e.x + level.bounds.x + k * img.getWidth(), e.y + level.bounds.y + j * img.getHeight(), null);
						}
					}
				}
				
				if(selectedEntity == e && e.nodes.size() != 0) {
					Composite c = g2d.getComposite();
					g2d.setComposite(alpha);
					for(Point n : e.nodes) {
						if(ec.visualType == VisualType.Image) {
							g.drawImage(ec.getImage(), n.x + level.bounds.x - ec.imgOffsetX, n.y + level.bounds.y - ec.imgOffsetY, null);
						} else if(ec.visualType == VisualType.Box) {
							g.setColor(ec.fillColor);
							g.fillRect(n.x + level.bounds.x, n.y + level.bounds.y, eBounds.width, eBounds.height);
							g.setColor(ec.borderColor);
							g.drawRect(n.x + level.bounds.x, n.y + level.bounds.y, eBounds.width, eBounds.height);
						} else if(ec.visualType == VisualType.ImageBox) {
							int width = (int)e.getProperty("width").value;
							int height = (int)e.getProperty("height").value;
							BufferedImage img = ec.getImage();
							int repX = Math.round(width / (float) img.getWidth());
							int repY = Math.round(height / (float) img.getHeight());
							
							for(int j = 0; j < repY; j++) {
								for(int k = 0; k < repX; k++) {
									g.drawImage(ec.getImage(), e.x + level.bounds.x + k * img.getWidth(), e.y + level.bounds.y + j * img.getHeight(), null);
								}
							}
						}
					}
					g2d.setComposite(c);
				}
			}
		}
	}
	
	public void drawTriggers(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
		for(Level level : Main.loadedMap.levels) {
			for(int i = 0; i < level.triggers.items.size(); i++) {
				Entity e = (Entity)level.triggers.items.get(i);
				Rectangle triggerBounds = new Rectangle(e.x + level.bounds.x, e.y + level.bounds.y, (int)e.getProperty("width").value, (int)e.getProperty("height").value);
				g.setColor(new Color(200, 0, 0, 100));
				g.fillRect(triggerBounds.x, triggerBounds.y, triggerBounds.width, triggerBounds.height);
				g.setColor(Color.black);
				TextRenderer.drawString(g, e.name, getFont(), g.getColor(), triggerBounds, TextAlignment.MIDDLE);
				g.setColor(Color.darkGray);
				g.drawRect(triggerBounds.x, triggerBounds.y, triggerBounds.width, triggerBounds.height);
				
				if(selectedEntity == e && e.nodes.size() != 0) {
					Composite c = g2d.getComposite();
					g2d.setComposite(alpha);
					for(Point n : e.nodes) {
						g.setColor(new Color(200, 0, 0, 100));
						g.fillRect(n.x + level.bounds.x, n.y + level.bounds.y, triggerBounds.width, triggerBounds.height);
						g.setColor(Color.darkGray);
						g.drawRect(n.x + level.bounds.x, n.y + level.bounds.y, triggerBounds.width, triggerBounds.height);
					}
					g2d.setComposite(c);
				}
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
			} else if(Main.editingPanel.tiles.selectedTileTool != null && Main.editingPanel.getCurrentPanel() == EditPanel.Tiles) {
				// TODO draw entity preview
				// Draw brush preview
				boolean[][] tileOverlay = Main.editingPanel.tiles.selectedTileTool.getTileOverlay();
				Point tileOverlayPos = Main.editingPanel.tiles.selectedTileTool.getTileOverlayPos();
				if(tileOverlay != null && tileOverlayPos != null && Main.editingPanel.tiles.selectedTiletype != null) {
					for(int i = 0; i < tileOverlay.length; i++) {
						for(int j = 0; j < tileOverlay[i].length; j++) {
							if(i + tileOverlayPos.y >= 0 && j + tileOverlayPos.x >= 0 && i + tileOverlayPos.y < level.solids.tileMap.length && j + tileOverlayPos.x < level.solids.tileMap[i + tileOverlayPos.y].length) {
								if(tileOverlay[i][j]) {
									g.setColor(Main.editingPanel.tiles.selectedTiletype.color);
									g.fillRect(level.bounds.x + (j + tileOverlayPos.x) * 8, level.bounds.y + (i + tileOverlayPos.y) * 8, 8, 8);
								}
							}
						}
					}
					g.setColor(Color.black);
				}
			}
			g.drawRect(level.bounds.x, level.bounds.y, level.bounds.width, level.bounds.height);
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
