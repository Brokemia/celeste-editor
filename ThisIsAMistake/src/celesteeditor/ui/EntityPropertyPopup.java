package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import celesteeditor.Main;
import celesteeditor.data.Entity;
import celesteeditor.data.EntityProperty;
import celesteeditor.data.EntityProperty.PropertyType;
import celesteeditor.editing.EntityConfig;
import celesteeditor.util.Util;

public class EntityPropertyPopup extends JFrame {
	
	public static final int width = 450;
	
	public static EntityPropertyPopup currentPopup;
	
	public Entity entity;
	
	public JTextField xField, yField;
	
	public ArrayList<JTextField> propertyFields = new ArrayList<>();
	
	public static JPopupMenu rightClickProperty = new JPopupMenu();
	
	static {
		JMenuItem delete = new JMenuItem("Delete Property");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel propPanel = (JPanel)rightClickProperty.getInvoker();
				String name = ((JLabel)propPanel.getComponent(0)).getText();
				for(int i = 0; i < currentPopup.entity.properties.size(); i++) {
					if(currentPopup.entity.properties.get(i).name.equals(name)) {
						currentPopup.entity.properties.remove(i);
						propPanel.getParent().remove(propPanel);
						currentPopup.revalidate();
						currentPopup.repaint();
						return;
					}
				}
			}
		});
		rightClickProperty.add(delete);
		JMenu changeType = new JMenu("Set Property Type To...");
		JMenuItem stringType = new JMenuItem("String");
		stringType.addActionListener(EntityPropertyPopup::changePropertyType);
	    changeType.add(stringType);
	    JMenuItem integerType = new JMenuItem("Integer");
	    integerType.addActionListener(EntityPropertyPopup::changePropertyType);
	    changeType.add(integerType);
	    JMenuItem floatType = new JMenuItem("Float");
	    floatType.addActionListener(EntityPropertyPopup::changePropertyType);
	    changeType.add(floatType);
	    JMenuItem boolType = new JMenuItem("Boolean");
	    boolType.addActionListener(EntityPropertyPopup::changePropertyType);
	    changeType.add(boolType);
	    rightClickProperty.add(changeType);
	}
	
	private static void changePropertyType(ActionEvent e) {
		JPanel propPanel = (JPanel)rightClickProperty.getInvoker();
		String name = ((JLabel)propPanel.getComponent(0)).getText();
		for(int i = 0; i < currentPopup.entity.properties.size(); i++) {
			if(currentPopup.entity.properties.get(i).name.equals(name)) {
				System.out.println(e.getActionCommand());
				currentPopup.entity.properties.get(i).type = PropertyType.valueOf(e.getActionCommand());
				((JTextField)propPanel.getComponent(1)).setText("");
				currentPopup.revalidate();
				currentPopup.repaint();
				return;
			}
		}
	}
	
	public EntityPropertyPopup(Entity e, boolean isTrigger) {
		super(e.id + " - " + e.name + " Properties");
		if(currentPopup != null) {
			currentPopup.dispose();
		}
		if(DecalPropertyPopup.currentPopup != null) {
			DecalPropertyPopup.currentPopup.dispose();
		}
		currentPopup = this;
		entity = e;
		
		JPanel fieldPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(fieldPanel);
		add(scrollPane);
		JButton submitButton = new JButton("Update");
		submitButton.addActionListener(this::actionPerformed);
		add(submitButton, BorderLayout.SOUTH);
		
		BoxLayout layout = new BoxLayout(fieldPanel, BoxLayout.Y_AXIS);
		fieldPanel.setLayout(layout);
		fieldPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel coordPanel = new JPanel();
		coordPanel.setLayout(new GridLayout(1, 4));
		coordPanel.add(new JLabel("X"));
		coordPanel.add(xField = new JTextField(e.x + "", 5));
		coordPanel.add(new JLabel("Y"));
		coordPanel.add(yField = new JTextField(e.y + "", 5));
		fieldPanel.add(coordPanel);
		
		for(EntityProperty prop : e.properties) {
			JPanel propPanel = createPropPanel(prop);
			if(isTrigger && (prop.name.equals("width") || prop.name.equals("height"))) {
				propPanel.setComponentPopupMenu(null);
			}
			fieldPanel.add(propPanel);
		}
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 3));
		if(isTrigger) {
			topPanel.add(new JPanel());
		} else {
			JLabel configButton = new JLabel(new ImageIcon(Util.getImage("/assets/options.png")));
			configButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					EntityConfig ec = Main.entityConfig.get(entity.name);
					new EntityConfigPopup(ec).setVisible(true);
				}
			});
			topPanel.add(configButton);
		}
		topPanel.add(new JLabel(e.name, JLabel.CENTER));
		JLabel addButton = new JLabel(new ImageIcon(Util.getImage("/assets/add.png")));
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final NewPropertyPopup popup = new NewPropertyPopup(entity);
				popup.setOnAddAction(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String name = popup.nameField.getText();
						if(name.isBlank()) {
							JOptionPane.showMessageDialog(null, "Name cannot be blank");
						} else if(name.equals("id") || name.equals("x") || name.equals("y") || name.equals("originX") || name.equals("originY") || entity.properties.stream().anyMatch((p) -> p.name.equals(name))) {
							JOptionPane.showMessageDialog(null, "Property \"" + name + "\" already exists");
						} else {
							EntityProperty prop = new EntityProperty();
							prop.name = name;
							prop.type = (PropertyType)popup.propertyTypeBox.getSelectedItem();
							switch(prop.type) {
							case Integer:
								prop.value = 0;
								break;
							case Float:
								prop.value = 0f;
								break;
							case Boolean:
								prop.value = false;
								break;
							default:
								prop.value = "";
								break;
							}
							entity.properties.add(prop);
							fieldPanel.add(createPropPanel(prop));
							revalidate();
							repaint();
							popup.dispose();
						}
					}
				});
				
				popup.setVisible(true);
			}
		});
		topPanel.add(addButton);
		add(topPanel, BorderLayout.NORTH);
		
		setSize(width, 200);
		setLocationRelativeTo(null);
	}
	
	public JPanel createPropPanel(EntityProperty prop) {
		JPanel propPanel = new JPanel();
		propPanel.setLayout(new GridLayout(1, 2));
		propPanel.add(new JLabel(prop.name));
		JTextField field = new JTextField(String.valueOf(prop.value));
		propertyFields.add(field);
		propPanel.add(field);
		propPanel.setComponentPopupMenu(rightClickProperty);
		return propPanel;
	}
	
	public void actionPerformed(ActionEvent e) {
		ArrayList<String> invalid = new ArrayList<>();
		
		try {
			entity.x = Integer.parseInt(xField.getText());
		} catch(NumberFormatException ex) {
			invalid.add("x");
		}
		try {
			entity.y = Integer.parseInt(yField.getText());
		} catch(NumberFormatException ex) {
			invalid.add("y");
		}
				
		int i = 0;
		for(EntityProperty prop : entity.properties) {
			try {
				prop.value = EntityProperty.convertFromString(propertyFields.get(i).getText(), prop.type);
			} catch(NumberFormatException ex) {
				invalid.add(prop.name);
			}
			i++;
		}
		
		if(invalid.size() != 0) {
			String msg = "Invalid value" + (invalid.size() == 1 ? "" : "s") + " provided for: ";
			for(int j = 0; j < invalid.size(); j++) {
				msg += invalid.get(j);
				if(j != invalid.size() - 1) {
					msg += ", ";
				}
			}
			JOptionPane.showMessageDialog(this, msg);
		}
		
		this.dispose();
	}
	
}
