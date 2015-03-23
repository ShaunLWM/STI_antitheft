package dev.blacksheep.sti_antitheft.classes;

public class GoogleDriveActivity {
	
}

/*
public class GoogleDriveActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	private static final int REQUEST_CODE_RESOLUTION = 3;
	String TAG = "GoogleDriveActivity";
	private GoogleApiClient mGoogleApiClient;
	SecurePreferences sp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = new SecurePreferences(GoogleDriveActivity.this);
		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		if (checkIfGDriveAuth()) {
			showDialogBox();
			// credential.setSelectedAccountName(getCurrentAccount());
			// service = getDriveService(credential);
		} else {

		}
	}

	private boolean checkIfGDriveAuth() {
		String email = sp.getString(Consts.BACKUP_GDRIVE_EMAIL, "");
		if (email.length() > 0) {
			Log.e("EMAIL", email);
			return true;
		}
		return false;
	}

	private void showDialogBox() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GoogleDriveActivity.this);
		alertDialogBuilder.setTitle(R.string.backup_gdrive_success_title);
		alertDialogBuilder.setMessage(R.string.backup_gdrive_success_message).setCancelable(false).setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void showErrorDialogBox() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GoogleDriveActivity.this);
		alertDialogBuilder.setTitle(R.string.backup_gdrive_error_title);
		alertDialogBuilder.setMessage(R.string.backup_gdrive_error_message).setCancelable(false).setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
			return;
		}
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "API client connected.");
		sp.edit().putString(Consts.BACKUP_GDRIVE_EMAIL, "").commit();
		showDialogBox();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "GoogleApiClient connection suspended");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mGoogleApiClient == null) {
			// Create the API client and bind it to an instance variable.
			// We use this instance as the callback for connection and
			// connection
			// failures.
			// Since no account name is passed, the user is prompted to choose.
			mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		}
		// Connect the client. Once connected, the camera is launched.
		mGoogleApiClient.connect();
	}

	@Override
	protected void onPause() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPause();
	}
}*/