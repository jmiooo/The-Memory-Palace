package com.example.memorymockup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.memorymockup.DrawUtility.DoorSprite;
import com.example.memorymockup.DrawUtility.PlayerSprite;
import com.example.memorymockup.DrawUtility.RoomSprite;
import com.example.memorymockup.RoomUtility.ImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.NonImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.Room;
import com.example.memorymockup.RoomUtility.RoomManager;
import com.example.memorymockup.RoomUtility.SemiEquilateralTriangle;
import com.google.gson.Gson;

public class MemoryView extends View {
	public static final int centerX = 550;
	public static final int centerY = 700;
	public static final int tileSize = 140;
	public static final int tileGridSize = 6;
	
	public MainActivity mainActivity;
	
	public ShapeDrawable[] squareTiles;
	public Paint doorPaint;
	public Paint doorPaintLast;
	public SemiEquilateralTriangle[] doorTiles;
	public ShapeDrawable player;
	public Paint textPaint;
	public Camera camera;
	public Matrix matrix, inverse;
	public int adjustX, adjustY;
	
	public int offset;
	public AssetManager assetManager;
	public RoomSprite roomSprite;
	public Paint[] roomPaints;
	public DoorSprite[] doorSprites;
	public PlayerSprite playerSprite;
	
	public Handler handler;
	public Runnable runnable;
	
	public int mode;
	public int task;
	
	public static int IMPOSSIBLE = 0;
	public static int NONIMPOSSIBLE = 1;
	
	public static int MATCH = 0;
	public static int ENTRY = 1;
	
	public RoomManager roomManager;
	
	public Gson gson;
	
	public int toProcess;
	
	// Just for matching
	public List<Integer> pathToFollow;
	public String status = "";
	
	public MemoryView(Context context) {
		super(context);
        setFocusable(true);
        initMemoryView(context);
	}
	
	public MemoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setFocusable(true);
        initMemoryView(context);
	}
	
	public Bitmap readBitmap(String fileName) {
		try {
			InputStream inputStream = assetManager.open(fileName);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			inputStream.close();
			return bitmap;
		}
		catch (IOException e) {
			return null;
		}
	}
	
	public void initMemoryView(Context context) {
		mainActivity = (MainActivity) context;
		
		offset = 200;
		assetManager = context.getAssets();
		
		Bitmap roomBitmap = readBitmap("room.png");
		Matrix roomMatrix = new Matrix();
		roomMatrix.postScale(0.9f, 1.42f);
        roomMatrix.postRotate(90);
        roomBitmap = Bitmap.createBitmap(roomBitmap, 0, 0, roomBitmap.getWidth(), roomBitmap.getHeight(), roomMatrix, true);
		roomSprite = new RoomSprite(roomBitmap);
		roomSprite.position = new double[] {roomSprite.spriteDim[0] / 2, roomSprite.spriteDim[1] / 2 + offset};
		
		doorSprites = new DoorSprite[4];
		for (int i = 0; i < doorSprites.length; i++) {
			Bitmap doorBitmap = readBitmap("door.png");
			Matrix doorMatrix = new Matrix();
	        doorMatrix.postRotate(90 * (1 - i));
	        doorBitmap = Bitmap.createBitmap(doorBitmap, 0, 0, doorBitmap.getWidth(), doorBitmap.getHeight(), doorMatrix, true);
			doorSprites[i] = new DoorSprite(doorBitmap);
			doorSprites[i].setDirection(i);
			
			switch (i) {
				case 0:
					doorSprites[i].position = new double[] {doorSprites[i].spriteDim[0] / 2,
														    roomSprite.spriteDim[1] / 2 + offset};
					break;
				case 1:
					doorSprites[i].position = new double[] {roomSprite.spriteDim[0] / 2,
														    doorSprites[i].spriteDim[1] / 2 + offset};
					break;
				case 2:
					doorSprites[i].position = new double[] {roomSprite.spriteDim[0] - doorSprites[i].spriteDim[0] / 2,
							 							    roomSprite.spriteDim[1] / 2 + offset};
					break;
				case 3:
					doorSprites[i].position = new double[] {roomSprite.spriteDim[0] / 2,
							 							    roomSprite.spriteDim[1] - doorSprites[i].spriteDim[1] / 2 + offset};
					break;
				default:
					break;
			}
		}
		
		Bitmap playerBitmap = readBitmap("player.png");
		Matrix playerMatrix = new Matrix();
		playerMatrix.postScale(2f, 2f);
        playerBitmap = Bitmap.createBitmap(playerBitmap, 0, 0, playerBitmap.getWidth(), playerBitmap.getHeight(), playerMatrix, true);
		playerSprite = new PlayerSprite(playerBitmap, new int[] {3, 4}, this);
		playerSprite.position = new double[] {roomSprite.spriteDim[0] / 2, roomSprite.spriteDim[1] / 2 + offset};
		
		roomPaints = new Paint[RoomUtility.COLORS.length];
		for (int i = 0; i < RoomUtility.COLORS.length; i++) {
			Paint paint = new Paint();
			ColorFilter colorFilter = new LightingColorFilter(RoomUtility.COLORS[i], 1);
			paint.setColorFilter(colorFilter);
			roomPaints[i] = paint;
		}
		
		doorPaintLast = new Paint(); 
		ColorFilter colorFilter = new LightingColorFilter(0xFFFFFF00, 1);
		doorPaintLast.setColorFilter(colorFilter);
		
		
		/*squareTiles = new ShapeDrawable[tileGridSize * tileGridSize];
		for (int i = 0; i < squareTiles.length; i++) {
			squareTiles[i] = new ShapeDrawable(new RectShape());
		}
		
		doorPaint = new Paint(); 
		doorPaint.setStyle(Style.FILL);
	    doorPaint.setColor(0xFFA52A2A);
	    
	    doorPaintLast = new Paint(); 
		doorPaintLast.setStyle(Style.FILL);
	    doorPaintLast.setColor(0xFF000000);
		
	    doorTiles = new SemiEquilateralTriangle[tileGridSize * tileGridSize];
		for (int i = 0; i < doorTiles.length; i++) {
			doorTiles[i] = new SemiEquilateralTriangle();
		}
		
		player = new ShapeDrawable(new OvalShape());
		player.getPaint().setColor(0xFF000000);*/
		
		textPaint = new Paint(); 
	    textPaint.setColor(Color.WHITE); 
	    textPaint.setTextSize(100);
	    
	    /*camera = new Camera();
	    matrix = new Matrix();
	    inverse = new Matrix();*/
	    
	    handler = new Handler();
	    runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				invalidate();
			}
		};
	    
	    mode = IMPOSSIBLE;
	    
	    gson = new Gson();
	}
	
	public void setMode(String modeString) {
		if (modeString.equals(StartActivity.IMPOSSIBLE)) {
			mode = IMPOSSIBLE;
			roomManager = new ImpossibleRoomManager();
		}
		else if (modeString.equals(StartActivity.NONIMPOSSIBLE)) {
			mode = NONIMPOSSIBLE;
			roomManager = new NonImpossibleRoomManager();
		}
	}
	
	public void setTask(String taskString) {
		if (taskString.equals(SetupActivity.MATCH)) {
			task = MATCH;
		}
		else if (taskString.equals(SetupActivity.ENTRY)) {
			task = ENTRY;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		roomSprite.draw(canvas, roomPaints[roomManager.getCurrentRoom().getColorIndex()]);
		for (int i = 0; i < doorSprites.length; i++) {
			if (3 - i == roomManager.lastDoorIndex)
				doorSprites[i].draw(canvas, doorPaintLast);
			else
				doorSprites[i].draw(canvas, roomPaints[roomManager.getCurrentRoom().getColorIndex()]);
		}
		playerSprite.draw(canvas);
		
		Room currentRoom = roomManager.getCurrentRoom();
		/*int[] position = currentRoom.getPosition();
		int x = position[0];
		int y = position[1];
		int color = currentRoom.getColor();
		int[][] squares = currentRoom.getOccupiedSquares();
		List<Door> doors = currentRoom.getDoors();
		int squareTileIndex = 0;
		int doorTileIndex = 0;
		int lastDoorIndex = roomManager.getLastDoorIndex();		
		
		int minX, minY, maxX, maxY;
		minX = minY = maxX = maxY = 0;
		for (int i = 0; i < squares.length; i++) {
	    	int squareX = squares[i][0] - x;
	    	int squareY = squares[i][1] - y;
	    	
	    	minX = Math.min(minX, squareX);
	    	minY = Math.min(minY, squareY);
	    	maxX = Math.max(maxX, squareX);
	    	maxY = Math.max(maxY, squareY);
		}
		for (int i = 0; i < doors.size(); i++) {
	    	int[] doorPosition = doors.get(i).getPosition();
	    	int doorX = doorPosition[0] - x;
	    	int doorY = doorPosition[1] - y;
	    	
	    	minX = Math.min(minX, doorX);
	    	minY = Math.min(minY, doorY);
	    	maxX = Math.max(maxX, doorX);
	    	maxY = Math.max(maxY, doorY);
	    }
		adjustX = -(minX + maxX) * tileSize / 2;
		adjustY = -(minY + maxY) * tileSize / 2;

		camera.save();
		camera.rotate((float) 35.264, 0, 45);
		camera.getMatrix(matrix);
		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
		camera.restore();
		matrix.invert(inverse);
		canvas.concat(matrix);
	    
	    for (int i = 0; i < squares.length; i++) {
	    	int squareX = squares[i][0] - x;
	    	int squareY = squares[i][1] - y;
	    	
			squareTiles[squareTileIndex].setBounds(centerX + adjustX + squareX * tileSize - tileSize * 9 / 20,
												   centerY + adjustY + squareY * tileSize - tileSize * 9 / 20,
												   centerX + adjustX + squareX * tileSize + tileSize * 9 / 20,
												   centerY + adjustY + squareY * tileSize + tileSize * 9 / 20);
			squareTiles[squareTileIndex].getPaint().setColor(color);
			squareTiles[squareTileIndex].draw(canvas);
			squareTileIndex++;
	    }
	    
	    for (int i = 0; i < doors.size(); i++) {
	    	Door door = doors.get(i);
	    	
	    	int[] doorPosition = door.getPosition();
	    	int doorDirection = door.getDirection();
	    	int doorX = doorPosition[0] - x;
	    	int doorY = doorPosition[1] - y;
	    	
	    	doorTiles[doorTileIndex].setVals(centerX + adjustX + doorX * tileSize,
									   		 centerY + adjustY + doorY * tileSize,
											 tileSize * 27 / 40,
											 doorDirection);
	    	if (i == lastDoorIndex) {
	    		canvas.drawPath(doorTiles[doorTileIndex], doorPaintLast);
	    	}
	    	else {
	    		canvas.drawPath(doorTiles[doorTileIndex], doorPaint);
	    	}
			doorTileIndex++;
	    }
	    
	    if (lastDoorIndex >= 0) {
	    	Door door = doors.get(lastDoorIndex);
	    	int[] lastDoorPosition = door.getPosition();
	    	int lastDoorDirection = door.getDirection();
	    	int playerX = 0;
	    	int playerY = 0;
	    	
	    	switch (lastDoorDirection) {
	    		case MainActivity.LEFT:
	    			playerX = lastDoorPosition[0] - x + 1;
	    			playerY = lastDoorPosition[1] - y;
	    			break;
	    		case MainActivity.UP:
	    			playerX = lastDoorPosition[0] - x;
	    			playerY = lastDoorPosition[1] - y + 1;
	    			break;
	    		case MainActivity.RIGHT:
	    			playerX = lastDoorPosition[0] - x - 1;
	    			playerY = lastDoorPosition[1] - y;
	    			break;
	    		case MainActivity.DOWN:
	    			playerX = lastDoorPosition[0] - x;
	    			playerY = lastDoorPosition[1] - y - 1;
	    			break;
	    	}
	    	
		    player.setBounds(centerX + adjustX + playerX * tileSize - tileSize * 2 / 5,
		    				 centerY + adjustY + playerY * tileSize - tileSize * 2 / 5,
		    				 centerX + adjustX + playerX * tileSize + tileSize * 2 / 5,
		    				 centerY + adjustY + playerY * tileSize + tileSize * 2 / 5);
		    player.draw(canvas);
	    }
	    else {
	    	player.setBounds(centerX + adjustX - tileSize * 2 / 5,
			   				 centerY + adjustY - tileSize * 2 / 5,
			   				 centerX + adjustX + tileSize * 2 / 5,
			   				 centerY + adjustY + tileSize * 2 / 5);
	    	player.draw(canvas);
	    }
	    
	    canvas.restore();
		*/
		
	    
	    if (task == MATCH) {
	    	canvas.drawText(status, 150, 125, textPaint);
	    	status = "";
	    }
	    if (task == ENTRY) {
	    	canvas.drawText("Room Number: " + Integer.toString(currentRoom.getNumber()), 150, 125, textPaint);
	    }
	    
	    if (mainActivity.inTransition) {
	    	if ((Math.abs(doorSprites[toProcess].position[0] - playerSprite.position[0]) < 25) &&
	    		(Math.abs(doorSprites[toProcess].position[1] - playerSprite.position[1]) < 25)) {
	    		mainActivity.inTransition = false;
	    		roomManager.processMove(3 - toProcess);
	    		
	    		int leaveDoorIndex = (toProcess + 2) % 4;
	    		playerSprite.position = new double[] {doorSprites[leaveDoorIndex].position[0],
						  							  doorSprites[leaveDoorIndex].position[1]};
	    		switch (leaveDoorIndex) {
	    			case 0:
	    				playerSprite.position[0] += doorSprites[leaveDoorIndex].spriteDim[0] / 2 + playerSprite.spriteDim[0] / 2;
	    				break;
	    			case 1:
	    				playerSprite.position[1] += doorSprites[leaveDoorIndex].spriteDim[1] / 2 + playerSprite.spriteDim[1] / 2;
	    				break;
	    			case 2:
	    				playerSprite.position[0] -= doorSprites[leaveDoorIndex].spriteDim[0] / 2 + playerSprite.spriteDim[0] / 2;
	    				break;
	    			case 3:
	    				playerSprite.position[1] -= doorSprites[leaveDoorIndex].spriteDim[1] / 2 + playerSprite.spriteDim[1] / 2;
	    				break;
	    			default:
	    				break;
	    		}
	    		playerSprite.velocity = new double[] {0.0, 0.0};
	    		invalidate();
	    	}
		    else {
			    handler.postDelayed(runnable, 1000/25);
		    }
	    }
	}
	

	public void moveRoom(int x, int y) {
		float[] position = new float[] {x, y};
		/*inverse.mapPoints(position);
		int tileX = Math.round(position[0]) - centerX - adjustX + tileSize / 2;
		int tileY = Math.round(position[1]) - centerY - adjustY + tileSize / 2;
		
		List<Door> doors = roomManager.getCurrentRoom().getDoors();
		for (int i = 0; i < doors.size(); i++) {
			Door door = doors.get(i);
			int[] doorPosition = door.getPosition();
			
			if ((doorPosition[0] - 0.5) * tileSize <= tileX && tileX < (doorPosition[0] + 1.5) * tileSize && 
				(doorPosition[1] - 0.5) * tileSize <= tileY && tileY < (doorPosition[1] + 1.5) * tileSize) {
				roomManager.processMove(i);
			}
		}*/
		
		for (int i = 0; i < doorSprites.length; i++) {
			int[] dimensions = doorSprites[i].spriteDim;
			
			if ((doorSprites[i].position[0] - dimensions[0] <= position[0]) &&
				(doorSprites[i].position[0] + dimensions[0] >= position[0]) &&
				(doorSprites[i].position[1] - dimensions[1] <= position[1]) &&
				(doorSprites[i].position[1] + dimensions[1] >= position[1])) {
				double[] accelerationVector = new double[] {doorSprites[i].position[0] - playerSprite.position[0],
															doorSprites[i].position[1] - playerSprite.position[1]};
				double acceleration = Math.sqrt(Math.pow(accelerationVector[0], 2) + Math.pow(accelerationVector[1], 2));
				accelerationVector[0] = accelerationVector[0] / acceleration;
				accelerationVector[1] = accelerationVector[1] / acceleration;
				playerSprite.accelerationVector = accelerationVector;
				toProcess = i;
				Log.e("e", String.valueOf(toProcess));
				mainActivity.inTransition = true;
			}
		}
		
		invalidate();
	}
	
	public void setRoomManager(String roomInfo) {
		if (this.mode == IMPOSSIBLE) {
			this.roomManager = gson.fromJson(roomInfo, ImpossibleRoomManager.class);
		}
		else {
			this.roomManager = gson.fromJson(roomInfo, NonImpossibleRoomManager.class);
		}
		
		Room room = this.roomManager.getRooms().get(0);
		pathToFollow = this.roomManager.getPath();
		this.roomManager.setCurrentRoom(room);
		List<Integer> newPath = new ArrayList<Integer>();
		newPath.add(0);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDoorIndex(-1);
	}
	
	public boolean matchPath() {
		List<Integer> pathUntilNow = roomManager.getPath();
		
		if (pathUntilNow.size() == pathToFollow.size()) {
			for (int i = 0; i < pathUntilNow.size(); i++) {
				if (pathUntilNow.get(i) != pathToFollow.get(i)) {
					status = "Path is incorrect.";
					invalidate();
					return false;
				}
			}
			
			status = "Path is correct.";
			invalidate();
			return true;
		}
		
		status = "Path is incorrect.";
		invalidate();
		return false;
	}
	
	public String getRoomInfoStringified() {
		return gson.toJson(roomManager);
	}
	
	public void resetPath() {
		Room room = this.roomManager.getRooms().get(0);
		this.roomManager.setCurrentRoom(room);
		List<Integer> newPath = new ArrayList<Integer>();
		newPath.add(0);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDoorIndex(-1);
		playerSprite.position = new double[] {roomSprite.spriteDim[0] / 2, roomSprite.spriteDim[1] / 2 + offset};
		playerSprite.row = 0;
		invalidate();
		mainActivity.inTransition = false;
	}
}