package in.rgksugan.latex4android;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LogViewer extends Activity {

	private String TAG = "LogViewer";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_viewer);
		TextView txtview = (TextView) findViewById(R.id.txt_status);
		File file = new File(getIntent().getStringExtra("filename"));
		FileInputStream fis;
		String txt = "";
		try {

			// Opens the log file and display in the TextView
			fis = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int i = 1;
			while ((strLine = br.readLine()) != null) {
				txt += strLine + "\n";
				if (i == 100) {
					txtview.setText(txtview.getText().toString() + txt);
					i = 0;
					txt = "";
				}
				i++;
			}
			txtview.setText(txtview.getText().toString() + txt);
			in.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File Not Found Exception", e);
		} catch (IOException e) {
			Log.e(TAG, "IO Exception", e);
		}
		Button ok = (Button) findViewById(R.id.btn_Ok);

		// OnClickListener for the Ok button
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				returnActivity();
			}
		});
	}

	// To return to the initial activity
	private void returnActivity() {
		Intent retIntent = new Intent();

		// puts extra data into the intent
		this.setResult(RESULT_OK, retIntent);

		// finishes the activity
		this.finish();
	}
}
