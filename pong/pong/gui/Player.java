package pong.gui;

import java.awt.Point;

import java.nio.channels.SocketChannel;

/**
 *	Un player est un joueur possedant sa raquette et ses outils pour communiquer avec les autres joueurs
 */
public class Player {
	private int id;
	private Racket racket;
	private int score;
	private SocketChannel socketChannel;
	private boolean ready;
	private StringBuffer currentMessage;
	private Point nextMove;
	
	/**
	 * Constructeur de player
	 * @param id l id du joueur
	 * @param racket la raquette associe a ce joueur
	 */
	public Player(int id, Racket racket){
		this.id = id;
		this.racket = racket.clone();
		this.score = 0;
		this.socketChannel = null;
		this.currentMessage = new StringBuffer();
		this.ready = false;
		this.nextMove = null;
	}
	
	/**
	 * @return l id du joueur en question
	 */
	public int getId() {
		return id;
	}

	/**
	 * Modifie l id du joueur en question
	 * @param id le nouvel id a donner au joueur
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return la raquette du joueur en question
	 */
	public Racket getRacket() {
		return racket;
	}

	/**
	 * Modifie la raquette du joueur en question
	 * @param racket la raquette a donner au joueur
	 */
	public void setRacket(Racket racket) {
		this.racket = racket;
	}

	/**
	 * @return le score du joueur en question
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Modifie le score du joueur en question
	 * @param score le socre a donne au joueur
	 */
	public void setScore(int score) {
		this.score = score;
	}
	
	/**
	 * Incremente le score du joueur en question
	 * @param value la value avec laquelle on incremente le score du joueur
	 */
	public void updateScore (int value) {
		this.score += value;
	}
	
	/**
	 * @return le socketChannel du joueur en question
	 */
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
	
	/**
	 * Modifie la socketChannel du joueur en question
	 * @param sc la socketChannel a donner au joueur
	 */
	public void setSocketChannel(SocketChannel sc) {
		this.socketChannel = sc;
	}

	/**
	 * @return un booleen correspondant a l etat du joueur en question
	 */
	public boolean isReady(){
		return this.ready;
	}
	
	/**
	 * Modifie l etat du joueur en question
	 * @param state l etat a donner au joueur en question
	 */
	public void setReady(boolean state){
		this.ready = state;
	}
	
	/**
	 * @return un boolean qui indique si le joueur en question a bouge
	 */
	public boolean hasMoved(){
		return this.nextMove != null;
	}
	
	/**
	 * Ajoute une String au buffer currentMessage du joueur en question 
	 * @param s la String a ajouter au buffer du joueur
	 */
	public void addToBuffer(String s) {
		this.currentMessage.append(s);
	}
	
	/**
	 * @return le buffer currentMessage du joueur en question
	 */
	public String getCurrentMessage() {
		return this.currentMessage.toString();
	}
	
	/**
	 * Efface le buffer du joueur
	 */
	public void eraseBuffer() {
		this.currentMessage.delete(0, this.currentMessage.length());
	}
	
	/**
	 * Efface tout le buffer jusqu a la derniere occurence du character $ 
	 * Ceci permet d effacer les messages complets et traites du buffer du joueur
	 */
	public void refreshBuffer() {
		  this.currentMessage.delete(0, (this.currentMessage.lastIndexOf(Character.toString(Pong.END_CHAR)) + 1));
	}

	/**
	 * Assigne au joueur en question, sa prochaine position
	 * @param m un point
	 */
	public void setNextMove(Point m) {
		if (m == null)
			this.nextMove = null;
		else
			this.nextMove = (Point) m.clone();
	}
	
	/**
	 * Met la position de la raquette a nextMove (le mouvement calculé auparavant)
	 */
	public Point updateMove() {
		if (this.nextMove != null) {
			this.racket.setPos(this.nextMove);
			this.nextMove = null;
			return this.racket.getPos();
		}
		return null;
	}
}
