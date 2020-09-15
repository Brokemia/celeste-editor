package celesteeditor.data;

import java.util.ArrayList;
import java.util.HashMap;

import celesteeditor.BinaryPacker.Element;

public class Styleground implements ElementEncoded {	
	public String exclude = "", only = "";
	
	public String flag = "", notFlag = "", alwaysIfFlag = "";
	
	public String tag = "";
	
	public float x, y;
	
	public float scrollX = 1, scrollY = 1;
	
	public float speedX, speedY;
	
	public String color = "FFFFFF";
	
	public boolean loopX, loopY;
	
	public boolean flipX, flipY;
	
	public String fadeX = "", fadeY = "";
	
	public boolean dreaming;
	
	public boolean instantIn, instantOut;
	
	public float windMultiplier;
	
	// Only the highest level of stylegrounds are allowed to have children
	public ArrayList<Styleground> children = new ArrayList<>();

	@Override
	public Element asElement() {
		Element res = new Element("apply");
		res.Attributes = new HashMap<>();
		res.Attributes.put("exclude", exclude);
		res.Attributes.put("only", only);
		res.Attributes.put("flag", flag);
		res.Attributes.put("notflag", notFlag);
		res.Attributes.put("always", alwaysIfFlag);
		res.Attributes.put("tag", tag);
		res.Attributes.put("x", x);
		res.Attributes.put("y", y);
		res.Attributes.put("scrollx", scrollX);
		res.Attributes.put("scrolly", scrollY);
		res.Attributes.put("speedx", speedX);
		res.Attributes.put("speedy", speedY);
		res.Attributes.put("color", color);
		res.Attributes.put("loopx", loopX);
		res.Attributes.put("loopy", loopY);
		res.Attributes.put("flipx", flipX);
		res.Attributes.put("flipy", flipY);
		res.Attributes.put("fadex", fadeX);
		res.Attributes.put("fadey", fadeY);
		res.Attributes.put("dreaming", dreaming);
		res.Attributes.put("instantIn", instantIn);
		res.Attributes.put("instantOut", instantOut);
		res.Attributes.put("wind", windMultiplier);
		if(children != null && children.size() != 0) {
			res.Children = new ArrayList<>();
			for(Styleground c : children) {
				res.Children.add(c.asElement());
			}
		}
		return res;
	}

	@Override
	public Styleground fromElement(Element element) {
		exclude = element.Attr("exclude");
		only = element.Attr("only");
		flag = element.Attr("flag");
		notFlag = element.Attr("notflag");
		alwaysIfFlag = element.Attr("always");
		tag = element.Attr("tag");
		x = element.AttrFloat("x", 0);
		y = element.AttrFloat("y", 0);
		scrollX = element.AttrFloat("scrollx");
		scrollY = element.AttrFloat("scrolly");
		speedX = element.AttrFloat("speedx", 0);
		speedY = element.AttrFloat("speedy", 0);
		color = element.Attr("color");
		loopX = element.AttrBool("loopx");
		loopY = element.AttrBool("loopy");
		flipX = element.AttrBool("flipx");
		flipY = element.AttrBool("flipy");
		fadeX = element.Attr("fadex");
		fadeY = element.Attr("fadey");
		dreaming = element.AttrBool("dreaming");
		instantIn = element.AttrBool("instantIn");
		instantOut = element.AttrBool("instantOut");
		windMultiplier = element.AttrFloat("wind");
		if(element.Children != null && element.Children.size() != 0) {
			children.clear();
			for(Element c : element.Children) {
				if(c.Name.equals("apply")) {
					// Nested styleground groups are probably illegal, but I'll address it anyway
					children.add(new Styleground().fromElement(c));
				} else if(c.Name.equals("parallax")) {
					children.add(new Parallax().fromElement(c));
				} else {
					children.add(new Effect().fromElement(c));
				}
			}
		}
		return this;
	}
}
