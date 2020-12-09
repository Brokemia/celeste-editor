package celesteeditor;

public class UpdateThread implements Runnable {
	
	@Override
	public void run() {
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
