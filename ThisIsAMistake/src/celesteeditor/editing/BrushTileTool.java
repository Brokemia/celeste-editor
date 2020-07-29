package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

public class BrushTileTool extends TileTool {
	
	public BrushTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(char[][] tileMap, Tiletype tileType, Point p, MouseAction action) {
		tileMap[p.y][p.x] = tileType.tile;
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
