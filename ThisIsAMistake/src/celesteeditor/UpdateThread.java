package celesteeditor;

import com.jogamp.opengl.util.FPSAnimator;

public class UpdateThread implements Runnable {
	
	@Override
	public void run() {
		final FPSAnimator animator = new FPSAnimator(Main.mapPanel.glcanvas, 60, true);
		new Thread() {
			public void run() {
				animator.start();
			}
		}.start();
		
		while(true) {
			//try {
				Thread.yield();
				if(Main.mapPanel.renderingComplete) {
					Main.mainWindow.repaint();
				}
			/*} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
	}

}
