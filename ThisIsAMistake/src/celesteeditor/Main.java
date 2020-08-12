package celesteeditor;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import celesteeditor.BinaryPacker.Element;
import celesteeditor.data.Map;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.PlacementConfig;
import celesteeditor.editing.Tiletype;
import celesteeditor.ui.EditingPanel;
import celesteeditor.ui.PlacementsTab;
import celesteeditor.ui.MapPanel;
import celesteeditor.ui.TilesTab;

public class Main {
	
	public static Map loadedMap;
	
	public static JFrame mainWindow;
	
	public static MapPanel mapPanel;
	
	public static EditingPanel editingPanel;
		
	public static HashMap<String, EntityConfig> entityConfig = new HashMap<>();

	public static void main(String[] args) throws FileNotFoundException, IOException {
		//Element map = BinaryPacker.fromBinary("test_out.bin");
		//System.setOut(new PrintStream(new File("test_out.txt")));
		//printElement(map, "");
		//Element levels = map.Children.stream().filter((Element e) -> e.Name.equals("levels")).findFirst().get();
		//for(Element level : levels.Children) {
			//printElement(level, "");
		//}
		//Map m = new Map().fromElement(map);
		//System.out.println(m.toString());
		
//		File mapDir = new File("maps");
//		for(File f : mapDir.listFiles()) {
//			Element e = BinaryPacker.fromBinary(f.getAbsolutePath());
//			//System.out.println(new Map().fromElement(e));
//			Level l = new Map().fromElement(e).levels.get(0);
//			System.out.println("\n\n\n" + e.Package + " - " + l.name + ":");
//			char[][] s = l.solids.getTilemap(l.bounds.width / 8, l.bounds.height / 8);
//			for(int i = 0; i < s[0].length; i++) {
//				System.out.println();
//				for(int j = 0; j < s.length; j++) {
//					System.out.print(s[j][i]);
//				}
//			}
//		}
		
		openMap();
		loadConfig();
		setupMainWindow();
		new Thread(new UpdateThread()).start();
	}
	
	public static void saveMap() throws FileNotFoundException, IOException {
		if(loadedMap == null) return;
		JFileChooser j = new JFileChooser(new File("./")); 
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "Map Binary", "bin");
		j.setFileFilter(filter);
		// Open the save dialog 
		int res = j.showSaveDialog(null);
		
		// if the user selects a file 
        if (res == JFileChooser.APPROVE_OPTION) { 
        	BinaryPacker.toBinary(loadedMap.asElement(), new File(j.getSelectedFile().getAbsolutePath()));
        }
	}
	
	public static void openMap() throws FileNotFoundException, IOException {
		JFileChooser j = new JFileChooser(new File("./")); 
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "Map Binary", "bin");
		j.setFileFilter(filter);
		// Open the save dialog 
		int res = j.showOpenDialog(null);
		
		// if the user selects a file 
        if (res == JFileChooser.APPROVE_OPTION) { 
            loadedMap = new Map().fromElement(BinaryPacker.fromBinary(j.getSelectedFile().getAbsolutePath()));
        }
	}
	
	public static void loadConfig() {
		File configFolder = new File("config");
		if(!configFolder.exists())
			configFolder.mkdir();
		for(File subfolder : configFolder.listFiles()) {
			if(subfolder.isDirectory()) {
				switch(subfolder.getName()) {
				case "entity":
					loadEntityConfig(subfolder);
					break;
				case "tile":
					loadTileConfig(subfolder);
					break;
				case "placement":
					loadPlacementConfig(subfolder);
				}
				
			}
			
		}
	}
	
	public static void loadEntityConfig(File folder) {
		for(File config : folder.listFiles()) {
			if(config.getPath().endsWith(".config")) {
				EntityConfig ec = EntityConfig.fromFile(config);
				entityConfig.put(ec.name, ec);
			}
		}
	}
	
	public static void loadTileConfig(File folder) {
		for(File config : folder.listFiles()) {
			if(config.getPath().endsWith(".config")) {
				Tiletype tt = Tiletype.fromFile(config);
				if(tt.fg) {
					TilesTab.fgTileTypes.add(tt);
				} else {
					TilesTab.bgTileTypes.add(tt);
				}
			}
		}
	}
	
	public static void loadPlacementConfig(File folder) {
		for(File config : folder.listFiles()) {
			if(config.getPath().endsWith(".config")) {
				PlacementConfig pc = PlacementConfig.fromFile(config);
				PlacementsTab.placementConfig.put(pc.name, pc);
			}
		}
	}
	
	public static void setupMainWindow() {
		mainWindow = new JFrame("I don't know what to call this");
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.add(mapPanel = new MapPanel());	
		mapPanel.setMinimumSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width / 3, 100));
		mapPanel.setPreferredSize(new Dimension(2 * Toolkit.getDefaultToolkit().getScreenSize().width / 3, Toolkit.getDefaultToolkit().getScreenSize().height));
		splitPane.add(editingPanel = new EditingPanel());
		mainWindow.add(splitPane);
		mainWindow.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setVisible(true);
	}
	
	static void printElement(Element e, String indent) {
		System.out.println(indent + e.Name);
		if(e.Attributes != null && e.Attributes.size() != 0) {
			System.out.println(indent + " Attr:");
			for (Entry<String, Object> entry : e.Attributes.entrySet()) {
				System.out.println(indent + "  " + entry.getKey() + ": " + entry.getValue() + " (" + entry.getValue().getClass().getCanonicalName() + ")");
			}
		}
		if(e.Children != null && e.Children.size() != 0) {
			System.out.println(indent + " Children:");
			for (Element c : e.Children) {
				printElement(c, indent + "  ");
				//System.out.println(c.Name);
			}
		}
	}

}
