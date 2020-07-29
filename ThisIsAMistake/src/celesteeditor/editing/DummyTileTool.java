package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

public class DummyTileTool extends TileTool {
	
	public DummyTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(char[][] tileMap, Tiletype tileType, Point p, MouseAction action) {
		
	}

	@Override
	public boolean[][] getTileOverlay() {
		return null;
	}

	@Override
	public Point getTileOverlayPos() {
		return null;
	}
	
}
