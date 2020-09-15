package celesteeditor.data;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import celesteeditor.BinaryPacker.Element;

public class Map implements ElementEncoded {
	
	public String pkg;
	
	public Meta meta;
	
	public ArrayList<Rectangle> filler = new ArrayList<>();
	
	public ArrayList<Level> levels = new ArrayList<>();
	
	public String backgroundColor = "000000";
	
	public ArrayList<Styleground> backgrounds = new ArrayList<>();
	
	public ArrayList<Styleground> foregrounds = new ArrayList<>();
	
	@Override
	public String toString() {
		String res = "";
		res += pkg + ": { filler: [ ";
		for(Rectangle r : filler) {
			res += "{ " + r.x + ", " + r.y + ", " + r.width + ", " + r.height + " }, "; 
		}
		res += "], levels: [ ";
		for(Level l : levels) {
			res += "{ " + l.toString() + " }, ";
		}
		res += "], backgrounds: [ ";
		for(Styleground s : backgrounds) {
			res += "{ " + s.toString() + " }, ";
		}
		res += "], foregrounds: [ ";
		for(Styleground s : foregrounds) {
			res += "{ " + s.toString() + " }, ";
		}
		res += "] }";
		return res;
	}
	
	@Override
	public Element asElement() {
		Element res = new Element("Map");
		res.Package = pkg;
		res.Children = new ArrayList<>();
		
		Element Filler = new Element("Filler");
		Filler.Children = new ArrayList<>();
		for(Rectangle r : filler) {
			Element f = new Element("rect");
			f.Attributes = new HashMap<>();
			f.Attributes.put("x", r.x);
			f.Attributes.put("y", r.y);
			f.Attributes.put("w", r.width);
			f.Attributes.put("h", r.height);
			Filler.Children.add(f);
		}
		res.Children.add(Filler);
		
		Element levelsE = new Element("levels");
		levelsE.Children = new ArrayList<>();
		for(Level l : levels) {
			levelsE.Children.add(l.asElement());
		}
		res.Children.add(levelsE);
		
		Element style = new Element("Style");
		style.Children = new ArrayList<>();
		style.Attributes = new HashMap<>();
		if(!backgroundColor.equals("000000")) {
			style.Attributes.put("color", backgroundColor);
		}
		Element bg, fg;
		style.Children.add(bg = new Element("Backgrounds"));
		style.Children.add(fg = new Element("Foregrounds"));
		bg.Children = new ArrayList<>();
		fg.Children = new ArrayList<>();
		for(Styleground s : backgrounds) {
			bg.Children.add(s.asElement());
		}
		for(Styleground s : foregrounds) {
			fg.Children.add(s.asElement());
		}
		res.Children.add(style);
		if(meta != null) {
			res.Children.add(meta.asElement());
		}
		
		return res;
	}

	@Override
	public Map fromElement(Element element) {
		pkg = element.Package;
		filler.clear();
		levels.clear();
		backgrounds.clear();
		foregrounds.clear();
		for(Element c : element.Children) {
			if(c.Name.equals("Filler")) {
				if(c.Children != null) {
					for(Element f : c.Children) {
						if(f.Name.equals("rect")) {
							filler.add(new Rectangle((int)f.Attributes.get("x"), (int)f.Attributes.get("y"), (int)f.Attributes.get("w"), (int)f.Attributes.get("h")));
						}
					}
				}
			} else if(c.Name.equals("levels")) {
				for(Element l : c.Children) {
					if(l.Name.equals("level")) {
						levels.add(new Level().fromElement(l));
					}
				}
			} else if(c.Name.equals("Style")) {
				if(c.HasAttr("color")) {
					backgroundColor = c.Attr("color");
				}
				if(c.Children != null) {
					for(Element e : c.Children) {
						ArrayList<Styleground> layer = e.Name.equals("Backgrounds") ? backgrounds : foregrounds;
						if(e.Children != null) {
							for(Element s : e.Children) {
								if(s.Name.equals("apply")) {
									layer.add(new Styleground().fromElement(s));
								} else if(s.Name.equals("parallax")) {
									layer.add(new Parallax().fromElement(s));
								} else {
									layer.add(new Effect().fromElement(s));
								}
							}
						}
					}
				}
			} else if(c.Name.equals("meta")) {
				meta = new Meta().fromElement(c);
			}
		}
		
		return this;
	}
	
}
