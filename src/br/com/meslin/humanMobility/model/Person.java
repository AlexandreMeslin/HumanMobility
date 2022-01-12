package br.com.meslin.humanMobility.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Person {
	private String username;
	private List<Position> positions;
	private UUID uuid;
	
	public Person() {
		this.username = null;
		this.positions =  new ArrayList<Position>();
		this.uuid = UUID.randomUUID();
	}
	
	/**
	 * Sets a username
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * returns a username
	 * @return a person username
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Adds a position
	 * @param t time
	 * @param x delta x
	 * @param y delta y
	 */
	public void addPosition(double t, double x, double y) {
		Position position = new Position();
		position.setDeltaX(x);
		position.setDeltaY(y);
		position.setTime(t);
		
		this.positions.add(position);
	}
	
	/**
	 * Adds a position
	 * @param position
	 */
	public void addPosition(Position position) {
		this.positions.add(position);
	}
	
	/**
	 * Returns a person position at i
	 * @param i
	 * @return
	 */
	public Position getPosition(int i) {
		try {
			return this.positions.get(i);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return the positions
	 */
	public List<Position> getPositions() {
		return positions;
	}

	/**
	 * @param positions the positions to set
	 */
	public void setPositions(List<Position> positions) {
		this.positions = positions;
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
