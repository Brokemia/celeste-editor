package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import celesteeditor.data.Entity;
import celesteeditor.util.EntityProperty.PropertyType;

public class NewPropertyPopup extends JFrame {
	
	public static final int width = 450;
	
	public static NewPropertyPopup currentPopup;
	
	public Entity entity;
	
	public JTextField nameField;
	
	public JComboBox<PropertyType> propertyTypeBox;
	
	private JButton submitButton;
	
	public NewPropertyPopup(Entity e) {
		super("New Property");
		if(currentPopup != null) {
			currentPopup.dispose();
		}
		currentPopup = this;
		entity = e;
		
		JPanel entryPanel = new JPanel();
		entryPanel.setLayout(new GridLayout(2, 2));
		entryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		entryPanel.add(new JLabel("Type", JLabel.CENTER));
		entryPanel.add(new JLabel("Name", JLabel.CENTER));
		
	    propertyTypeBox = new JComboBox<PropertyType>(PropertyType.values());
	    entryPanel.add(propertyTypeBox);
	    entryPanel.add(nameField = new JTextField());
	    add(entryPanel);
		
	    submitButton = new JButton("Add");
		add(submitButton, BorderLayout.SOUTH);
	    
		setSize(width, 200);
		setLocationRelativeTo(null);
	}
	
	public void setOnAddAction(ActionListener listener) {
		submitButton.addActionListener(listener);
	}
	
}
