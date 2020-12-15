package celesteeditor.ui;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import celesteeditor.Main;
import celesteeditor.data.Level;

public class MapRenderThread implements Runnable {

	@Override
	public void run() {
		AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
		while(true) {
			if(Main.mapPanel.selectedLevel != null) {
				Level level = Main.mapPanel.selectedLevel;
				BufferedImage img = new BufferedImage(level.frameBuffer.getWidth(), level.frameBuffer.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = img.createGraphics();
				g.translate(-level.bounds.x, -level.bounds.y);
				Main.mapPanel.drawTiles(g, level, false);
				Main.mapPanel.drawDecals(g, level, false);
				Main.mapPanel.drawEntities(g, level, alpha);
				Main.mapPanel.drawTiles(g, level, true);
				Main.mapPanel.drawDecals(g, level, true);
				Main.mapPanel.drawTriggers(g, level, alpha);
				if(level == Main.mapPanel.selectedLevel) {
					Main.mapPanel.lock.lock();
					Graphics2D g2d = level.frameBuffer.createGraphics();
					Composite old = g2d.getComposite();
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
					g2d.fillRect(0, 0, level.frameBuffer.getWidth(), level.frameBuffer.getHeight());
					g2d.setComposite(old);
					g2d.drawImage(img, 0, 0, null);
					Main.mapPanel.lock.unlock();
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
