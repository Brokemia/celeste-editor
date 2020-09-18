package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

import celesteeditor.data.TileLevelLayer;

public abstract class TileTool {
	
	public enum MouseAction {
		PRESSED, RELEASED, DRAGGED
	}
	
	public Point lastMousePos;
	
	public String name;
	
	public Icon icon;
	
	public abstract boolean[][] getTileOverlay();
	
	public abstract Point getTileOverlayPos();
	
	public abstract void drawAt(TileLevelLayer tileLayer, Tiletype tileType, Point p, MouseAction action);
	
	public void placeTile(TileLevelLayer tileLayer, Tiletype tileType, Point p) {
		char[][] tileMap = tileLayer.tileMap;
		
		if(tileMap.length <= p.y) {
			char[][] copy = new char[Math.max(tileMap.length, p.y+1)][];
			for(int i = 0; i < copy.length; i++) {
				if(i < tileMap.length) {
					copy[i] = tileMap[i];
				} else {
					copy[i] = new char[p.x+1];
				}
			}
			
			tileMap = copy;
			tileLayer.tileMap = tileMap;
		}
		if(tileMap[p.y].length <= p.x) {
			char[] row = tileMap[p.y];
			tileMap[p.y] = new char[p.x + 1];
			for(int i = 0; i < row.length; i++) {
				tileMap[p.y][i] = row[i];
			}
		}
		tileMap[p.y][p.x] = tileType.tile;
	}
	
}
