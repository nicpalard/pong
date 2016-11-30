 package pong.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

/**
 *	Un movable est un PongItem dote d une speed, ie: capable de bouger
 */
public class Movable extends PongItem {
	
	private Point speed;

	/**
	 * Constructeur de movable
	 * @param pos la position actuelle de l objet Movable
	 * @param width la largeur de l objet
	 * @param height la hauteur de l objet
	 * @param sprite l image de l objet
	 * @param speed la vitesse de l objet
	 */
	public Movable(Point pos, int width, int height, Image sprite, Point speed) {
		super(pos, width, height, sprite);
		this.speed = (Point) speed.clone();
	}
	
	/**
	 * Constructeur de copie d un objet Movable
	 * @param m un objet Movable m
	 */
	public Movable (Movable m) {
		super(m);
		this.speed = (Point) m.speed.clone();
	}
	
	
	/* (non-Javadoc)
	 * @see pong.gui.PongItem#draw(java.awt.Graphics)
	 */
	public void draw(Graphics graphicContext){
		super.draw(graphicContext);
	}

	/**
	 * Permet de récuperer la speed d un Movable
	 * @return la speed (Point) d'un Movable
	 */
	public Point getSpeed() {
		return (Point) speed.clone();
	}
	
	/**
	 * Permet de modifier la speedX d un Movable
	 * @param s la speed x a donner au Movable
	 */
	public void setSpeedX(int s){
		this.speed.x = s;
	}
	
	/**
	 * Permet de modifier la speedY d un Movable
	 * @param s la speed y a donner au Movable
	 */
	public void setSpeedY(int s){
		this.speed.y = s;
	}
	
	/**
	 * Permet de mofier le Point speed d un Movable
	 * @param speed le Point speed a donner au Movable
	 */
	public void setSpeed(Point speed){
		this.speed = speed;
	}
	
	/**
	 * Permet de modifier la posX d un Movable
	 * @param x la pos x a donner au Movable
	 */
	public void setPosX(int x) {
		super.setPosX(x);
	}
	
	/**
	 * Permet de modifier la posY d un Movable
	 * @param y la pos y a donner au Movable
	 */
	public void setPosY(int y) {
		super.setPosY(y);
	}
	
	/**
	 * Appel translate d un PongItem qui permet d appliquer la speed a un PongItem
	 */
	public void move() {
		this.translate(speed);
	}
	
	/**
	 * Fait une copie profonde de l'objet.
	 * @param m un Movable
	 */
	public void set(Movable m) {
		this.speed = m.speed;
		super.set(m);
	}
	
	/**
	 * Clone d'un movable
	 */
	public Movable clone() {
		return new Movable (this);
	}

}
