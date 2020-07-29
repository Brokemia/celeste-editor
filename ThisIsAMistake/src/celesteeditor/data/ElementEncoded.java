package celesteeditor.data;

import celesteeditor.BinaryPacker.Element;

public interface ElementEncoded {
	
	public Element asElement();
	
	/**
	 * Sets the values of this object, based on the element passed in
	 * @param element
	 * @return this
	 */
	public ElementEncoded fromElement(Element element);
}
