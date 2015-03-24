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
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity {
	public static final int LEFT = 0;
	public static final int UP = 1;
	public static final int RIGHT = 2;
	public static final int DOWN = 3;
	
	public static final int SWIPELENGTH = 200;
	
	//public static String SAVE_KEY = "memory-view";
	
	public Intent intent;
	public String mode;
	public String task;
	public String pathName;
	public String inputMethod;
	
	public MemoryView memoryView;
	public MapView mapView;
	public ViewFlipper minimapViewFlipper;
	public HorizontalScrollView minimapScroll;
	public LinearLayout minimap;
	public TextView statusText;
	public EditText editText;
	
	public SharedPreferences sharedPreferences;
	public Editor editor;
	public SharedPreferences authenticatorSharedPreferences;
	public Editor authenticatorEditor;
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
		inputMethod = intent.getStringExtra(SetupActivity.INPUTMETHOD);
		System.out.println(inputMethod);
		
		if (task.equals(SetupActivity.MATCH)) {
			setContentView(R.layout.activity_main_match);
		}
		else if (task.equals(SetupActivity.ENTRY)) {
			setContentView(R.layout.activity_main_entry);
			editText = (EditText) findViewById(R.id.enter_path_name);
		}
		else if (task.equals(SetupActivity.SELECT)) {
			setContentView(R.layout.activity_main_match);
		}
		else if (task.equals(SetupActivity.AUTHENTICATE)) {
			setContentView(R.layout.activity_main_authenticate);
		}
		
		memoryView = (MemoryView) findViewById(R.id.memory);
		if (memoryView.getViewTreeObserver().isAlive()) {
		    memoryView.getViewTreeObserver().addOnGlobalLayoutListener( 
		    	    new OnGlobalLayoutListener(){
		    	        @Override
		    	        public void onGlobalLayout() {
		    	            // gets called after layout has been done
		    	        	if (memoryView.getViewTreeObserver().isAlive()) {
		    	        		memoryView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
		    	        	}
		    	            memoryView.initDrawables();
		    	        }
		    	});
		}
		
		mapView = (MapView) findViewById(R.id.map);
		if (mapView.getViewTreeObserver().isAlive()) {
		    mapView.getViewTreeObserver().addOnGlobalLayoutListener( 
		    	    new OnGlobalLayoutListener(){
		    	        @Override
		    	        public void onGlobalLayout() {
		    	            // gets called after layout has been done
		    	        	if (mapView.getViewTreeObserver().isAlive()) {
		    	        		mapView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
		    	        	}
		    	            mapView.initDrawables();
		    	        }
		    	});
		}
		
		memoryView.setMode(mode);
		memoryView.setTask(task);
		
		minimapViewFlipper = (ViewFlipper) findViewById(R.id.minimapViewFlipper);
		minimapScroll = (HorizontalScrollView) findViewById(R.id.minimapScroll);
		//minimapViewFlipper.showNext();
		minimap = (LinearLayout) findViewById(R.id.minimap);
		statusText = (TextView) findViewById(R.id.statusText);
		
		String fileName = "";
		if (mode.equals(StartActivity.IMPOSSIBLE)) {
			fileName = ListActivity.IMPOSSIBLEFILE;
		}
		else if (mode.equals(StartActivity.NONIMPOSSIBLE)) {
			fileName = ListActivity.NONIMPOSSIBLEFILE;
		}
		sharedPreferences = this.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		gson = new Gson();
		
		pathName = "";
		if (task.equals(SetupActivity.MATCH) || 
			task.equals(SetupActivity.AUTHENTICATE) ||
			task.equals(SetupActivity.SELECT)) {
			pathName = intent.getStringExtra(ListActivity.PATHNAME);
			memoryView.setRoomManager(sharedPreferences.getString(pathName, ""));
		}
		
		if (task.equals(SetupActivity.SELECT)) {
			authenticatorSharedPreferences = this.getSharedPreferences(
					ListActivity.AUTHENTICATORFILE, Context.MODE_PRIVATE);
			authenticatorEditor = authenticatorSharedPreferences.edit();
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
			
			if (result) {
				if (task.equals(SetupActivity.AUTHENTICATE)) {
					exit(view);
				}
				if (task.equals(SetupActivity.SELECT)) {
					authenticatorEditor.putString(ListActivity.AUTHENTICATORNAME, pathName);
					authenticatorEditor.putString(ListActivity.AUTHENTICATORMODE, mode);
					authenticatorEditor.putString(ListActivity.AUTHENTICATORINPUTMETHOD, inputMethod);
					authenticatorEditor.commit();
					finish();
				}
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
				if (sharedPreferences.contains(ListActivity.PATHLIST)) {
					String json = sharedPreferences.getString(ListActivity.PATHLIST, "");
					Type type = new TypeToken<List<String>>(){}.getType();
					pathList = gson.fromJson(json, type);
				}
				pathList.add(pathName);
				editor.putString(ListActivity.PATHLIST, gson.toJson(pathList));
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
	
	public void processTouchEventSlow(MotionEvent event) {
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
	            		memoryView.tryMoveSlow(swipeStart[0], swipeStart[1] - minimap.getHeight());
	            	}
        		}
        		
            	inSwipe = false;
                break;
            default:
            	inSwipe = false;
                break;
        }
	}
	
	public void processTouchEventFast(MotionEvent event) {
		int x = (int)event.getX();
	    int y = (int)event.getY();

        switch (event.getAction()) {
        	case MotionEvent.ACTION_DOWN:
                inSwipe = true;
                swipeStart[0] = x;
                swipeStart[1] = y;
                break;
        	case MotionEvent.ACTION_MOVE:
        		if (inSwipe) {
	            	int deltaX = x - swipeStart[0];
	            	int deltaY = y - swipeStart[1];
	            	
	            	if (Math.abs(deltaX) >= SWIPELENGTH ||
	            		Math.abs(deltaY) >= SWIPELENGTH) {
	            		if (deltaX <= -SWIPELENGTH)
	            			memoryView.tryMoveFast(LEFT);
	            		else if (deltaY <= -SWIPELENGTH)
	            			memoryView.tryMoveFast(UP);
	            		else if (deltaX >= SWIPELENGTH)
	            			memoryView.tryMoveFast(RIGHT);
	            		else if (deltaY >= SWIPELENGTH)
	            			memoryView.tryMoveFast(DOWN);
	            		
	            		memoryView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
	            										 HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
	            		
	            		swipeStart[0] = x;
	            		swipeStart[1] = y;
	            	}
        		}
                break;
        	case MotionEvent.ACTION_UP:
            	inSwipe = false;
                break;
            default:
            	inSwipe = false;
                break;
        }
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		// Method to get motion with swipes instead of presses
		
		if (task.equals(SetupActivity.ENTRY) && editText.isFocused()) {
			editText.clearFocus();
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		}
		
		if (!inTransition) {
			if (inputMethod.equals(SetupActivity.SLOW)) {
				processTouchEventSlow(event);
			}
			else if (inputMethod.equals(SetupActivity.FAST)) {
				// Fast is actually both fast and slow
				processTouchEventSlow(event);
				processTouchEventFast(event);
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
