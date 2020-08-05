package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import celesteeditor.Main;
import celesteeditor.editing.PlacementConfig;
import celesteeditor.editing.PlacementConfig.PlacementType;
import celesteeditor.util.Util;

public class EntitiesTab extends JPanel {
	
	public static HashMap<String, PlacementConfig> placementConfig = new HashMap<>();
	
	JTabbedPane tabbedPane = new JTabbedPane();
	
	JPanel entities = new JPanel();
	
	JList<String> entityList = new JList<>();
	
	JPanel triggers = new JPanel();
	
	JPopupMenu rightClickEntity = new JPopupMenu();

	public EntitiesTab() {
		setLayout(new BorderLayout());
		tabbedPane.addTab("Entities", entities);
		tabbedPane.addTab("Triggers", triggers);
		add(tabbedPane);
		
		entities.setLayout(new BorderLayout());
		JLabel addTool = new JLabel(new ImageIcon(Util.getImage("/assets/add.png")));
		addTool.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PlacementConfigPopup popup = new PlacementConfigPopup(null, PlacementType.Entity);
				popup.setVisible(true);
			}
		});
		addTool.addMouseListener(new ColoredHoverListener(addTool));
		entities.add(addTool, BorderLayout.NORTH);
		
		entityList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scrollPane = new JScrollPane(entityList);
		entities.add(scrollPane);
		refreshLists();
		setupRightClickMenu();
	}
	
	public void refreshLists() {
		entityList.setListData(placementConfig.entrySet().stream().filter((e) -> e.getValue().placementType == PlacementType.Entity).map((e) -> e.getValue().name).toArray(String[]::new));
		revalidate();
	}
	
	public void setupRightClickMenu() {
		JMenuItem edit = new JMenuItem("Edit Placement");
		edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = entityList.getSelectedValue();
				PlacementConfig config = placementConfig.get(name);
				PlacementConfigPopup popup = new PlacementConfigPopup(config, PlacementType.Entity);
				popup.setVisible(true);
			}
		});
		rightClickEntity.add(edit);
		
		JMenuItem delete = new JMenuItem("Delete Placement");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = entityList.getSelectedValue();
				int result = JOptionPane.showConfirmDialog(Main.mainWindow, "Are you sure you want to delete \"" + name + "\"?", "Delete Placement",
	               JOptionPane.YES_NO_OPTION,
	               JOptionPane.QUESTION_MESSAGE);
	            if(result == JOptionPane.YES_OPTION){
	            	PlacementConfig removed = placementConfig.remove(name);
	            	File configFile = new File("config/placement/" + name + ".config");
					if(configFile.exists()) {
						configFile.delete();
					}
					if(PlacementConfigPopup.currentPopup != null && removed.equals(PlacementConfigPopup.currentPopup.config)) {
						PlacementConfigPopup.currentPopup.dispose();
					}
					refreshLists();
	            }
			}
		});
		rightClickEntity.add(delete);
		
		entityList.addMouseListener( new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	            if(SwingUtilities.isRightMouseButton(e)) {
	                entityList.setSelectedIndex(entityList.locationToIndex(e.getPoint()));
	                rightClickEntity.show(entityList, e.getX(), e.getY());
	            }
	        }
	    });
	}
	
	public static class ColoredHoverListener extends MouseAdapter {
		
		public final Color hoveringBg = new Color(40, 160, 80);
						
		public JLabel buttonLbl;
		
		public ColoredHoverListener(JLabel lbl) {
			buttonLbl = lbl;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			buttonLbl.setBackground(hoveringBg);
			buttonLbl.setOpaque(true);
			buttonLbl.repaint();
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			buttonLbl.setOpaque(false);
			buttonLbl.repaint();
		}
		
	}
	
}
