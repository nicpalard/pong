package pong.gui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * Un MenuWindow est une Java Frame contenant un menu qui permet de lancer une partie de pong
 */
public class MenuWindow extends JFrame {


	private static final long serialVersionUID = 1L ;

	private Menu m;
	private Pong pong;
	
	/**
	 * @param pong une instance du pong
	 * @param nbPlayers le nombre maximum de joueur
	 * @param ips les ips des joueurs auxquels on d�sire se connecter
	 */
	public MenuWindow(Pong pong, int nbPlayers, int nbIp){
		this.pong = pong;
		this.m = new Menu(nbPlayers, nbIp);
	}
	
	/**
	 * @throws IOException
	 */
	public void go() throws IOException{
			this.m.waitClients(this.pong);
	}
	
	
	/**
	 * Cette fonction permet d'afficher le menu.
	 */
	public void displayOnScreen(){
		this.add(m);
		this.setResizable(false);
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	public static class Menu extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		//Positions des diff�rents �l�ments graphiques
		private static final int SIZE_MENU_X = 350;
		private static final int SIZE_MENU_Y = 450;
		
		private static final int SIZE_TITLE_AREA_Y = 148;
		
		private static final int START_MAKING_RECT_X = 50;
		private static final int SIZE_SQUARE = 250;
		
		private static final int TITLE_SIZE = 20;
		private static final int SUBTITLE_SIZE = 14;
		
		private static final int MARGIN_SEARCHING = 10;
		private static final int MARGIN_MAKING = 117;
		private static final int MARGIN_WAITING = 208;
		
		//String des �l�ments graphiques
		private static final String MATCHMAKING = "MATCHMAKING";
		private static final String SEARCHING = "Currently searching";
		
		//Elements pour l'animation du cercle
		private static final int START_ROUND_ANIM_Y = 80;
		private static final int LITTLE_ROUND_SIZE = 8;
		private static final int RADIUS = 35;
		private static final double MAX_ANGLE = 360.0;
		private static final double ANGLE_INC = 5.0;
		private static final double ANGLE_DEC = 30.0;
		private static final int NB_POINTS_ANIM = 12;
		private ArrayList<Double> angles = null;
		
		//Clock interne pour l'affichage
		private static final int timestep = 30;
		private long lastTickTime = 0;
		
		//Element utiliser pour detecter ou non le lancement d'une partie
		public static final int WAITING_TIME = 3000;
		private boolean timerEnd = false;
		private long end = 0;
		private int cptCo;
		private int stillMissing = 0;

		private int nbPlayers;
		
		//Elements pour utiliser les graphics
		private BufferedImage buffer = null;
		private Graphics2D g2d = null;
		
		/**
		 * Constructeur de menu
		 * @param nbPlayers le nombre de joueurs maximum dans la partie
		 * @param alreadyCo le nombre de joueurs qui sont d�j� connect�s et en attente d'une partie
		 */
		public Menu(int nbPlayers, int alreadyCo){
			this.nbPlayers = nbPlayers;
			this.setPreferredSize(new Dimension(SIZE_MENU_X, SIZE_MENU_Y)); 
			this.angles = new ArrayList<Double>(NB_POINTS_ANIM);
			for(int i = 0 ; i < NB_POINTS_ANIM ; i++){
				this.angles.add(i*ANGLE_DEC);
			}
			this.cptCo = alreadyCo + 1;
			this.updateScreen();
		}

		/**
		 * @param pong Une instance du pong
		 * @throws IOException
		 */
		public void waitClients(Pong pong) throws IOException{
			boolean allConnected = false;
			while(!allConnected || !this.timerEnd){
				
				this.stillMissing = pong.getMissingPlayers();
				
				//Clock pour ralentir le refresh l'�cran sans devoir sleep.
				long currentTime = System.currentTimeMillis();
				if(this.lastTickTime + timestep < currentTime){
					updateScreen();
					this.lastTickTime = currentTime;
				}
				
				//Si on peut encore acceuillir des joueurs
				if (cptCo < nbPlayers) {
					if (cptCo != pong.getMyId()) {
						SocketChannel sc = null;
						//On accepte tout
						sc = pong.getServerSocketChannel().accept();
						if (sc != null) {
							pong.linkPlayerWithSocket(cptCo, sc);
							cptCo++;
						}
					}
				}
				//Si on a tous nos joueurs
				if(this.stillMissing == 0){
					allConnected = true;
					if(end == 0)
						end = System.currentTimeMillis() + WAITING_TIME;
					//Si on a attendu
					if(System.currentTimeMillis() > end){
						timerEnd = true;
					}
				}
			}
		}
		
		/**
		 * Animation du cercle
		 */
		public void roundAnimation(){
			for(int i = 0 ; i < NB_POINTS_ANIM ; i++){
				angles.set(i, (angles.get(i)+ANGLE_INC) % MAX_ANGLE);
				Point pos = pointFromAngle(angles.get(i));
				g2d.fillOval(SIZE_MENU_X/2 + pos.x, SIZE_TITLE_AREA_Y + START_ROUND_ANIM_Y + RADIUS + pos.y, 
						LITTLE_ROUND_SIZE, LITTLE_ROUND_SIZE);
			}
		}

		/**
		 * @param angle l'angle avec lequel on souhaite calculer le nouveau Point.
		 * @return le dit Point.
		 */
		public Point pointFromAngle(double angle){
			int x = new Double(Math.cos(Math.toRadians(angle)) * RADIUS).intValue();
			int y = new Double(Math.sin(Math.toRadians(angle)) * RADIUS).intValue();
			return new Point(x, y);
		}
		
		/**
		 * Creer et ajoute au buffer les �l�ments graphiques
		 */
		public void updateScreen(){
			if(this.buffer == null){
				this.buffer = new BufferedImage(SIZE_MENU_X, SIZE_MENU_Y, BufferedImage.TYPE_INT_ARGB);
				
				if(this.buffer == null)
					throw new RuntimeException("Could not instanciate graphics"); 
				else
					g2d = buffer.createGraphics();
			}
			
			//Pour etre en HD.
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			g2d.setColor(Pong.backgroundColor);
			g2d.fillRect(0, 0, SIZE_MENU_X, SIZE_MENU_Y);
			
			Image title = Toolkit.getDefaultToolkit().createImage(
					ClassLoader.getSystemResource("image/Pong.png"));
			ImageIcon icon = new ImageIcon(title);
			
			g2d.drawImage(title, (SIZE_MENU_X - icon.getIconWidth())/2, SIZE_TITLE_AREA_Y/2 - icon.getIconHeight(), null);
			
			InputStream fontStream = getClass().getResourceAsStream(Pong.FONT);
			try {
				//On charge le fichier
				Font fontCus = Font.createFont(Font.TRUETYPE_FONT, fontStream); 
				//On ajuste la taille de la police
				Font myFont = fontCus.deriveFont((float)TITLE_SIZE);
				g2d.setFont(myFont);
			} catch (FontFormatException | IOException e) {e.printStackTrace();}
			
			g2d.setColor(Pong.whiteColor1);
			
			int fontW = g2d.getFontMetrics().stringWidth(MATCHMAKING);
			int fontH = g2d.getFontMetrics().getHeight();
			g2d.drawString(MATCHMAKING, (SIZE_MENU_X - fontW)/2, MARGIN_MAKING);
			
			g2d.setFont(g2d.getFont().deriveFont((float)SUBTITLE_SIZE));
			
			
			fontW = g2d.getFontMetrics().stringWidth(SEARCHING);
			fontH = g2d.getFontMetrics().getHeight();
			g2d.drawString(SEARCHING, (SIZE_MENU_X - fontW)/2, SIZE_TITLE_AREA_Y + MARGIN_SEARCHING + fontH);
			
			String wait = null;
			if(this.stillMissing != 0){
				wait = "still " + stillMissing + " players left";
			} else {
				wait = "Starting game ...";
			}
			
			fontW = g2d.getFontMetrics().stringWidth(wait);
			g2d.drawString(wait, (SIZE_MENU_X - fontW)/2, SIZE_TITLE_AREA_Y + MARGIN_WAITING + fontH);
			
			g2d.setColor(Pong.blueColor);
			g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2d.drawRect(START_MAKING_RECT_X, SIZE_TITLE_AREA_Y, SIZE_SQUARE, SIZE_SQUARE);
			
			//Petite animation
			roundAnimation();
			
			this.repaint();
		}
		
		/**
		 * Paint les elements graphiques.
		 */
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(buffer, 0, 0, this);
		}
		
	}
}
