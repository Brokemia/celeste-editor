package celesteeditor.data;

import java.util.ArrayList;
import java.util.HashMap;

import celesteeditor.BinaryPacker.Element;

public class Meta implements ElementEncoded {
	
	public enum IntroTypes {
		Transition, Respawn, WalkInRight, WalkInLeft, Jump, WakeUp, Fall, TempleMirrorVoid, None, ThinkForABit
	}
	
	public enum CoreModes {
		None, Hot, Cold
	}
	
	public String parent;
	
	public String icon;
	
	public boolean interlude;
	
	public int cassetteCheckpointIndex;
	
	public String titleBaseColor;
	
	public String titleAccentColor;
	
	public String titleTextColor;
	
	public IntroTypes introType;
	
	public boolean dreaming;
	
	public String colorGrade;
	
	public String wipe;
	
	public float darknessAlpha;
	
	public float bloomBase;
	
	public float bloomStrength;
	
	public String jumpthru;
	
	public CoreModes coreMode;
	
	public String cassetteNoteColor;
	
	public String cassetteSong;
	
	public String postcardSoundID;
	
	public String foregroundTiles;
	
	public String backgroundTiles;
	
	public String animatedTiles;
	
	public String sprites;
	
	public String portraits;
	
	public boolean overrideASideMeta;
	
	public ModeMeta mode;
	

	@Override
	public Element asElement() {
		Element res = new Element("meta");
		res.Attributes = new HashMap<>();
		
		if(parent != null) {
			res.Attributes.put("Parent", parent);
		}
		if(icon != null) {
			res.Attributes.put("Icon", icon);
		}
		res.Attributes.put("Interlude", interlude);
		res.Attributes.put("CassetteCheckpointIndex", cassetteCheckpointIndex);
		if(titleBaseColor != null) {
			res.Attributes.put("TitleBaseColor", titleBaseColor);
		}
		if(titleAccentColor != null) {
			res.Attributes.put("TitleAccentColor", titleAccentColor);
		}
		if(titleTextColor != null) {
			res.Attributes.put("TitleTextColor", titleTextColor);
		}
		if(introType != null) {
			res.Attributes.put("IntroType", introType);
		}
		res.Attributes.put("Dreaming", dreaming);
		if(colorGrade != null) {
			res.Attributes.put("ColorGrade", colorGrade);
		}
		if(wipe != null) {
			res.Attributes.put("Wipe", wipe);
		}
		res.Attributes.put("DarknessAlpha", darknessAlpha);
		res.Attributes.put("BloomBase", bloomBase);
		res.Attributes.put("BloomStrength", bloomStrength);
		if(jumpthru != null) {
			res.Attributes.put("Jumpthru", jumpthru);
		}
		if(coreMode != null) {
			res.Attributes.put("CoreMode", coreMode);
		}
		if(cassetteNoteColor != null) {
			res.Attributes.put("CassetteNoteColor", cassetteNoteColor);
		}
		if(cassetteSong != null) {
			res.Attributes.put("CassetteSong", cassetteSong);
		}
		if(postcardSoundID != null) {
			res.Attributes.put("PostcardSoundID", postcardSoundID);
		}
		if(foregroundTiles != null) {
			res.Attributes.put("ForegroundTiles", foregroundTiles);
		}
		if(backgroundTiles != null) {
			res.Attributes.put("BackgroundTiles", backgroundTiles);
		}
		if(animatedTiles != null) {
			res.Attributes.put("AnimatedTiles", animatedTiles);
		}
		if(sprites != null) {
			res.Attributes.put("Sprites", sprites);
		}
		if(portraits != null) {
			res.Attributes.put("Portraits", portraits);
		}
		res.Attributes.put("OverrideASideMeta", overrideASideMeta);
		if(mode != null) {
			res.Children = new ArrayList<>();
			res.Children.add(mode.asElement());
		}
		
		return res;
	}

	@Override
	public Meta fromElement(Element element) {
		parent = element.Attr("Parent", null);
		icon = element.Attr("Icon", null);
		interlude = element.AttrBool("Interlude");
		cassetteCheckpointIndex = element.AttrInt("CassetteCheckpointIndex");
		titleBaseColor = element.Attr("TitleBaseColor", null);
		titleAccentColor = element.Attr("TitleAccentColor", null);
		titleTextColor = element.Attr("TitleTextColor", null);
		introType = IntroTypes.valueOf(element.Attr("IntroType", null));
		dreaming = element.AttrBool("Dreaming");
		colorGrade = element.Attr("ColorGrade", null);
		wipe = element.Attr("Wipe", null);
		darknessAlpha = element.AttrFloat("DarknessAlpha");
		bloomBase = element.AttrFloat("BloomBase");
		bloomStrength = element.AttrFloat("BloomStrength");
		jumpthru = element.Attr("Jumpthru", null);
		coreMode = CoreModes.valueOf(element.Attr("CoreMode", "None"));
		cassetteNoteColor = element.Attr("CassetteNoteColor", null);
		cassetteSong = element.Attr("CassetteSong", null);
		postcardSoundID = element.Attr("PostcardSoundID", null);
		foregroundTiles = element.Attr("ForegroundTiles", null);
		backgroundTiles = element.Attr("BackgroundTiles", null);
		animatedTiles = element.Attr("AnimatedTiles", null);
		sprites = element.Attr("Sprites", null);
		portraits = element.Attr("Portraits", null);
		overrideASideMeta = element.AttrBool("OverrideASideMeta");
		
		if(element.Children != null) {
			for(Element c : element.Children) {
				if(c.Name.equals("mode")) {
					mode = new ModeMeta().fromElement(c);
				}
			}
		}
		
		return this;
	}

}
