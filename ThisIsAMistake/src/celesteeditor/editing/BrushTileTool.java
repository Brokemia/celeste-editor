package celesteeditor.editing;

import java.awt.Point;

import javax.swing.Icon;

import celesteeditor.data.TileLevelLayer;
import celesteeditor.ui.autotiler.TerrainType;

public class BrushTileTool extends TileTool {
	
	public BrushTileTool(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void drawAt(TileLevelLayer tileLayer, TerrainType tileType, Point p, MouseAction action) {
		placeTile(tileLayer, tileType, p);
	}

	@Override
	public char[][] getTileOverlay(char tile) {
		return new char[][] {{tile}};
	}

	@Override
	public Point getTileOverlayPos() {
		return lastMousePos;
	}
	
}
