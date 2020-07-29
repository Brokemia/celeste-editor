package celesteeditor;

public class UpdateThread implements Runnable {
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(70);
				Main.mainWindow.repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
