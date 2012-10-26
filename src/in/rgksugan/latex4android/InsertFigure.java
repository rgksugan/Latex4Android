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

public class InsertFigure extends Activity {

	// Holds the options for the multiselect
	protected CharSequence[] options = { "Here", "Top of the page",
			"Bottom of the page", "Page of floats" };

	// Holds all the selected options from the multiselect
	protected boolean[] selections = new boolean[options.length];
	private String pos = "[";
	protected Button position;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.insert_figure);
		position = (Button) findViewById(R.id.btn_position);
		position.setOnClickListener(new ButtonClickHandler());
		final EditText filename = (EditText) findViewById(R.id.txt_filename);
		final EditText caption = (EditText) findViewById(R.id.txt_caption);
		final EditText label = (EditText) findViewById(R.id.txt_label);
		final EditText percent = (EditText) findViewById(R.id.txt_percent);
		final CheckBox textwidth = (CheckBox) findViewById(R.id.chk_width);
		final CheckBox inflation = (CheckBox) findViewById(R.id.chk_Inflation);

		// To enable percent and textwidth when the value of inflation changes
		inflation.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					percent.setEnabled(true);
					textwidth.setEnabled(true);
				} else {
					percent.setEnabled(false);
					textwidth.setEnabled(false);
				}
			}
		});
		final CheckBox center = (CheckBox) findViewById(R.id.chk_center);
		final CheckBox insert = (CheckBox) findViewById(R.id.chk_insert);
		final Spinner spinner = (Spinner) findViewById(R.id.spn_expansion);

		// To enable caption, label and position options based on Insert as
		// Float
		insert.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					caption.setEnabled(true);
					label.setEnabled(true);
					position.setEnabled(true);
					spinner.setEnabled(true);
				} else {
					caption.setEnabled(false);
					label.setEnabled(false);
					position.setEnabled(false);
					spinner.setEnabled(false);
				}
			}
		});
		spinner.setSelection(0);
		Button insertbutton = (Button) findViewById(R.id.btn_insert);

		// Finishes the activity and returns to the last activity
		insertbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// based on the selesctions, the resulting string is computed
				String result = "";
				if (spinner.getSelectedItemPosition() == 0) {
					result += "\\begin{figure}\n";
				} else if (spinner.getSelectedItemPosition() == 1
						&& insert.isChecked()) {
					result += "\\begin{figure*}\n";
				}
				if (insert.isChecked()) {
					if (pos.length() > 1) {
						result += pos + "]";
					}
				}
				if (center.isChecked()) {
					result += "\\centering\n";
				}
				String tmp = "";
				if (inflation.isChecked()
						&& percent.getText().toString().length() > 0) {
					if (textwidth.isChecked()) {
						tmp += "[width=" + percent.getText().toString()
								+ "\\textwidth]";
					} else {
						tmp += "[scale=" + percent.getText().toString() + "]";
					}
				}
				result += "\\includegraphics" + tmp + "{"
						+ filename.getText().toString() + "}\n";
				if (label.getText().toString().length() > 0
						&& insert.isChecked()) {
					result += "\\label{" + label.getText().toString() + "}\n";
				}
				if (caption.getText().toString().length() > 0
						&& insert.isChecked()) {
					result += "\\caption{" + caption.getText().toString()
							+ "}\n";
				}
				if (spinner.getSelectedItemPosition() == 0) {
					result += "\\end{figure}\n";
				} else {
					result += "\\end{figure*}\n";
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

		// display the multi-select dialog box
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
		for (int i = 0; i < options.length; i++) {
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

	// To return the flow to the last activity
	private void returnActivity(String result) {
		Intent retIntent = new Intent();

		// adds the reulting code to the intent extras
		retIntent.putExtra("result", result);
		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}
}
