package celesteeditor.ui.autotiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import celesteeditor.ui.autotiler.Autotiler.Tiles;

public class TerrainType {
		public String name;
		
		public char ID;

		public HashSet<Character> Ignores;

		public ArrayList<Autotiler.Masked> Masked;

		public Tiles Center;

		public Tiles Padded;

		public int ScanWidth;

		public int ScanHeight;

		public ArrayList<Tiles> CustomFills;

		public HashMap<Byte, String> whitelists;

		public HashMap<Byte, String> blacklists;

		public TerrainType(String name, char id)
		{
			Ignores = new HashSet<Character>();
			Masked = new ArrayList<Autotiler.Masked>();
			Center = new Tiles();
			Padded = new Tiles();
			this.name = name;
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