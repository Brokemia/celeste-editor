package celesteeditor.data;

import java.util.ArrayList;
import java.util.HashMap;

import celesteeditor.BinaryPacker.Element;

public class ModeMeta implements ElementEncoded {
	
	public AudioMeta audioState;
	
	public CheckpointMeta[] checkpoints;
	
	public boolean ignoreAudioLayerData;
	
	public String inventory;
	
	public String path;
	
	public String poemID;
	
	public String startLevel;
	
	public boolean heartIsEnd;
	
	public boolean seekerSlowdown;
	
	public boolean theoInBubble;

	@Override
	public Element asElement() {
		Element res = new Element("mode");
		res.Attributes = new HashMap<>();
		
		res.Attributes.put("IgnoreLevelAudioLayerData", ignoreAudioLayerData);
		if(inventory != null) {
			res.Attributes.put("Inventory", inventory);
		}
		if(path != null) {
			res.Attributes.put("Path", path);
		}
		if(poemID != null) {
			res.Attributes.put("PoemID", poemID);
		}
		if(startLevel != null) {
			res.Attributes.put("StartLevel", startLevel);
		}
		res.Attributes.put("HeartIsEnd", heartIsEnd);
		res.Attributes.put("SeekerSlowdown", seekerSlowdown);
		res.Attributes.put("TheoInBubble", theoInBubble);
		
		if(audioState != null || (checkpoints != null && checkpoints.length != 0)) {
			res.Children = new ArrayList<>();
			
			if(audioState != null) {
				res.Children.add(audioState.asElement());
			}
			
			if(checkpoints != null && checkpoints.length != 0) {
				Element cpElement = new Element("checkpoints");
				res.Children.add(cpElement);
				cpElement.Children = new ArrayList<>();
				for(int i = 0; i < checkpoints.length; i++) {
					cpElement.Children.add(checkpoints[i].asElement());
				}
			}
		}
		
		return res;
	}

	@Override
	public ModeMeta fromElement(Element element) {
		ignoreAudioLayerData = element.AttrBool("IgnoreLevelAudioLayerData");
		inventory = element.Attr("Inventory", null);
		path = element.Attr("Path", null);
		poemID = element.Attr("PoemID", null);
		startLevel = element.Attr("StartLevel", null);
		heartIsEnd = element.AttrBool("HeartIsEnd");
		seekerSlowdown = element.AttrBool("SeekerSlowdown");
		theoInBubble = element.AttrBool("TheoInBubble");
		
		if(element.Children != null) {
			for(Element c : element.Children) {
				if(c.Name.equals("audiostate")) {
					audioState = new AudioMeta().fromElement(c);
				} else if(c.Name.equals("checkpoints")) {
					checkpoints = new CheckpointMeta[c.Children != null ? c.Children.size() : 0];
					for(int i = 0; i < checkpoints.length; i++) {
						checkpoints[i] = new CheckpointMeta().fromElement(c.Children.get(i));
					}
				}
			}
		}
		
		return this;
	}

}
