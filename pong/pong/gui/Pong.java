 package pong.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import pong.util.Timer;
import pong.util.UtilsFunc;

public class Pong extends JPanel implements KeyListener {

	private static final long serialVersionUID = 1L;

	// Couleurs
	public static final Color backgroundColor = new Color(8, 24, 50);
	
	public static final Color greenColor = new Color(144, 255, 0);
	public static final Color redColor = new Color(255,0,48);
	public static final Color pinkColor = new Color(255, 0, 156);
	public static final Color blueColor = new Color(0, 240, 255);
	public static final Color yellowColor = new Color(255, 240, 0);
	public static final Color whiteColor = new Color(255, 255, 255);
	public static final Color whiteColor1 = new Color(233, 233, 235);
	
	public static final Color fontColor1 = new Color(200, 200, 200);
	
	private static final Color lineColor = new Color(18, 39, 72);
	private static final Color scoreBoardColor = new Color(93,200,68);

	// Element du reseau
	private static final int PROPERTIES_ID = 0;
	private static final int RECEPTION_BUFFER_SIZE = 100;
	public static final char END_CHAR = '$';
	public static final char SEPARATOR_CHAR = ':';
	
	// Element fin de partie
	public static final int SIZE_END_MESS = 40;
	public static final String WIN_STRING = "YOU WIN !";
	public static final String LOSE_STRING = "YOU LOSE !";

	// Element du gameplay
	public static final int SIZE_PONG_X = 800;
	public static final int SIZE_PONG_Y = 600;
	
	public static final int SCORE_BOARD_Y = SIZE_PONG_Y;
	public static final int SIZE_SCORE_BOARD = 75;

	public static final int RACKET_WIDTH = 20;
	public static final int RACKET_HEIGHT = 84;
	public static final int BALL_SIZE = 22;
	
	public static final int RACKET_BORDER_MARGIN = 0;

	public static final Point[] RACKET_STARTING_POS = {new Point(RACKET_BORDER_MARGIN , SIZE_PONG_Y/2 - (RACKET_HEIGHT / 2)),
			new Point(SIZE_PONG_X -(RACKET_WIDTH + RACKET_BORDER_MARGIN) , SIZE_PONG_Y/2 - (RACKET_HEIGHT / 2)),
			new Point(SIZE_PONG_X/2 - (RACKET_HEIGHT / 2) , SIZE_PONG_Y-(RACKET_WIDTH + RACKET_BORDER_MARGIN)),
			new Point(SIZE_PONG_X/2 - (RACKET_HEIGHT / 2) , RACKET_BORDER_MARGIN) };

	public static final Point BALL_STARTING_POS = new Point((SIZE_PONG_X - BALL_SIZE)/2, (SIZE_PONG_Y - BALL_SIZE)/2);

	public static final int B_SPEED = 7;
	public static final Point BALL_SPEED = new Point(B_SPEED, B_SPEED);
	public static final int WIN_SCORE = 10;
	
	public static final int UNREACHABLE_LENGHT = SIZE_PONG_X + SIZE_PONG_Y;

	// Element utilises pour la police custom
	public static final int MARGIN_TEXT_SCORE_BOARD = 10;
	public static final int SCORE_SIZE = 20;
	public static final String FONT = "/font/RetroFont.ttf";

	// Elment du systeme ready.
	private static final String NOT_READY = "Press SPACE when you are ready !";
	private static final int FONT_SIZE = 15;
	private static final int MARGIN_TEXT_READY = 30;
	
	// Elements utilises pour les graphiques
	private BufferedImage initBuffer = null;
	private BufferedImage buffer = null;
	private Graphics2D g2d = null;
	private ArrayList<Image> ballSprites;

	// Elements utilises pour le gameplay
	private Movable ballGhost = null;
	private Ball ball;
	private ArrayList<Player> players;
	private boolean haveMoved = false;

	// Element utilises pour le reseau
	ServerSocketChannel ssc;
	Selector selector;
	private final static int[] PORTS = {7777, 7778, 7779, 7780};
	ByteBuffer byteBuff = ByteBuffer.allocate(RECEPTION_BUFFER_SIZE);

	//Variable utilises pendant le deroulement de la partie
	private int nbPlayers;
	private int myId;
	private boolean screenToUpdate = false;
	private boolean gameEnd = false;
	private boolean win = false;

	//Element utilises par la clock interne pour reguler le jeu.
	private int tick = 0;
	public Timer tickTimer = new Timer(timestep);
	public static final int timestep = 10;

	/**
	 * Creer un Pong. Un Pong regroupe l'enssemble des fonctions necessaire pour jouer.
	 * @param nbPlayers Le nombre de joueurs dans la partie de Pong lancée
	 * @param ips Les adresses IP des joueurs auxquelles il faut se connecter pour jouer.
	 * @throws IOException
	 */
	public Pong(int nbPlayers, String[] ips) throws IOException {		
		this.nbPlayers = nbPlayers;		
		ImageIcon icon;
		//Init du tableau de joueur et ballSprites qui va servir pour afficher la balle de plusieurs couleurs.
		this.ballSprites = new ArrayList<Image>();
		this.players = new ArrayList<Player>();

		//Pour chaque raquette, lui associer une image
		for (int i = 0 ; i != nbPlayers ; i++) {
			Image imageRacket = Toolkit.getDefaultToolkit().createImage(
					ClassLoader.getSystemResource("image/racket" + (i+1) + ".png"));
			icon = new ImageIcon(imageRacket);
			this.players.add(new Player(i, new Racket(RACKET_STARTING_POS[i], icon.getIconWidth(), icon.getIconHeight(), imageRacket)));
			ballSprites.add(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("image/ball" + (i+1) + ".png")));
		}

		icon = new ImageIcon(ballSprites.get(0));
		this.ball = new Ball(BALL_STARTING_POS, icon.getIconWidth(), icon.getIconHeight(), ballSprites.get(0), BALL_SPEED);

		// l'ID du client est egale au nombre d'autre joueurs presents dans la partie.
		this.myId = ips.length;

		if (masterId(ball) != myId)
			ballGhost = moveBall(ball);

		//Pour chaque nouveau client on creer un SSC et on le bind a son port.
		ssc = ServerSocketChannel.open();

		ssc.bind(new InetSocketAddress(PORTS[myId]));
		ssc.configureBlocking(false);

		selector = Selector.open();

		// Pour tous les autres clients deja presents, on se connecte a eux.
		setUpConnexions(ips);

		System.out.println("MyId : " + myId + " players : " + nbPlayers + " already co : "+ ips.length);
		//waitClients(ssc, nbPlayers, ips.length);

		//On initialise fenetre + gestion d'evenement.
		this.setPreferredSize(new Dimension(SIZE_PONG_X, SIZE_PONG_Y+SIZE_SCORE_BOARD)); 
		this.addKeyListener(this);
		initWin();
		this.updateScreen();

	}
		
	/**
	 * Lie un joueur , a l'aide de son ID, a un SocketChannel
	 * @param id l'id du joueur a lier
	 * @param sc la socketChannel a attribué au joueur
	 * @throws IOException
	 */
	public void linkPlayerWithSocket(int id, SocketChannel sc) throws IOException{
		//Si on attrape une connexion, on le met en non-blockant et on la lie a sa raquette.
		sc.configureBlocking(false);
		//On lie un joueur a une socket
		players.get(id).setSocketChannel(sc);
		//On enregistre sa clef
		SelectionKey clientKey = sc.register(
				selector, SelectionKey.OP_READ);
		//On la stock dans notre Map (pour la retrouver facilement apres grace a son id)
		Map<Integer, Integer> properties = new HashMap<Integer, Integer>();
		properties.put(PROPERTIES_ID, id);
		clientKey.attach(properties);
	}

	/**
	 * Permet de récuperer une ServerSocketChannel
	 * @return la dite ServerSocketChannel
	 */
	public ServerSocketChannel getServerSocketChannel(){
		return this.ssc;
	}
		
	/**
	 * Permet de vérifier combien de joueurs il manque pour lancer une partie
	 * @return le nombre de joueurs manquants
	 */
	public int getMissingPlayers(){
		int cpt = 0;
		for (Player p : players){
			if(p.getSocketChannel() == null)
				cpt++;
		}
		return cpt - 1;
	}
		
	/**
	 * @return Son id dans la partie
	 */
	public int getMyId() {
		return this.myId;
	}
	
	/** 
	 * Boucle princpale du jeu qui s'occupe a l'aide de fonction annexes le bon déroulement d'une partie
	 * @throws IOException
	 */
	public void play() throws IOException {
		
		everyoneReady();
		
		boolean playing = true;
		while (playing) {
			
			if(!haveMoved){
				Point p = moveRacket(this.players.get(myId).getRacket()); //Return player pos if speed == 0 return current
				if (!p.equals(this.players.get(myId).getRacket().getPos())) {
					if (this.myId == masterId(this.ball)) 
						this.players.get(this.myId).setNextMove(p);
					else{
						sendRacket(p);
						haveMoved = true;
					}
				}
			}
			
			//Si on est le maitre :
			if (this.myId == masterId(ball)) {
				if (tickTimer.timeUp()) {
					this.ball.set(moveBall(ball));
					this.screenToUpdate = true;
					tickTimer.reset();
					masterSend();
					
					//Si à partir d'ici ce client n'est plus maitre alors il faut commencer à précalculer les positions de balle
					if (masterId(ball) != myId){
						this.ballGhost = moveBall(ball);
					}
				}
			}

			//Ecoute en permanence sur l'entrï¿½e de la socket pour obtenir des infos.
			receive();

			//On update l'affichage si on est dans un bon tick pour.
			if (screenToUpdate) {
				this.updateScreen();
				this.screenToUpdate = false;
			}
		}	
	}
	
	/**
	 * Cette fonction permet d'attendre que tous les joueurs soient prêts pour lancer une partie
	 * @throws IOException
	 */
	private void everyoneReady() throws IOException{
		boolean ready;
		do{
			ready = true;
			receive();
			for (Player p : players){
				if (p != null) {
					if(!p.isReady())
						ready = false;
				}
			}
			try {
				Thread.sleep(timestep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.updateScreen();
		} while(!ready);
	}
	
	/**
	 * Permet de se connecter a tous les autres joueurs
	 * @param ips Les IPs des autres joueurs auxquels il faut se connecter
	 * @throws IOException
	 */
	private void setUpConnexions(String[] ips) throws IOException{
		//Pour chaque IPs on s'y connecte en crï¿½ant une connexion.
		if(ips.length > this.nbPlayers - 1)
			return;
		for(int i = 0 ; i != ips.length ; i++){
			String s = ips[i];
			SocketChannel sc =  SocketChannel.open();
			sc.configureBlocking(false);
			sc.connect(new InetSocketAddress(s, PORTS[i]));

			while (!sc.finishConnect()) {
				System.out.println("Connecting to client nÂ° " + i + " on adress " + s + " on port " + PORTS[i]);
			}
			//On init notre propre tableau de socket en liant les joueurs dï¿½ja prï¿½sent a leur socket respectives
			this.players.get(i).setSocketChannel(sc);

			SelectionKey clientKey = sc.register(
					selector, SelectionKey.OP_READ);

			Map<Integer, Integer> properties = new HashMap<Integer, Integer>();
			properties.put(PROPERTIES_ID, i);
			clientKey.attach(properties);
		}
	}
	
	/**
	 * Permet de lire les messages envoyés par le joueur lié a la clef Key.
	 * @param key la clef associée au joueur envoyant le message
	 */
	private void read(SelectionKey key){
		SocketChannel sc = (SocketChannel) key.channel();
		@SuppressWarnings("unchecked") // TODO lol
		int id = ((Map<Integer, Integer>) key.attachment()).get(PROPERTIES_ID);
		Player p = this.players.get(id);
		byteBuff.clear();
		int bytesRead;
		try {
			if ((bytesRead = sc.read(byteBuff)) > 0) {
				byteBuff.flip();
				String message = Charset.defaultCharset().decode(byteBuff).toString();
				p.addToBuffer(message);
			} else if (bytesRead < 0) {
				afficher("Connection got closed with player : " + id + "!!!");
				removePlayer(id);
			}
		} catch (IOException e) {
			removePlayer(id);
		}
	}
	
	/**
	 * Permet de recevoir les messages
	 * @throws IOException
	 */
	private void receive() throws IOException{
		if (selector.selectNow() > 0) {// Peut throw
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectedKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				// S'il n'y a pas que des clients (ex nouvelles connections entrantes possibles), le gerer.
				if (key.isReadable()) {
					this.read(key);
				}
			}
			selectedKeys.clear();
		}

		//Une fois qu'on a tout recu, on dï¿½crypte tout ce que l'on peut.
		for (Player p : players){
			if (p != null) {
				if (p.getCurrentMessage().lastIndexOf(Character.toString(END_CHAR)) != -1){
					decryptMessage(p.getId(), p.getCurrentMessage());
					p.refreshBuffer();
				}
			}
		}
	}
	
	/**
	 * Cette fonction permet de determier qui est le maître actuel d'un objet Movable en fonction de la position de celui ci
	 * @param m un objet de type Movable (la plus part du temps : la balle)
	 * @return l'ID du maitre actuel
	 */
	private int masterId(Movable m) { 
		ArrayList<Integer> l = new ArrayList<Integer>();
		for (int i = 0 ; i != players.size() ; i++) {
			if (players.get(i) == null)
				l.add(UNREACHABLE_LENGHT);
			else
				switch (i) {
				case 0:
					l.add(m.getPos().x);
					break;
				case 1:
					l.add(SIZE_PONG_X - (m.getPos().x + m.getWidth()));
					break;
				case 2:
					l.add((SIZE_PONG_Y - (m.getPos().y + m.getHeight())) * SIZE_PONG_X / SIZE_PONG_Y);
					break;
				case 3:
					l.add(m.getPos().y * SIZE_PONG_X / SIZE_PONG_Y);
					break;
				}
		}
		return UtilsFunc.lowestOf(l);

	}
	
	/**
	 * Envoi un message a tous les joueurs connectés sauf sois même
	 * @param s le message a envoyer
	 */
	private void sendToAll(String s)  {
		CharBuffer charBuff = CharBuffer.wrap(s);
		for (Player p : players) {
			if (p != null) {
				if(p.getId() != myId){
					while (charBuff.hasRemaining()) {
						try {
							p.getSocketChannel().write(Charset.defaultCharset().encode(charBuff));
						} catch (IOException e) { // TODO gï¿½rer ClosedChannelException
							this.removePlayer(p.getId());
						}
					}
					charBuff.position(0);
				}
			}
		}
	}
	
	/**
	 * Envoi un message au maître actuel
	 * @param s le message a envoyer
	 */
	private void sendToMaster (String s) {
		CharBuffer charBuff = CharBuffer.wrap(s);
		Player p = players.get(masterId(this.ball));
		while (charBuff.hasRemaining()) {
			try {
				p.getSocketChannel().write(Charset.defaultCharset().encode(charBuff));
			} catch (IOException e) { // TODO gï¿½rer ClosedChannelException
				this.removePlayer(p.getId());
			}
		}
	}
	
	/**
	 * Supprime un joueur : le deconnecte
	 * @param id l'id du joueur a supprimer
	 */
	private void removePlayer(int id) {
		SelectionKey sk = this.players.get(id).getSocketChannel().keyFor(selector);
		sk.cancel();
		this.players.set(id, null);
	}
	
	/**
	 * Fonction utilisee par le maitre pour envoyer la balle + les pos des raquettes aux autres joueurs
	 */
	private void masterSend() {
		StringBuffer sb = new StringBuffer();
		sb.append("M" + SEPARATOR_CHAR
				+ (int) this.ball.getPos().getX() + SEPARATOR_CHAR 
				+ (int) this.ball.getPos().getY());
		
		for (Player p : players) {
			if(p !=null){
				Point pt = p.updateMove(); 
				if(pt != null){
					sb.append(SEPARATOR_CHAR);
					sb.append(p.getId());
					sb.append(SEPARATOR_CHAR);
					sb.append(new Double(pt.getX()).intValue());
					sb.append(SEPARATOR_CHAR);
					sb.append(new Double(pt.getY()).intValue());
				}
			}
		}
		sb.append(END_CHAR);
		sendToAll(sb.toString());
		
		tick++;
	}
	
	/**
	 * Fonction utilisee par les joueurs non maitre pour envoyer la position de leur raquette au maitre
	 * @param p la position de la raquette
	 */
	private void sendRacket(Point p) {
		sendToMaster("R" + SEPARATOR_CHAR 
				+ (int) p.getX() + SEPARATOR_CHAR 
				+ (int) p.getY() + END_CHAR); // 
	}
	
	/**
	 * Envoi le signal PRET aux autres joueurs
	 */
	private void sendReady() {
		sendToAll("READY" + END_CHAR);
	}	

	/**
	 * Decrypte un un message reçu : le partitionne en plusieurs petits messages, eux mêmes décrypter par une autre fonction
	 * @param playerId l'ID du joueur envoyant le message
	 * @param s le message a decrypter
	 */
	private void decryptMessage(int playerId, String s){
		StringBuffer b = new StringBuffer();

		for (int i = 0 ; i != s.length() ; i++) {
			char c = s.charAt(i);
			if (c == END_CHAR) {
				decrypt(playerId, b.toString());
				b.delete(0, b.length());
			} else
				b.append(c);
		}
	}
	
	/**
	 * Decrypte un petit message
	 * @param playerId l'ID du joueur envoyant le message
	 * @param s le petit message
	 */
	private void decrypt(int playerId, String s){
		StringTokenizer st = new StringTokenizer(s, Character.toString(SEPARATOR_CHAR));
		String itemType = st.nextToken();

		ArrayList<Integer> values = new ArrayList<Integer>();

		while(st.hasMoreTokens()){
			String tmp = st.nextToken();
			values.add(Integer.parseInt(tmp));
		}

		switch(itemType){
		case "R":
			updateNextRacket(playerId, values.get(0), values.get(1));
			break;
		case "M":
			updateBall(values.get(0), values.get(1));
			
			for (int i = 2 ; i < values.size() ; i += 3) {
				updateRacket(values.get(i), values.get(i+1), values.get(i+2));
			}
			break;
		/*case "B":
			updateBall(values.get(0), values.get(1));
			break;*/
		case "READY":
			this.players.get(playerId).setReady(true);
			break;
		}
	}
	
	/**
	 * Fonction utiliser par le maitre pour mettre a jour la position d'un joueur
	 * @param playerId l'ID du joueur dont il faut mettre a jour la position
	 * @param x la position x
	 * @param y la position y
	 */
	private void updateNextRacket(int playerId, int x, int y){
		//afficher("player : " + playerId + "pos :("+ x + "," + y +")");
		boolean canMove = false;
		
		if(playerId < 2){
			if(this.players.get(playerId).getRacket().getPos().y + Racket.SPEED >= y ){
				if(x == RACKET_STARTING_POS[playerId].x)
					canMove = true;
			}
		}else{
			if(this.players.get(playerId).getRacket().getPos().x + Racket.SPEED >= x){
				if(y == RACKET_STARTING_POS[playerId].y)
					canMove = true;
			}
		}
		if(canMove){
			this.players.get(playerId).setNextMove(new Point(x, y));
			this.screenToUpdate = true;
		} else {
			afficher("x=" + x + "y=" + y + "myX = " + this.players.get(playerId).getRacket().getPos().x + "myY =" + this.players.get(playerId).getRacket().getPos().y);
			//removePlayer(playerId);
		}
	}
	
	/**
	 * Fonction utilisée par un joueur non maitre pour mettre a jour la position d'un joueur
	 * * @param playerId l'ID du joueur dont il faut mettre a jour la position
	 * @param x la position x
	 * @param y la position y
	 */
	private void updateRacket(int playerId, int x, int y){
		//afficher("MASTER TO SLAVE -> player : " + playerId + "pos :("+ x + "," + y +")");
		boolean canMove = false;
		
		if(playerId < 2){
			if(this.players.get(playerId).getRacket().getPos().y + Racket.SPEED >= y ){
				if(x == RACKET_STARTING_POS[playerId].x)
					canMove = true;
			}
		}else{
			if(this.players.get(playerId).getRacket().getPos().x + Racket.SPEED >= x){
				if(y == RACKET_STARTING_POS[playerId].y)
					canMove = true;
			}
		}
		if(canMove){
			this.players.get(playerId).getRacket().setPos(new Point(x, y));
			this.screenToUpdate = true;
		} else {
			afficher("x=" + x + "y=" + y + "myX = " + this.players.get(playerId).getRacket().getPos().x + "myY =" + this.players.get(playerId).getRacket().getPos().y);
			//removePlayer(playerId);
		}
	}
	
	/**
	 * Met a jour la position de la balle en fonction de la position de son ghost
	 * @param x la position x
	 * @param y la position y
	 */
	private void updateBall(int x, int y){
		if(ballGhost.getPos().x != x || ballGhost.getPos().y != y){
			//afficher("ERROR 215;Mon Ghost: " + ballGhost.getPos().x + ":" + ballGhost.getPos().y + ";Master Ball : " + x + ": " + y);
		}// else {
		this.ball.set(ballGhost);
		
		//TODEL
		this.ball.setPos(new Point(x, y));
		
		this.screenToUpdate = true;
		tickTimer.reset();
		this.haveMoved = false;
		
		this.players.get(this.myId).setNextMove(null);

		tick++;
		
		if(masterId(ball) == myId)
			ballGhost = null;
		else
			this.ballGhost.set(moveBall(ball));	
	}
	
	/**
	 * Simule un mouvement de raquette en fonction de RACKET_SPEED
	 * @param r la raquette a sa position de base
	 * @return la nouvelle position si la raquette bouge
	 */
	private Point moveRacket(Movable r) {
		Point p = r.getPos();
		p.translate(r.getSpeed().x, r.getSpeed().y);
		if (p.x < 0)
			p.x = 0;
		else if (p.x > Pong.SIZE_PONG_X - r.getWidth()) 
			p.x = Pong.SIZE_PONG_X - r.getWidth();
		if (p.y < 0)
			p.y = 0;
		else if (p.y > Pong.SIZE_PONG_Y - r.getHeight())
			p.y = Pong.SIZE_PONG_Y - r.getHeight();
		
		return p;
	}
	
	/**
	 * Simule un mouvement de la balle en fonction de BALL_SPEED
	 * Calcul en même temps s'il y en a : les collisions , et les points a attribuer
	 * @param b la balle a sa position actuelle
	 * @return la nouvelle balle a sa nouvelle position
	 */
	private Movable moveBall(Ball b) {
		Movable m = new Movable(b);//
		m.move();
		m.setSprite(this.ballSprites.get(masterId(b)));
		if (m.getPos().x < 0) {
			if(players.size() > 0){
				if(players.get(0) != null){
					for(int i = 0 ; i < players.size() ; i++){
						if (players.get(i) != null && players.get(i).getId() != 0){
							players.get(i).updateScore(1);
						}
					}
					checkState();
				}
			}
			m.setPosX(0);
			m.setSpeedX(-m.getSpeed().x);
		} else if (m.getPos().x > Pong.SIZE_PONG_X - m.getWidth()) {
			if(players.size() > 1){
				if(players.get(1) != null){
					for(int i = 0 ; i < players.size() ; i++){
						if (players.get(i) != null && players.get(i).getId() != 1){
							players.get(i).updateScore(1);
						}
					}
					checkState();
				}
			}
			m.setPosX(Pong.SIZE_PONG_X - m.getWidth());
			m.setSpeedX(-m.getSpeed().x);
		}
		if (m.getPos().y < 0) {
			if(players.size() > 3){
				if(players.get(3) != null){
					for(int i = 0 ; i < players.size() ; i++){
						if (players.get(i) != null && players.get(i).getId() != 3){
							players.get(i).updateScore(1);
						}
					}
					checkState();
				}
			}
			m.setPosY(0);
			m.setSpeedY(-m.getSpeed().y);
		} else if (m.getPos().y > Pong.SIZE_PONG_Y - m.getHeight()) {
			if(players.size() > 2){
				if(players.get(2) != null){
					for(int i = 0 ; i < players.size() ; i++){
						if (players.get(i) != null && players.get(i).getId() != 2){
							players.get(i).updateScore(1);
						}
					}
					checkState();
				}
			}
			m.setPosY(Pong.SIZE_PONG_Y - m.getHeight());
			m.setSpeedY(-m.getSpeed().y);
		}
		
		for (Player p : players) {
			if (p != null) {
				Racket racket = p.getRacket();
				if (racket.collision(m)) {
					afficher("player :" + p.getId() + " pos :(" + racket.getPos().x + "," + racket.getPos().y + ") ball pos :(" + this.ball.getPos().x + "," + this.ball.getPos().y + ")");
					int speedX;
					int speedY;
					if (p.getId() == 0 || p.getId() == 1) {
						speedY = -new Double(Math.cos(Math.toRadians((double) 45.0 + 
								(m.getPos().y - racket.getPos().y) * 90.0/racket.getHeight())) 
								* Pong.B_SPEED).intValue();
						if (p.getId() == 0) {
							ball.setPosX(racket.getPos().x + racket.getWidth());
							speedX = (int) Math.sqrt(Pong.B_SPEED * Pong.B_SPEED - speedY * speedY);
						} else {
							ball.setPosX(racket.getPos().x - ball.getWidth());
							speedX = -(int) Math.sqrt(Pong.B_SPEED * Pong.B_SPEED - speedY * speedY);
							
						}
					} else {
						speedX = -new Double(Math.cos(Math.toRadians((double) 45.0 + 
								(m.getPos().x - racket.getPos().x) * 90.0/racket.getWidth())) 
								* Pong.B_SPEED).intValue();
						if (p.getId() == 2) {
							m.setPosY(racket.getPos().y - m.getHeight());
							speedY = -(int) Math.sqrt(Pong.B_SPEED * Pong.B_SPEED - speedX * speedX);
						} else {
							m.setPosY(racket.getPos().y + racket.getHeight());
							speedY = (int) Math.sqrt(Pong.B_SPEED * Pong.B_SPEED - speedX * speedX);
						}
					}
					afficher("new speed :("+speedX+"," + speedY + ")");
					m.setSpeed(new Point(speedX, speedY));
				}
			}
		}
		return m;
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
			if(this.myId < 2)
				this.players.get(myId).getRacket().up();
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			if(this.myId > 1)
				this.players.get(myId).getRacket().left();
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
			if(this.myId < 2)
				this.players.get(myId).getRacket().down();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			if(this.myId > 1)
				this.players.get(myId).getRacket().right();
			break;
		case KeyEvent.VK_SPACE:
			this.sendReady();
			this.players.get(myId).setReady(true);
			break;

		default:
			System.out.println("got press "+e);
		}
	}
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
			if(this.myId < 2)
				this.players.get(myId).getRacket().noMove();
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			if(this.myId > 1)
				this.players.get(myId).getRacket().noMove();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			if(this.myId > 1)
				this.players.get(myId).getRacket().noMove();
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
			if(this.myId < 2)
				this.players.get(myId).getRacket().noMove();
			break;
		case KeyEvent.VK_SPACE:
			break;
		default:
			System.out.println("got release "+e);
		}
	}
	public void keyTyped(KeyEvent e) { }
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.drawImage(buffer, 0, 0, this);
	}
	
	/**
	 * Fonction qui permet d'afficher les elements graphiques
	 */
	public void updateScreen() {		
		if (buffer == null)
			throw new RuntimeException("Could not instanciate graphics");
		else {
			buffer=UtilsFunc.deepCopy(initBuffer);
			g2d = buffer.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g2d.setFont(setUpFont(createFont(FONT), (float)(FONT_SIZE)));
		
		if(!gameEnd){
			// Dessine la balle
			ball.draw(g2d);
			
			g2d.setColor(whiteColor1);
			
			int posNotReady = SIZE_PONG_Y/2;
			if(!this.players.get(myId).isReady()){
				drawCenteredString(0, 0, SIZE_PONG_X, SIZE_PONG_Y, NOT_READY);
				posNotReady += MARGIN_TEXT_READY;
			}
			
			int part = SIZE_PONG_X/nbPlayers; //On separe ce tableau en X parts (X = nombre de joueurs)
			int cpt = 0;
			
			//Dessin des raquettes + des joueurs non prets
			for (int i = 0 ; i < nbPlayers ; i++){
				g2d.setColor(whiteColor1);
				// Retabli la taille de la police
				g2d.setFont(setUpFont(g2d.getFont(), (float)(FONT_SIZE)));
				
				if (players.get(i) != null) {
					if(!players.get(i).isReady() && players.get(i).getId() != this.myId ){
						String message = "Player " + (players.get(i).getId()+1) + " still not ready" ;
						g2d.drawString(message, SIZE_PONG_X/2 - g2d.getFontMetrics().stringWidth(message)/2, posNotReady);
						posNotReady += MARGIN_TEXT_READY;
					}
					players.get(i).getRacket().draw(g2d);
				}
				
				
				//Score
				g2d.setColor(fontColor1);
				g2d.setFont(setUpFont(g2d.getFont(), (float)(SCORE_SIZE)));
				
				if(i == myId){
					g2d.setColor(backgroundColor);
				} else {
					//Sinon, si c'est pas la bonne couleur je la remet
					if( g2d.getColor().equals(backgroundColor)){
						g2d.setColor(fontColor1);
					}
				}
				
				int yPart = SIZE_SCORE_BOARD / 4;
				int y = SCORE_BOARD_Y + 2*yPart;
				y+= yPart;
				if(players.get(i) != null)
					drawXCenteredString(cpt, y, part, Integer.toString(this.players.get(i).getScore()));
				else{
					drawXCenteredString(cpt, y, part, "OUT");
				}
				cpt+=part;
			}
		} else if(win){
			dispEnd(WIN_STRING, greenColor);
		} else {
			dispEnd(LOSE_STRING, redColor);
		}
		
		this.repaint();
	}
	
	/**
	 * Initialise les elements graphiques permanent pour alleger le travail de updateScreen()
	 */
	private void initWin(){
		if (initBuffer == null) {
			this.initBuffer = new BufferedImage(SIZE_PONG_X, SIZE_PONG_Y + SIZE_SCORE_BOARD, BufferedImage.TYPE_INT_RGB);
			this.buffer = new BufferedImage(SIZE_PONG_X, SIZE_PONG_Y + SIZE_SCORE_BOARD, BufferedImage.TYPE_INT_RGB);
		} if (initBuffer == null){
			
		} else {		
			g2d = initBuffer.createGraphics();
		}

		int nbPlayers = this.players.size();
		
		
		//On rempli la fenetre de jeu (sans le score board) de backgroundColor
		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, SIZE_PONG_X, SIZE_PONG_Y);

		//On rempli le score board de scoreBoardColor
		g2d.setColor(scoreBoardColor);
		g2d.fillRect(0, SIZE_PONG_Y, SIZE_PONG_X, SIZE_PONG_Y + SIZE_SCORE_BOARD);

		//On dessine le terrain
		g2d.setColor(lineColor);
		g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.drawOval((SIZE_PONG_X/2)-150, (SIZE_PONG_Y/2)-150, 300, 300);
		g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.drawLine(SIZE_PONG_X/2, 0, SIZE_PONG_X/2, SIZE_PONG_Y);
		//Si le nombre de joueur > 2 alors on dessine aussi la ligne verticale
		if(nbPlayers > 2){
			g2d.drawLine(0, SIZE_PONG_Y/2, SIZE_PONG_X, SIZE_PONG_Y/2);
		}
		//On dessine la sï¿½paration avec le score board
		g2d.drawLine(0, SIZE_PONG_Y, SIZE_PONG_X, SIZE_PONG_Y);

		//On init notre police d'ï¿½criture
		g2d.setColor(fontColor1);
		g2d.setFont(setUpFont(createFont(FONT), (float)(SCORE_SIZE)));

		//On dessine le tableau de score
		int part = SIZE_PONG_X/nbPlayers; //On sï¿½pare ce tableau en X parts (X = nombre de joueurs)
		int cpt = 0;
		
		//Pour chaque joueur on lui ecris son nom et son score.
		for(int i = 0 ; i < nbPlayers ; i++){
			//Si c'est moi j'ai le droit a une couleur spï¿½ciale
			if(i == myId){
				g2d.setColor(backgroundColor);
			} else {
				//Sinon, si c'est pas la bonne couleur je la remet
				if( g2d.getColor().equals(backgroundColor)){
					g2d.setColor(fontColor1);
				}
			}
			String s = "Player " + (i+1);
			int yPart = SIZE_SCORE_BOARD / 4;
			int y = SCORE_BOARD_Y + 2*yPart;
			drawXCenteredString(cpt, y, part, s);
			y+= yPart;
			cpt+=part;
		}
	}
	
	/**
	 * Fonction de debuggage qui permet d'afficher un message et l'heure interne a laquelle il a été emis
	 * @param s le message a afficher
	 */
	private void afficher(String s) {
		System.out.println("T="+tick+";"+s);
	}
	
	/**
	 * Affiche une String centré en fonction de x, y, w
	 * @param x la position x
	 * @param y la position y
	 * @param w la largeur
	 * @param s la string
	 */
	private void drawXCenteredString(int x, int y, int w, String s) {
		g2d.drawString(s, x + w/2 - g2d.getFontMetrics().stringWidth(s)/2, y);
	}
	
	/**
	 * Affiche une String centré en fonction de x, y, h, w
	 * @param x la position x
	 * @param y la position y
	 * @param w la largeur
	 * @param h la hauteur
	 * @param s la string
	 */
	private void drawCenteredString(int x, int y, int w, int h, String s) {
		g2d.drawString(s, x + w/2 - g2d.getFontMetrics().stringWidth(s)/2,
				y + h/2);
	}

	/**
	 * Affiche l'écran de fin
	 * @param mess le message de l'ecran de fin
	 * @param c la couleur du message
	 */
	private void dispEnd(String mess, Color c){
		g2d.setColor(c);
		g2d.setFont(setUpFont(createFont(FONT), (float)(SIZE_END_MESS)));
		drawCenteredString(0, 0, SIZE_PONG_X, SIZE_PONG_Y,  mess);
	}
	
	/**
	 * Utilise par createFont, permet de parametrer la taille d'une police personelle
	 * @param f la police
	 * @param size la nouvelle taille
	 * @return la police parametree
	 */
	public Font setUpFont(Font f, float size){
		return f.deriveFont(size);
	}
	
	/**
	 * Creer un objet Font a partir d'un fichier de police
	 * @param name le nom du fichier a charger
	 * @return la police
	 */
	public Font createFont(String name){
		InputStream fontStream = getClass().getResourceAsStream(name);
		Font f = null;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT, fontStream);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return f;
	}
	
	/**
	 * Verifie l'etat actuel d'une partie : Un seul gagnant => Termine sinon en cours
	 */
	private void checkState(){
		boolean IAmALoser = false;
		ArrayList<Integer> winnersID = new ArrayList<Integer>();
		ArrayList<Integer> losersID = new ArrayList<Integer>();
		
		for(Player p : players){
			if(p != null){
				if(p.getScore() < WIN_SCORE){
					if(p.getId()== myId){
						IAmALoser = true;
					}
					losersID.add(p.getId());
				} else {
					//Dans l'optique ou on est plusieurs winner, on recommence tous a 0.
					p.setScore(0);
					winnersID.add(p.getId());
				}
			}
		}//Fin d'update les deux tableaux
		
		if(winnersID.size() > 0){
			if(IAmALoser){ //Si je suis un loser
				this.gameEnd = true;
			} else if(winnersID.size() == 1){ //Sinon si il y a qu'un winner c'est forcement moi
				this.win = true; // J'ai gagnï¿½
				this.gameEnd = true;				
			}
			for(Integer i : losersID){ //Je me deco de tous ceux qui ont perdu et dans le cas ou je suis moi mï¿½me perdant j'ï¿½vite de me dï¿½co tout seul
				if(players.get(i).getId() != myId){
					removePlayer(i);
				}
			}
		}
	}
	
}



