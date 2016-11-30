package pong;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Arrays;

import pong.gui.MenuWindow;
import pong.gui.Pong;
import pong.gui.Window;

/**
 * Point de d�part du Pong
 */
public class Main  {
	public static void main(String[] args) throws IOException {
		
		int nbPlayer = Integer.parseInt(args[0]);
		String[] ips = Arrays.copyOfRange(args, 1, args.length);
		
		Pong pong = new Pong(nbPlayer, ips);
		
		//Afficher le matchmaking
		MenuWindow menu = new MenuWindow(pong, nbPlayer, ips.length);
		menu.setTitle("Client " + (args.length));
		menu.displayOnScreen();
		menu.go();
		
		//Affiche le jeu apres le matchmaking
		Window window = new Window(pong);
		Image icon = Toolkit.getDefaultToolkit().createImage(
				ClassLoader.getSystemResource("image/ball" + (args.length) +".png"));
		window.setIconImage(icon);
		window.setTitle("Ponginator client " + (args.length));
		window.displayOnscreen();
	}
}
