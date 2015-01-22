package com.example.memorymockup;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends Activity {
	public static String MODE = "mode";
	
	public static String IMPOSSIBLE = "impossible";
	public static String NONIMPOSSIBLE = "non-impossible";
	
	public Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
	}
	
	public void startImpossible(View view) {
		intent = new Intent(this, SetupActivity.class);
		intent.putExtra(MODE, IMPOSSIBLE);
		startActivity(intent);
	}
	
	public void startNonImpossible(View view) {
		intent = new Intent(this, SetupActivity.class);
		intent.putExtra(MODE, NONIMPOSSIBLE);
		startActivity(intent);
	}
}
