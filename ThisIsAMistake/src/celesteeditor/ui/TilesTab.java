package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import celesteeditor.Main;
import celesteeditor.editing.BrushTileTool;
import celesteeditor.editing.RectangleTileTool;
import celesteeditor.editing.TileTool;
import celesteeditor.ui.autotiler.TerrainType;
import celesteeditor.util.Util;

public class TilesTab extends JPanel {
	
	public JPanel fgTiles = new JPanel();
	
	public JPanel bgTiles = new JPanel();
	
	public static final int TOOLS_PER_ROW = 4;
	
	public ArrayList<TileTool> tileTools = new ArrayList<>(Arrays.asList(new BrushTileTool(new ImageIcon(Util.getImage("/assets/paintbrush.png"))),
			new RectangleTileTool(new ImageIcon(Util.getImage("/assets/rect.png")))));
	
	public TileTool selectedTileTool;
	
	public TerrainType selectedTiletype;
	
	public boolean selectedFg = true;
	
	public TilesTab() {
		setLayout(new BorderLayout());
		JPanel toolsPanel = new JPanel();
		toolsPanel.setLayout(new GridLayout((int)Math.ceil((tileTools.size() + 1) / (float)TOOLS_PER_ROW), TOOLS_PER_ROW));
		toolsPanel.setBorder(new LineBorder(Color.black));
		for(TileTool tool : tileTools) {
			JLabel toolLbl = new JLabel(tool.icon);
			toolLbl.addMouseListener(new ToolMouseListener(tool, toolLbl));
			toolsPanel.add(toolLbl);
		}
		add(toolsPanel, BorderLayout.NORTH);
		JPanel tileTypesPanel = new JPanel();
		JScrollPane tileScroll = new JScrollPane(tileTypesPanel);
		tileTypesPanel.setLayout(new GridLayout(1, 2));
		tileTypesPanel.add(fgTiles);
		tileTypesPanel.add(bgTiles);
		int size = Math.max(Math.max(Main.fgAutotiler.tileTypes.size()+2, Main.bgAutotiler.tileTypes.size()+2), 10);
		System.out.println(size);
		fgTiles.setLayout(new GridLayout(size, 1));
		bgTiles.setLayout(new GridLayout(size, 1));
		JLabel fgTitle = new JLabel("Foreground", JLabel.CENTER);
		Font headerFont = new Font(fgTitle.getFont().getName(), Font.BOLD, fgTitle.getFont().getSize() + 5);
		fgTitle.setFont(headerFont);
		fgTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.black));
		JLabel bgTitle = new JLabel("Background", JLabel.CENTER);
		bgTitle.setFont(headerFont);
		bgTitle.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.black));
		fgTiles.add(fgTitle);
		bgTiles.add(bgTitle);
		addNewTiletype(new TerrainType("air", '0'), true);
		addNewTiletype(new TerrainType("air", '0'), false);
		for(TerrainType t : Main.fgAutotiler.tileTypes.values()) {
			addNewTiletype(t, true);
		}
		for(TerrainType t : Main.bgAutotiler.tileTypes.values()) {
			addNewTiletype(t, false);
		}
		add(tileScroll);
	}
	
	public void addNewTiletype(TerrainType t, boolean fg) {
		if(t.ID == 0) return;
		JLabel tileType = new JLabel(t.name, JLabel.CENTER);
		tileType.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
		Box b = Box.createHorizontalBox();
		if(fg) {
			b.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.black));
		} else {
			b.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.black));
		}
		b.add(tileType);
		b.addMouseListener(new TiletypeMouseListener(t, fg, b));
		if(fg) {
			fgTiles.add(b);
		} else {
			bgTiles.add(b);
		}
	}
	
private class ToolMouseListener extends MouseAdapter {
		
		public final Color hoveringBg = new Color(40, 160, 80);
		
		public final Color selectedBg = new Color(20, 95, 40);
		
		public TileTool tool;
		
		public JLabel toolLbl;
		
		public ToolMouseListener(TileTool t, JLabel lbl) {
			tool = t;
			toolLbl = lbl;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(tool != null) {
				for(Component c : toolLbl.getParent().getComponents()) {
					if(c instanceof JLabel) {
						((JLabel) c).setOpaque(false);
					}
				}
				selectedTileTool = tool;
				toolLbl.setBackground(selectedBg);
				toolLbl.setOpaque(true);
				toolLbl.repaint();
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if(selectedTileTool != tool || tool == null) {
				toolLbl.setBackground(hoveringBg);
				toolLbl.setOpaque(true);
				toolLbl.repaint();
			}
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			if(selectedTileTool != tool || tool == null) {
				toolLbl.setOpaque(false);
				toolLbl.repaint();
			}
		}
		
	}
	
	private class TiletypeMouseListener extends MouseAdapter {
		
		public final Color hoveringBg = new Color(40, 160, 80);
		
		public final Color selectedBg = new Color(20, 95, 40);
		
		public TerrainType tileType;
		
		public boolean fg;
		
		public Box box;
		
		public TiletypeMouseListener(TerrainType t, boolean fg, Box b) {
			tileType = t;
			this.fg = fg;
			box = b;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			for(Component c : fgTiles.getComponents()) {
				if(c instanceof Box) {
					((Box) c).setOpaque(false);
				}
			}
			for(Component c : bgTiles.getComponents()) {
				if(c instanceof Box) {
					((Box) c).setOpaque(false);
				}
			}
			selectedTiletype = tileType;
			selectedFg = fg;
			box.setBackground(selectedBg);
			box.setOpaque(true);
			box.repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if(selectedTiletype != tileType || tileType == null) {
				box.setBackground(hoveringBg);
				box.setOpaque(true);
				box.repaint();
			}
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			if(selectedTiletype != tileType || tileType == null) {
				box.setOpaque(false);
				box.repaint();
			}
		}
		
	}

}
