package celesteeditor;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.jogamp.opengl.util.awt.TextureRenderer;

import celesteeditor.util.StringEncoding;
import celesteeditor.util.TextureArea;
import github.MichaelBeeu.util.EndianDataInputStream;

public class AtlasUnpacker {
	
	public static HashMap<String, TextureArea> gameplay = new HashMap<>();
	
	public static void loadAtlases() {
		try {
			loadAtlas(gameplay, new File(Main.globalConfig.celesteDir + "\\Content\\Graphics\\Atlases\\Gameplay.meta"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadAtlas(HashMap<String, TextureArea> atlas, File meta) throws FileNotFoundException, IOException {
		try(EndianDataInputStream dis = new EndianDataInputStream(new FileInputStream(meta))) {
			dis.order(ByteOrder.LITTLE_ENDIAN);
			dis.readInt();
			StringEncoding.readString(dis);
			dis.readInt();
			short numFiles = dis.readShort();
			for(int i = 0; i < numFiles; i++) {
				BufferedImage img = loadImage(new DataInputStream(new FileInputStream(new File(meta.getAbsoluteFile().getParent() + "\\" + StringEncoding.readString(dis) + ".data"))));
				ImageIO.write(img, "png", new File("output_img.png"));
				TextureRenderer texRend = new TextureRenderer(img.getWidth(), img.getHeight(), true);
				texRend.createGraphics().drawImage(img, 0, 0, null);
				
				short numImgs = dis.readShort();
				for (int j = 0; j < numImgs; j++) {
					String path = StringEncoding.readString(dis).replace('\\', '/');
					short x = dis.readShort();
					short y = dis.readShort();
					short rWidth = dis.readShort();
					short rHeight = dis.readShort();
					short offsetX = dis.readShort();
					short offsetY = dis.readShort();
					short width = dis.readShort();
					short height = dis.readShort();
					BufferedImage subImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					subImage.createGraphics().drawImage(img.getSubimage(x, y, rWidth, rHeight), -offsetX, -offsetY, null);
					atlas.put(path + ".png", new TextureArea(texRend.getTexture(), new Rectangle(x, img.getHeight() - y - rHeight, rWidth, rHeight), (int)width, (int)height, -offsetX, -offsetY));
				}
			}
		
			
//			if (dis.available() >= 1 && StringEncoding.readString(dis) == "LINKS")
//			{
//				short numLinks = dis.readShort();
//				for (int i = 0; i < numLinks; i++)
//				{
//					String key = StringEncoding.readString(dis);
//					String value = StringEncoding.readString(dis);
//					atlas2.links.Add(key, value);
//				}
//			}
		}
	}
	
	public static BufferedImage loadImage(DataInputStream stream) throws IOException {
		byte[] bytes = new byte[524288];
		byte[] buffer = new byte[67108864];
		stream.read(bytes, 0, 524288);
		int num2 = 0;
		int width = bytesToInt32(bytes, num2);
		int height = bytesToInt32(bytes, num2 + 4);
		boolean flag = bytes[num2 + 8] == 1;
		num2 += 9;
		int num5 = width * height * 4;
		int num6 = 0;
		while (num6 < num5) {
			int num7 = (bytes[num2] & 0xFF) * 4;
			if (flag) {
				byte b = bytes[num2 + 1];
				if (b != 0) {
					buffer[num6] = bytes[num2 + 4];
					buffer[num6 + 1] = bytes[num2 + 3];
					buffer[num6 + 2] = bytes[num2 + 2];
					buffer[num6 + 3] = b;
					num2 += 5;
				} else {
					buffer[num6] = 0;
					buffer[num6 + 1] = 0;
					buffer[num6 + 2] = 0;
					buffer[num6 + 3] = 0;
					num2 += 2;
				}
			} else {
				buffer[num6] = bytes[num2 + 3];
				buffer[num6 + 1] = bytes[num2 + 2];
				buffer[num6 + 2] = bytes[num2 + 1];
				buffer[num6 + 3] = Byte.MAX_VALUE;
				num2 += 4;
			}
			if (num7 > 4) {
				int k = num6 + 4;
				for (int num8 = num6 + num7; k < num8; k += 4) {
					buffer[k] = buffer[num6];
					buffer[k + 1] = buffer[num6 + 1];
					buffer[k + 2] = buffer[num6 + 2];
					buffer[k + 3] = buffer[num6 + 3];
				}
			}
			num6 += num7;
			if (num2 > 524256) {
				int num9 = 524288 - num2;
				for (int l = 0; l < num9; l++) {
					bytes[l] = bytes[num2 + l];
				}
				stream.read(bytes, num9, 524288 - num9);
				num2 = 0;
			}
		}
		BufferedImage texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		int i = 0;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				texture.setRGB(x, y, new Color(buffer[i]& 0xFF, buffer[i+1]& 0xFF, buffer[i+2]& 0xFF, buffer[i+3]& 0xFF).getRGB());
				i += 4;
			}
		}
		
		return texture;
	}
	
	public static int bytesToInt32(byte[] bytes, int startIndex) {
	    ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
	    buffer.order(ByteOrder.LITTLE_ENDIAN);
	    buffer.put(bytes);
	    buffer.flip(); 
	    return buffer.getInt(startIndex);
	}
}
