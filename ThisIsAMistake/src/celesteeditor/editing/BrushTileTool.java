package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

public class BrushTileTool extends TileTool {
	
	public BrushTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(char[][] tileMap, Tiletype tileType, Point p, MouseAction action) {
		placeTile(tileMap, tileType, p);
	}

	@Override
	public boolean[][] getTileOverlay() {
		return new boolean[][] {{true}};
	}

	@Override
	public Point getTileOverlayPos() {
		return lastMousePos;
	}
	
}
