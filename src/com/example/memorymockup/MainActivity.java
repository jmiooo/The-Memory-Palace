package com.example.memorymockup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity {
	public static final int LEFT = 0;
	public static final int UP = 1;
	public static final int RIGHT = 2;
	public static final int DOWN = 3;
	
	//public static String SAVE_KEY = "memory-view";
	
	public Intent intent;
	public String mode;
	public String task;
	
	public MemoryView memoryView;
	public EditText editText;
	
	public SharedPreferences sharedPreferences;
	public Editor editor;
	public Gson gson;
	
	public boolean inTransition;
	public boolean inSwipe;
	public int[] swipeStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		intent = getIntent();
		mode = intent.getStringExtra(StartActivity.MODE);
		task = intent.getStringExtra(SetupActivity.TASK);
		
		if (task.equals(SetupActivity.MATCH)) {
			setContentView(R.layout.activity_main_match);
		}
		else if (task.equals(SetupActivity.ENTRY)) {
			setContentView(R.layout.activity_main_entry);
			editText = (EditText) findViewById(R.id.enter_path_name);
		}
		else if (task.equals(SetupActivity.AUTHENTICATE)) {
			setContentView(R.layout.activity_main_authenticate);
		}
		memoryView = (MemoryView) findViewById(R.id.memory);
		memoryView.setMode(mode);
		memoryView.setTask(task);
		
		String fileName = "";
		if (mode.equals(StartActivity.IMPOSSIBLE)) {
			fileName = SetupActivity.IMPOSSIBLEFILE;
		}
		else if (mode.equals(StartActivity.NONIMPOSSIBLE)) {
			fileName = SetupActivity.NONIMPOSSIBLEFILE;
		}
		sharedPreferences = this.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		gson = new Gson();
		
		if (task.equals(SetupActivity.MATCH) || task.equals(SetupActivity.AUTHENTICATE)) {
			String pathName = intent.getStringExtra(SetupActivity.PATHNAME);
			memoryView.setRoomManager(sharedPreferences.getString(pathName, ""));
		}
		
		inTransition = false;
		inSwipe = false;
		swipeStart = new int[2];
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//@Override
    //public void onSaveInstanceState(Bundle outState) {
        // Store the game state
        //outState.putBundle(SAVE_KEY, memoryView.saveState());
    //}
	
	public void matchPath(View view) {
		if (!inTransition) {
			boolean result = memoryView.matchPath();
			
			 if (result && task.equals(SetupActivity.AUTHENTICATE)) {
				exit(view);
			}
		}
	}
	
	public void enterPath(View view) {
		if (!inTransition) {
			String pathName = editText.getText().toString();
			
			if (!pathName.equals("")) {
				String roomInfo = memoryView.getRoomInfoStringified();
				System.out.println(roomInfo);
				editor.putString(pathName, roomInfo);
				
				List<String> pathList = new ArrayList<String>();
				if (sharedPreferences.contains(SetupActivity.PATHLIST)) {
					String json = sharedPreferences.getString(SetupActivity.PATHLIST, "");
					Type type = new TypeToken<List<String>>(){}.getType();
					pathList = gson.fromJson(json, type);
				}
				pathList.add(pathName);
				editor.putString(SetupActivity.PATHLIST, gson.toJson(pathList));
				editor.commit();
				this.finish();
			}
		}
	}
	
	public void resetPath(View view) {
		if (!inTransition) {
			memoryView.resetPath();
		}
	}
	
	public void exit(View view) {
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		/*if (!inTransition) {
			int x = (int)event.getX();
		    int y = (int)event.getY();
	
	        switch (event.getAction()) {
	        	case MotionEvent.ACTION_DOWN:
	                inSwipe = true;
	                swipeStart[0] = x;
	                swipeStart[1] = y;
	                break;
	        	case MotionEvent.ACTION_MOVE:
	                break;
	        	case MotionEvent.ACTION_UP:
	        		if (inSwipe) {
		            	int deltaX = x - swipeStart[0];
		            	int deltaY = y - swipeStart[1];
		            	
		            	if (Math.abs(deltaX) >= Math.abs(deltaY)) {
		            		if (deltaX < 0) {
		            			memoryView.moveRoom(LEFT);
		            		}
		            		else {
		            			memoryView.moveRoom(RIGHT);
		            		}
		            	}
		            	else {
		            		if (deltaY < 0) {
		            			memoryView.moveRoom(UP);
		            		}
		            		else {
		            			memoryView.moveRoom(DOWN);
		            		}
		            	}
	        		}
	        		
	            	inSwipe = false;
	                break;
	            default:
	            	inSwipe = false;
	                break;
	        }
		}*/
		
		if (task.equals(SetupActivity.ENTRY) && editText.isFocused()) {
			editText.clearFocus();
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		}
		
		if (!inTransition) {
			int x = (int)event.getX();
		    int y = (int)event.getY();
	
	        switch (event.getAction()) {
	        	case MotionEvent.ACTION_DOWN:
	                inSwipe = true;
	                swipeStart[0] = x;
	                swipeStart[1] = y;
	                break;
	        	case MotionEvent.ACTION_MOVE:
	                break;
	        	case MotionEvent.ACTION_UP:
	        		if (inSwipe) {
		            	int deltaX = x - swipeStart[0];
		            	int deltaY = y - swipeStart[1];
		            	
		            	if (Math.abs(deltaX) <= 100 &&
		            		Math.abs(deltaY) <= 100) {
		            		memoryView.moveRoom(swipeStart[0], swipeStart[1]);
		            	}
	        		}
	        		
	            	inSwipe = false;
	                break;
	            default:
	            	inSwipe = false;
	                break;
	        }
		}

        return super.onTouchEvent(event);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (task.equals(SetupActivity.AUTHENTICATE)) {
			return true;
		}
	    
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (task.equals(SetupActivity.AUTHENTICATE)) {
			return true;
		}
		
	    return super.onKeyUp(keyCode, event);
	}
}
