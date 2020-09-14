package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import celesteeditor.editing.BrushTileTool;
import celesteeditor.editing.RectangleTileTool;
import celesteeditor.editing.TileTool;
import celesteeditor.editing.Tiletype;
import celesteeditor.util.Util;

public class TilesTab extends JPanel {
	
	public JPanel fgTiles = new JPanel();
	
	public JPanel bgTiles = new JPanel();
	
	public static final int TOOLS_PER_ROW = 4;
	
	public ArrayList<TileTool> tileTools = new ArrayList<>(Arrays.asList(new BrushTileTool(new ImageIcon(Util.getImage("/assets/paintbrush.png"))),
			new RectangleTileTool(new ImageIcon(Util.getImage("/assets/rect.png")))));
	
	public TileTool selectedTileTool;
	
	public Tiletype selectedTiletype;
	
	public static ArrayList<Tiletype> fgTileTypes = new ArrayList<>();
	
	public static ArrayList<Tiletype> bgTileTypes = new ArrayList<>();
	
	public JPopupMenu rightClickFgTiletype = new JPopupMenu();
	
	public JPopupMenu rightClickBgTiletype = new JPopupMenu();
	
	public TilesTab() {
		setupRightClickMenu();
		setLayout(new BorderLayout());
		JPanel toolsPanel = new JPanel();
		toolsPanel.setLayout(new GridLayout((int)Math.ceil((tileTools.size() + 1) / (float)TOOLS_PER_ROW), TOOLS_PER_ROW));
		toolsPanel.setBorder(new LineBorder(Color.black));
		JLabel addTool = new JLabel(new ImageIcon(Util.getImage("/assets/add.png")));
		addTool.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				EditTiletypePopup popup = new EditTiletypePopup(null);
				popup.setVisible(true);
			}
		});
		addTool.addMouseListener(new ToolMouseListener(null, addTool));
		toolsPanel.add(addTool);
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
		fgTiles.setLayout(new GridLayout(Math.max(Math.max(fgTileTypes.size(), bgTileTypes.size()), 10), 1));
		bgTiles.setLayout(new GridLayout(Math.max(Math.max(fgTileTypes.size(), bgTileTypes.size()), 10), 1));
		JLabel fgTitle = new JLabel("Foreground", JLabel.CENTER);
		Font headerFont = new Font(fgTitle.getFont().getName(), Font.BOLD, fgTitle.getFont().getSize() + 5);
		fgTitle.setFont(headerFont);
		fgTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.black));
		JLabel bgTitle = new JLabel("Background", JLabel.CENTER);
		bgTitle.setFont(headerFont);
		bgTitle.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.black));
		fgTiles.add(fgTitle);
		bgTiles.add(bgTitle);
		for(int i = 0; i < fgTileTypes.size(); i++) {
			addNewTiletype(fgTileTypes.get(i));
		}
		for(int i = 0; i < bgTileTypes.size(); i++) {
			addNewTiletype(bgTileTypes.get(i));
		}
		add(tileScroll);
	}
	
	public void setupRightClickMenu() {
		JMenuItem editFg = new JMenuItem("Edit Tiletype");
		editFg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JLabel tileLabel = (JLabel)((Box)rightClickFgTiletype.getInvoker()).getComponent(0);
				String name = tileLabel.getText();
				Tiletype tt = fgTileTypes.stream().filter((t) -> t.name.equals(name)).findFirst().get();
				EditTiletypePopup popup = new EditTiletypePopup(tt);
				popup.tileNameLabel = tileLabel;
				popup.setVisible(true);
			}
		});
		rightClickFgTiletype.add(editFg);
		
		JMenuItem deleteFg = new JMenuItem("Delete Tiletype");
		deleteFg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JLabel tileLabel = (JLabel)((Box)rightClickFgTiletype.getInvoker()).getComponent(0);
				String name = tileLabel.getText();
				Tiletype tt = fgTileTypes.stream().filter((t) -> t.name.equals(name)).findFirst().get();
				fgTiles.remove(tileLabel.getParent());
				fgTileTypes.remove(tt);
				File tileFile = new File("config/tile/fg" + tt.name + ".config");
				if(tileFile.exists()) {
					tileFile.delete();
				}
				if(EditTiletypePopup.currentPopup != null && EditTiletypePopup.currentPopup.tileType.equals(tt)) {
					EditTiletypePopup.currentPopup.dispose();
				}
				revalidate();
				repaint();
			}
		});
		rightClickFgTiletype.add(deleteFg);
		
		
		JMenuItem editBg = new JMenuItem("Edit Tiletype");
		editBg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JLabel tileLabel = (JLabel)((Box)rightClickBgTiletype.getInvoker()).getComponent(0);
				String name = tileLabel.getText();
				Tiletype tt = bgTileTypes.stream().filter((t) -> t.name.equals(name)).findFirst().get();
				EditTiletypePopup popup = new EditTiletypePopup(tt);
				popup.tileNameLabel = tileLabel;
				popup.setVisible(true);
			}
		});
		rightClickBgTiletype.add(editBg);
		
		JMenuItem deleteBg = new JMenuItem("Delete Tiletype");
		deleteBg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JLabel tileLabel = (JLabel)((Box)rightClickBgTiletype.getInvoker()).getComponent(0);
				String name = tileLabel.getText();
				Tiletype tt = bgTileTypes.stream().filter((t) -> t.name.equals(name)).findFirst().get();
				bgTiles.remove(tileLabel.getParent());
				bgTileTypes.remove(tt);
				File tileFile = new File("config/tile/bg" + tt.name + ".config");
				if(tileFile.exists()) {
					tileFile.delete();
				}
				if(EditTiletypePopup.currentPopup != null && tt.equals(EditTiletypePopup.currentPopup.tileType)) {
					EditTiletypePopup.currentPopup.dispose();
				}
				revalidate();
				repaint();
			}
		});
		rightClickBgTiletype.add(deleteBg);
	}
	
	public void addNewTiletype(Tiletype t) {
		if(t.tile == 0) return;
		JLabel tileType = new JLabel(t.name, JLabel.CENTER);
		tileType.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
		Box b = Box.createHorizontalBox();
		if(t.fg) {
			b.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.black));
		} else {
			b.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.black));
		}
		b.add(tileType);
		if(!t.name.equals("Air")) {
			if(t.fg) {
				b.setComponentPopupMenu(rightClickFgTiletype);
			} else {
				b.setComponentPopupMenu(rightClickBgTiletype);
			}
		}
		b.addMouseListener(new TiletypeMouseListener(t, b));
		if(t.fg) {
			fgTiles.add(b);
		} else {
			bgTiles.add(b);
		}
		((GridLayout)fgTiles.getLayout()).setRows(Math.max(Math.max(fgTileTypes.size(), bgTileTypes.size()), 10));
		fgTiles.revalidate();
		((GridLayout)bgTiles.getLayout()).setRows(Math.max(Math.max(fgTileTypes.size(), bgTileTypes.size()), 10));
		bgTiles.revalidate();
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
		
		public Tiletype tileType;
		
		public Box box;
		
		public TiletypeMouseListener(Tiletype t, Box b) {
			tileType = t;
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
