package celesteeditor.data;

import celesteeditor.BinaryPacker.Element;

public class LevelLayer implements ElementEncoded {
	public String name;
	
	public int offsetX, offsetY;

	@Override
	public Element asElement() {
		Element res = new Element(name);
		res.Attributes.put("offsetX", offsetX);
		res.Attributes.put("offsetY", offsetY);
		
		return res;
	}

	@Override
	public LevelLayer fromElement(Element element) {
		name = element.Name;
		offsetX = element.AttrInt("offsetX", 0);
		offsetY = element.AttrInt("offsetY", 0);
		
		return this;
	}
}
