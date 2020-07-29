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
	
}
