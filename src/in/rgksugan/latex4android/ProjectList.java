package in.rgksugan.latex4android;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ProjectList extends Activity implements OnItemClickListener {

	private ArrayAdapter<String> adapter = null;
	private SharedPreferences choosenproject = null;
	private ArrayList<String> projectList = new ArrayList<String>();
	private File path = null;
	private Context context = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);
		final EditText title = (EditText) findViewById(R.id.txt_project_title);
		Button create = (Button) findViewById(R.id.btn_ceate);
		ListView list = (ListView) findViewById(R.id.listprojects);
		path = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		path = new File(path, "LaTeX");
		context = this;

		// Find the existing projects
		findProjects(path);
		adapter = new ArrayAdapter<String>(this, R.layout.projectlist,
				projectList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		// registers context menu for the list
		registerForContextMenu(list);

		// OnClickListener for the create button
		create.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String content = title.getText().toString();
				if (content != "") {
					File tmp = new File(path + File.separator + content);

					// makes a new directory with the mentioned name
					if (!tmp.mkdir()) {

						// if not created display a failure message
						new AlertDialog.Builder(context)
								.setMessage(
										"Project not created. Please recheck the name. It may already exist.")
								.setCancelable(true).show();
					} else {

						// update the projectlist
						projectList.add(content);
						adapter.notifyDataSetChanged();
					}
					title.setText("");
				}
			}
		});
	}

	// Lists all the available projects
	private void findProjects(File path) {
		if (path.exists()) {
			String[] children = path.list();
			if (children.length != 0) {
				for (String child : children) {
					File f = new File(path, child);
					if (f.isDirectory()) {
						if (!projectList.contains(child)) {
							projectList.add(child);
						}
					}
				}
			}
		} else {

			// creates the LaTeX folder if the application is ran for the first
			// time
			path.mkdir();
		}
	}

	/*
	 * 
	 * Method to create a Context Menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listprojects) {
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getMenuInflater();
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(projectList.get(info.position));
			inflater.inflate(R.menu.projectlist_context, menu);
		}
	}

	// This method is called each time the context menu item is selected
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		// switches over the context menu items
		switch (item.getItemId()) {

		// delete option is chose
		case R.id.ctm_delete:
			String projectName = projectList.get(info.position);
			projectList.remove(projectName);
			File tmp = new File(path + File.separator + projectName);

			// delete project
			deleteProject(tmp);
			tmp.delete();

			// update the list
			adapter.notifyDataSetChanged();
			return true;

			// rename option is chose
		case R.id.ctm_rename:

			// alert box to get the new file name
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Rename Project");
			final EditText input = new EditText(context);
			input.setHint("Enter new name");
			alert.setView(input);
			alert.setPositiveButton("Rename",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String newName = input.getText().toString();
							String oldName = projectList.get(info.position);
							File olddir = new File(path + File.separator
									+ oldName);
							File newdir = new File(path + File.separator
									+ newName);
							if (olddir.renameTo(newdir)) {
								projectList.remove(oldName);
								projectList.add(newName);
								adapter.notifyDataSetChanged();
							} else {
								new AlertDialog.Builder(context).setMessage(
										"Project cannot be renamed.").show();
							}
						}
					});
			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					});
			alert.show();
			return true;

			// view project
		case R.id.ctm_view:

			// project is written to the preferences
			choosenproject = getSharedPreferences("choosenproject",
					Context.MODE_PRIVATE);
			Editor prefeditor = choosenproject.edit();
			prefeditor.putString("project", projectList.get(info.position));
			prefeditor.commit();

			// FilEList activity is invoked
			Intent startNewActivityOpen = new Intent(ProjectList.this,
					FileList.class);
			startActivityForResult(startNewActivityOpen, 0);
			return true;
		}
		return true;
	}

	// Project is deleted
	private void deleteProject(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteProject(child);
		fileOrDirectory.delete();
	}

	// to open a project
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		choosenproject = getSharedPreferences("choosenproject",
				Context.MODE_PRIVATE);
		Editor prefeditor = choosenproject.edit();
		prefeditor
				.putString("project", arg0.getItemAtPosition(arg2).toString());
		prefeditor.commit();
		Intent startNewActivityOpen = new Intent(ProjectList.this,
				FileList.class);
		startActivityForResult(startNewActivityOpen, 0);
	}

	/*
	 * 
	 * Method to create a Options Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.projectlist_options, menu);
		return true;
	}

	/*
	 * This method is called when the options menu is seletced
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.otm_refresh:
			findProjects(path);
			adapter.notifyDataSetChanged();
			return true;
		case R.id.otm_about:
			new AlertDialog.Builder(context)
					.setTitle("About LaTeX Android Editor")
					.setMessage(
							"Developed by : Sugan \nSupervised by : Daniela & Stephan")
					.show();
			return true;
		}
		return true;
	}
}
