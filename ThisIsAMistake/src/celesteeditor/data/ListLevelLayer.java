package celesteeditor.data;
	
import java.util.ArrayList;

import celesteeditor.BinaryPacker.Element;

public class ListLevelLayer extends LevelLayer {
	public ArrayList<ElementEncoded> items = new ArrayList<>();
	
	public Class<? extends ElementEncoded> itemType;
	
	public ListLevelLayer(Class<? extends ElementEncoded> itemType) {
		this.itemType = itemType;
	}
	
	@Override
	public Element asElement() {
		Element res = super.asElement();
		for(ElementEncoded e : items) {
			res.Children.add(e.asElement());
		}
		return res;
	}

	@Override
	public ListLevelLayer fromElement(Element element) {		
		super.fromElement(element);
		items.clear();
		if(element.Children != null) {
			for(Element c : element.Children) {
				try {
					items.add(itemType.getConstructor().newInstance().fromElement(c));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return this;
	}
}
