package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

public abstract class TileTool {
	
	public enum MouseAction {
		PRESSED, RELEASED, DRAGGED
	}
	
	public Point lastMousePos;
	
	public String name;
	
	public Icon icon;
	
	public abstract boolean[][] getTileOverlay();
	
	public abstract Point getTileOverlayPos();
	
	public abstract void drawAt(char[][] tileMap, Tiletype tileType, Point p, MouseAction action);
	
	public void placeTile(char[][] tileMap, Tiletype tileType, Point p) {
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
