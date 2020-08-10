package celesteeditor.data;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import celesteeditor.BinaryPacker.Element;
import celesteeditor.Main;
import celesteeditor.data.EntityProperty.PropertyType;
import celesteeditor.editing.EntityConfig;
import celesteeditor.editing.EntityConfig.VisualType;
import celesteeditor.editing.PlacementConfig;

public class Entity implements ElementEncoded {
	public String name;
	
	public int x, y;
	
	public int originX, originY;
	
	public static int NEXT_ID;
	
	public int id;
	
	public ArrayList<EntityProperty> properties = new ArrayList<>();
	
	public ArrayList<Point> nodes = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(String name, T def) {
		EntityProperty prop = properties.stream().filter((p) -> p.name.equals(name)).findFirst().orElse(null);
		if(prop == null)
			return def;
		return (T)prop.value;
	}
	
	public EntityProperty getProperty(String name) {
		return properties.stream().filter((p) -> p.name.equals(name)).findFirst().orElse(null);
	}
	
	public Rectangle getBounds(Level level) {
		return getBounds(level, -1, new Point(0, 0), 1);
	}
	
	public Rectangle getBounds(Level level, int node) {
		return getBounds(level, node, new Point(0, 0), 1);
	}
	
	public Rectangle getBounds(Level level, int node, Point offset, double zoom) {
		EntityConfig ec = Main.entityConfig.get(name);
		int xPos = x;
		int yPos = y;
		if(node >= 0) {
			if(node < nodes.size()) {
				xPos = nodes.get(node).x;
				yPos = nodes.get(node).y;
			}
		}
		// If there is no EntityConfig, it must be a trigger
		if(ec == null || ec.visualType != VisualType.Image) {
			return new Rectangle((int)((xPos + level.bounds.x + offset.x) * zoom), (int)((yPos + level.bounds.y + offset.y) * zoom), (int)(getPropertyValue("width", 8) * zoom), (int)(getPropertyValue("height", 8) * zoom));
		} else {
			return new Rectangle((int)((xPos + level.bounds.x - ec.imgOffsetX + offset.x) * zoom), (int)((yPos + level.bounds.y - ec.imgOffsetY + offset.y) * zoom), (int)((int)ec.getImage().getWidth() * zoom), (int)((int)ec.getImage().getHeight() * zoom));
		}
	}
	
	@Override
	public Element asElement() {
		Element res = new Element(name);
		res.Attributes = new HashMap<>();
		res.Attributes.put("x", x);
		res.Attributes.put("y", y);
		res.Attributes.put("originX", originX);
		res.Attributes.put("originY", originY);
		res.Attributes.put("id", id);
		for(EntityProperty p : properties) {
			res.Attributes.put(p.name, p.value);
		}
		if(nodes.size() > 0) {
			res.Children = new ArrayList<>();
			for(Point n : nodes) {
				Element node = new Element("node");
				node.Attributes = new HashMap<>();
				node.Attributes.put("x", n.x);
				node.Attributes.put("y", n.y);
				res.Children.add(node);
			}
		}
		return res;
	}

	@Override
	public Entity fromElement(Element element) {
		name = element.Name;
		float tempX = element.AttrFloat("x");
		float tempY = element.AttrFloat("y");
		if(((int) tempX) != tempX || ((int) tempY) != tempY) {
			System.out.println(element.Name + ": Position isn't an integer (" + tempX + ", " + tempY + "). What the heck have you done?");
		}
		
		x = (int)tempX;
		y = (int)tempY;
		originX = element.AttrInt("originX");
		originY = element.AttrInt("originY");
		id = element.AttrInt("id");
		if(id >= NEXT_ID) {
			NEXT_ID = id + 1;
		}
		properties.clear();
		for(Entry<String, Object> e : element.Attributes.entrySet()) {
			if(!(e.getKey().equals("x") || e.getKey().equals("y") || e.getKey().equals("originX") || e.getKey().equals("originY") || e.getKey().equals("id"))) {
				EntityProperty p = new EntityProperty();
				p.name = e.getKey();
				p.value = e.getValue();
				if(p.value instanceof Boolean) {
					p.type = PropertyType.Boolean;
				} else if(p.value instanceof Integer || p.value instanceof Byte || p.value instanceof Short || p.value instanceof Long) {
					p.type = PropertyType.Integer;
				} else if(p.value instanceof Float || p.value instanceof Double) {
					p.type = PropertyType.Float;
				}
				properties.add(p);
			}
		}
		
		nodes.clear();
		if(element.Children != null) {
			for(Element c : element.Children) {
				if(c.Name.equals("node")) {
					nodes.add(new Point(c.AttrInt("x"), c.AttrInt("y")));
				}
			}
		}
		
		return this;
	}

	public static Entity fromPlacementConfig(PlacementConfig config) {
		Entity res = new Entity();
		res.id = NEXT_ID;
		NEXT_ID++;
		res.name = config.name;
		for(EntityProperty ep : config.defaultProperties) {
			res.properties.add(new EntityProperty(ep.name, ep.type, ep.value));
		}
		return res;
	}
}
