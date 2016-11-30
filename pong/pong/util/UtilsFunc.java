package pong.util;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

/**
 * Fonction utilitaire utilisées dans le code
 */
public class UtilsFunc {
	
	/**
	 * @param l un tableau
	 * @return l'indice de la plus petite valeur d'un tableau
	 */
	public static int lowestOf(ArrayList<Integer> l) {
		int tmp = 0;
		for (int i = 1 ; i < l.size() ; i++) {
			if (l.get(i) < l.get(tmp)) {
				tmp = i;
			}
		}
		return tmp;
	}
	
	/**
	 * Permet d'effecter une copie en profondeur (champ par champ) d'un objet BufferedImage
	 * @param bi la bufferedImage a copier
	 * @return la nouvelle BufferedImage
	 */
	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}
