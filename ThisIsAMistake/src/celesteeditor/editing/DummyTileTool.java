package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

import celesteeditor.data.TileLevelLayer;

public class DummyTileTool extends TileTool {
	
	public DummyTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(TileLevelLayer tileLayer, Tiletype tileType, Point p, MouseAction action) {
		
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
