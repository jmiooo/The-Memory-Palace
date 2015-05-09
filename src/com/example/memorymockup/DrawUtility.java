package com.example.memorymockup;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class DrawUtility {
	
	public static class Sprite {
		public double[] position = {0.0, 0.0};
		public Bitmap bitmap;
		public int[] arrayDim;
		public int[] spriteDim;
		
		public Sprite(Bitmap bitmap, int[] arrayDim) {
			this.bitmap = bitmap;
			this.arrayDim = arrayDim;
			this.spriteDim = new int[] { bitmap.getWidth() / arrayDim[0],
									     bitmap.getHeight() / arrayDim[1] };
		}
		
		public void draw(Canvas canvas, Paint paint) {
			Rect src = new Rect(0, 0, spriteDim[0], spriteDim[1]);
			Rect dest = new Rect((int) position[0] - spriteDim[0] / 2, (int) position[1] - spriteDim[1] / 2,
								 (int) position[0] + spriteDim[0] / 2, (int) position[1] + spriteDim[1] / 2);
			canvas.drawBitmap(this.bitmap, src, dest, paint);
		}
		
		public void draw(Canvas canvas, Rect src, Rect dest, Paint paint) {
			canvas.drawBitmap(this.bitmap, src, dest, paint);
		}
	}
	
	public static class RoomSprite extends Sprite{
		public int color;
		
		public RoomSprite(Bitmap bitmap) {
			super(bitmap, new int[] {1, 1});
		}
		
		public void setColor(int color) {
			this.color = color;
		}
		
		// Translate by the width or height in a direction
		public void directionTranslate(int direction, int width, int height) {
			
			switch (direction) {
				case 0:
					position = new double[] {position[0] - width, position[1]};
					break;
				case 1:
					position = new double[] {position[0], position[1] - height};
					break;
				case 2:
					position = new double[] {position[0] + width, position[1]};
					break;
				case 3:
					position = new double[] {position[0], position[1] + height};
					break;
				default:
					break;
			}
		}
	}
	
	public static class DoorSprite extends Sprite{
		public int direction;
		
		public DoorSprite(Bitmap bitmap) {
			super(bitmap, new int[] {1, 1});
		}
		
		public void setDirection(int direction) {
			this.direction = direction;
		}
	}

	public static class PlayerSprite extends Sprite{
		public double[] velocity = {0.0, 0.0};
		public double maxSpeed = 10.0;
		public int walkDuration = 2;
		public double acceleration = 10.0;
		public double deceleration = -1.0;
		public double[] accelerationVector = {0.0, 0.0};
		public int currentFrame = walkDuration - 1;
		public int col = 1;
		public int row = 0;
		public MemoryView memoryView;
		
		public PlayerSprite(Bitmap bitmap, int[] arrayDim, MemoryView memoryView) {
			super(bitmap, arrayDim);
			this.memoryView = memoryView;
		}
		
		public void adjustMaxSpeed(double maxSpeed) {
			this.maxSpeed = maxSpeed;
		}
		
		public void update() {
			if (memoryView.mainActivity.inTransition) {
				double totalAcceleration = acceleration + deceleration;
				if (totalAcceleration < 0) {
					double potentialVelocityX = (double) (velocity[0] + accelerationVector[0] * totalAcceleration);
					velocity[0] = velocity[0] < 0 ?
								  Math.min(0, potentialVelocityX) :
								  Math.max(0, potentialVelocityX);
					double potentialVelocityY = (double) (velocity[1] + accelerationVector[1] * totalAcceleration);
					velocity[1] = velocity[1] < 0 ?
								  Math.min(0, potentialVelocityY) :
								  Math.max(0, potentialVelocityY);	
				}
				else {
					velocity[0] = (double) (velocity[0] + accelerationVector[0] * totalAcceleration);
					velocity[1] = (double) (velocity[1] + accelerationVector[1] * totalAcceleration);
				}
				
				double speed = Math.sqrt(Math.pow(velocity[0], 2) + Math.pow(velocity[1], 2));
				if (speed > maxSpeed) {
					velocity[0] = velocity[0] * maxSpeed / speed;
					velocity[1] = velocity[1] * maxSpeed / speed;
				}
				
				position[0] += velocity[0];
				position[1] += velocity[1];
				currentFrame += 1;
				
				if (velocity[0] == 0 && velocity[1] == 0) {
					col = 1;
				}
				else {
					col = (currentFrame / walkDuration) % arrayDim[0];
				}
				
				if (Math.abs(velocity[0]) > Math.abs(velocity[1])) {
					if (velocity[0] >= 0)
						row = 2;
					else
						row = 1;
				}
				else {
					if (velocity[1] >= 0)
						row = 0;
					else
						row = 3;
				}
			}
			else if (memoryView.mainActivity.inBlur) {
				velocity[0] = (double) (accelerationVector[0] * maxSpeed);
				velocity[1] = (double) (accelerationVector[1] * maxSpeed);
				
				position[0] += velocity[0];
				position[1] += velocity[1];
				currentFrame += 1;
				
				if (velocity[0] == 0 && velocity[1] == 0) {
					col = 1;
				}
				else {
					col = (currentFrame / walkDuration) % arrayDim[0];
				}
				
				if (Math.abs(velocity[0]) > Math.abs(velocity[1])) {
					if (velocity[0] >= 0)
						row = 2;
					else
						row = 1;
				}
				else {
					if (velocity[1] >= 0)
						row = 0;
					else
						row = 3;
				}
			}
			
			if (velocity[0] == 0 && velocity[1] == 0) {
				col = 1;
			}
		}
		
		public void draw(Canvas canvas) {
			this.update();
			
			Rect src = new Rect(col * spriteDim[0], row * spriteDim[1],
								(col + 1) * spriteDim[0], (row + 1) * spriteDim[1]);
			Rect dest = new Rect((int) position[0] - spriteDim[0] / 2, (int) position[1] - spriteDim[1] / 2,
								 (int) position[0] + spriteDim[0] / 2, (int) position[1] + spriteDim[1] / 2);
			super.draw(canvas, src, dest, null);
		}
	}
}