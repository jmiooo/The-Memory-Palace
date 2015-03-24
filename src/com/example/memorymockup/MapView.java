package com.example.memorymockup;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class MapView extends View {
	public MainActivity mainActivity;
	
	private Paint paint;
	private List<Integer> path;
	private int col, row;
	
	private float screenX, screenY;
	private float segmentX, segmentY;
	private Bitmap[] arrowList;

	public static int latticeSize = 5;

	public MapView(Context context) {
		super(context);
        setFocusable(true);
        initMapView(context);
	}
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setFocusable(true);
        initMapView(context);
	}
	
	public void initMapView(Context context) {
		mainActivity = (MainActivity) context;
		
		paint = new Paint();
		row = latticeSize / 2;
		col = latticeSize / 2;
		path = new ArrayList<Integer>();
	}
	
	public void initDrawables() {
		screenX = getWidth();
		screenY = getHeight();
		
		segmentX = screenX / (latticeSize + 1);
		segmentY = screenY / (latticeSize + 1);
		
		arrowList = new Bitmap[4];
		arrowList[0] = BitmapFactory.decodeResource(mainActivity.getResources(),
											 		R.drawable.arrow_left);
		arrowList[1] = BitmapFactory.decodeResource(mainActivity.getResources(),
		 											R.drawable.arrow_up);
		arrowList[2] = BitmapFactory.decodeResource(mainActivity.getResources(),
		 											R.drawable.arrow_right);
		arrowList[3] = BitmapFactory.decodeResource(mainActivity.getResources(),
		 											R.drawable.arrow_down);
	}
	
	protected void drawMap(int direction) {
		switch (direction) {
			case 0:
				if (col > 0) col -= 1;
				break;
			case 1:
				if (row > 0) row -= 1;
				break;
			case 2:
				if (col < latticeSize - 1) col += 1;
				break;
			case 3:
				if (row < latticeSize - 1) row += 1;
				break;
			default:
				break;
		}
		
		path.add(direction);
		invalidate();
	}
	
	protected void resetMap() {
		row = latticeSize / 2;
		col = latticeSize / 2;
		path = new ArrayList<Integer>();
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		// Generic background drawing
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, screenX, screenY, paint);
		paint.setColor(Color.BLACK);
		canvas.drawRect(5, 5, screenX - 5, screenY - 5, paint);
		
		paint.setColor(Color.WHITE);
		for (int i = 0; i < latticeSize; i++) {
			for (int j = 0; j < latticeSize; j++) {
				canvas.drawCircle((j + 1) * screenX / (latticeSize + 1),
								  (i + 1) * screenY / (latticeSize + 1),
								  screenX / ((latticeSize + 1) * 15),
								  paint);
			}
		}
		
		// Drawing based on path
		Rect src = new Rect(0, 0, arrowList[0].getWidth(), arrowList[0].getHeight());
		
		int col = this.col;
		int row = this.row;
		
		for (int i = 0; i < path.size(); i++) {
			int direction = path.get(path.size() - 1 - i);
			
			int newCol = col;
			int newRow = row;
			switch (direction) {
				case 0:
					newCol = col + 1;
					break;
				case 1:
					newRow = row + 1;
					break;
				case 2:
					newCol = col - 1;
					break;
				case 3:
					newRow = row - 1;
					break;
				default:
					break;
			}
			
			if ((0 <= col && col <= latticeSize - 1) &&
				(0 <= row && row <= latticeSize - 1) &&
				(0 <= newCol && newCol <= latticeSize - 1) &&
				(0 <= newRow && newRow <= latticeSize - 1)) {
				Bitmap drawArrow = arrowList[direction];
				
				float endpointX = segmentX * (col + 1);
				float endpointY = segmentY * (row + 1);
				float newEndpointX = segmentX * (newCol + 1);
				float newEndpointY = segmentY * (newRow + 1);
				
				float offsetX = endpointX - newEndpointX;
				float offsetY = endpointY - newEndpointY;
				
				for (int j = 0; j < 3; j++) {
					Rect dest = new Rect((int) (newEndpointX + offsetX * (j + 1) / 4 - segmentX / 8),
										 (int) (newEndpointY + offsetY * (j + 1) / 4 - segmentY / 8),
										 (int) (newEndpointX + offsetX * (j + 1) / 4 + segmentX / 8),
										 (int) (newEndpointY + offsetY * (j + 1) / 4 + segmentY / 8));
					canvas.drawBitmap(drawArrow, src, dest, null);
				}
			}
			
			row = newRow;
			col = newCol;
		}
		
		if ((0 <= col && col <= latticeSize - 1) &&
			(0 <= row && row <= latticeSize - 1)) {
			paint.setColor(Color.YELLOW);
			canvas.drawCircle((col + 1) * screenX / (latticeSize + 1),
							  (row + 1) * screenY / (latticeSize + 1),
							  screenX / ((latticeSize + 1) * 15),
							  paint);
		}
		
		// Drawing based on current room
		paint.setColor(Color.GREEN);
		canvas.drawCircle((this.col + 1) * screenX / (latticeSize + 1),
						  (this.row + 1) * screenY / (latticeSize + 1),
						  screenX / ((latticeSize + 1) * 15),
						  paint);
		
		
		
	}
}