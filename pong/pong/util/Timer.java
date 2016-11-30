package pong.util;

/**
 * Un timer est un utilitaire nous permettant de gerer notre clock interne.
 */

public class Timer {
	private long start;
	private long delay;
	
	public Timer(long delay) {
		this.start = System.currentTimeMillis();
		this.delay = delay;
	}
	
	/**
	 * Reinitialise le timer
	 */
	public void reset() {
		this.start = System.currentTimeMillis();
	}
	
	/**
	 * @return true si le temps est ecoule
	 */
	public boolean timeUp() {
		return this.start + delay < System.currentTimeMillis();
	}
}
