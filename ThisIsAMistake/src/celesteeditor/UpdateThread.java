package celesteeditor;

import com.jogamp.opengl.util.FPSAnimator;

public class UpdateThread implements Runnable {
	
	@Override
	public void run() {
		final FPSAnimator animator = new FPSAnimator(Main.mapPanel.panel, 60, true);
		animator.start();
	}

}
