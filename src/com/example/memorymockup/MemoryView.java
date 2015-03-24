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
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.memorymockup.DrawUtility.DoorSprite;
import com.example.memorymockup.DrawUtility.PlayerSprite;
import com.example.memorymockup.DrawUtility.RoomSprite;
import com.example.memorymockup.RoomUtility.ImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.NonImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.Room;
import com.example.memorymockup.RoomUtility.RoomManager;
import com.google.gson.Gson;

public class MemoryView extends View {
	public static final int centerX = 550;
	public static final int centerY = 700;
	public static final int tileSize = 140;
	public static final int tileGridSize = 6;
	
	public static final String[] roomFileNames =
		{"room1.png", "room2.png", "room3.png", "room4.png", "room5.png", "room6.png", "room7.png"};
	
	public MainActivity mainActivity;
	
	public int offset;
	public AssetManager assetManager;
	public Display display;
	
	public int screenX;
	public int screenY;
	
	public RoomSprite[] roomSprites;
	public RoomSprite[] roomSpritesNext;
	public DoorSprite[] doorSprites;	
	public PlayerSprite playerSprite;
	
	public Paint[] roomPaints;
	public Paint doorPaintLast;
	
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
		
		// Initialize room sprites
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
		
		// Initialize door sprites
		doorSprites = new DoorSprite[4];
		Bitmap doorBitmap = readBitmap("door.png");
		for (int i = 0; i < doorSprites.length; i++) {
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
		
		//Initialize player sprite
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
	    
		//Initialize map components
		//mapPaint = new Paint();
		
	    camera = new Camera();
	    camera.save();
	}
	
	public void initMemoryView(Context context) {
		mainActivity = (MainActivity) context;
	
		// Initialize the sprites for the different entities
		offset = 0;
		assetManager = context.getAssets();
		
		display = mainActivity.getWindowManager().getDefaultDisplay();
		
	    // Initialize the handler for the animation
	    handler = new Handler();
	    runnable = new Runnable() {
			@Override
			public void run() {
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
		RoomSprite currentRoomSprite = roomSprites[currentRoom.getType()];
		currentRoomSprite.draw(canvas, roomPaints[currentRoom.getColorIndex()]);
	    
	    if (mainActivity.inTransition) {
	    	RoomSprite nextRoomSprite = roomSpritesNext[roomManager.getNextType()];
			nextRoomSprite.draw(canvas, roomPaints[roomManager.getNextColorIndex()]);
			
			int status = 0;
			DoorSprite leaveDoor = doorSprites[toProcess];

			// 0 = In room, 1 = In door, 2 = Finished
			switch (toProcess) {
				case 0:
					if ((playerSprite.position[0] <= leaveDoor.position[0]) &&
						(playerSprite.position[0] >= nextRoomSprite.position[0] - 
						(leaveDoor.position[0] - currentRoomSprite.position[0]))) {
						status = 1;
					}
					else if (playerSprite.position[0] <= nextRoomSprite.position[0]) {
						status = 2;
					}
					break;
				case 1:
					if ((playerSprite.position[1] <= leaveDoor.position[1]) &&
						(playerSprite.position[1] >= nextRoomSprite.position[1] - 
						(leaveDoor.position[1] - currentRoomSprite.position[1]))) {
						status = 1;
					}
					else if (playerSprite.position[1] <= nextRoomSprite.position[1]) {
						status = 2;
					}
					break;
				case 2:
					if ((playerSprite.position[0] >= leaveDoor.position[0]) &&
						(playerSprite.position[0] <= nextRoomSprite.position[0] - 
						(leaveDoor.position[0] - currentRoomSprite.position[0]))) {
						status = 1;
					}
					else if (playerSprite.position[0] >= nextRoomSprite.position[0]) {
						status = 2;
					}
					break;
				case 3:
					if ((playerSprite.position[1] >= leaveDoor.position[1]) &&
						(playerSprite.position[1] <= nextRoomSprite.position[1] - 
						(leaveDoor.position[1] - currentRoomSprite.position[1]))) {
						status = 1;
					}
					else if (playerSprite.position[1] >= nextRoomSprite.position[1]) {
						status = 2;
					}
					break;
				default:
					break;
			}
			
			if (status == 0) {
				playerSprite.draw(canvas);
				handler.postDelayed(runnable, 1000/25);
			}
			else if (status == 1) {
				playerSprite.update();
				handler.postDelayed(runnable, 1000/25);
			}
			else if (status == 2) {
	    		mainActivity.inTransition = false;
	    		playerSprite.draw(canvas);
	    		
	    		roomManager.processMove(toProcess);
	    		nextRoomSprite.directionTranslate((toProcess + 2) % 4, screenX, screenY - offset);
	    		
	    		camera.restore();
	    		camera.save();
	    		
	    		playerSprite.velocity = new double[] {0.0, 0.0};
	    		
	    		playerSprite.position = new double[] {currentRoomSprite.position[0],
						  							  currentRoomSprite.position[1]};
	    		invalidate();
	    	}
	    }
	    else {
	    	playerSprite.draw(canvas);
	    	/*mapPaint.setColor(Color.WHITE);
	    	canvas.drawRect(screenX - 380, screenY - 380, screenX - 30, screenY - 30, mapPaint);
	    	mapPaint.setColor(Color.BLACK);
	    	canvas.drawRect(screenX - 375, screenY - 375, screenX - 35, screenY - 35, mapPaint);*/
	    }
	}
	
	public void showMinimap() {
		mainActivity.minimapViewFlipper.setDisplayedChild(0);
	}
	
	public void showStatusText() {
		mainActivity.minimapViewFlipper.setDisplayedChild(1);
	}
	
	public void updateMinimap() {
		ImageView arrow = new ImageView(mainActivity);
		switch(toProcess) {
			case 0:
				arrow.setImageResource(R.drawable.arrow_left);
				break;
			case 1:
				arrow.setImageResource(R.drawable.arrow_up);
				break;
			case 2:
				arrow.setImageResource(R.drawable.arrow_right);
				break;
			case 3:
				arrow.setImageResource(R.drawable.arrow_down);
				break;
			default:
				break;
		}
		
		// Hard coded in sizes and margins of images
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
		layoutParams.setMargins(10, 0, 10, 0);
		arrow.setLayoutParams(layoutParams);
		
		//mainActivity.minimap.addView(arrow);
		mainActivity.minimapScroll.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
		    @Override
		    public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		        mainActivity.minimapScroll.removeOnLayoutChangeListener(this);
		        mainActivity.minimapScroll.fullScroll(View.FOCUS_RIGHT);
		    }
		});
		showMinimap();
	}
	
	public void tryMoveSlow(int x, int y) {
		float[] position = new float[] {x, y};
		boolean willMove = false;
		
		for (int i = 0; i < doorSprites.length; i++) {
			int[] dimensions = doorSprites[i].spriteDim;
			
			// Need to account for the height of the minimap in the y-direction
			if ((doorSprites[i].position[0] - dimensions[0] <= position[0]) &&
				(doorSprites[i].position[0] + dimensions[0] >= position[0]) &&
				(doorSprites[i].position[1] - dimensions[1] <= position[1]) &&
				(doorSprites[i].position[1] + dimensions[1] >= position[1])) {
				toProcess = i;
				willMove = true;
			}
		}
		
		if (willMove) {
			//updateMinimap();
			mainActivity.mapView.drawMap(toProcess);
			roomManager.preProcessMove(toProcess);
			
			RoomSprite roomSpriteNext = roomSpritesNext[roomManager.getNextType()];
			roomSpriteNext.directionTranslate(toProcess, screenX, screenY - offset);
			
			double[] accelerationVector = new double[] {
					roomSpriteNext.position[0] - playerSprite.position[0],
					roomSpriteNext.position[1] - playerSprite.position[1]};
			double acceleration = Math.sqrt(Math.pow(accelerationVector[0], 2) + Math.pow(accelerationVector[1], 2));
			accelerationVector[0] = accelerationVector[0] / acceleration;
			accelerationVector[1] = accelerationVector[1] / acceleration;
			playerSprite.accelerationVector = accelerationVector;
			
			mainActivity.inTransition = true;
		}
		
		invalidate();
	}
	
	public void tryMoveFast(int direction) {
		toProcess = direction;
		//updateMinimap();
		mainActivity.mapView.drawMap(toProcess);
		roomManager.processMove(toProcess);
		
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
					mainActivity.statusText.setText(status);
					showStatusText();
					invalidate();
					return false;
				}
			}
			
			status = "Path is correct.";
			mainActivity.statusText.setText(status);
			showStatusText();
			invalidate();
			return true;
		}
		
		status = "Path is incorrect.";
		mainActivity.statusText.setText(status);
		showStatusText();
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
		mainActivity.minimap.removeAllViews();
		showMinimap();
		mainActivity.mapView.resetMap();
		invalidate();
		mainActivity.inTransition = false;
	}
}