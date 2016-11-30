package pong.gui;

import java.awt.Image;
import java.awt.Point;

/**
 *	Representation de la balle, un objet du Pong.
 */
public class Ball extends Movable{
	
	public Ball(Point pos, int width, int height, Image sprite, Point speed){
		super(pos, width, height, sprite, speed);
	}
	

}
