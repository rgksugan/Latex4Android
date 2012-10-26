package in.rgksugan.latex4android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class InsertTable extends Activity {

	// Holds the options for the multiselect
	protected CharSequence[] options = { "Here", "Top of the page",
			"Bottom of the page", "Page of floats" };

	// Holds the selections of the multiselect
	protected boolean[] selections = new boolean[options.length];
	private String pos = "[";
	protected Button position;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.insert_table);
		position = (Button) findViewById(R.id.btn_position);
		position.setOnClickListener(new ButtonClickHandler());
		final Spinner expansion = (Spinner) findViewById(R.id.spn_expansion);
		final Spinner alignment = (Spinner) findViewById(R.id.spn_alignment);
		final CheckBox center = (CheckBox) findViewById(R.id.chk_center);
		final CheckBox insert = (CheckBox) findViewById(R.id.chk_insert);
		final EditText caption = (EditText) findViewById(R.id.txt_caption);
		final EditText label = (EditText) findViewById(R.id.txt_label);
		expansion.setSelection(0);
		alignment.setSelection(1);

		// Alters if the caption, label and position options are enabled based
		// on insert as Float
		insert.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					caption.setEnabled(true);
					label.setEnabled(true);
					position.setEnabled(true);
					expansion.setEnabled(true);
				} else {
					caption.setEnabled(false);
					label.setEnabled(false);
					position.setEnabled(false);
					expansion.setEnabled(false);
				}
			}
		});
		Button insertbutton = (Button) findViewById(R.id.btn_insert);

		// OnClickListener for the Insert button
		insertbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Computes the resulting string
				String result = "";
				if (expansion.getSelectedItemPosition() == 0) {
					result += "\\begin{table}";
				} else if (expansion.getSelectedItemPosition() == 1
						&& insert.isChecked()) {
					result += "\\begin{table*}";
				}
				if (insert.isChecked()) {
					if (pos.length() > 1) {
						result += pos + "]";
					}
				}
				result += "\n";
				if (center.isChecked()) {
					result += "\\centering\n";
				}
				result += "\\begin{tabular}";
				if (alignment.getSelectedItemPosition() == 0) {
					result += "[t]\n";
				} else if (alignment.getSelectedItemPosition() == 2) {
					result += "[b]\n";
				}
				result += "\\end{tabular}\n";
				if (label.getText().toString().length() > 0
						&& insert.isChecked()) {
					result += "\\label{" + label.getText().toString() + "}\n";
				}
				if (caption.getText().toString().length() > 0
						&& insert.isChecked()) {
					result += "\\caption{" + caption.getText().toString()
							+ "}\n";
				}
				if (expansion.getSelectedItemPosition() == 0) {
					result += "\\end{table}\n";
				} else {
					result += "\\end{table*}\n";
				}
				returnActivity(result);
			}
		});
	}

	public class ButtonClickHandler implements View.OnClickListener {
		public void onClick(View view) {
			showDialog(0);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		// Displays the multiselect dialog box
		return new AlertDialog.Builder(this)
				.setTitle("Position")
				.setMultiChoiceItems(options, selections,
						new DialogSelectionClickHandler())
				.setPositiveButton("OK", new DialogButtonClickHandler())
				.create();
	}

	public class DialogSelectionClickHandler implements
			DialogInterface.OnMultiChoiceClickListener {
		public void onClick(DialogInterface dialog, int clicked,
				boolean selected) {
		}
	}

	public class DialogButtonClickHandler implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int clicked) {
			switch (clicked) {
			case DialogInterface.BUTTON_POSITIVE:
				addSelectedPositions();
				break;
			}
		}
	}

	protected void addSelectedPositions() {

		// Loops for the options selected in the multiselect
		for (int i = 0; i < options.length; i++) {

			// Based on the option selected the result is modified
			if (selections[i]) {
				switch (i) {
				case 0:
					pos += "h";
					break;
				case 1:
					pos += "t";
					break;
				case 2:
					pos += "b";
					break;
				case 3:
					pos += "p";
					break;
				}
			}
		}
	}

	// This method returns the control to the initial activity
	private void returnActivity(String result) {
		Intent retIntent = new Intent();

		// Adds extra content to the intent
		retIntent.putExtra("result", result);
		this.setResult(RESULT_OK, retIntent);

		// finishes the current activity
		this.finish();
	}
}
