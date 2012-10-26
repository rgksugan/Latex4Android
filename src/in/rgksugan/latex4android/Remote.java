package in.rgksugan.latex4android;

import in.rgksugan.latex4android.util.NullHostKeyVerifier;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.AndroidConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.xfer.FileSystemFile;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Remote extends Activity {

	private static final String TAG = "Remote";
	private SharedPreferences choosenproject = null;
	private SharedPreferences remoteMachine = null;
	private String filename = null;
	private String foldername = null;
	private String projectfolder = null;
	private String txtstatus = null;
	private final Context context = this;
	private EditText username, password, ipaddress, portno;
	private TextView status = null;
	private Button viewpdf = null, viewlog = null;
	private long initial;
	private boolean pdf = false, log = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remote);
		status = (TextView) findViewById(R.id.txt_status);
		viewpdf = (Button) findViewById(R.id.btn_viewpdf);
		viewlog = (Button) findViewById(R.id.btn_viewlog);

		// Check if the state is already saved
		final MyDataObject data = (MyDataObject) getLastNonConfigurationInstance();

		// If state is saved load the last state
		if (data != null) {
			if (data.isPdf()) {
				viewpdf.setVisibility(LinearLayout.VISIBLE);
				pdf = true;
			} else {
				viewpdf.setVisibility(LinearLayout.GONE);
				pdf = false;
			}
			if (data.isLog()) {
				viewlog.setVisibility(LinearLayout.VISIBLE);
				log = true;
			} else {
				viewlog.setVisibility(LinearLayout.GONE);
				log = false;
			}
			status.setText(data.getStatus());
			txtstatus = data.getStatus();
		}

		// else load the initial state
		username = (EditText) findViewById(R.id.txt_username);
		password = (EditText) findViewById(R.id.txt_password);
		ipaddress = (EditText) findViewById(R.id.txt_ipaddress);
		portno = (EditText) findViewById(R.id.txt_portno);

		// Get the saved data of the last used machine details
		remoteMachine = getSharedPreferences("remoteMachine",
				Context.MODE_PRIVATE);
		username.setText(remoteMachine.getString("username", ""));
		ipaddress.setText(remoteMachine.getString("ipaddress", ""));
		portno.setText(remoteMachine.getString("portno", ""));
		Button generate = (Button) findViewById(R.id.btn_generate);
		choosenproject = getSharedPreferences("choosenproject",
				Context.MODE_PRIVATE);
		foldername = choosenproject.getString("project", "");
		filename = choosenproject.getString("file", "");
		projectfolder = Environment.getExternalStorageDirectory()
				+ File.separator + "LaTeX" + File.separator + foldername;

		// Generate button OnClickListener
		generate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Hide the two buttons
				viewpdf.setVisibility(LinearLayout.GONE);
				viewlog.setVisibility(LinearLayout.GONE);

				// Save the machine details to preferences
				Editor prefeditor = remoteMachine.edit();
				prefeditor.putString("username", username.getText().toString());
				prefeditor.putString("ipaddress", ipaddress.getText()
						.toString());
				prefeditor.putString("potno", portno.getText().toString());
				prefeditor.commit();

				// Check if network is available
				if (isNetworkAvailable()) {
					File pdfFile = new File(projectfolder + File.separator
							+ filename.substring(0, filename.lastIndexOf('.'))
							+ ".pdf");

					// Note down the last modified time of the PDF file
					if (pdfFile.exists()) {
						initial = pdfFile.lastModified();
					}

					// Run in a separate thread
					ConvertPDF task = new ConvertPDF();
					task.execute(username.getText().toString(), password
							.getText().toString(), ipaddress.getText()
							.toString(), portno.getText().toString());
				} else {

					// Alert if there is no internet connection
					new AlertDialog.Builder(context)
							.setMessage(
									"No Internet connection available. Please make sure your internet connection is on and working.")
							.show();
				}
			}
		});

		// OnClickListener for ViewPDF button
		viewpdf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Opens the file in the pre-installed PDF viewer
				File pdfFile = new File(projectfolder + File.separator
						+ filename.substring(0, filename.lastIndexOf('.'))
						+ ".pdf");
				if (pdfFile.exists()) {
					Uri path = Uri.fromFile(pdfFile);
					Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
					pdfIntent.setDataAndType(path, "application/pdf");
					pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					try {
						startActivityForResult(pdfIntent, 0);
					} catch (ActivityNotFoundException e) {

						// Alert if no PDF viewer is available
						new AlertDialog.Builder(context)
								.setMessage(
										"No Application available to view PDF files. Please download an app to view PDF file from Google Play")
								.show();
						Log.e(TAG, "No Application available to view PDF", e);
					}
				}
			}
		});

		// OnClickListener for viewLog
		viewlog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File logFile = new File(projectfolder + File.separator
						+ filename.substring(0, filename.lastIndexOf('.'))
						+ ".log");
				if (logFile.exists()) {
					if (logFile.exists()) {

						// Opens the LogViewer activity
						Intent logintent = new Intent(context, LogViewer.class);
						logintent.putExtra("filename",
								logFile.getAbsolutePath());
						startActivityForResult(logintent, 1);
					}
				}
			}
		});
	}

	// AsyncTask to run as a separate thread
	private class ConvertPDF extends AsyncTask<String, Void, String> {
		ProgressDialog dialog;

		// Method executed before the main process
		@Override
		protected void onPreExecute() {

			// Display a progress dialog
			dialog = ProgressDialog.show(context, "",
					"Generating. Please wait...");
		}

		// Main process of the thread
		@Override
		protected String doInBackground(String... arg) {
			String result = "";

			// creates new SSH client
			final SSHClient ssh = new SSHClient(new AndroidConfig());
			Command cmd = null;
			try {

				// Adds a nullHostKeyVerifier
				ssh.addHostKeyVerifier(new NullHostKeyVerifier());

				// default port number
				int pn = 22;
				if (arg[3].length() > 0) {
					pn = Integer.parseInt(arg[3]);
				}

				// connect to the machine
				try {
					ssh.connect(arg[2], pn);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}

				// Authenticate with the password entered
				ssh.authPassword(arg[0], arg[1]);

				// start a new session
				final Session session = ssh.startSession();
				try {

					// Sends all the files to the remote server
					for (String file : new File(projectfolder).list()) {
						final String src = projectfolder + File.separator
								+ file;
						ssh.newSCPFileTransfer().upload(
								new FileSystemFile(src), "/tmp/");
					}

					// runs the commands in the remote machine
					cmd = session
							.exec("cd /tmp/;latex " + filename + "; latex "
									+ filename + " ; latex " + filename
									+ "; latex " + filename + " ; pdflatex "
									+ filename);

					// reads the output of the command
					result = IOUtils.readFully(cmd.getInputStream()).toString();

					// Download the PDF and log file
					final SFTPClient sftp = ssh.newSFTPClient();
					try {
						sftp.get(
								"/tmp/"
										+ filename.substring(0,
												filename.lastIndexOf('.'))
										+ ".pdf", new FileSystemFile(
										projectfolder));
						sftp.get(
								"/tmp/"
										+ filename.substring(0,
												filename.lastIndexOf('.'))
										+ ".log", new FileSystemFile(
										projectfolder));
					} catch (Exception e) {
						Log.e(TAG, "Exception", e);
					} finally {
						sftp.close();
					}
					cmd.join(100, TimeUnit.SECONDS);
				} finally {
					session.close();
				}
			} catch (UserAuthException e) {
				Log.e(TAG, "User Authentication Exception", e);
				return "user not authentic";
			} catch (TransportException e) {
				Log.e(TAG, "Transport Exception", e);
			} catch (ConnectionException e) {
				Log.e(TAG, "Exception", e);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				try {

					// close the connection
					ssh.disconnect();
					ssh.close();
				} catch (IOException e) {
					Log.e(TAG, "IO Exception", e);
				} catch (Exception e) {
					Log.e(TAG, "Exception", e);
				}
			}
			if (cmd != null && cmd.getExitStatus() == 0) {
				return result;
			} else {
				return "Failure";
			}
		}

		// Executed after the main process
		@Override
		protected void onPostExecute(String result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			try {
				if (!result.equals("Failure")) {

					// If the last modified time is larger than the old file
					// ViewPDF button is displayed
					File pdfFile = new File(projectfolder + File.separator
							+ filename.substring(0, filename.lastIndexOf('.'))
							+ ".pdf");
					if (pdfFile.exists() && pdfFile.lastModified() > initial) {
						viewpdf.setVisibility(LinearLayout.VISIBLE);
						pdf = true;
					} else {
						Toast.makeText(context, "PDF not generated",
								Toast.LENGTH_LONG);
					}

					// Display viewLog file
					viewlog.setVisibility(LinearLayout.VISIBLE);
					log = true;

					// The output is set in the TextBox
					status.setText(result);
					txtstatus = result;
				} else if (result.equals("user not authentic")) {
					Toast.makeText(context, "User authentication failed.",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				Log.e(TAG, "Post Execution", e);
			}
		}
	}

	// Checks if the network is available
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final MyDataObject data = collectMyLoadedData();
		return data;
	}

	private MyDataObject collectMyLoadedData() {
		return new MyDataObject(pdf, log, txtstatus);
	}

	private class MyDataObject {
		private boolean pdf, log;
		private String status;

		public MyDataObject(boolean pdf, boolean log, String status) {
			super();
			this.pdf = pdf;
			this.log = log;
			this.status = status;
		}

		public boolean isPdf() {
			return pdf;
		}

		public boolean isLog() {
			return log;
		}

		public String getStatus() {
			return status;
		}

	}
}
