package celesteeditor.data;

import celesteeditor.BinaryPacker.Element;

public class Effect extends Styleground {
	String name;
	
	// TODO Other effect-specific properties

	@Override
	public Element asElement() {
		Element res = super.asElement();
		res.Name = name;
		return res;
	}

	@Override
	public Effect fromElement(Element element) {
		super.fromElement(element);
		name = element.Name;
		return this;
	}
}
