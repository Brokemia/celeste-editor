package celesteeditor.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import celesteeditor.Main;
import celesteeditor.data.Decal;
import celesteeditor.data.Entity;
import celesteeditor.data.Level;
import celesteeditor.data.ListLevelLayer;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.TileTool.MouseAction;
import celesteeditor.ui.EditingPanel.EditPanel;

public class MapMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {
	
	public Point dragStart;
	
	public MapPanel panel;
	
	public boolean button1Down, button2Down, button3Down;
	
	public MapMouseListener(MapPanel p) {
		panel = p;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(button1Down && Main.editingPanel.getCurrentPanel() == EditPanel.Entities && panel.draggingEntity && (panel.selectedEntity != null || panel.selectedDecal != null)) {
			int newX = (int)(e.getPoint().x / panel.getActualZoom(panel.getZoom()) - dragStart.x);
			int newY = (int)(e.getPoint().y / panel.getActualZoom(panel.getZoom()) - dragStart.y);
			
			// Snap to grid if ctrl not pressed
			if(!panel.ctrlPressed) {
				newX = Math.round(newX / 8f) * 8;
				newY = Math.round(newY / 8f) * 8;
			}
			
			if(panel.selectedEntity != null) {
				if(panel.selectedNode < 0) {
					panel.selectedEntity.x = newX;
					panel.selectedEntity.y = newY;
				} else {
					panel.selectedEntity.nodes.set(panel.selectedNode, new Point(newX, newY));
				}
			} else {
				panel.selectedDecal.x = newX;
				panel.selectedDecal.y = newY;
			}
		} else {
			panel.draggingEntity = false;
		}
		
		if(button3Down || (button1Down && panel.altPressed && !panel.draggingEntity)) {
			panel.offset = new Point((int)(e.getPoint().x * panel.getActualZoom(-panel.getZoom()) - dragStart.x), (int)(e.getPoint().y * panel.getActualZoom(-panel.getZoom()) - dragStart.y));
		} else if(Main.editingPanel.getCurrentPanel() == EditPanel.Tiles) {
			placeTile(e.getPoint(), MouseAction.DRAGGED);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(Main.editingPanel.tiles.selectedTileTool != null) {
			for(Level level : Main.loadedMap.levels) {
				Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
				if(lBounds.contains(e.getPoint())) {
					if(level == panel.selectedLevel) {
						Main.editingPanel.tiles.selectedTileTool.lastMousePos = new Point((int)((e.getPoint().x -  lBounds.x) / panel.getActualZoom() / 8), (int)((e.getPoint().y - lBounds.y) / panel.getActualZoom() / 8));
					} else {
						Main.editingPanel.tiles.selectedTileTool.lastMousePos = null;
					}
				}
			}
			
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(Main.editingPanel.getCurrentPanel() == EditPanel.Entities) {
			Entity openPopupFor = null;
			boolean done = false;
			boolean isTrigger = false;
			// Prioritize nodes of the selected entity
			if(panel.selectedEntity != null) {
				EntityConfig ec = Main.entityConfig.get(panel.selectedEntity.name);
				isTrigger = ec == null;
				for(int i = 0; i < panel.selectedEntity.nodes.size(); i++) {
					Rectangle eBounds = panel.selectedEntity.getBounds(panel.selectedLevel, i, panel.offset, panel.getActualZoom());
					
					if(eBounds.contains(e.getPoint())) {
						switch(e.getButton()) {
						case MouseEvent.BUTTON1:
							panel.selectedNode = i;
							done = true;
							break;
						case MouseEvent.BUTTON3:
							openPopupFor = panel.selectedEntity;
							done = true;
							break;
						}
					}
				}
			}
			if(openPopupFor != null) {
				EntityPropertyPopup popup = new EntityPropertyPopup(openPopupFor, isTrigger);
				popup.setVisible(true);
			}
			if(done) return;
			
			for(Level level : Main.loadedMap.levels) {
				Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
				if(lBounds.contains(e.getPoint()) && panel.selectedLevel != level) {
					panel.selectedLevel = level;
					panel.selectedEntity = null;
					panel.selectedDecal = null;
					panel.selectedNode = -1;
				}
				openPopupFor = null;
				done = false;
				for(int i = 0; i < level.entities.items.size(); i++) {
					Entity entity = (Entity)level.entities.items.get(i);
					Rectangle eBounds = entity.getBounds(level, -1, panel.offset, panel.getActualZoom());
					if(eBounds.contains(e.getPoint())) {
						switch(e.getButton()) {
						case MouseEvent.BUTTON1:
							panel.selectedLevel = level;
							panel.selectedEntity = entity;
							panel.selectedDecal = null;
							panel.selectedNode = -1;
							done = true;
							break;
						case MouseEvent.BUTTON3:
							openPopupFor = entity;
							done = true;
							break;
						}
					}
				}
				if(openPopupFor != null) {
					EntityPropertyPopup popup = new EntityPropertyPopup(openPopupFor, false);
					popup.setVisible(true);
				}
				if(done) return;
				
				Decal decalOpenPopupFor = null;
				done = false;
				for(ListLevelLayer decals : new ListLevelLayer[] {level.bgDecals, level.fgDecals} ) {  
					for(int i = 0; i < decals.items.size(); i++) {
						Decal decal = (Decal)decals.items.get(i);
						BufferedImage img = decal.getImage();
						Rectangle dBounds = new Rectangle((int)((level.bounds.x + decal.x - img.getWidth() / 2 * Math.abs(decal.scaleX) + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + decal.y - img.getHeight() / 2 * Math.abs(decal.scaleY) + panel.offset.y) * panel.getActualZoom()), (int)(img.getWidth() * Math.abs(decal.scaleX) * panel.getActualZoom()), (int)(img.getHeight() * Math.abs(decal.scaleY) * panel.getActualZoom()));
						
						if(dBounds.contains(e.getPoint())) {
							switch(e.getButton()) {
							case MouseEvent.BUTTON1:
								panel.selectedLevel = level;
								panel.selectedEntity = null;
								panel.selectedDecal = decal;
								panel.selectedNode = -1;
								done = true;
								break;
							case MouseEvent.BUTTON3:
								decalOpenPopupFor = decal;
								done = true;
								break;
							}
						}
					}
				}
				
				if(decalOpenPopupFor != null) {
					DecalPropertyPopup popup = new DecalPropertyPopup(decalOpenPopupFor);
					popup.setVisible(true);
				}
				if(done) return;
				
				openPopupFor = null;
				done = false;
				for(int i = 0; i < level.triggers.items.size(); i++) {
					Entity trigger = (Entity)level.triggers.items.get(i);
					Rectangle tBounds = trigger.getBounds(level, -1, panel.offset, panel.getActualZoom());
					if(tBounds.contains(e.getPoint())) {
						switch(e.getButton()) {
						case MouseEvent.BUTTON1:
							panel.selectedLevel = level;
							panel.selectedEntity = trigger;
							panel.selectedDecal = null;
							panel.selectedNode = -1;
							done = true;
							break;
						case MouseEvent.BUTTON3:
							openPopupFor = trigger;
							done = true;
							break;
						}
					}
				}
				if(openPopupFor != null) {
					EntityPropertyPopup popup = new EntityPropertyPopup(openPopupFor, true);
					popup.setVisible(true);
				}
				if(done) return;
			}
			
			panel.selectedEntity = null;
			panel.selectedNode = -1;
		} else {
			for(Level level : Main.loadedMap.levels) {
				Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
				if(lBounds.contains(e.getPoint()) && panel.selectedLevel != level) {
					panel.selectedLevel = level;
					panel.selectedEntity = null;
					panel.selectedDecal = null;
					panel.selectedNode = -1;
					return;
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		switch(e.getButton()) {
		case MouseEvent.BUTTON1:
			button1Down = true;
			break;
		case MouseEvent.BUTTON2:
			button2Down = true;
			break;
		case MouseEvent.BUTTON3:
			button3Down = true;
			break;
		}

		if(button1Down && !panel.altPressed) {
			if(Main.editingPanel.getCurrentPanel() == EditPanel.Entities) {
				if(panel.selectedEntity != null) {
					int x = panel.selectedEntity.x;
					int y = panel.selectedEntity.y;
					if(panel.selectedNode >= 0) {
						x = panel.selectedEntity.nodes.get(panel.selectedNode).x;
						y = panel.selectedEntity.nodes.get(panel.selectedNode).y;
					}
					
					Rectangle eBounds = panel.selectedEntity.getBounds(panel.selectedLevel, panel.selectedNode, panel.offset, panel.getActualZoom());
					if(eBounds.contains(e.getPoint())) {
						panel.draggingEntity = true;
						dragStart = e.getPoint();
						dragStart.setLocation(dragStart.x / panel.getActualZoom(panel.getZoom()), dragStart.y / panel.getActualZoom(panel.getZoom()));
						dragStart.translate(-x, -y);
					}
				} else if(panel.selectedDecal != null) {
					BufferedImage img = panel.selectedDecal.getImage();
					Rectangle dBounds = new Rectangle((int)((panel.selectedLevel.bounds.x + panel.selectedDecal.x - img.getWidth() / 2 * Math.abs(panel.selectedDecal.scaleX) + panel.offset.x) * panel.getActualZoom()), (int)((panel.selectedLevel.bounds.y + panel.selectedDecal.y - img.getHeight() / 2 * Math.abs(panel.selectedDecal.scaleY) + panel.offset.y) * panel.getActualZoom()), (int)(img.getWidth() * Math.abs(panel.selectedDecal.scaleX) * panel.getActualZoom()), (int)(img.getHeight() * Math.abs(panel.selectedDecal.scaleY) * panel.getActualZoom()));
						
					if(dBounds.contains(e.getPoint())) {
						panel.draggingEntity = true;
						dragStart = e.getPoint();
						dragStart.setLocation(dragStart.x / panel.getActualZoom(panel.getZoom()), dragStart.y / panel.getActualZoom(panel.getZoom()));
						dragStart.translate(-panel.selectedDecal.x, -panel.selectedDecal.y);
					}
				}
			} else if(Main.editingPanel.getCurrentPanel() == EditPanel.Tiles) {
				placeTile(e.getPoint(), MouseAction.PRESSED);
			}
		}
		
		if(button3Down || (button1Down && panel.altPressed)) {
			dragStart = e.getPoint();
			dragStart.setLocation(dragStart.x * panel.getActualZoom(-panel.getZoom()), dragStart.y * panel.getActualZoom(-panel.getZoom()));
			dragStart.translate(-panel.offset.x, -panel.offset.y);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch(e.getButton()) {
		case MouseEvent.BUTTON1:
			button1Down = false;
			break;
		case MouseEvent.BUTTON2:
			button2Down = false;
			break;
		case MouseEvent.BUTTON3:
			button3Down = false;
			break;
		}
		
		if(Main.editingPanel.getCurrentPanel() == EditPanel.Tiles) {
			placeTile(e.getPoint(), MouseAction.RELEASED);
		}
		
		panel.draggingEntity = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		panel.setZoom(panel.getZoom() + e.getUnitsToScroll());
	}
	
	public void placeTile(Point screenPos, MouseAction action) {
		for(Level level : Main.loadedMap.levels) {
			Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
			if(lBounds.contains(screenPos)) {
				if(panel.selectedLevel != level) {
					panel.selectedLevel = level;
					panel.selectedEntity = null;
					panel.selectedDecal = null;
					panel.selectedNode = -1;
					return;
				}
				Point tileCoords = new Point((int)((screenPos.x -  lBounds.x) / panel.getActualZoom() / 8), (int)((screenPos.y - lBounds.y) / panel.getActualZoom() / 8));
				if(Main.editingPanel.tiles.selectedTiletype != null && Main.editingPanel.tiles.selectedTileTool != null) {
					if(Main.editingPanel.tiles.selectedTiletype.fg) {
						Main.editingPanel.tiles.selectedTileTool.drawAt(level.solids.tileMap, Main.editingPanel.tiles.selectedTiletype, tileCoords, action);
					} else {
						Main.editingPanel.tiles.selectedTileTool.drawAt(level.bg.tileMap, Main.editingPanel.tiles.selectedTiletype, tileCoords, action);
					}
				}
			}
		}
	}

}
