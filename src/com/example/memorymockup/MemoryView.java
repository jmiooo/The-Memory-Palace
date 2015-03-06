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
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.example.memorymockup.DrawUtility.DoorSprite;
import com.example.memorymockup.DrawUtility.PlayerSprite;
import com.example.memorymockup.DrawUtility.RoomSprite;
import com.example.memorymockup.RoomUtility.ImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.NonImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.Room;
import com.example.memorymockup.RoomUtility.RoomManager;
import com.google.gson.Gson;
//import com.example.memorymockup.RoomUtility.SemiEquilateralTriangle;

public class MemoryView extends View {
	public static final int centerX = 550;
	public static final int centerY = 700;
	public static final int tileSize = 140;
	public static final int tileGridSize = 6;
	
	public static final String[] roomFileNames =
		{"room1.png", "room2.png", "room3.png", "room4.png", "room5.png", "room6.png", "room7.png"};
	
	public MainActivity mainActivity;
	
	public Paint doorPaint;
	public Paint doorPaintLast;
	public ShapeDrawable player;
	public Paint textPaint;
	public int adjustX, adjustY;
	
	public int offset;
	public AssetManager assetManager;
	public Display display;
	
	public int screenX;
	public int screenY;
	public RoomSprite[] roomSprites;
	public RoomSprite[] roomSpritesNext;
	public Paint[] roomPaints;
	public DoorSprite[] doorSprites;
	public PlayerSprite playerSprite;
	public Camera camera;
	
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
	
	public void initDrawables() {
		screenX = getWidth();
		screenY = getHeight();
		
		
		roomSprites = new RoomSprite[RoomUtility.TYPES.length];
		roomSpritesNext = new RoomSprite[RoomUtility.TYPES.length];
		for (int i = 0; i < roomSprites.length; i++) {
			Bitmap roomBitmap = readBitmap(roomFileNames[i]);
			int imageX = roomBitmap.getHeight();
			int imageY = roomBitmap.getWidth();
			Matrix roomMatrix = new Matrix();
	        roomMatrix.postRotate(90);
	        roomMatrix.postScale(((float) screenX / imageX), ((float) (screenY - offset) / imageY));
	        
	        roomBitmap = Bitmap.createBitmap(roomBitmap, 0, 0, roomBitmap.getWidth(), roomBitmap.getHeight(), roomMatrix, true);
			roomSprites[i] = new RoomSprite(roomBitmap);
			roomSprites[i].position = new double[] {roomSprites[i].spriteDim[0] / 2, roomSprites[i].spriteDim[1] / 2 + offset};
			roomSpritesNext[i] = new RoomSprite(roomBitmap);
			roomSpritesNext[i].position = new double[] {roomSpritesNext[i].spriteDim[0] / 2, roomSpritesNext[i].spriteDim[1] / 2 + offset};
		}
		
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
														    roomSprites[0].spriteDim[1] / 2 + offset};
					break;
				case 1:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] / 2,
														    doorSprites[i].spriteDim[1] / 2 + offset};
					break;
				case 2:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] - doorSprites[i].spriteDim[0] / 2,
							 							    roomSprites[0].spriteDim[1] / 2 + offset};
					break;
				case 3:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] / 2,
							 							    roomSprites[0].spriteDim[1] - doorSprites[i].spriteDim[1] / 2 + offset};
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
		playerSprite.position = new double[] {roomSprites[0].spriteDim[0] / 2, roomSprites[0].spriteDim[1] / 2 + offset};
		
		
		// Initialize paints
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
		
		textPaint = new Paint(); 
	    textPaint.setColor(Color.WHITE); 
	    textPaint.setTextSize(100);
	    
	    camera = new Camera();
	    camera.save();
	}
	
	public void initMemoryView(Context context) {
		mainActivity = (MainActivity) context;
	
		// Initialize the sprites for the different entities
		offset = 200;
		assetManager = context.getAssets();
		
		display = mainActivity.getWindowManager().getDefaultDisplay();
		
	    // Initialize the handler for the animation
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
		if (mainActivity.inTransition) {
			switch (toProcess) {
				case 0:
				case 2:
					camera.translate((float) -playerSprite.velocity[0], 0f, 0f);
					break;
				case 1:
				case 3:
					camera.translate(0f, (float) playerSprite.velocity[1], 0f);
					break;
				default:
					break;
			}
		}
		
		camera.applyToCanvas(canvas);
		
		Room currentRoom = roomManager.getCurrentRoom();
		roomSprites[currentRoom.getType()].draw(canvas, roomPaints[currentRoom.getColorIndex()]);
		
		if (mainActivity.inTransition) {
			roomSpritesNext[roomManager.getNextType()].draw(canvas, roomPaints[roomManager.getNextColorIndex()]);
		}
		
		for (int i = 0; i < doorSprites.length; i++) {
			/*if (i == roomManager.lastDoorIndex)
				doorSprites[i].draw(canvas, doorPaintLast);
			else
				doorSprites[i].draw(canvas, roomPaints[roomManager.getCurrentRoom().getColorIndex()]);*/
		}
		
		playerSprite.draw(canvas);
	    
	    if (!mainActivity.inTransition && task == MATCH) {
	    	canvas.drawText(status, 150, 125, textPaint);
	    	status = "";
	    }
	    if (!mainActivity.inTransition && task == ENTRY) {
	    	canvas.drawText("Room Number: " + Integer.toString(currentRoom.getNumber()), 150, 125, textPaint);
	    }
	    
	    if (mainActivity.inTransition) {
	    	if ((Math.abs(doorSprites[toProcess].position[0] - playerSprite.position[0]) < 25) &&
	    		(Math.abs(doorSprites[toProcess].position[1] - playerSprite.position[1]) < 25)) {
	    		mainActivity.inTransition = false;
	    		roomSpritesNext[roomManager.getNextType()].directionTranslate((toProcess + 2) % 4, screenX, screenY);
	    		roomManager.processMove(toProcess);
	    		
	    		camera.restore();
	    		camera.save();
	    		
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
				roomManager.preProcessMove(toProcess);
				
				roomSpritesNext[roomManager.getNextType()].directionTranslate(toProcess, screenX, screenY);
				
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
		playerSprite.position = new double[] {roomSprites[room.getType()].spriteDim[0] / 2, 
										      roomSprites[room.getType()].spriteDim[1] / 2 + offset};
		playerSprite.row = 0;
		invalidate();
		mainActivity.inTransition = false;
	}
}