package celesteeditor.data;

import java.awt.Rectangle;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.jogamp.opengl.util.texture.Texture;

import celesteeditor.BinaryPacker.Element;

public class Level implements ElementEncoded {
	// Color of the room?
	public int c;
	
	public String name = "";
	
	public Rectangle bounds = new Rectangle(0, 0, 320, 184);
	
	public float cameraOffsetX, cameraOffsetY;
	
	public String ambience = "";
	
	public String ambienceProgress = "";
	
	public String music = "";
	
	public String musicProgress = "";
	
	public String altMusic = "";
	
	public boolean delayAltMusicFade;
	
	public boolean musicLayer1 = true, musicLayer2 = true, musicLayer3 = true, musicLayer4 = true;
	
	public boolean whisper = false;
	
	public String windPattern = "None";
	
	public boolean disableDownTransition;
	
	public boolean dark;
	
	public boolean underwater;
	
	public boolean space;
	
	public ListLevelLayer triggers;
	
	public IntTileLevelLayer fgTiles; // ignoring because Ahorn does
	
	public ListLevelLayer fgDecals;
	
	public TileLevelLayer solids;
	
	public ListLevelLayer entities;
	
	public IntTileLevelLayer bgTiles; // ignoring because Ahorn does
	
	public ListLevelLayer bgDecals;
	
	public TileLevelLayer bg;
	
	public IntTileLevelLayer objTiles; // TODO object tiles
		
	public IntBuffer frameBuffer = IntBuffer.allocate(1);
	
	public Texture roomTexture;
	
	public void adjustCanvasSize() {
		Rectangle bounds = new Rectangle(this.bounds);
		
		for(ElementEncoded ee : triggers.items) {
			Entity e = (Entity) ee;
			bounds.x = Math.min(bounds.x, e.getUnadjustedBounds(this).x);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public Element asElement() {
		Element res = new Element("level");
		res.Attributes = new HashMap<>();
		res.Children = new ArrayList<>();
		res.Attributes.put("c", c);
		res.Attributes.put("name", name);
		res.Attributes.put("x", bounds.x);
		res.Attributes.put("y", bounds.y);
		res.Attributes.put("width", bounds.width);
		res.Attributes.put("height", bounds.height);
		res.Attributes.put("cameraOffsetX", cameraOffsetX);
		res.Attributes.put("cameraOffsetY", cameraOffsetY);
		res.Attributes.put("ambience", ambience);
		res.Attributes.put("ambienceProgress", ambienceProgress);
		res.Attributes.put("music", music);
		res.Attributes.put("musicProgress", musicProgress);
		res.Attributes.put("alt_music", altMusic);
		res.Attributes.put("delayAltMusicFade", delayAltMusicFade);
		res.Attributes.put("musicLayer1", musicLayer1);
		res.Attributes.put("musicLayer2", musicLayer2);
		res.Attributes.put("musicLayer3", musicLayer3);
		res.Attributes.put("musicLayer4", musicLayer4);
		res.Attributes.put("whisper", whisper);
		res.Attributes.put("windPattern", windPattern);
		res.Attributes.put("disableDownTransition", disableDownTransition);
		res.Attributes.put("dark", dark);
		res.Attributes.put("underwater", underwater);
		res.Attributes.put("space", space);
		
		res.Children.add(triggers.asElement());
		res.Children.add(fgTiles.asElement());
		if(fgDecals != null)
			res.Children.add(fgDecals.asElement());
		res.Children.add(solids.asElement());
		res.Children.add(entities.asElement());
		res.Children.add(bgTiles.asElement());
		if(bgDecals != null)
			res.Children.add(bgDecals.asElement());
		res.Children.add(bg.asElement());
		if(objTiles != null)
			res.Children.add(objTiles.asElement());
		return res;
	}

	@Override
	public Level fromElement(Element element) {
		c = (int)element.Attributes.get("c");
		name = (String)element.Attributes.get("name");
		bounds.x = (int)element.Attributes.get("x");
		bounds.y = (int)element.Attributes.get("y");
		bounds.width = (int)element.Attributes.get("width");
		bounds.height = (int)element.Attributes.get("height");
		cameraOffsetX = element.AttrFloat("cameraOffsetX", 0);
		cameraOffsetY = element.AttrFloat("cameraOffsetY", 0);
		ambience = element.Attr("ambience");
		ambienceProgress = element.Attr("ambienceProgress");
		music = element.Attr("music");
		musicProgress = element.Attr("musicProgress", "");
		altMusic = element.Attr("alt_music");
		delayAltMusicFade = element.AttrBool("delayAltMusicFade", false);
		musicLayer1 = element.AttrBool("musicLayer1", true);
		musicLayer2 = element.AttrBool("musicLayer2", true);
		musicLayer3 = element.AttrBool("musicLayer3", true);
		musicLayer4 = element.AttrBool("musicLayer4", true);
		whisper = element.AttrBool("whisper", false);
		windPattern = element.Attr("windPattern");
		disableDownTransition = element.AttrBool("disableDownTransition", false);
		dark = element.AttrBool("dark", false);
		underwater = element.AttrBool("underwater", false);
		space = element.AttrBool("space", false);
		for(Element c : element.Children) {
			switch(c.Name) {
			case "triggers":
				triggers = new ListLevelLayer(Entity.class).fromElement(c);
				break;
			case "fgtiles":
				fgTiles = new IntTileLevelLayer().fromElement(c);
				break;
			case "fgdecals":
				fgDecals = new ListLevelLayer(Decal.class).fromElement(c);
				break;
			case "solids":
				solids = new TileLevelLayer(bounds.width, bounds.height).fromElement(c);
				break;
			case "entities":
				entities = new ListLevelLayer(Entity.class).fromElement(c);
				break;
			case "bgtiles":
				bgTiles = new IntTileLevelLayer().fromElement(c);
				break;
			case "bgdecals":
				bgDecals = new ListLevelLayer(Decal.class).fromElement(c);
				break;
			case "bg":
				bg = new TileLevelLayer(bounds.width, bounds.height).fromElement(c);
				break;
			case "objtiles":
				objTiles = new IntTileLevelLayer().fromElement(c);
				break;
			default:
				throw new RuntimeException("Unknown level layer found: " + c.Name);
			}
		}
		return this;
	}
}
