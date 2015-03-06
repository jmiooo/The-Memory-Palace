package com.example.memorymockup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.graphics.Path;

public class RoomUtility {
	/* Constants for colors of rooms, types of rooms (Elaborated below), and rotations */
	/* 0: 3 block line, 1: 4 block line,
	 * 2: 4 block square, 3: 9 block square,
	 * 4: 4 block L, 5: 5 block L,
	 * 6: 5 block thumb */
	public static final int[] COLORS =
		{0xFFEEEEEE, 0xFFF2D387, Color.LTGRAY};
	
	public static final int[] TYPES =
		{0, 1, 2, 3, 4, 5, 6};
	
	/*public static final int THREE_LINE = 0;
	public static final int FOUR_LINE = 1;
	public static final int FOUR_SQUARE = 2;
	public static final int NINE_SQUARE = 3;
	public static final int FOUR_EL = 4;
	public static final int FIVE_EL = 5;
	public static final int FIVE_THUMB = 6;
	public static final int[] TYPES =
		{THREE_LINE, FOUR_LINE, FOUR_SQUARE, NINE_SQUARE, FOUR_EL, FIVE_EL, FIVE_THUMB};
	
	public static final int ZERO_DEG = 0;
	public static final int NINETY_DEG = 1;
	public static final int ONE_EIGHTY_DEG = 2;
	public static final int TWO_SEVENTY_DEG = 3;
	public static final int[] ROTATIONS =
		{ZERO_DEG, NINETY_DEG, ONE_EIGHTY_DEG, TWO_SEVENTY_DEG};*/
	
	/* A bunch of room utility functions, used for both impossible and non-impossible spaces */
	/*public static void rotateSquares(int[][] squares, int rotation) {
		switch (rotation) {
			case ZERO_DEG:
				break;
			case NINETY_DEG:
				for (int i = 0; i < squares.length; i++) {
					int temp = squares[i][0];
					squares[i][0] = -squares[i][1];
					squares[i][1] = temp;
				}
				break;
			case ONE_EIGHTY_DEG:
				for (int i = 0; i < squares.length; i++) {
					squares[i][0] = -squares[i][0];
					squares[i][1] = -squares[i][1];
				}
				break;
			case TWO_SEVENTY_DEG:
				for (int i = 0; i < squares.length; i++) {
					int temp = squares[i][0];
					squares[i][0] = squares[i][1];
					squares[i][1] = -temp;
				}
				break;
		}
	}
	
	public static void rotateDirections(int[] directions, int rotation) {
		for (int i = 0; i < directions.length; i++) {
			directions[i] = (directions[i] + rotation) % 4;
		}
	}
	
	public static int[][] getOccupiedSquares(int x, int y, int type, int rotation) {
		int[][] squares = new int[0][0];
		
		switch (type) {
			case THREE_LINE:
				squares = new int[][] {new int[] {0, 0},
									   new int[] {0, 1},
									   new int[] {0, 2}};
				break;
			case FOUR_LINE:
				squares = new int[][] {new int[] {0, 0},
									   new int[] {0, 1},
									   new int[] {0, 2},
									   new int[] {0, 3}};
				break;
			case FOUR_SQUARE:
				squares = new int[][] {new int[] {0, 0},
									   new int[] {0, 1},
									   new int[] {1, 0},
									   new int[] {1, 1}};
				break;
			case NINE_SQUARE:
				squares = new int[][] {new int[] {0, 0},
									   new int[] {0, 1},
									   new int[] {0, 2},
									   new int[] {1, 0},
									   new int[] {1, 1},
									   new int[] {1, 2},
									   new int[] {2, 0},
									   new int[] {2, 1},
									   new int[] {2, 2}};
				break;
			case FOUR_EL:
				squares = new int[][] {new int[] {0, 0},
									   new int[] {0, 1},
									   new int[] {0, 2},
									   new int[] {1, 0}};
				break;
			case FIVE_EL:
				squares = new int[][] {new int[] {0, 0},
									   new int[] {0, 1},
									   new int[] {0, 2},
									   new int[] {1, 0},
									   new int[] {2, 0}};
				break;
			case FIVE_THUMB:
				squares = new int[][] {new int[] {0, 0},
									   new int[] {0, 1},
									   new int[] {0, 2},
									   new int[] {1, 0},
									   new int[] {1, 1}};
				break;
		}
		
		rotateSquares(squares, rotation);
		
		for (int i = 0; i < squares.length; i++) {
			squares[i][0] += x;
			squares[i][1] += y;
		}
		
		return squares;
	}*/
	
	/* The placement of doors are limited as of now, but can be expanded later */
	public static List<Door> getDoors(int x, int y) {
		/*int[][] squares = new int[0][0];
		int[] directions = new int[0];
		
		switch (type) {
			case THREE_LINE:
				squares = new int[][] {new int[] {0, -1},
									   new int[] {1, 1},
									   new int[] {0, 3},
									   new int[] {-1, 1}};
				directions = new int[] {1, 2, 3, 0};
				break;
			case FOUR_LINE:
				squares = new int[][] {new int[] {0, -1},
									   new int[] {1, 2},
									   new int[] {0, 4},
									   new int[] {-1, 1}};
				directions = new int[] {1, 2, 3, 0};
				break;
			case FOUR_SQUARE:
				squares = new int[][] {new int[] {0, -1},
									   new int[] {2, 0},
									   new int[] {1, 2},
									   new int[] {-1, 1}};
				directions = new int[] {1, 2, 3, 0};
				break;
			case NINE_SQUARE:
				squares = new int[][] {new int[] {0, -1},
									   new int[] {3, 0},
									   new int[] {2, 3},
									   new int[] {-1, 2}};
				directions = new int[] {1, 2, 3, 0};
				break;
			case FOUR_EL:
				squares = new int[][] {new int[] {0, -1},
									   new int[] {2, 0},
									   new int[] {0, 3},
									   new int[] {-1, 1}};
				directions = new int[] {1, 2, 3, 0};
				break;
			case FIVE_EL:
				squares = new int[][] {new int[] {0, -1},
									   new int[] {3, 0},
									   new int[] {0, 3},
									   new int[] {-1, 1}};
				directions = new int[] {1, 2, 3, 0};
				break;
			case FIVE_THUMB:
				squares = new int[][] {new int[] {0, -1},
									   new int[] {2, 1},
									   new int[] {0, 3},
									   new int[] {-1, 1}};
				directions = new int[] {1, 2, 3, 0};
				break;
		}
		
		rotateSquares(squares, rotation);
		rotateDirections(directions, rotation);
		
		for (int i = 0; i < squares.length; i++) {
			squares[i][0] += x;
			squares[i][1] += y;
		}*/
		
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
		//private int rotation;
		private int colorIndex;
		private int color;
		private List<Door> doors;
		private List<Item> items;
		//private int[][] occupiedSquares;
		
		public Room(int number, int x, int y, int type, int colorIndex, int color) {
			this.number = number;
			this.x = x;
			this.y = y;
			this.type = type;
			//this.rotation = rotation;
			this.colorIndex = colorIndex;
			this.color = color;
			this.doors = RoomUtility.getDoors(x, y);
			this.items = new ArrayList<Item>();
			//this.occupiedSquares = null;
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
		
		/*public int[][] getOccupiedSquares() {
			if (null == this.occupiedSquares) {
				this.occupiedSquares = 
					RoomUtility.getOccupiedSquares(this.x, this.y, this.type, this.rotation);
			}
			
			return this.occupiedSquares;
		}*/
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
		
		protected Random randomGenerator;
		
		public RoomManager() {
			rooms = new ArrayList<Room>();
			path = new ArrayList<Integer>();
			
			randomGenerator = new Random();
			int type = randomGenerator.nextInt(TYPES.length);
			//int rotation = 0;
			/*switch (type) {
				case THREE_LINE:
					rotation = randomGenerator.nextInt(2);
					break;
				case FOUR_SQUARE:
				case NINE_SQUARE:
					rotation = 0;
					break;
				case FOUR_LINE:
				case FOUR_EL:
				case FIVE_EL:
				case FIVE_THUMB:
					rotation = randomGenerator.nextInt(ROTATIONS.length);
					break;
				default:
					break;
			}*/
			int colorIndex = randomGenerator.nextInt(COLORS.length);
			int color = COLORS[colorIndex];
			currentRoom = new Room(0, 0, 0, type, colorIndex, color);
			
			rooms.add(currentRoom);
			path.add(0);
			
			lastDoorIndex = -1;
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
		
		public void processMove(int i) {
		}
		
		public void preProcessMove(int i) {
			preProcessed = true;
			nextType = randomGenerator.nextInt(TYPES.length);
			nextColorIndex = randomGenerator.nextInt(COLORS.length);
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
					/*int rotation = 0;
					switch (type) {
						case THREE_LINE:
							rotation = randomGenerator.nextInt(2);
							break;
						case FOUR_SQUARE:
						case NINE_SQUARE:
							rotation = 0;
							break;
						case FOUR_LINE:
						case FOUR_EL:
						case FIVE_EL:
						case FIVE_THUMB:
							rotation = randomGenerator.nextInt(ROTATIONS.length);
							break;
						default:
							break;
					}*/
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
		
		//public void moveRoom(int x, int y) {
			/*switch (direction) {
				case MainActivity.LEFT:				
					this.roomX -= 1;
					break;
				case MainActivity.UP:				
					this.roomY += 1;
					break;
				case MainActivity.RIGHT:				
					this.roomX += 1;
					break;
				case MainActivity.DOWN:				
					this.roomY -= 1;
					break;
			}
			
			path.add(direction);
			this.processMove();*/
		//}
	}
	
	public static class NonImpossibleRoomManager extends RoomManager {
		//protected RoomGrid roomGrid;
		
		public NonImpossibleRoomManager() {
			super();
			
			//roomGrid = new RoomGrid();
			//roomGrid.addRoom(currentRoom);
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
					/*int rotation = 0;
					switch (type) {
						case THREE_LINE:
							rotation = randomGenerator.nextInt(2);
							break;
						case FOUR_SQUARE:
						case NINE_SQUARE:
							rotation = 0;
							break;
						case FOUR_LINE:
						case FOUR_EL:
						case FIVE_EL:
						case FIVE_THUMB:
							rotation = randomGenerator.nextInt(ROTATIONS.length);
							break;
						default:
							break;
					}*/
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
	
	/*public static class SemiEquilateralTriangle extends Path {
		
		public SemiEquilateralTriangle() {
			super();
		}
		
		public void setVals(int x, int y, int width, int direction) {
			this.reset();
			
			switch (direction) {
				case MainActivity.LEFT:
					this.moveTo(x + width / 2, y + width / 2);
					this.lineTo(x + width / 2, y - width / 2);
					this.lineTo(x - width / 2, y);
					break;
				case MainActivity.UP:
					this.moveTo(x - width / 2, y + width / 2);
					this.lineTo(x + width / 2, y + width / 2);
					this.lineTo(x, y - width / 2);
					break;
				case MainActivity.RIGHT:
					this.moveTo(x - width / 2, y - width / 2);
					this.lineTo(x - width / 2, y + width / 2);
					this.lineTo(x + width / 2, y);
					break;
				case MainActivity.DOWN:
					this.moveTo(x + width / 2, y - width / 2);
					this.lineTo(x - width / 2, y - width / 2);
					this.lineTo(x, y + width / 2);
					break;
			}
		}
	}*/
}
