package br.com.meslin.humanMobility.model;

public class Position {
	private double time;
	private double deltaX;
	private double deltaY;
	
	/**
	 * Constructor
	 */
	public Position() {
		
	}
	/**
	 * Constructor
	 * @param t time
	 * @param x delta x
	 * @param y delta y
	 */
	public Position(double t, double x, double y) {
		this.time = t;
		this.deltaX = x;
		this.deltaY = y;
	}
	
	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}
	/**
	 * @return the deltaX
	 */
	public double getDeltaX() {
		return deltaX;
	}
	/**
	 * @param deltaX the deltaX to set
	 */
	public void setDeltaX(double deltaX) {
		this.deltaX = deltaX;
	}
	/**
	 * @return the deltaY
	 */
	public double getDeltaY() {
		return deltaY;
	}
	/**
	 * @param deltaY the deltaY to set
	 */
	public void setDeltaY(double deltaY) {
		this.deltaY = deltaY;
	}
}
