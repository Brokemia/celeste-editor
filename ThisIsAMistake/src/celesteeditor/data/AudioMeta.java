package celesteeditor.data;

import java.util.HashMap;

import celesteeditor.BinaryPacker.Element;

public class AudioMeta implements ElementEncoded {
	
	public String music;
	
	public String ambience;

	@Override
	public Element asElement() {
		Element res = new Element("audiostate");
		res.Attributes = new HashMap<>();
		
		res.Attributes.put("Music", music);
		res.Attributes.put("Ambience", ambience);
		
		return res;
	}

	@Override
	public AudioMeta fromElement(Element element) {
		music = element.Attr("Music");
		ambience = element.Attr("Ambience");
		
		return this;
	}

}
