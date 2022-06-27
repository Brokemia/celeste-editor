package celesteeditor.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;

import celesteeditor.Main;
import celesteeditor.data.Decal;
import celesteeditor.data.ElementEncoded;
import celesteeditor.data.Entity;
import celesteeditor.data.Level;
import celesteeditor.data.ListLevelLayer;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.PlacementConfig.PlacementType;
import celesteeditor.editing.TileTool.MouseAction;
import celesteeditor.ui.EditingPanel.EditPanel;
import celesteeditor.ui.MapPanel.LevelEdge;
import celesteeditor.util.TextureArea;

public class MapMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {
	
	public Point dragStart;
	
	public MapPanel panel;
	
	public boolean button1Down, button2Down, button3Down;
	
	public MapMouseListener(MapPanel p) {
		panel = p;
	}
	
	public Point mouseCoordsToOpenGL(Point p, Component c) {
		return new Point((int)Math.round(p.x / ((Graphics2D)c.getGraphics()).getTransform().getScaleX()), (int)Math.round((p.y - c.getHeight()) / ((Graphics2D)c.getGraphics()).getTransform().getScaleX()));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point pt = mouseCoordsToOpenGL(e.getPoint(), e.getComponent());
		if(button1Down && (Main.editingPanel.getCurrentPanel() == EditPanel.Entities || Main.editingPanel.getCurrentPanel() == EditPanel.Selection) && panel.draggingEntity && (panel.selectedEntity != null || panel.selectedDecal != null)) {
			int newX = (int)(pt.x / panel.getActualZoom(panel.getZoom()) - dragStart.x);
			int newY = (int)(pt.y / panel.getActualZoom(panel.getZoom()) - dragStart.y);
			
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
			if(button1Down && Main.editingPanel.getCurrentPanel() == EditPanel.Selection && panel.selectedLevel != null) {
				Rectangle lBounds = new Rectangle((int)((panel.selectedLevel.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((panel.selectedLevel.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(panel.selectedLevel.bounds.width * panel.getActualZoom()), (int)(panel.selectedLevel.bounds.height * panel.getActualZoom()));
				Point tileCoords = new Point((int)((pt.x -  lBounds.x) / panel.getActualZoom() / 8), (int)((pt.y - lBounds.y) / panel.getActualZoom() / 8));
				
				switch(panel.selectedEdge) {
				case Left:
					if(tileCoords.x >= panel.selectedLevel.bounds.width / 8) {
						tileCoords.x = panel.selectedLevel.bounds.width / 8 - 1;
					}
					panel.selectedEdgeOffset = tileCoords.x;
					break;
				case Right:
					if(tileCoords.x < 1) {
						tileCoords.x = 1;
					}
					panel.selectedEdgeOffset = tileCoords.x - panel.selectedLevel.bounds.width / 8;
					break;
				case Top:
					if(tileCoords.y >= panel.selectedLevel.bounds.height / 8) {
						tileCoords.y = panel.selectedLevel.bounds.height / 8 - 1;
					}
					panel.selectedEdgeOffset = tileCoords.y;
					break;
				case Bottom:
					if(tileCoords.y < 1) {
						tileCoords.y = 1;
					}
					panel.selectedEdgeOffset = tileCoords.y - panel.selectedLevel.bounds.height / 8;
					break;
				case None:
					break;
				}
			}
		}
		
		if(button3Down || (button1Down && panel.altPressed && !panel.draggingEntity)) {
			if(dragStart != null)
				panel.offset = new Point((int)(pt.x * panel.getActualZoom(-panel.getZoom()) - dragStart.x), (int)(pt.y * panel.getActualZoom(-panel.getZoom()) - dragStart.y));
		} else if(Main.editingPanel.getCurrentPanel() == EditPanel.Tiles && Main.loadedMap != null) {
			placeTile(pt, MouseAction.DRAGGED);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		boolean mouseSet = false;
		Point pt = mouseCoordsToOpenGL(e.getPoint(), e.getComponent());
		if(Main.loadedMap != null) {
			// Change the mouse to a resize icon if over the edge of a level
			for(Level level : Main.loadedMap.levels) {
				Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
				if(lBounds.contains(pt) && Main.editingPanel.tiles.selectedTileTool != null) {
					if(level == panel.selectedLevel) {
						Main.editingPanel.tiles.selectedTileTool.lastMousePos = new Point((int)((pt.x -  lBounds.x) / panel.getActualZoom() / 8), (int)((pt.y - lBounds.y) / panel.getActualZoom() / 8));
					} else {
						Main.editingPanel.tiles.selectedTileTool.lastMousePos = null;
					}
				}
				if(Main.editingPanel.getCurrentPanel() == EditPanel.Selection && !mouseSet) {
					if(new Rectangle(lBounds.x - 4, lBounds.y, 8, lBounds.height).contains(pt) || new Rectangle(lBounds.x + lBounds.width - 4, lBounds.y, 8, lBounds.height).contains(pt)) {
						Main.mapPanel.panel.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
						mouseSet = true;
					} else if(new Rectangle(lBounds.x, lBounds.y - 4, lBounds.width, 8).contains(pt) || new Rectangle(lBounds.x, lBounds.y + lBounds.height - 4, lBounds.width, 8).contains(pt)) {
						Main.mapPanel.panel.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
						mouseSet = true;
					}
				}
			}
			// Change the mouse to a resize icon if over the edge of a filler
			for(Rectangle filler : Main.loadedMap.filler) {
				Rectangle fBounds = new Rectangle((int)((filler.x * 8 + panel.offset.x) * panel.getActualZoom()), (int)((filler.y * 8 + panel.offset.y) * panel.getActualZoom()), (int)(filler.width * 8 * panel.getActualZoom()), (int)(filler.height * 8 * panel.getActualZoom()));
				if(Main.editingPanel.getCurrentPanel() == EditPanel.Selection && !mouseSet) {
					if(new Rectangle(fBounds.x - 4, fBounds.y, 8, fBounds.height).contains(pt) || new Rectangle(fBounds.x + fBounds.width - 4, fBounds.y, 8, fBounds.height).contains(pt)) {
						Main.mapPanel.panel.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
						mouseSet = true;
					} else if(new Rectangle(fBounds.x, fBounds.y - 4, fBounds.width, 8).contains(pt) || new Rectangle(fBounds.x, fBounds.y + fBounds.height - 4, fBounds.width, 8).contains(pt)) {
						Main.mapPanel.panel.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
						mouseSet = true;
					}
				}
			}
			
			if(!mouseSet) {
				Main.mapPanel.panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point pt = mouseCoordsToOpenGL(e.getPoint(), e.getComponent());
		if(Main.editingPanel.getCurrentPanel() == EditPanel.Entities && Main.editingPanel.placements.isPlacementSelected() && e.getButton() == MouseEvent.BUTTON1) {
			if(Main.loadedMap != null) {
				for(Level level : Main.loadedMap.levels) {
					Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
					if(lBounds.contains(pt)) {
						panel.selectedEntity = null;
						panel.selectedDecal = null;
						panel.selectedNode = -1;
						
						Point coords = new Point((int)((pt.x -  lBounds.x) / panel.getActualZoom()), (int)((pt.y - lBounds.y) / panel.getActualZoom()));
						// Snap to grid if not holding ctrl
						if(!Main.mapPanel.ctrlPressed) {
							coords.x /= 8;
							coords.y /= 8;
							coords.x *= 8;
							coords.y *= 8;
						}
						
						if(panel.selectedLevel == level) {
							if(Main.editingPanel.placements.getCurrentPlacementType() == PlacementType.Decal) {
								Decal decal = new Decal(Main.editingPanel.placements.decalList.getSelectedValue());
								decal.x = coords.x;
								decal.y = coords.y;
								
								if(Main.editingPanel.placements.decalFg.isSelected()) {
									level.fgDecals.items.add(decal);
								} else {
									level.bgDecals.items.add(decal);
								}
							} else {
								Entity entity = Entity.fromPlacementConfig(PlacementsTab.placementConfig.get((Main.editingPanel.placements.getCurrentPlacementType() == PlacementType.Entity ? Main.editingPanel.placements.entityList : Main.editingPanel.placements.triggerList).getSelectedValue()));
								entity.x = coords.x;
								entity.y = coords.y;
								
								if(Main.editingPanel.placements.getCurrentPlacementType() == PlacementType.Entity) {
									level.entities.items.add(entity);
								} else if(Main.editingPanel.placements.getCurrentPlacementType() == PlacementType.Trigger) {
									level.triggers.items.add(entity);
								}
							}
						}
						panel.selectedLevel = level;
						
						return;
					}
				}
			}
		} else if(Main.editingPanel.getCurrentPanel() == EditPanel.Entities || Main.editingPanel.getCurrentPanel() == EditPanel.Selection) {
			Entity openPopupFor = null;
			boolean done = false;
			boolean isTrigger = false;
			// Prioritize nodes of the selected entity
			if(panel.selectedEntity != null) {
				EntityConfig ec = Main.entityConfig.get(panel.selectedEntity.name);
				isTrigger = ec == null;
				for(int i = 0; i < panel.selectedEntity.nodes.size(); i++) {
					Rectangle eBounds = panel.selectedEntity.getBounds(panel.selectedLevel, i, panel.offset, panel.getActualZoom());
					
					if(eBounds.contains(pt)) {
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
			
			if(Main.loadedMap != null) {
				for(Level level : Main.loadedMap.levels) {
					Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
					if(lBounds.contains(pt) && panel.selectedLevel != level) {
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
						if(eBounds.contains(pt)) {
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
						if(decals != null) {
							for(int i = 0; i < decals.items.size(); i++) {
								Decal decal = (Decal)decals.items.get(i);
								TextureArea texArea = decal.getTextureArea();
								Rectangle dBounds = new Rectangle((int)((level.bounds.x + decal.x - texArea.width / 2 * Math.abs(decal.scaleX) + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + decal.y - texArea.height / 2 * Math.abs(decal.scaleY) + panel.offset.y) * panel.getActualZoom()), (int)(texArea.width * Math.abs(decal.scaleX) * panel.getActualZoom()), (int)(texArea.height * Math.abs(decal.scaleY) * panel.getActualZoom()));
								
								if(dBounds.contains(pt)) {
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
						if(tBounds.contains(pt)) {
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
			}
			
			panel.selectedEntity = null;
			panel.selectedNode = -1;
		} else if(Main.loadedMap != null) {
			for(Level level : Main.loadedMap.levels) {
				Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
				if(lBounds.contains(pt) && panel.selectedLevel != level) {
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
		Point pt = mouseCoordsToOpenGL(e.getPoint(), e.getComponent());
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
			if(Main.editingPanel.getCurrentPanel() == EditPanel.Selection || (Main.editingPanel.getCurrentPanel() == EditPanel.Entities && !Main.editingPanel.placements.isPlacementSelected())) {
				if(panel.selectedEntity != null) {
					int x = panel.selectedEntity.x;
					int y = panel.selectedEntity.y;
					if(panel.selectedNode >= 0) {
						x = panel.selectedEntity.nodes.get(panel.selectedNode).x;
						y = panel.selectedEntity.nodes.get(panel.selectedNode).y;
					}
					
					Rectangle eBounds = panel.selectedEntity.getBounds(panel.selectedLevel, panel.selectedNode, panel.offset, panel.getActualZoom());
					if(eBounds.contains(pt)) {
						panel.draggingEntity = true;
						dragStart = pt;
						dragStart.setLocation(dragStart.x / panel.getActualZoom(panel.getZoom()), dragStart.y / panel.getActualZoom(panel.getZoom()));
						dragStart.translate(-x, -y);
					}
				} else if(panel.selectedDecal != null) {
					TextureArea texArea = panel.selectedDecal.getTextureArea();
					Rectangle dBounds = new Rectangle((int)((panel.selectedLevel.bounds.x + panel.selectedDecal.x - texArea.width / 2 * Math.abs(panel.selectedDecal.scaleX) + panel.offset.x) * panel.getActualZoom()), (int)((panel.selectedLevel.bounds.y + panel.selectedDecal.y - texArea.height / 2 * Math.abs(panel.selectedDecal.scaleY) + panel.offset.y) * panel.getActualZoom()), (int)(texArea.width * Math.abs(panel.selectedDecal.scaleX) * panel.getActualZoom()), (int)(texArea.height * Math.abs(panel.selectedDecal.scaleY) * panel.getActualZoom()));
						
					if(dBounds.contains(pt)) {
						panel.draggingEntity = true;
						dragStart = pt;
						dragStart.setLocation(dragStart.x / panel.getActualZoom(panel.getZoom()), dragStart.y / panel.getActualZoom(panel.getZoom()));
						dragStart.translate(-panel.selectedDecal.x, -panel.selectedDecal.y);
					}
				}
				
				if(!panel.draggingEntity && Main.editingPanel.getCurrentPanel() == EditPanel.Selection) {
					if(Main.loadedMap != null) {
						for(Level level : Main.loadedMap.levels) {
							Rectangle lBounds = new Rectangle((int)((level.bounds.x + panel.offset.x) * panel.getActualZoom()), (int)((level.bounds.y + panel.offset.y) * panel.getActualZoom()), (int)(level.bounds.width * panel.getActualZoom()), (int)(level.bounds.height * panel.getActualZoom()));
							if(new Rectangle(lBounds.x - 4, lBounds.y, 8, lBounds.height).contains(pt)) {
								panel.selectedEdge = LevelEdge.Left;
							} else if(new Rectangle(lBounds.x + lBounds.width - 4, lBounds.y, 8, lBounds.height).contains(pt)) {
								panel.selectedEdge = LevelEdge.Right;
							} else if(new Rectangle(lBounds.x, lBounds.y - 4, lBounds.width, 8).contains(pt)) {
								panel.selectedEdge = LevelEdge.Top;
							} else if(new Rectangle(lBounds.x, lBounds.y + lBounds.height - 4, lBounds.width, 8).contains(pt)) {
								panel.selectedEdge = LevelEdge.Bottom;
							} else {
								panel.selectedEdge = LevelEdge.None;
							}
							
							if(panel.selectedEdge != LevelEdge.None) {
								if(panel.selectedLevel != level) {
									panel.selectedLevel = level;
								}
								return;
							}
						}
						
						if(panel.selectedEdge == LevelEdge.None) {
							// TODO Select filler edges
						}
					}
				}
			} else if(Main.editingPanel.getCurrentPanel() == EditPanel.Tiles && Main.loadedMap != null) {
				placeTile(pt, MouseAction.PRESSED);
			}
		}
		
		if(button3Down || (button1Down && panel.altPressed)) {
			dragStart = pt;
			dragStart.setLocation(dragStart.x * panel.getActualZoom(-panel.getZoom()), dragStart.y * panel.getActualZoom(-panel.getZoom()));
			dragStart.translate(-panel.offset.x, -panel.offset.y);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Point pt = mouseCoordsToOpenGL(e.getPoint(), e.getComponent());
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
		
		if(Main.editingPanel.getCurrentPanel() == EditPanel.Tiles && Main.loadedMap != null) {
			placeTile(pt, MouseAction.RELEASED);
		} else if(Main.mapPanel.selectedEdgeOffset != 0) {
			switch(Main.mapPanel.selectedEdge) {
			case Right:
				panel.selectedLevel.bounds.width += Main.mapPanel.selectedEdgeOffset * 8;
			    for(int i = 0; i < panel.selectedLevel.solids.tileMap.length; i++) {
			    	if(panel.selectedLevel.solids.tileMap[i].length > panel.selectedLevel.bounds.width / 8) {
			    		panel.selectedLevel.solids.tileMap[i] = Arrays.copyOf(panel.selectedLevel.solids.tileMap[i], panel.selectedLevel.bounds.width / 8);
			    	}
		    	}
			    for(int i = 0; i < panel.selectedLevel.bg.tileMap.length; i++) {
			    	if(panel.selectedLevel.bg.tileMap[i].length > panel.selectedLevel.bounds.width / 8) {
			    		panel.selectedLevel.bg.tileMap[i] = Arrays.copyOf(panel.selectedLevel.bg.tileMap[i], panel.selectedLevel.bounds.width / 8);
			    	}
		    	}
				break;
			case Bottom:
				panel.selectedLevel.bounds.height += Main.mapPanel.selectedEdgeOffset * 8;
				char[][] newSolids = Arrays.copyOf(panel.selectedLevel.solids.tileMap, panel.selectedLevel.bounds.height / 8);
			    for(int i = panel.selectedLevel.solids.tileMap.length; i < panel.selectedLevel.bounds.height / 8; i++) {
			    	newSolids[i] = new char[0];
			    }
				panel.selectedLevel.solids.tileMap = newSolids;
				
				char[][] newBg = Arrays.copyOf(panel.selectedLevel.bg.tileMap, panel.selectedLevel.bounds.height / 8);
			    for(int i = panel.selectedLevel.bg.tileMap.length; i < panel.selectedLevel.bounds.height / 8; i++) {
			    	newBg[i] = new char[0];
			    }
				panel.selectedLevel.bg.tileMap = newBg;
				break;
			case Top:
				panel.selectedLevel.bounds.y += Main.mapPanel.selectedEdgeOffset * 8;
				panel.selectedLevel.bounds.height -= Main.mapPanel.selectedEdgeOffset * 8;
				if(Main.mapPanel.selectedEdgeOffset < 0) {
					newSolids = Arrays.copyOf(panel.selectedLevel.solids.tileMap, panel.selectedLevel.bounds.height / 8);
				    for(int i = newSolids.length + Main.mapPanel.selectedEdgeOffset - 1; i >= 0; i--) {
				    	newSolids[i - Main.mapPanel.selectedEdgeOffset] = newSolids[i];
				    }
				    for(int i = 0; i < -Main.mapPanel.selectedEdgeOffset; i++) {
				    	newSolids[i] = new char[0];
				    }
					panel.selectedLevel.solids.tileMap = newSolids;
					
					newBg = Arrays.copyOf(panel.selectedLevel.bg.tileMap, panel.selectedLevel.bounds.height / 8);
				    for(int i = newBg.length + Main.mapPanel.selectedEdgeOffset - 1; i >= 0; i--) {
				    	newBg[i - Main.mapPanel.selectedEdgeOffset] = newBg[i];
				    }
				    for(int i = 0; i < -Main.mapPanel.selectedEdgeOffset; i++) {
				    	newBg[i] = new char[0];
				    }
					panel.selectedLevel.bg.tileMap = newBg;
				} else {
				    for(int i = Main.mapPanel.selectedEdgeOffset; i < panel.selectedLevel.solids.tileMap.length; i++) {
				    	panel.selectedLevel.solids.tileMap[i - Main.mapPanel.selectedEdgeOffset] = panel.selectedLevel.solids.tileMap[i];
				    }
					panel.selectedLevel.solids.tileMap = Arrays.copyOf(panel.selectedLevel.solids.tileMap, panel.selectedLevel.bounds.height / 8);
					
					for(int i = Main.mapPanel.selectedEdgeOffset; i < panel.selectedLevel.bg.tileMap.length; i++) {
				    	panel.selectedLevel.bg.tileMap[i - Main.mapPanel.selectedEdgeOffset] = panel.selectedLevel.bg.tileMap[i];
				    }
					panel.selectedLevel.bg.tileMap = Arrays.copyOf(panel.selectedLevel.bg.tileMap, panel.selectedLevel.bounds.height / 8);
				}
				
				for(ElementEncoded ee : panel.selectedLevel.triggers.items) {
					Entity t = (Entity) ee;
					t.y -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				for(ElementEncoded ee : panel.selectedLevel.entities.items) {
					Entity entity = (Entity) ee;
					entity.y -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				for(ElementEncoded ee : panel.selectedLevel.fgDecals.items) {
					Decal d = (Decal) ee;
					d.y -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				for(ElementEncoded ee : panel.selectedLevel.bgDecals.items) {
					Decal d = (Decal) ee;
					d.y -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				break;
			case Left:
				panel.selectedLevel.bounds.x += Main.mapPanel.selectedEdgeOffset * 8;
				panel.selectedLevel.bounds.width -= Main.mapPanel.selectedEdgeOffset * 8;
				if(Main.mapPanel.selectedEdgeOffset < 0) {
					for(int i = 0; i < panel.selectedLevel.solids.tileMap.length; i++) {
						if(panel.selectedLevel.solids.tileMap[i].length > 0) {
							panel.selectedLevel.solids.tileMap[i] = Arrays.copyOf(panel.selectedLevel.solids.tileMap[i], panel.selectedLevel.bounds.width / 8);
							// Shift the row over
							for(int j = panel.selectedLevel.bounds.width / 8 + Main.mapPanel.selectedEdgeOffset - 1; j >= 0; j--) {
								panel.selectedLevel.solids.tileMap[i][j - Main.mapPanel.selectedEdgeOffset] = panel.selectedLevel.solids.tileMap[i][j];
								panel.selectedLevel.solids.tileMap[i][j] = 0;
							}
						}
					}
					for(int i = 0; i < panel.selectedLevel.bg.tileMap.length; i++) {
						if(panel.selectedLevel.bg.tileMap[i].length > 0) {
							panel.selectedLevel.bg.tileMap[i] = Arrays.copyOf(panel.selectedLevel.bg.tileMap[i], panel.selectedLevel.bounds.width / 8);
							// Shift the row over
							for(int j = panel.selectedLevel.bounds.width / 8 + Main.mapPanel.selectedEdgeOffset - 1; j >= 0; j--) {
								panel.selectedLevel.bg.tileMap[i][j - Main.mapPanel.selectedEdgeOffset] = panel.selectedLevel.bg.tileMap[i][j];
								panel.selectedLevel.bg.tileMap[i][j] = 0;
							}
						}
					}
				} else {
					for(int i = 0; i < panel.selectedLevel.solids.tileMap.length; i++) {
						if(panel.selectedLevel.solids.tileMap[i].length > 0) {
							// Shift the row over
							for(int j = Main.mapPanel.selectedEdgeOffset; j < panel.selectedLevel.solids.tileMap[i].length; j++) {
								panel.selectedLevel.solids.tileMap[i][j - Main.mapPanel.selectedEdgeOffset] = panel.selectedLevel.solids.tileMap[i][j];
							}
							panel.selectedLevel.solids.tileMap[i] = Arrays.copyOf(panel.selectedLevel.solids.tileMap[i], Math.max(0, panel.selectedLevel.bounds.width / 8));
						}
					}
					for(int i = 0; i < panel.selectedLevel.bg.tileMap.length; i++) {
						if(panel.selectedLevel.bg.tileMap[i].length > 0) {
							// Shift the row over
							for(int j = Main.mapPanel.selectedEdgeOffset; j < panel.selectedLevel.bg.tileMap[i].length; j++) {
								panel.selectedLevel.bg.tileMap[i][j - Main.mapPanel.selectedEdgeOffset] = panel.selectedLevel.bg.tileMap[i][j];
							}
							panel.selectedLevel.bg.tileMap[i] = Arrays.copyOf(panel.selectedLevel.bg.tileMap[i], Math.max(0, panel.selectedLevel.bounds.width / 8));
						}
					}
				}
				
				for(ElementEncoded ee : panel.selectedLevel.triggers.items) {
					Entity t = (Entity) ee;
					t.x -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				for(ElementEncoded ee : panel.selectedLevel.entities.items) {
					Entity entity = (Entity) ee;
					entity.x -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				for(ElementEncoded ee : panel.selectedLevel.fgDecals.items) {
					Decal d = (Decal) ee;
					d.x -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				for(ElementEncoded ee : panel.selectedLevel.bgDecals.items) {
					Decal d = (Decal) ee;
					d.x -= Main.mapPanel.selectedEdgeOffset * 8;
				}
				break;
			case None:
				break;
			}
			panel.selectedLevel.roomTexture = null;
			panel.selectedLevel.solids.setSize(panel.selectedLevel.bounds.width / 8, panel.selectedLevel.bounds.height / 8);
			panel.selectedLevel.bg.setSize(panel.selectedLevel.bounds.width / 8, panel.selectedLevel.bounds.height / 8);
			Main.mapPanel.selectedEdgeOffset = 0;
			Main.mapPanel.selectedEdge = LevelEdge.None;
		}
		
		panel.draggingEntity = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double oldActualZoom = panel.getActualZoom();
		panel.setZoom(panel.getZoom() + e.getUnitsToScroll());
		Dimension panelDims = panel.panel.getSize();
		System.out.println(panel.getActualZoom() + " " + oldActualZoom + " " + (panelDims.height / panel.getActualZoom() - panelDims.height / oldActualZoom) + " " + panel.panel.getSize() + " " + ((oldActualZoom - panel.getActualZoom()) * panel.panel.getSize().height / 16));
		
		panel.offset.y -= (panelDims.height / panel.getActualZoom() - panelDims.height / oldActualZoom) / 4;
		panel.offset.x += (panelDims.width / panel.getActualZoom() - panelDims.width / oldActualZoom) / 4;
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
					if(Main.editingPanel.tiles.selectedFg) {
						Main.editingPanel.tiles.selectedTileTool.drawAt(level.solids, Main.editingPanel.tiles.selectedTiletype, tileCoords, action);
					} else {
						Main.editingPanel.tiles.selectedTileTool.drawAt(level.bg, Main.editingPanel.tiles.selectedTiletype, tileCoords, action);
					}
				}
			}
		}
	}

}
