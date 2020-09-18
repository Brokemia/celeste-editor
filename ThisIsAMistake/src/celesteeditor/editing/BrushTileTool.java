package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

import celesteeditor.data.TileLevelLayer;

public class BrushTileTool extends TileTool {
	
	public BrushTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(TileLevelLayer tileLayer, Tiletype tileType, Point p, MouseAction action) {
		placeTile(tileLayer, tileType, p);
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
