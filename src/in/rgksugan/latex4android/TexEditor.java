package in.rgksugan.latex4android;

import in.rgksugan.latex4android.util.SyntaxHighlighter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TexEditor extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	public static EditText editor, search, newtext;
	private SharedPreferences choosenproject = null;
	private String filename = null;
	private String foldername = null;
	private Button toolbar[];
	private boolean display = false;
	private boolean enable = false;
	private boolean enablesearch = false;
	private SyntaxHighlighter watcher = null;
	private static final String TAG = "TexEditor";
	private Context context = this;

	/*
	 * Called when the activity is created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		watcher = new SyntaxHighlighter();
		SyntaxHighlighter.first = true;

		// If there is a saved state load it
		final MyDataObject data = (MyDataObject) getLastNonConfigurationInstance();
		if (data != null) {
			enable = data.isEnable();
			enableHighlighting(!enable);
			display = data.isDisplay();
			displayToolbar(display);
			enablesearch = data.isEnableSearch();
			displaySearch(enablesearch);
		}

		editor = (EditText) findViewById(R.id.editor);

		// enable vertical scrollbars for EditBox
		editor.setVerticalScrollBarEnabled(true);

		// get opened file
		choosenproject = getSharedPreferences("choosenproject",
				Context.MODE_PRIVATE);
		filename = choosenproject.getString("file", "");
		foldername = choosenproject.getString("project", "");
		toolbar = new Button[4];
		toolbar[0] = (Button) findViewById(R.id.btn_images);
		toolbar[1] = (Button) findViewById(R.id.btn_table);
		toolbar[2] = (Button) findViewById(R.id.btn_symbol);
		((Button) findViewById(R.id.one)).setOnClickListener(this);
		((Button) findViewById(R.id.two)).setOnClickListener(this);
		((Button) findViewById(R.id.three)).setOnClickListener(this);
		((Button) findViewById(R.id.four)).setOnClickListener(this);
		((Button) findViewById(R.id.five)).setOnClickListener(this);
		((Button) findViewById(R.id.six)).setOnClickListener(this);
		Button previous = (Button) findViewById(R.id.btn_previous);
		Button next = (Button) findViewById(R.id.btn_next);
		Button replace = (Button) findViewById(R.id.btn_replace);
		Button replaceall = (Button) findViewById(R.id.btn_replaceall);
		search = (EditText) findViewById(R.id.txt_searchbox);
		newtext = (EditText) findViewById(R.id.txt_replace);

		// OnKeyListener for search TextBox
		search.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				// If enter key is pressed
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == 0) {

					String text = editor.getText().toString();

					// Clear all existing spans
					editor.getText().clearSpans();
					String term = search.getText().toString();
					int stpos = text.indexOf(term, editor.getSelectionStart());
					int first = stpos;
					int endpos = stpos + term.length();
					if (stpos > 0) {
						SpannableString selectedtext = new SpannableString(
								editor.getText());

						// Loop through all the appearnace of the text
						while (stpos != -1) {

							// set the span to highlight searched text
							selectedtext.setSpan(new BackgroundColorSpan(
									0xFFFFFF00), stpos, endpos, 0);
							stpos = text.indexOf(term, endpos);
							endpos = stpos + term.length();
						}
						editor.setText(selectedtext,
								TextView.BufferType.SPANNABLE);

						// request focus
						editor.requestFocus();

						// set the cursor position
						editor.setSelection(first);
					}
					return true;
				}
				return false;
			}
		});

		// Insert Figure
		toolbar[0].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent insertFigure = new Intent(TexEditor.this,
						InsertFigure.class);
				startActivityForResult(insertFigure, 2);
			}
		});

		// Insert Table
		toolbar[1].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent insertTable = new Intent(TexEditor.this,
						InsertTable.class);
				startActivityForResult(insertTable, 3);
			}
		});

		// Insert Symbol
		toolbar[2].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent insertSymbol = new Intent(TexEditor.this,
						InsertSymbol.class);
				startActivityForResult(insertSymbol, 4);
			}
		});

		// next button
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = editor.getSelectionStart();
				Spannable text = editor.getText();
				int t = text.nextSpanTransition(position, text.length(),
						BackgroundColorSpan.class);
				String searchterm = search.getText().toString();
				if (position + searchterm.length() == t) {
					t = text.nextSpanTransition(t, text.length(),
							BackgroundColorSpan.class);
				}
				editor.requestFocus();
				editor.setSelection(t);
			}
		});

		// previous button
		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = editor.getSelectionStart();
				Spannable text = editor.getText();
				int t = 0;
				int tmp = 0;
				while (t < position) {
					tmp = t;
					t = text.nextSpanTransition(t, position,
							BackgroundColorSpan.class);
				}
				String term = search.getText().toString();
				editor.requestFocus();
				if (tmp - term.length() >= 0) {
					editor.setSelection(tmp - term.length());
				} else {
					Toast.makeText(context, "Search completed.",
							Toast.LENGTH_LONG);
				}
			}
		});

		// replace button
		replace.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = editor.getSelectionStart();
				Editable text = editor.getText();
				String searchterm = search.getText().toString();
				String replaceterm = newtext.getText().toString();
				text.replace(position, position + searchterm.length(),
						replaceterm);
			}
		});

		// replace all button
		replaceall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String text = editor.getText().toString();
				String term = search.getText().toString();
				String replace = newtext.getText().toString();
				text = text.replaceAll(term, replace);
				editor.setText(text);
			}
		});

		if (data == null) {
			editor.addTextChangedListener(watcher);
		}
		displayContent(filename);
	}

	// Display/hide searchbar
	private void displaySearch(boolean enablesearch) {
		LinearLayout mainLayout = (LinearLayout) this
				.findViewById(R.id.searchbar);
		if (enablesearch) {
			// make the layout visible
			mainLayout.setVisibility(LinearLayout.VISIBLE);
		} else {
			// make the layout hidden
			mainLayout.setVisibility(LinearLayout.GONE);
		}
	}

	/*
	 * Create options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();

		// Show/hide menus
		if (enable) {
			menu.removeItem(R.id.otm_disableHigh);
		} else {
			menu.removeItem(R.id.otm_enableHigh);
		}
		if (display) {
			menu.removeItem(R.id.otm_hidetoolbar);
		} else {
			menu.removeItem(R.id.otm_showtoolbar);
		}
		if (enablesearch) {
			menu.removeItem(R.id.otm_showsearchbar);
		} else {
			menu.removeItem(R.id.otm_hidesearchbar);
		}
		inflater.inflate(R.menu.editor_options, menu);
		return true;
	}

	// Method displays the content of the file in the editbox
	private void displayContent(String fname) {
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "LaTeX"
				+ File.separator
				+ foldername + File.separator);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(file, fname);
		FileInputStream fis;
		String txt = "";

		// read the contents of the file to the EditBox
		try {
			fis = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				txt += strLine + "\n";
			}
			in.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File Not Found Exception", e);
		} catch (IOException e) {
			Log.e(TAG, "IO Exception", e);
		}
		editor.setText(txt);
	}

	/*
	 * Called each time a menu item is selected
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.otm_save:
			handleSave();
			return true;
		case R.id.otm_showtoolbar:
			display = true;
			displayToolbar(true);
			return true;
		case R.id.otm_hidetoolbar:
			display = false;
			displayToolbar(false);
			return true;
		case R.id.otm_enableHigh:
			enable = false;
			enableHighlighting(true);
			return true;
		case R.id.otm_disableHigh:
			enable = true;
			enableHighlighting(false);
			return true;
		case R.id.otm_showsearchbar:
			enablesearch = true;
			displaySearch(true);
			return true;
		case R.id.otm_hidesearchbar:
			enablesearch = false;
			displaySearch(false);
			return true;
		case R.id.otm_pdf:

			// file is written to the preference
			Editor prefeditor = choosenproject.edit();
			prefeditor.putString("file", filename);
			prefeditor.commit();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(true);
			builder.setTitle("How to generate");
			builder.setPositiveButton("Remote",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent startNewActivityOpen = new Intent(
									TexEditor.this, Remote.class);
							startActivityForResult(startNewActivityOpen, 0);
						}
					});
			builder.setNegativeButton("Local",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(context,
									"Feature not yet implemented",
									Toast.LENGTH_LONG).show();
							dialog.dismiss();
						}

					});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	// method to show/hide toolbar
	private void displayToolbar(boolean value) {
		if (value) {
			LinearLayout mainLayout = (LinearLayout) this
					.findViewById(R.id.toolbar);
			mainLayout.setVisibility(LinearLayout.VISIBLE);
		} else {
			LinearLayout mainLayout = (LinearLayout) this
					.findViewById(R.id.toolbar);
			mainLayout.setVisibility(LinearLayout.GONE);
		}
	}

	// method to enable/disable highlighting
	private void enableHighlighting(boolean value) {
		if (value) {

			// adds TextChangedListener
			SyntaxHighlighter.first = true;
			editor.addTextChangedListener(watcher);
			watcher.afterTextChanged(editor.getText());
		} else {

			// clear all highlighting
			Editable ed = editor.getEditableText();
			ForegroundColorSpan[] spans = ed.getSpans(0, ed.length(),
					ForegroundColorSpan.class);
			for (ForegroundColorSpan span : spans) {
				ed.removeSpan(span);
			}

			// remove textChangedListener
			editor.removeTextChangedListener(watcher);
		}
		return;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (display) {
			menu.getItem(1).setVisible(false);
			menu.getItem(2).setVisible(true);
		} else {
			menu.getItem(1).setVisible(true);
			menu.getItem(2).setVisible(false);
		}
		if (enable) {
			SyntaxHighlighter.first = true;
			menu.getItem(3).setVisible(false);
			menu.getItem(4).setVisible(true);
		} else {
			SyntaxHighlighter.first = true;
			menu.getItem(3).setVisible(true);
			menu.getItem(4).setVisible(false);
		}
		if (enablesearch) {
			menu.getItem(7).setVisible(true);
			menu.getItem(6).setVisible(false);
		} else {
			menu.getItem(7).setVisible(false);
			menu.getItem(6).setVisible(true);
		}
		return true;
	}

	// Saves the content to the file
	private void handleSave() {
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/LaTeX" + File.separator + foldername);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(file, filename);
		FileOutputStream fos;
		byte[] data = editor.getText().toString().getBytes();
		try {
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File Not Found Exception", e);
		} catch (IOException e) {
			Log.e(TAG, "IO Exception", e);
		}
	}

	// Inserts the character into the tex file
	public void insertChar(String txt) {
		Editable text = editor.getText();
		int pos = editor.getSelectionStart();
		text.insert(pos, txt);
	}

	// Called when a child activity returns
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 2 || requestCode == 3 || requestCode == 4) {
			if (resultCode == Activity.RESULT_OK) {
				String result = data.getStringExtra("result");
				insertChar(result);
			}
		}
		// super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final MyDataObject data = collectMyLoadedData();
		return data;
	}

	private MyDataObject collectMyLoadedData() {
		return new MyDataObject(display, enable, enablesearch);
	}

	private class MyDataObject {
		private boolean display, enable, enablesearch;

		public boolean isDisplay() {
			return display;
		}

		public boolean isEnable() {
			return enable;
		}

		public boolean isEnableSearch() {
			return enablesearch;
		}

		public MyDataObject(boolean display, boolean enable,
				boolean enablesearch) {
			super();
			this.display = display;
			this.enable = enable;
			this.enablesearch = enablesearch;
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Do you want to save file")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								handleSave();
								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).show();
	}

	@Override
	public void onClick(View v) {
		Button symbol = (Button) v;
		insertChar(symbol.getText().toString());
	}

}
