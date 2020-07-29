package celesteeditor.data;

import java.awt.Rectangle;
import java.util.ArrayList;

import celesteeditor.BinaryPacker.Element;

public class Map implements ElementEncoded {
	
	public String pkg;
	
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
		
		Element Filler = new Element("Filler");
		for(Rectangle r : filler) {
			Element f = new Element("rect");
			f.Attributes.put("x", r.x);
			f.Attributes.put("y", r.y);
			f.Attributes.put("width", r.width);
			f.Attributes.put("height", r.height);
			Filler.Children.add(f);
		}
		res.Children.add(Filler);
		
		Element levelsE = new Element("levels");
		for(Level l : levels) {
			levelsE.Children.add(l.asElement());
		}
		res.Children.add(levelsE);
		
		Element style = new Element("Style");
		if(!backgroundColor.equals("000000")) {
			style.Attributes.put("color", backgroundColor);
		}
		Element bg, fg;
		style.Children.add(bg = new Element("Backgrounds"));
		style.Children.add(fg = new Element("Foregrounds"));
		for(Styleground s : backgrounds) {
			bg.Children.add(s.asElement());
		}
		for(Styleground s : foregrounds) {
			fg.Children.add(s.asElement());
		}
		res.Children.add(style);
		
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
				for(Element e : c.Children) {
					ArrayList<Styleground> layer = e.Name.equals("Backgrounds") ? backgrounds : foregrounds;
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
		
		return this;
	}
	
}
