package celesteeditor.data;

import celesteeditor.BinaryPacker.Element;

public class Parallax extends Styleground {
	public String texture = "";
	
	public String blendMode = "alphablend";
	
	public float alpha = 1;
	
	@Override
	public Element asElement() {
		Element res = super.asElement();
		res.Name = "parallax";
		res.Attributes.put("texture", texture);
		res.Attributes.put("blendmode", blendMode);
		res.Attributes.put("alpha", alpha);
		return res;
	}
	
	@Override
	public Parallax fromElement(Element e) {
		super.fromElement(e);
		texture = e.Attr("texture");
		blendMode = e.Attr("blendmode");
		alpha = e.AttrFloat("alpha");
		return this;
	}
}
