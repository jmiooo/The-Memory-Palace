package com.example.memorymockup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.graphics.Path;

public class RoomUtility {
	/* Constants for colors of rooms, types of rooms */
	public static final int[] COLORS =
		{0xFFEEEEEE, 0xFFF2D387, Color.LTGRAY};
	
	public static final int[] TYPES =
		{0, 1, 2, 3, 4, 5, 6};
	
	/* The placement of doors are limited as of now, but can be expanded later */
	public static List<Door> getDoors(int x, int y) {
		List<Door> result = new ArrayList<Door>();
		
		for (int i = 0; i < 4; i++) {
			result.add(new Door(0, 0, i, -1, -1));
		}
		
		return result;
	}
	
	/* Door, Item, and Room classes, which are essential to both impossible and
	 * non-impossible spaces
	 */
	public static class Door {
		private int x, y; // Relative to base of room
		private int direction; // Direction when exiting from room
		private int destination; //Next room number
		private int destinationDoor; //Door in next room
		
		public Door(int x, int y, int direction, int destination, int destinationDoor) {
			this.x = x;
			this.y = y;
			this.direction = direction;
			this.destination = destination;
			this.destinationDoor = destination;
		}
		
		public int[] getPosition() {
			return new int[] {x, y};
		}
		
		public int getDirection() {
			return direction;
		}
		
		public int getDestination() {
			return destination;
		}
		
		public void setDestination(int destination) {
			this.destination = destination;
		}
		
		public int getDestinationDoor() {
			return destinationDoor;
		}
		
		public void setDestinationDoor(int destinationDoor) {
			this.destinationDoor = destinationDoor;
		}
	}
	
	public static class Item {
		public final int x, y; // Relative to base of room
		private String hint;
		
		public Item(int x, int y, String hint) {
			this.x = x;
			this.y = y;
			this.hint = hint;
		}
		
		public int[] getPosition() {
			return new int[] {x, y};
		}
		
		public String getHint() {
			return hint; 
		}
		
		public void updateHint(String hint) {
			this.hint = hint;
		}
	}
	
	public static class Room {
		private int number;
		private int x, y;
		private int type;
		private int colorIndex;
		private int color;
		private List<Door> doors;
		private List<Item> items;
		
		public Room(int number, int x, int y, int type, int colorIndex, int color) {
			this.number = number;
			this.x = x;
			this.y = y;
			this.type = type;
			this.colorIndex = colorIndex;
			this.color = color;
			this.doors = RoomUtility.getDoors(x, y);
			this.items = new ArrayList<Item>();
		}
		
		public int getNumber() {
			return this.number;
		}
		
		public int[] getPosition() {
			return new int[] {x, y};
		}
		
		public int getType() {
			return this.type;
		}
		
		public int getColorIndex() {
			return this.colorIndex;
		}
		
		public int getColor() {
			return this.color;
		}
		
		public List<Door> getDoors() {
			return this.doors;
		}
		
		public List<Item> getItems() {
			return this.items;
		}
	}

	/* Main room manager class */
	public static class RoomManager {
		protected Room currentRoom;
		protected List<Room> rooms;
		protected List<Integer> path; /* List of room numbers */
		protected int lastDoorIndex;
		protected boolean preProcessed = false;
		protected int nextType;
		protected int nextColorIndex;
		protected String[] prompts;
		
		protected Random randomGenerator;
		
		public RoomManager() {
			rooms = new ArrayList<Room>();
			path = new ArrayList<Integer>();
			
			randomGenerator = new Random();
			int type = randomGenerator.nextInt(TYPES.length);
			int colorIndex = randomGenerator.nextInt(COLORS.length);
			int color = COLORS[colorIndex];
			currentRoom = new Room(0, 0, 0, type, colorIndex, color);
			
			rooms.add(currentRoom);
			path.add(0);
			
			lastDoorIndex = -1;
			
			prompts = new String[] {};
		}
		
		public Room getCurrentRoom() {
			return this.currentRoom;
		}
		
		public void setCurrentRoom(Room room) {
			this.currentRoom = room;
		}
		
		public List<Room> getRooms() {
			return this.rooms;
		}
		
		public void setRooms(List<Room> rooms) {
			this.rooms = rooms;
		}
		
		public List<Integer> getPath() {
			return this.path;
		}
		
		public void setPath(List<Integer> path) {
			this.path = path;
		}
		
		public int getLastDoorIndex() {
			return this.lastDoorIndex;
		}
		
		public void setLastDoorIndex(int lastDoorIndex) {
			this.lastDoorIndex = lastDoorIndex;
		}
		
		public int getNextType() {
			return this.nextType;
		}
		
		public int getNextColorIndex() {
			return this.nextColorIndex;
		}
		
		public void setPrompts(String[] prompts) {
			this.prompts = prompts;
		}
		
		public String[] getPrompts() {
			return this.prompts;
		}
		
		public void processMove(int doorIndex) {
		}
		
		public void preProcessMove(int doorIndex) {
			Door door = currentRoom.getDoors().get(doorIndex);
			int destination = door.getDestination();
			
			if (destination >= 0) {
				Room nextRoom = rooms.get(destination);
				nextType = nextRoom.getType();
				nextColorIndex = nextRoom.getColorIndex();
			}
			else {
				preProcessed = true;
				nextType = randomGenerator.nextInt(TYPES.length);
				nextColorIndex = randomGenerator.nextInt(COLORS.length);
			}
		}
	}
	
	public static class ImpossibleRoomManager extends RoomManager {
		public ImpossibleRoomManager() {
			super();
		}
		
		public void processMove(int doorIndex) {
			Door door = currentRoom.getDoors().get(doorIndex);
			int direction = door.getDirection();
			int destination = door.getDestination();
			
			if (destination >= 0) {
				currentRoom = rooms.get(destination);
				path.add(destination);
				lastDoorIndex = door.getDestinationDoor();
			}
			else {
				int number = rooms.size();
				int type, colorIndex;
				if (preProcessed) {
					preProcessed = false;
					type = nextType;
					colorIndex = nextColorIndex;
				}
				else {
					type = randomGenerator.nextInt(TYPES.length);
					colorIndex = randomGenerator.nextInt(COLORS.length);
				}
				int color = COLORS[colorIndex];
				Room newRoom = new Room(number, 0, 0, type, colorIndex, color);				
				List<Door> newDoors = newRoom.getDoors();
				List<Integer> possibleDoorIndices = new ArrayList<Integer> ();
				for (int i = 0; i < newDoors.size(); i++) {
					if ((newDoors.get(i).getDirection() + 2) % 4 == direction) {
						possibleDoorIndices.add(i);
					}
				}
				int newDoorIndex = possibleDoorIndices.get(randomGenerator.nextInt(possibleDoorIndices.size()));

				rooms.add(newRoom);
				door.setDestination(number);
				door.setDestinationDoor(newDoorIndex);
				currentRoom = newRoom;
				
				path.add(number);
				
				lastDoorIndex = newDoorIndex;
			}
		}
	}
	
	public static class NonImpossibleRoomManager extends RoomManager {
		public NonImpossibleRoomManager() {
			super();
		}
		
		public void processMove(int doorIndex) {
			Door door = currentRoom.getDoors().get(doorIndex);
			int direction = door.getDirection();
			int destination = door.getDestination();
			
			if (destination >= 0) {
				currentRoom = rooms.get(destination);
				path.add(destination);
				lastDoorIndex = door.getDestinationDoor();
			}
			else {
				int number = rooms.size();
				int type, colorIndex;
				if (preProcessed) {
					preProcessed = false;
					type = nextType;
					colorIndex = nextColorIndex;
				}
				else {
					type = randomGenerator.nextInt(TYPES.length);
					colorIndex = randomGenerator.nextInt(COLORS.length);
				}
				int color = COLORS[colorIndex];
				Room newRoom = new Room(number, 0, 0, type, colorIndex, color);				
				List<Door> newDoors = newRoom.getDoors();
				List<Integer> possibleDoorIndices = new ArrayList<Integer> ();
				for (int i = 0; i < newDoors.size(); i++) {
					if ((newDoors.get(i).getDirection() + 2) % 4 == direction) {
						possibleDoorIndices.add(i);
					}
				}
				int newDoorIndex = possibleDoorIndices.get(randomGenerator.nextInt(possibleDoorIndices.size()));
				Door newDoor = newDoors.get(newDoorIndex);
				newDoor.setDestination(currentRoom.getNumber());
				newDoor.setDestinationDoor(doorIndex);

				rooms.add(newRoom);
				door.setDestination(number);
				door.setDestinationDoor(newDoorIndex);
				currentRoom = newRoom;
				
				path.add(number);
				
				lastDoorIndex = newDoorIndex;
			}
		}
	}
}
