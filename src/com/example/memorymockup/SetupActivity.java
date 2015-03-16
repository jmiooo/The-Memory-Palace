package com.example.memorymockup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SetupActivity extends Activity {
	public static String TASK = "task";
	public static String MATCH = "match";
	public static String ENTRY = "entry";
	public static String DELETE = "delete";
	public static String SELECT = "select";
	public static String AUTHENTICATE = "authenticate";
	
	public static String INPUTMETHOD = "input-method";
	public static String SLOW = "slow";
	public static String FAST = "fast";
	
	public Intent intent;
	public String mode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		intent = getIntent();
		mode = intent.getStringExtra(StartActivity.MODE);
		
		setContentView(R.layout.activity_setup);
	}
	
	protected void onStart() {
		super.onStart();
	}
	
	public void startMatch(View view) {
		intent = new Intent(this, ListActivity.class);
		intent.putExtra(StartActivity.MODE, mode);
		intent.putExtra(TASK, MATCH);
		
		startActivity(intent);
	}
	
	
	public void startEntry(View view) {
		intent = new Intent(this, MainActivity.class);
		intent.putExtra(StartActivity.MODE, mode);
		intent.putExtra(TASK, ENTRY);
		intent.putExtra(INPUTMETHOD, SLOW);
		
		startActivity(intent);
	}
	
	public void deletePath(View view) {
		intent = new Intent(this, ListActivity.class);
		intent.putExtra(StartActivity.MODE, mode);
		intent.putExtra(TASK, DELETE);
		
		startActivity(intent);
	}
	
	// Select to be the authenticator path
	public void selectPath(View view) {
		intent = new Intent(this, ListActivity.class);
		intent.putExtra(StartActivity.MODE, mode);
		intent.putExtra(TASK, SELECT);
		
		startActivity(intent);
	}
}
