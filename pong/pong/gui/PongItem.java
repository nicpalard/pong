package pong.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
/**
 * Un PongItem est un objet du pong pouvant être afficher et possedant une taille une position et une image.
 */

public class PongItem{
	private Point pos;
	private int width;
	private int height;
	private Image sprite;
	
	public PongItem(Point pos, int width, int height, Image sprite) {
		this.pos = (Point) pos.clone();
		
		this.width = width;
		this.height = height;
		
		this.sprite = sprite;
	}
	
	/**
	 * Constructeur de copie
	 * @param pi le PongItem
	 */
	public PongItem(PongItem pi) {
		this.set(pi);
	}
	
	/**
	 * @return le sprite d'un PongItem
	 */
	public Image getSprite() {
		return this.sprite;
	}
	
	/**
	 * Modifie le sprite d'un PongItem
	 * @param sprite le nouveau sprite
	 */
	public void setSprite(Image sprite){
		this.sprite = sprite;
	}
	
	/**
	 * @return la largeur d'un PongItem
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Modifie la largeur d'un PongItem
	 * @param width la nouvelle largeur
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return la hauteur d'un PongItem
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Modifie la hauteur d'un PongItem
	 * @param height la nouvelle hauteur
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Permet de recuperer la position d'un PongItem
	 * @return un clone de la pos d'un PongItem
	 */
	public Point getPos() {
		return (Point)pos.clone();
	}

	/**
	 * Modifdie la posX d'un PongItem
	 * @param posX la nouvelle posX
	 */
	public void setPosX(int posX) {
		this.pos.x = posX;
	}
	
	/**
	 * Modifie la posY d'un PongItem
	 * @param posY la nouvelle posY
	 */
	public void setPosY(int posY) {
		this.pos.y = posY;
	}
	/**
	 * Modifie la position d'un PongItem
	 * @param p la nouvelle position
	 */
	public void setPos(Point p){
		this.pos = (Point) p.clone();
	}
	
	/**
	 * Applique le déplacement en fonction de sa speed a un PongItem
	 * @param speed le vecteur de déplacement a appliquer
	 */
	public void translate(Point speed) {
		pos.translate(speed.x, speed.y);
	}
	
	/**
	 * Verifie si il y a une collision entre deux PongItem
	 * @param pi un PongItem
	 * @return true si il y a collision, false sinon
	 */
	public boolean collision(PongItem pi) {
		if ((this.getPos().x <= pi.getPos().x + pi.getWidth() - 1) &&
			(this.getPos().x + this.getWidth() - 1 >= pi.getPos().x)&&
			(this.getPos().y <= pi.getPos().y + pi.getHeight() - 1) &&
			(this.getPos().y + this.getHeight() - 1 >= pi.getPos().y)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Affiche a l'écran un PongItem
	 * @param graphicContext le graphicContext du Pong sur lequel il faut afficher le PongItem
	 */
	public void draw(Graphics graphicContext) {
		graphicContext.drawImage(this.getSprite(), this.getPos().x, this.getPos().y, this.getWidth(), this.getHeight(), null);
	}
	
	/**
	 * retoune le clone d'un PongItem
	 */
	public PongItem clone() {
		return new PongItem( (Point) this.getPos().clone(), this.getHeight(), this.getWidth(), this.getSprite());
	}
	
	/**
	 * Fais une copie profonde d'un PongItem pi
	 * @param pi le PongItem que l'on copie
	 */
	public void set(PongItem pi) {
		this.height = pi.height;
		this.width = pi.width;
		this.pos = (Point) pi.pos.clone();
		this.sprite = pi.sprite;
	}
}
