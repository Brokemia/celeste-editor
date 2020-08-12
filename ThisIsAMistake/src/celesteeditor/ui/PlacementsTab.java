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

public class PlacementsTab extends JPanel {
	
	public static HashMap<String, PlacementConfig> placementConfig = new HashMap<>();
	
	JTabbedPane tabbedPane = new JTabbedPane();
	
	JPanel entities = new JPanel();
	
	JList<String> entityList = new JList<>();
	
	JList<String> triggerList = new JList<>();
	
	JPanel triggers = new JPanel();
	
	JPopupMenu rightClickEntity = new JPopupMenu();

	public PlacementsTab() {
		setLayout(new BorderLayout());
		tabbedPane.addTab("Entities", entities);
		tabbedPane.addTab("Triggers", triggers);
		add(tabbedPane);
		
		// Entities
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
		
		// Triggers
		triggers.setLayout(new BorderLayout());
		addTool = new JLabel(new ImageIcon(Util.getImage("/assets/add.png")));
		addTool.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PlacementConfigPopup popup = new PlacementConfigPopup(null, PlacementType.Trigger);
				popup.setVisible(true);
			}
		});
		addTool.addMouseListener(new ColoredHoverListener(addTool));
		triggers.add(addTool, BorderLayout.NORTH);
		
		triggerList.setLayoutOrientation(JList.VERTICAL);
		scrollPane = new JScrollPane(triggerList);
		triggers.add(scrollPane);
		
		refreshLists();
		setupRightClickMenu();
	}
	
	public void refreshLists() {
		entityList.setListData(placementConfig.entrySet().stream().filter((e) -> e.getValue().placementType == PlacementType.Entity).map((e) -> e.getValue().name).toArray(String[]::new));
		triggerList.setListData(placementConfig.entrySet().stream().filter((e) -> e.getValue().placementType == PlacementType.Trigger).map((e) -> e.getValue().name).toArray(String[]::new));
		revalidate();
	}
	
	public void setupRightClickMenu() {
		JMenuItem edit = new JMenuItem("Edit Placement");
		edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = (getCurrentPlacementType() == PlacementType.Entity ? entityList : triggerList).getSelectedValue();
				PlacementConfig config = placementConfig.get(name);
				System.out.println(getCurrentPlacementType());
				PlacementConfigPopup popup = new PlacementConfigPopup(config, getCurrentPlacementType());
				popup.setVisible(true);
			}
		});
		rightClickEntity.add(edit);
		
		JMenuItem delete = new JMenuItem("Delete Placement");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = (getCurrentPlacementType() == PlacementType.Entity ? entityList : triggerList).getSelectedValue();
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
		
		triggerList.addMouseListener( new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	            if(SwingUtilities.isRightMouseButton(e)) {
	            	triggerList.setSelectedIndex(triggerList.locationToIndex(e.getPoint()));
	                rightClickEntity.show(triggerList, e.getX(), e.getY());
	            }
	        }
	    });
	}
	
	public PlacementType getCurrentPlacementType() {
		if(tabbedPane.getSelectedComponent().equals(entities)) {
			return PlacementType.Entity;
		} else if(tabbedPane.getSelectedComponent().equals(triggers)) {
			return PlacementType.Trigger;
		}
		return PlacementType.Decal;
	}
	
	public boolean isPlacementSelected() {
		return (getCurrentPlacementType() == PlacementType.Entity && !entityList.isSelectionEmpty()) || (getCurrentPlacementType() == PlacementType.Trigger && !triggerList.isSelectionEmpty());
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
