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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SetupActivity extends Activity {
	public static String TASK = "task";
	public static String MATCH = "match";
	public static String ENTRY = "entry";
	public static String AUTHENTICATE = "authenticate";
	
	public static String PATHNAME = "path-name";
	public static String AUTHENTICATORNAME = "authenticator-name";
	public static String AUTHENTICATORMODE = "authenticator-mode";
	
	public static String IMPOSSIBLEFILE = "impossible_paths";
	public static String NONIMPOSSIBLEFILE = "nonimpossible_paths";
	public static String AUTHENTICATORFILE = "authenticator_paths";
	
	public static String PATHLIST = "path_list";
	
	public Intent intent;
	public String mode;
	public PathAdapter listAdapter;
	public ListView listView;
	
	public SharedPreferences sharedPreferences;
	public Editor editor;
	public SharedPreferences authenticatorSharedPreferences;
	public Editor authenticatorEditor;
	public Gson gson;
	
	public int pathIndex;

	public class PathAdapter extends ArrayAdapter<String> {
		 
	    // Keeping the currently selected item
		private List<String> data;
	 
	    public PathAdapter(Context context, int textViewResourceId, List<String> data) {
	        super(context, textViewResourceId, data);
	        this.data = data;
	    }
	    
	    public List<String> getData() {
	    	return this.data;
	    }
	    
	    public String getDataAtPosition(int i) {
	    	return this.data.get(i);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		intent = getIntent();
		mode = intent.getStringExtra(StartActivity.MODE);
		
		setContentView(R.layout.activity_setup);
	}
	
	protected void onStart() {
		super.onStart();
		
		String fileName = "";
		if (mode.equals(StartActivity.IMPOSSIBLE)) {
			fileName = IMPOSSIBLEFILE;
		}
		else if (mode.equals(StartActivity.NONIMPOSSIBLE)) {
			fileName = NONIMPOSSIBLEFILE;
		}

		sharedPreferences = this.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		authenticatorSharedPreferences = this.getSharedPreferences(AUTHENTICATORFILE, Context.MODE_PRIVATE);
		authenticatorEditor = authenticatorSharedPreferences.edit();
		gson = new Gson();
		
		List<String> pathList = new ArrayList<String>();
		if (sharedPreferences.contains(PATHLIST)) {
			String json = sharedPreferences.getString(PATHLIST, "");
			Type type = new TypeToken<List<String>>(){}.getType();
			pathList = gson.fromJson(json, type);
		}
		
		listView = (ListView) findViewById(R.id.save_list);
	    listAdapter = new PathAdapter(this, R.layout.setup_row, pathList);  
	    listView.setAdapter(listAdapter);
	    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    
	    listView.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    		listView.setItemChecked(position, true);
	    		pathIndex = position;
	    	}
	    });
	    
	    pathIndex = -1;
	}
	
	public void startMatch(View view) {
		if (pathIndex >= 0) {
			intent = new Intent(this, MainActivity.class);
			intent.putExtra(StartActivity.MODE, mode);
			intent.putExtra(TASK, MATCH);
			intent.putExtra(PATHNAME, listAdapter.getDataAtPosition(pathIndex));
			
			startActivity(intent);
		}
	}
	
	/*public void startMatchRoom(View view) {
		intent = new Intent(this, MainActivity.class);
		intent.putExtra(StartActivity.MODE, mode);
		intent.putExtra(TASK, MATCHROOM);
		
		startActivity(intent);
	}*/
	
	public void startEntry(View view) {
		intent = new Intent(this, MainActivity.class);
		intent.putExtra(StartActivity.MODE, mode);
		intent.putExtra(TASK, ENTRY);
		
		startActivity(intent);
	}
	
	public void deletePath(View view) {
		if (pathIndex >= 0) {
			List<String> pathList = listAdapter.getData();
			String pathName = pathList.remove(pathIndex);
			editor.putString(PATHLIST, gson.toJson(pathList));
			editor.remove(pathName);
			editor.commit();
			onStart();
		}
	}
	
	// Select to be the authenticator path
	public void selectPath(View view) {
		if (pathIndex >= 0) {
			String pathName = listAdapter.getDataAtPosition(pathIndex);
			authenticatorEditor.putString(AUTHENTICATORNAME, pathName);
			authenticatorEditor.putString(AUTHENTICATORMODE, mode);
			authenticatorEditor.commit();
		}
	}
}
