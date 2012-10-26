package in.rgksugan.latex4android;

import in.rgksugan.latex4android.util.FileBrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class FileList extends Activity implements OnItemClickListener {

	private ArrayAdapter<String> adapter = null;
	private SharedPreferences choosenproject = null;
	private static final String TAG = "FileList";
	public ArrayList<String> fileList = new ArrayList<String>();
	private File path = null;
	private Context context = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Sets the UI of the activity
		setContentView(R.layout.filelist);
		final EditText title = (EditText) findViewById(R.id.txt_project_title);
		title.setHint("New Tex File Name");
		Button create = (Button) findViewById(R.id.btn_ceate);
		ListView list = (ListView) findViewById(R.id.listprojects);

		// Read the selected project from the preferences
		choosenproject = getSharedPreferences("choosenproject",
				Context.MODE_PRIVATE);
		path = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "LaTeX"
				+ File.separator
				+ choosenproject.getString("project", ""));

		findFiles(path);
		context = this;

		// sets the list adapter
		adapter = new ArrayAdapter<String>(this, R.layout.projectlist, fileList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		registerForContextMenu(list);

		// Onclick listener for the create button
		create.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String content = title.getText().toString();
				if (content != "") {
					try {
						File tmp = new File(path, content);

						// Creates a new file and also display it in the UI
						if (tmp.createNewFile()) {
							fileList.add(content);
							adapter.notifyDataSetChanged();
							title.setText("");
						} else {

							// If the file is not created show an alert to the
							// user
							new AlertDialog.Builder(context)
									.setMessage(
											"File not created. Please recheck the name. It may already exist.")
									.setCancelable(true).show();
						}
					} catch (IOException e) {
						Log.e(TAG, "Exception in creating new file.", e);
					}
				}
			}
		});
	}

	// This method finds all the files in a folder and updates the list adapter
	private void findFiles(File path) {
		if (path.isDirectory()) {
			String[] children = path.list();

			// Loop all the files in the directory
			for (String child : children) {
				File f = new File(path, child);

				// If the file is available and if the name doesn't exist in the
				// array list,
				// it is added to the list
				if (f.isFile() && !fileList.contains(child)) {
					fileList.add(child);
				}
			}
		}
	}

	// This method is called each time a user clicks on the list
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		// Gets the chosen file
		String name = arg0.getItemAtPosition(arg2).toString();

		// Open the chosen file
		displayFile(name);
	}

	/*
	 * Opens the file specified
	 */
	private void displayFile(String name) {

		// If the file ends with tex, the TexEditor activity is invoked and the
		// chosen file is written in the preferences
		if (name.endsWith(".tex")) {
			choosenproject = getSharedPreferences("choosenproject",
					Context.MODE_PRIVATE);
			Editor prefeditor = choosenproject.edit();
			prefeditor.putString("file", name);
			prefeditor.commit();
			Intent startNewActivityOpen = new Intent(FileList.this,
					TexEditor.class);
			startActivityForResult(startNewActivityOpen, 0);
		}

		// If the file name ends with a PDF the pre-installed PDF viewer
		// is used to view the PDF file
		else if (name.endsWith(".pdf")) {
			File pdfFile = new File(path, name);
			if (pdfFile.exists()) {
				Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
				pdfIntent.setDataAndType(Uri.fromFile(pdfFile),
						"application/pdf");
				try {
					startActivity(pdfIntent);
				} catch (ActivityNotFoundException e) {
					new AlertDialog.Builder(context)
							.setMessage(
									"No Application available to view PDF files. Please download an app to view PDF file from Google Play")
							.show();
				}
			}
		}

		// If the file name ends with log the LogViewer activity is invoked.

		else if (name.endsWith(".log")) {
			File logFile = new File(path, name);
			if (logFile.exists()) {
				Intent logintent = new Intent(this, LogViewer.class);
				logintent.putExtra("filename", logFile.getAbsolutePath());
				startActivityForResult(logintent, 1);
			}
		}

		// If the filename ends with something else the corresponding
		// application
		// will be opened.

		else {
			File file = new File(path, name);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String ext = name.substring(name.lastIndexOf('.') + 1);
			String type = mime.getMimeTypeFromExtension(ext);
			intent.setDataAndType(Uri.fromFile(file), type);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				new AlertDialog.Builder(context).setMessage(
						"No Application available to view " + ext
								+ " files. Please download an app to view "
								+ ext + " file from Google Play").show();
			}
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
			menu.setHeaderTitle(fileList.get(info.position));

			// Sets the XML file as the context menu for the list
			inflater.inflate(R.menu.projectlist_context, menu);
		}
	}

	/*
	 * This method is called each time the context menu is clicked
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		// switch over the ID of each context menu items to check which one is
		// clicked
		switch (item.getItemId()) {
		case R.id.ctm_delete:

			// If delete is clicked the file is deleted from the storage
			String fileName = fileList.get(info.position);
			File tmp = new File(path + File.separator + fileName);
			if (tmp.delete()) {
				fileList.remove(fileName);
				adapter.notifyDataSetChanged();
			}
			// If the file is not deleted an alert is issued.
			else {
				new AlertDialog.Builder(context)
						.setMessage("File cannot be deleted").setTitle("Error")
						.show();
			}
			return true;
		case R.id.ctm_rename:

			// If rename is clicked this piece of code is executed

			// An AlertDialog is displayed to get the new name of the file.
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Rename Project");
			final EditText input = new EditText(context);
			input.setHint("Enter new name");
			alert.setView(input);
			alert.setPositiveButton("Rename",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String name = fileList.get(info.position);
							String newName = input.getText().toString()
									+ name.substring(name.lastIndexOf('.'),
											name.length());
							String oldName = fileList.get(info.position);
							File olddir = new File(path + File.separator
									+ oldName);
							File newdir = new File(path + File.separator
									+ newName);

							// The underlying file is renamed
							if (olddir.renameTo(newdir)) {
								fileList.remove(oldName);
								fileList.add(newName);
								adapter.notifyDataSetChanged();
							}

							// If the file is not deleted, the user is alerted
							else {
								new AlertDialog.Builder(context).setMessage(
										"File cannot be renamed.").show();
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
		case R.id.ctm_view:

			// Opens the selected file
			displayFile(fileList.get(info.position));
			return true;
		}
		return true;
	}

	/*
	 * 
	 * Method to create a Options Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// The menu file is inflated as a options menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.filelist_options, menu);
		return true;
	}

	/*
	 * This method is called each time an options menu is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Switches over the ID of the options menu items
		switch (item.getItemId()) {

		// If Refresh is clicked
		case R.id.otm_refresh:

			// The list adapter is refreshed
			findFiles(path);
			adapter.notifyDataSetChanged();
			return true;

			// If Import is clicked
		case R.id.otm_import:

			// The FileBrowser activity is invoked
			Intent fileExploreIntent = new Intent(
					FileBrowser.INTENT_ACTION_SELECT_FILE, null, FileList.this,
					FileBrowser.class);
			fileExploreIntent
					.putExtra(FileBrowser.startDirectoryParameter, Environment
							.getExternalStorageDirectory().getAbsolutePath());
			startActivityForResult(fileExploreIntent, 2);
			return true;
		}
		return true;
	}

	/*
	 * This method is called each time an activity completes execution
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// If the resulting code is of FileBrowser
		if (requestCode == 2) {
			if (resultCode == Activity.RESULT_OK) {

				// Get the new file name
				String newFile = data
						.getStringExtra(FileBrowser.returnFileParameter);
				String fn = newFile.substring(newFile
						.lastIndexOf(File.separator) + 1);
				try {

					// Copy the file into the current directory
					File from = new File(newFile);
					File to = new File(path + File.separator + fn);
					InputStream in = new FileInputStream(from);
					OutputStream out = new FileOutputStream(to);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					out.close();

					// Update the list
					fileList.add(fn);
					adapter.notifyDataSetChanged();
				} catch (FileNotFoundException e) {
					Log.e(TAG, "import file", e);
				} catch (IOException e) {
					Log.e(TAG, "import file", e);
				}

				// Displays a success message
				Toast.makeText(this, "Imported : \n" + fn + " into project.",
						Toast.LENGTH_LONG).show();
			} else {

				// Displays a failure message
				Toast.makeText(this, "Received NO result from file browser",
						Toast.LENGTH_LONG).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
