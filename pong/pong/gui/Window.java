package pong.gui;

import java.io.IOException;
import javax.swing.JFrame;

/**
 * Une fenetre est une Java Frame contenant un objet Pong.
 */
public class Window extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Pong le jeu a afficher.
	 */
	private final Pong pong;

	/**
	 * Constructeur
	 */
	public Window(Pong pong) {
		this.pong = pong;
		this.addKeyListener(pong);
	}

	/**
	 * Affiche la fenetre et appel
	 * {@link Pong#play()} une methode du {@link Pong} pong.
	 * @throws IOException 
	 */
	public void displayOnscreen() throws IOException {
		this.add(pong);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.pack();
		this.pong.play();
		
	}
}
