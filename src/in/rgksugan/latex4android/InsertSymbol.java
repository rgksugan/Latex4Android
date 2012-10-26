package in.rgksugan.latex4android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class InsertSymbol extends Activity implements
		OnGesturePerformedListener {

	private GestureLibrary gestureLib;
	private EditText txtresult;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.insert_symbol);

		// Adds a gesture overlay view
		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater()
				.inflate(R.layout.insert_symbol, null);
		gestureOverlayView.addView(inflate);

		// Adds gesture listener
		gestureOverlayView.addOnGesturePerformedListener(this);

		// Load gesture library
		gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!gestureLib.load()) {
			finish();
		}
		setContentView(gestureOverlayView);
		txtresult = (EditText) findViewById(R.id.txt_result);
		Button insert = (Button) findViewById(R.id.btn_insert);

		// OnClickListener for the insert button
		insert.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				returnActivity(txtresult.getText().toString());
			}
		});
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

		// Returns identified gestures
		ArrayList<Prediction> predictions = gestureLib.recognize(gesture);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Did you mean");

		ListView symbolList = new ListView(this);

		String tmp = "";

		// Loops for all the gestures identified
		for (Prediction prediction : predictions) {

			// If prediction score is greater than 1 add it to the TextBox
			if (prediction.score > 1.0) {
				tmp += prediction.name + ",";
			}
		}
		if (tmp.length() > 0) {
			// tmp = tmp.substring(0, tmp.length() - 1);
			builder.setView(symbolList);
			final Dialog dialog = builder.create();
			String[] symbols = tmp.split(",");
			ArrayAdapter<String> symbolAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					symbols);
			symbolList.setAdapter(symbolAdapter);
			symbolList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					txtresult.append(arg0.getItemAtPosition(position)
							.toString());
					dialog.dismiss();
				}
			});
			dialog.show();
		} else {
			Toast.makeText(this, "No Symbol identified. Try again.",
					Toast.LENGTH_LONG).show();
		}
	}

	// Method to return the initial activity
	private void returnActivity(String result) {
		Intent retIntent = new Intent();

		// Adds the result as the intent extras
		retIntent.putExtra("result", result);
		this.setResult(RESULT_OK, retIntent);

		// finish activity
		this.finish();
	}
}
