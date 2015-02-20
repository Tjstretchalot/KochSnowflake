import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * Displays MoverPanel
 * 
 * @author Timothy
 */
public class MoverFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MoverFrame() {
		super("FRACTALS");
		
		this.setLocationRelativeTo(null);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setUndecorated(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(new MoverPanel());
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new MoverFrame();
			}
			
		});
	}
}
