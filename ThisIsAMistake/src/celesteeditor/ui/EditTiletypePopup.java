package celesteeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import celesteeditor.Main;
import celesteeditor.editing.Tiletype;

public class EditTiletypePopup extends JFrame {
	
	public static final int width = 450;
	
	public static EditTiletypePopup currentPopup;
	
	public JLabel tileNameLabel;
	
	public Tiletype tileType;
	
	public JTextField nameField, tileField, colorField;
	
	public JCheckBox fgBox;
		
	private JButton submitButton;
	
	public EditTiletypePopup(Tiletype t) {
		super((t == null ? "Add" : "Edit") + " Tiletype");
		if(currentPopup != null) {
			currentPopup.dispose();
		}
		currentPopup = this;
		tileType = t;
		
		JPanel entryPanel = new JPanel();
		entryPanel.setLayout(new GridLayout(2, t == null ? 4 : 3));
		entryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		entryPanel.add(new JLabel("Name", JLabel.CENTER));
		if(t == null) {
			entryPanel.add(new JLabel("FG", JLabel.CENTER));
		}
		entryPanel.add(new JLabel("Character", JLabel.CENTER));
		entryPanel.add(new JLabel("Color", JLabel.CENTER));
		
	    entryPanel.add(nameField = new JTextField(t == null ? "" : t.name));
	    fgBox = new JCheckBox("", t == null || t.fg);
	    if(t == null) {
	    	entryPanel.add(fgBox);
	    }
	    entryPanel.add(tileField = new JTextField(t == null ? "" : "" + t.tile, 1));
	    entryPanel.add(colorField = new JTextField(t == null ? "" : String.format("%06x", t.color.getRGB() & 0xFFFFFF), 6));
	    add(entryPanel);
		
	    submitButton = new JButton(t == null ? "Add" : "Edit");
	    submitButton.addActionListener(this::actionPerformed);
		add(submitButton, BorderLayout.SOUTH);
	    
		setSize(width, 200);
		setLocationRelativeTo(null);
	}
	
	public void actionPerformed(ActionEvent e) {
		String name = nameField.getText();
		if(name.isBlank()) {
			JOptionPane.showMessageDialog(this, "Tiletype name cannot be blank");
			return;
		}
		if((fgBox.isSelected() && TilesTab.fgTileTypes.stream().anyMatch((t) -> t.name.equals(name) && t != tileType)) ||
				(!fgBox.isSelected() && TilesTab.bgTileTypes.stream().anyMatch((t) -> t.name.equals(name) && t != tileType))) {
			JOptionPane.showMessageDialog(this, "Tiletype name already in use");
			return;
		}
		char tile;
		if(tileField.getText().length() == 1) {
			tile = tileField.getText().charAt(0);
			if((fgBox.isSelected() && TilesTab.fgTileTypes.stream().anyMatch((t) -> t.tile == tile && t != tileType)) ||
					(!fgBox.isSelected() && TilesTab.bgTileTypes.stream().anyMatch((t) -> t.tile == tile && t != tileType))) {
				JOptionPane.showMessageDialog(this, "Tiletype character already in use");
				return;
			}
		} else {
			JOptionPane.showMessageDialog(this, "Tiletype character must be a single character");
			return;
		}
		Color color;
		try {
			color = Color.decode("0x" + colorField.getText());
		} catch(NumberFormatException e1) {
			JOptionPane.showMessageDialog(this, "Color must be a valid hex code");
			return;
		}
		
		boolean add = tileType == null;
		if(add) {
			tileType = new Tiletype();
			if(fgBox.isSelected()) {
				TilesTab.fgTileTypes.add(tileType);
			} else {
				TilesTab.bgTileTypes.add(tileType);
			}
		}
		
		tileType.name = name;
		if(tileNameLabel != null) {
			tileNameLabel.setText(name);
			tileNameLabel.repaint();
		}
		tileType.fg = fgBox.isSelected();
		tileType.tile = tile;
		tileType.color = color;
		if(add) {
			Main.editingPanel.tiles.addNewTiletype(tileType);
		}
		try {
			File config = new File("config/tile/" + (tileType.fg ? "fg" : "bg") + tileType.name + ".config");
			PrintWriter pw = new PrintWriter(config);
			pw.append(tileType.toString());
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		this.dispose();
	}
	
}
