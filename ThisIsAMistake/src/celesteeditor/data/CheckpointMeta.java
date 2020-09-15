package celesteeditor.data;

import java.util.ArrayList;
import java.util.HashMap;

import celesteeditor.BinaryPacker;
import celesteeditor.BinaryPacker.Element;
import celesteeditor.data.Meta.CoreModes;

public class CheckpointMeta implements ElementEncoded {
	
	public String level;
	
	public String name;
	
	public boolean dreaming;
	
	public String inventory;
	
	public AudioMeta audioState;
	
	public String[] flags;
	
	public CoreModes coreMode;

	@Override
	public Element asElement() {
		Element res = new Element("checkpoint");
		res.Attributes = new HashMap<>();
		
		if(level != null) {
			res.Attributes.put("Level", level);
		}
		if(name != null) {
			res.Attributes.put("Name", name);
		}
		res.Attributes.put("Dreaming", dreaming);
		if(inventory != null) {
			res.Attributes.put("Inventory", inventory);
		}
		if(coreMode != null) {
			res.Attributes.put("CoreMode", coreMode);
		}
		
		if(audioState != null || (flags != null && flags.length != 0)) {
			res.Children = new ArrayList<>();

			if(audioState != null) {
				res.Children.add(audioState.asElement());
			}
			
			if(flags != null && flags.length != 0) {
				Element fElement = new Element("flags");
				res.Children.add(fElement);
				fElement.Children = new ArrayList<>();
				
				for(int i = 0; i < flags.length; i++) {
					Element child = new Element("flag");
					child.Attributes.put(BinaryPacker.InnerTextAttributeName, flags[i]);
					fElement.Children.add(child);
				}
			}
		}
		
		return res;
	}

	@Override
	public CheckpointMeta fromElement(Element element) {
		level = element.Attr("Level", null);
		name = element.Attr("Name", null);
		dreaming = element.AttrBool("Dreaming");
		inventory = element.Attr("Inventory", null);
		coreMode = CoreModes.valueOf(element.Attr("CoreMode", null));
		
		if(element.Children != null) {
			for(Element c : element.Children) {
				if(c.Name.equals("audiostate")) {
					audioState = new AudioMeta().fromElement(c);
				} else if(c.Name.equals("flags")) {
					flags = new String[c.Children != null ? c.Children.size() : 0];
					for(int i = 0; i < flags.length; i++) {
						flags[i] = c.Children.get(i).Attr(BinaryPacker.InnerTextAttributeName);
					}
				}
			}
		}

		return this;
	}

}
