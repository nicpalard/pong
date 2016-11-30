package pong.gui;

import java.awt.Image;
import java.awt.Point;

/**
 * Une raquette est un objet du Pong
 */

public class Racket extends Movable{

	public static final int SPEED = 5;
	
	public Racket(Point pos, int width, int height, Image sprite) {
		super(pos, width, height, sprite, new Point(0, 0));
	}
	
	/**
	 * Modifie la direction actuelle de la raquette
	 */
	public void up() {
		setSpeedY(-SPEED);
	}
	
	/**
	 * Modifie la direction actuelle de la raquette
	 */
	public void down() {
		setSpeedY(SPEED);
	}
	
	/**
	 * Modifie la direction actuelle de la raquette
	 */
	public void left(){
		setSpeedX(-SPEED);
	}
	
	/**
	 * Modifie la direction actuelle de la raquette
	 */
	public void right(){
		setSpeedX(SPEED);
	}
	
	/**
	 * Modifie la direction actuelle de la raquette en lui assignant le vecteur NULL (elle ne bouge plus)
	 */
	public void noMove() {
		setSpeedY(0);
		setSpeedX(0);
	}
	
	/**
	 * return un clone d'une raquette
	 */
	public Racket clone(){
		return new Racket(this.getPos(), this.getWidth(), this.getHeight(), this.getSprite());
	}
}
