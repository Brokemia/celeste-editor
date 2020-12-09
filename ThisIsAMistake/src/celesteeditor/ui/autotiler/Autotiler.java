package celesteeditor.ui.autotiler;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import celesteeditor.AtlasUnpacker;
import celesteeditor.data.TileLevelLayer;

public class Autotiler
{
	private class TerrainType {
		public char ID;

		public HashSet<Character> Ignores;

		public ArrayList<Masked> Masked;

		public Tiles Center;

		public Tiles Padded;

		public int ScanWidth;

		public int ScanHeight;

		public ArrayList<Tiles> CustomFills;

		public HashMap<Byte, String> whitelists;

		public HashMap<Byte, String> blacklists;

		public TerrainType(char id)
		{
			Ignores = new HashSet<Character>();
			Masked = new ArrayList<Masked>();
			Center = new Tiles();
			Padded = new Tiles();
			ID = id;
			whitelists = new HashMap<Byte, String>();
			blacklists = new HashMap<Byte, String>();
		}

		public boolean Ignore(char c)
		{
			if (ID != c)
			{
				if (!Ignores.contains(c))
				{
					return Ignores.contains('*');
				}
				return true;
			}
			return false;
		}
	}

	private class Masked
	{
		public byte[] Mask = new byte[9];

		public Tiles Tiles = new Tiles();
	}

	private class Tiles
	{
		public ArrayList<BufferedImage> Textures = new ArrayList<BufferedImage>();

		public ArrayList<String> OverlapSprites = new ArrayList<String>();

		public boolean HasOverlays;
	}

	public class Generated {
		public BufferedImage[][] tileImg;

		public BufferedImage[][] SpriteOverlay;
	}

	public class Behaviour
	{
		public boolean PaddingIgnoreOutOfLevel;

		public boolean EdgesIgnoreOutOfLevel;

		public boolean EdgesExtend;
	}
	
	private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	public ArrayList<Rectangle> LevelBounds = new ArrayList<Rectangle>();

	private HashMap<Character, TerrainType> lookup = new HashMap<Character, TerrainType>();

	private byte[] adjacent = new byte[9];
	
	// TODO Base off Celeste randomization if possible
	public Random rand = new Random();

	public Autotiler(String filename) throws SAXException, IOException {
		HashMap<Character, Node> map = new HashMap<>();
		Document doc = null;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(filename); 
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		 
		doc.getDocumentElement().normalize();
		NodeList tilesetNodes = doc.getElementsByTagName("Tileset");
		for(int i = 0; i < tilesetNodes.getLength(); i++) {
			Node item = tilesetNodes.item(i);
			NamedNodeMap itemAttr = item.getAttributes();
			char c = itemAttr.getNamedItem("id").getNodeValue().charAt(0);
			Tileset tileset = new Tileset(AtlasUnpacker.gameplay.get("tilesets/" + itemAttr.getNamedItem("path").getNodeValue() + ".png"), 8, 8);
			TerrainType terrainType = new TerrainType(c);
			readInto(terrainType, tileset, item);
			if (itemAttr.getNamedItem("copy") != null) {
				char key = itemAttr.getNamedItem("copy").getNodeValue().charAt(0);
				if (!map.containsKey(key)) {
					throw new RuntimeException("Copied tilesets must be defined before the tilesets that copy them!");
				}
				readInto(terrainType, tileset, map.get(key));
			}
			if (itemAttr.getNamedItem("ignores") != null) {
				String[] split = itemAttr.getNamedItem("ignores").getNodeValue().split(",");
				for (String text : split) {
					if (text.length() > 0) {
						terrainType.Ignores.add(text.charAt(0));
					}
				}
			}
			map.put(c, item);
			lookup.put(c, terrainType);
		}
	}

	private void readInto(TerrainType data, Tileset tileset, Node xml) {
		NamedNodeMap xmlAttr = xml.getAttributes();
		if (xmlAttr.getNamedItem("scanWidth") != null) {
			int scanWidth = Integer.parseInt(xmlAttr.getNamedItem("scanWidth").getNodeValue());
			if (scanWidth <= 0 || scanWidth % 2 == 0) {
				throw new RuntimeException("Tileset scan width must be a positive, odd integer.");
			}
			data.ScanWidth = scanWidth;
		} else {
			data.ScanWidth = 3;
		}
		
		if (xmlAttr.getNamedItem("scanHeight") != null) {
			int scanHeight = Integer.parseInt(xmlAttr.getNamedItem("scanHeight").getNodeValue());
			if (scanHeight <= 0 || scanHeight % 2 == 0) {
				throw new RuntimeException("Tileset scan height must be a positive, odd integer.");
			}
			data.ScanHeight = scanHeight;
		} else {
			data.ScanHeight = 3;
		}
		
		boolean hasDefineChild = false;
		// Find all customFill nodes and check for a child named "define"
		for(int i = 0; i < xml.getChildNodes().getLength(); i++) {
			Node child = xml.getChildNodes().item(i);
			if(child.hasAttributes()) {
				String mask = child.getAttributes().getNamedItem("mask").getNodeValue();
				if(mask != null && mask.startsWith("fill")) {
					if(data.CustomFills == null) {
						data.CustomFills = new ArrayList<Tiles>();
					}
					
					data.CustomFills.add(new Tiles());
				}
			}
			if(child.getNodeName().equals("define")) {
				hasDefineChild = true;
			}
		}
		
		if (data.CustomFills == null && data.ScanWidth == 3 && data.ScanHeight == 3 && !hasDefineChild) {
			for(int i = 0; i < xml.getChildNodes().getLength(); i++) {
				Node child = xml.getChildNodes().item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				String mask = child.getAttributes().getNamedItem("mask").getNodeValue();
				Tiles tiles;
				if (mask.equals("center")) {
					tiles = data.Center;
				} else if (mask.equals("padding")) {
					tiles = data.Padded;
				} else {
					Masked masked = new Masked();
					tiles = masked.Tiles;
					int index = 0;
					for (int j = 0; j < mask.length(); j++) {
						if (mask.charAt(j) == '0') {
							masked.Mask[index++] = 0;
						} else if (mask.charAt(j) == '1') {
							masked.Mask[index++] = 1;
						} else if (mask.charAt(j) == 'x' || mask.charAt(j) == 'X') {
							masked.Mask[index++] = 2;
						}
					}
					data.Masked.add(masked);
				}
				String[] rows = child.getAttributes().getNamedItem("tiles").getNodeValue().split(";");
				for (int j = 0; j < rows.length; j++) {
					String[] columns = rows[j].split(",");
					int x = Integer.parseInt(columns[0]);
					int y = Integer.parseInt(columns[1]);
					BufferedImage img = tileset.getTile(x, y);
					tiles.Textures.add(img);
				}
				
				// The sprites for animated tiles
				if (child.getAttributes().getNamedItem("sprites") != null) {
					String[] sprites = child.getAttributes().getNamedItem("sprites").getNodeValue().split(",");
					for(String sprite : sprites)
					{
						tiles.OverlapSprites.add(sprite);
					}
					tiles.HasOverlays = true;
				}
			}
			data.Masked.sort((a, b) -> {
				int i = 0;
				int j = 0;
				for (int k = 0; k < 9; k++) {
					if (a.Mask[k] == 2) {
						i++;
					}
					if (b.Mask[k] == 2) {
						j++;
					}
				}
				return i - j;
			});
		} else {
			System.out.println("Reading Tileset with scan height " + data.ScanHeight + " and scan width " + data.ScanWidth + ".");
			ReadIntoCustomTemplate(data, tileset, xml);
		}
	}

	public Generated generateMap(TileLevelLayer mapData, boolean paddingIgnoreOutOfLevel) {
		Behaviour behaviour = new Behaviour();
		behaviour.EdgesExtend = true;
		behaviour.EdgesIgnoreOutOfLevel = false;
		behaviour.PaddingIgnoreOutOfLevel = paddingIgnoreOutOfLevel;
		Behaviour behaviour2 = behaviour;
		return Generate(mapData, 0, 0, mapData.getWidth(), mapData.getHeight(), false, '0', behaviour2);
	}

	public Generated GenerateMap(TileLevelLayer mapData, Behaviour behaviour) {
		return Generate(mapData, 0, 0, mapData.getWidth(), mapData.getHeight(), false, '0', behaviour);
	}

	public Generated GenerateBox(char id, int tilesX, int tilesY) {
		return Generate(null, 0, 0, tilesX, tilesY, true, id, new Behaviour());
	}

	public Generated GenerateOverlay(char id, int x, int y, int tilesX, int tilesY, TileLevelLayer mapData) {
		Behaviour behaviour = new Behaviour();
		behaviour.EdgesExtend = true;
		behaviour.EdgesIgnoreOutOfLevel = true;
		behaviour.PaddingIgnoreOutOfLevel = true;
		Behaviour behaviour2 = behaviour;
		return Generate(mapData, x, y, tilesX, tilesY, true, id, behaviour2);
	}

	private Generated Generate(TileLevelLayer mapData, int startX, int startY, int tilesX, int tilesY, boolean forceSolid, char forceID, Behaviour behaviour) {
		BufferedImage[][] tileImg = new BufferedImage[tilesY][tilesX];
		BufferedImage[][] animatedTiles = new BufferedImage[tilesY][tilesX];
		Rectangle forceFill = new Rectangle();
		if (forceSolid) {
			forceFill = new Rectangle(startX, startY, tilesX, tilesY);
		}
		if (mapData != null) {
			for(int i = startX; i < startX + tilesX; i++) {
				for(int j = startY; j < startY + tilesY; j++) {
					Tiles tiles = TileHandler(mapData, i, j, forceFill, forceID, behaviour);
					if (tiles != null) {
						tileImg[j - startY][i - startX] = tiles.Textures.get(rand.nextInt(tiles.Textures.size()));
						if (tiles.HasOverlays) {
							// TODO Get from GFX.AnimatedTilesBank
							animatedTiles[j - startY][i - startX] = null;//tiles.OverlapSprites.get(rand.nextInt(tiles.OverlapSprites.size()));
						}
					} else {
						rand.nextInt();
					}
				}
			}
		} else {
			for (int i = startX; i < startX + tilesX; i++) {
				for (int j = startY; j < startY + tilesY; j++) {
					Tiles tiles = TileHandler(null, i, j, forceFill, forceID, behaviour);
					if (tiles != null) {
						tileImg[j - startY][i - startX] = tiles.Textures.get(rand.nextInt(tiles.Textures.size()));
						if (tiles.HasOverlays) {
							// TODO Get from GFX.AnimatedTilesBank
							animatedTiles[j - startY][i - startX] = null;//tiles.OverlapSprites.get(rand.nextInt(tiles.OverlapSprites.size()));
						}
					} else {
						rand.nextInt();
					}
				}
			}
		}
		Generated result = new Generated();
		result.tileImg = tileImg;
		result.SpriteOverlay = animatedTiles;
		return result;
	}

	private Tiles TileHandler(TileLevelLayer mapData, int x, int y, Rectangle forceFill, char forceID, Behaviour behaviour) {
		char tile = getTile(mapData, x, y, forceFill, forceID, behaviour);
		if (isEmpty(tile)) {
			return null;
		}
		TerrainType terrainType = lookup.get(tile);
		if(terrainType == null) {
			throw new RuntimeException("Level contains a tileset with an id of '" + tile + "' that is not defined.");
		}
		if (terrainType.CustomFills == null && terrainType.ScanWidth == 3 && terrainType.ScanHeight == 3 && terrainType.whitelists.size() == 0 && terrainType.blacklists.size() == 0) {
			boolean flag = true;
			int num = 0;
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					boolean flag2 = checkTile(terrainType, mapData, x + j, y + i, forceFill, behaviour);
					if (!flag2 && behaviour.EdgesIgnoreOutOfLevel && !CheckForSameLevel(x, y, x + j, y + i)) {
						flag2 = true;
					}
					adjacent[num++] = (byte)(flag2 ? 1 : 0);
					if (!flag2)
					{
						flag = false;
					}
				}
			}
			if (flag) {
				if (behaviour.PaddingIgnoreOutOfLevel ? ((!checkTile(terrainType, mapData, x - 2, y, forceFill, behaviour) && CheckForSameLevel(x, y, x - 2, y)) || (!checkTile(terrainType, mapData, x + 2, y, forceFill, behaviour) && CheckForSameLevel(x, y, x + 2, y)) || (!checkTile(terrainType, mapData, x, y - 2, forceFill, behaviour) && CheckForSameLevel(x, y, x, y - 2)) || (!checkTile(terrainType, mapData, x, y + 2, forceFill, behaviour) && CheckForSameLevel(x, y, x, y + 2))) : (!checkTile(terrainType, mapData, x - 2, y, forceFill, behaviour) || !checkTile(terrainType, mapData, x + 2, y, forceFill, behaviour) || !checkTile(terrainType, mapData, x, y - 2, forceFill, behaviour) || !checkTile(terrainType, mapData, x, y + 2, forceFill, behaviour))) {
					return terrainType.Padded;
				}
				return terrainType.Center;
			}
			for(Masked item : terrainType.Masked) {
				boolean maskMatch = true;
				for (int k = 0; k < 9 && maskMatch; k++) {
					if (item.Mask[k] != 2 && item.Mask[k] != adjacent[k]) {
						maskMatch = false;
					}
				}
				if (maskMatch) {
					return item.Tiles;
				}
			}
			return null;
		}
		boolean flag = true;
		char[] array = new char[terrainType.ScanWidth * terrainType.ScanHeight];
		int num = 0;
		for (int i = 0; i < terrainType.ScanHeight; i++) {
			for (int j = 0; j < terrainType.ScanWidth; j++) {
				char[] tile2 = new char[1];
				boolean flag2 = tryGetTile(terrainType, mapData, x + (j - terrainType.ScanWidth / 2), y + (i - terrainType.ScanHeight / 2), forceFill, forceID, behaviour, tile2);
				if (!flag2 && behaviour.EdgesIgnoreOutOfLevel && !CheckForSameLevel(x, y, x + j, y + i))
				{
					flag2 = true;
				}
				array[num++] = tile2[0];
				if (!flag2)
				{
					flag = false;
				}
			}
		}
		if (flag) {
			if (terrainType.CustomFills != null) {
				int depth = GetDepth(terrainType, mapData, x, y, forceFill, behaviour, 1);
				return terrainType.CustomFills.get(depth - 1);
			}
			if (!checkCross(terrainType, mapData, x, y, forceFill, behaviour, 1 + terrainType.ScanWidth / 2, 1 + terrainType.ScanHeight / 2)) {
				return terrainType.Center;
			}
			return terrainType.Padded;
		}
		for(Masked item : terrainType.Masked) {
			boolean flag3 = true;
			for (int k = 0; k < terrainType.ScanWidth * terrainType.ScanHeight; k++)
			{
				if (item.Mask[k] != 2)
				{
					if (item.Mask[k] == 1 && isEmpty(array[k]))
					{
						flag3 = false;
						break;
					}
					if (item.Mask[k] == 0 && !isEmpty(array[k]))
					{
						flag3 = false;
						break;
					}
					if (item.Mask[k] == 3 && array[k] == tile)
					{
						flag3 = false;
						break;
					}
					if (terrainType.blacklists.size() > 0 && terrainType.blacklists.containsKey(item.Mask[k]) && terrainType.blacklists.get(item.Mask[k]).contains(array[k] + ""))
					{
						flag3 = false;
						break;
					}
					if (terrainType.whitelists.size() > 0 && terrainType.whitelists.containsKey(item.Mask[k]) && !terrainType.whitelists.get(item.Mask[k]).contains(array[k] + ""))
					{
						flag3 = false;
						break;
					}
				}
			}
			if (flag3) {
				return item.Tiles;
			}
		}
		return null;
	}

	private boolean CheckForSameLevel(int x1, int y1, int x2, int y2)
	{
		for(Rectangle levelBound : LevelBounds)
		{
			if (levelBound.contains(x1, y1) && levelBound.contains(x2, y2))
			{
				return true;
			}
		}
		return false;
	}

	private boolean checkTile(TerrainType set, TileLevelLayer mapData, int x, int y, Rectangle forceFill, Behaviour behaviour) {
		if (forceFill.contains(x, y)) {
			return true;
		}
		if (mapData == null) {
			return behaviour.EdgesExtend;
		}
		if (x < 0 || y < 0 || x >= mapData.getWidth() || y >= mapData.getHeight()) {
			if (!behaviour.EdgesExtend) {
				return false;
			}
			char c = mapData.getTile(Math.min(Math.max(x, 0), mapData.getWidth() - 1), Math.min(Math.max(y, 0), mapData.getHeight() - 1));
			if (!isEmpty(c)) {
				return !set.Ignore(c);
			}
			return false;
		}
		if (!isEmpty(mapData.getTile(x, y))) {
			return !set.Ignore(mapData.getTile(x, y));
		}
		return false;
	}

	private char getTile(TileLevelLayer mapData, int x, int y, Rectangle forceFill, char forceID, Behaviour behaviour) {
		if (forceFill.contains(x, y)) {
			return forceID;
		}
		if (mapData == null) {
			if (!behaviour.EdgesExtend) {
				return '0';
			}
			return forceID;
		}
		if (x < 0 || y < 0 || y >= mapData.getHeight() || x >= mapData.getWidth()) {
			if (!behaviour.EdgesExtend) {
				return '0';
			}
			return mapData.getTile(Math.min(Math.max(x, 0), mapData.getWidth() - 1), Math.min(Math.max(y, 0), mapData.getHeight() - 1));
		}
		return mapData.getTile(x, y);
	}

	private boolean isEmpty(char id) {
		if (id != '0') {
			return id == '\0';
		}
		return true;
	}

	private void ReadIntoCustomTemplate(TerrainType data, Tileset tileset, Node xml) {
		for(int i = 0; i < xml.getChildNodes().getLength(); i++) {
			Node item = xml.getChildNodes().item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (item.getNodeName().equals("set")) {
				String text = item.getAttributes().getNamedItem("mask").getNodeValue();
				Tiles tiles;
				if (text.equals("center")) {
					if (data.CustomFills != null) {
						System.out.println("\"Center\" tiles will not be used if Custom Fills are present.");
					}
					tiles = data.Center;
				} else if (text.equals("padding")) {
					if (data.CustomFills != null) {
						System.out.println("\"Padding\" tiles will not be used if Custom Fills are present.");
					}
					tiles = data.Padded;
				} else if (text.startsWith("fill")) {
					tiles = data.CustomFills.get(Integer.parseInt(text.substring(4)));
				} else {
					Masked masked = new Masked();
					masked.Mask = new byte[data.ScanWidth * data.ScanHeight];
					tiles = masked.Tiles;
					try {
						int num = 0;
						for(char c : text.toCharArray()) {
							switch (c) {
							case '0':
								masked.Mask[num++] = 0;
								continue;
							case '1':
								masked.Mask[num++] = 1;
								continue;
							case 'X':
							case 'x':
								masked.Mask[num++] = 2;
								continue;
							case 'Y':
							case 'y':
								masked.Mask[num++] = 3;
								continue;
							case 'Z':
							case 'z':
								continue;
							}
							if (Character.isLetter(c)) {
								masked.Mask[num++] = GetByteLookup(c);
							}
						}
					} catch (ArrayIndexOutOfBoundsException innerException) {
						throw new ArrayIndexOutOfBoundsException("Mask size in tileset with id '" + data.ID + "' is greater than the size specified by scanWidth and scanHeight (defaults to 3x3).");
					}
					data.Masked.add(masked);
				}
				String[] rows = item.getAttributes().getNamedItem("tiles").getNodeValue().split(";");
				for (int j = 0; j < rows.length; j++) {
					String[] cols = rows[j].split(",");
					int x = Integer.parseInt(cols[0]);
					int y = Integer.parseInt(cols[1]);
					try {
						tiles.Textures.add(tileset.getTile(x, y));
					} catch (ArrayIndexOutOfBoundsException ex) {
						throw new ArrayIndexOutOfBoundsException("Tileset with id '" + data.ID + "' missing tile at (" + x + "," + y + ").");
					}
				}
				if (item.getAttributes().getNamedItem("sprites") != null) {
					String[] sprites = item.getAttributes().getNamedItem("tiles").getNodeValue().split(",");
					for(String sprite : sprites) {
						tiles.OverlapSprites.add(sprite);
					}
					tiles.HasOverlays = true;
				}
			} else if (item.getNodeName().equals("define")) {
				byte byteLookup = GetByteLookup(item.getAttributes().getNamedItem("id").getNodeValue().charAt(0));
				String value = item.getAttributes().getNamedItem("filter").getNodeValue();
				if (Boolean.parseBoolean(item.getAttributes().getNamedItem("ignore").getNodeValue())) {
					data.blacklists.put(byteLookup, value);
				} else {
					data.whitelists.put(byteLookup, value);
				}
			}
		}
		data.Masked.sort((a, b) -> {
			int num4 = 0;
			int num5 = 0;
			int num6 = 0;
			int num7 = 0;
			int num8 = 0;
			int num9 = 0;
			for (int j = 0; j < data.ScanWidth * data.ScanHeight; j++)
			{
				if (a.Mask[j] >= 10)
				{
					num4++;
				}
				if (b.Mask[j] >= 10)
				{
					num5++;
				}
				if (a.Mask[j] == 3)
				{
					num6++;
				}
				if (b.Mask[j] == 3)
				{
					num7++;
				}
				if (a.Mask[j] == 2)
				{
					num8++;
				}
				if (b.Mask[j] == 2)
				{
					num9++;
				}
			}
			if (num4 > 0 || num5 > 0)
			{
				return num4 - num5;
			}
			return (num6 > 0 || num7 > 0) ? (num6 - num7) : (num8 - num9);
		});
	}

	private byte GetByteLookup(char c) {
		if (Character.isLowerCase(c)) {
			return (byte)(c - 97 + 10);
		}
		if (Character.isUpperCase(c)) {
			return (byte)(c - 65 + 37);
		}
		throw new IllegalArgumentException("Parameter 'c' must be an uppercase or lowercase letter.");
	}

	// Use an array with one element rather than the out keyword
	private boolean tryGetTile(TerrainType set, TileLevelLayer mapData, int x, int y, Rectangle forceFill, char forceID, Behaviour behaviour, char[] tile) {
		tile[0] = '0';
		if (forceFill.contains(x, y)) {
			tile[0] = forceID;
			return true;
		}
		if (mapData == null) {
			return behaviour.EdgesExtend;
		}
		if (x >= 0 && y >= 0 && x < mapData.getWidth() && y < mapData.getHeight()) {
			tile[0] = mapData.getTile(x, y);
			if (!isEmpty(tile[0])) {
				return !set.Ignore(tile[0]);
			}
			return false;
		}
		if (!behaviour.EdgesExtend) {
			return false;
		}
		tile[0] = mapData.getTile(Math.min(Math.max(x, 0), mapData.getWidth() - 1), Math.min(Math.max(y, 0), mapData.getHeight() - 1));
		if (!isEmpty(tile[0])) {
			return !set.Ignore(tile[0]);
		}
		return false;
	}

	private int GetDepth(TerrainType terrainType, TileLevelLayer mapData, int x, int y, Rectangle forceFill, Behaviour behaviour, int depth) {
		int width = depth + terrainType.ScanWidth / 2;
		int height = depth + terrainType.ScanHeight / 2;
		if (checkCross(terrainType, mapData, x, y, forceFill, behaviour, width, height) && depth < terrainType.CustomFills.size()) {
			return GetDepth(terrainType, mapData, x, y, forceFill, behaviour, ++depth);
		}
		return depth;
	}

	private boolean checkCross(TerrainType terrainType, TileLevelLayer mapData, int x, int y, Rectangle forceFill, Behaviour behaviour, int width, int height) {
		if (behaviour.PaddingIgnoreOutOfLevel) {
			if ((checkTile(terrainType, mapData, x - width, y, forceFill, behaviour) || !CheckForSameLevel(x, y, x - width, y)) && (checkTile(terrainType, mapData, x + width, y, forceFill, behaviour) || !CheckForSameLevel(x, y, x + width, y)) && (checkTile(terrainType, mapData, x, y - height, forceFill, behaviour) || !CheckForSameLevel(x, y, x, y - height)))
			{
				if (!checkTile(terrainType, mapData, x, y + height, forceFill, behaviour))
				{
					return !CheckForSameLevel(x, y, x, y + height);
				}
				return true;
			}
			return false;
		}
		if (checkTile(terrainType, mapData, x - width, y, forceFill, behaviour) && checkTile(terrainType, mapData, x + width, y, forceFill, behaviour) && checkTile(terrainType, mapData, x, y - height, forceFill, behaviour)) {
			return checkTile(terrainType, mapData, x, y + height, forceFill, behaviour);
		}
		return false;
	}
}
