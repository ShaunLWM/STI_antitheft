package dev.blacksheep.sti_antitheft.classes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.securepreferences.SecurePreferences;

import dev.blacksheep.sti_antitheft.Consts;
import dev.blacksheep.sti_antitheft.R;

public class DropboxActivity extends SherlockActivity {
	final static private String APP_KEY = Consts.DROPBOX_APP_KEY;
	final static private String APP_SECRET = Consts.DROPBOX_APP_SECRET;
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private DropboxAPI<AndroidAuthSession> mDBApi;
	AppKeyPair appKeys;
	boolean authPreviously = false;
	SecurePreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_dropbox_gdrive);
		sp = new SecurePreferences(DropboxActivity.this);
		if (checkIfDropboxAuth()) {
			Log.e("Db", "Auth previously");
			authPreviously = true;
			showDialogBox();
		} else {
			Log.e("Db", "Never Auth previously");
			appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
			mDBApi = new DropboxAPI<AndroidAuthSession>(session);
			mDBApi.getSession().startAuthentication(DropboxActivity.this);
		}
	}

	private boolean checkIfDropboxAuth() {
		String key = sp.getString(Consts.BACKUP_DROPBOX_KEY, "");
		String secret = sp.getString(Consts.BACKUP_DROPBOX_SECRET, "");
		if (key.length() > 0 && secret.length() > 0) {
			return true;
		}
		return false;
	}

	private void showDialogBox() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DropboxActivity.this);
		alertDialogBuilder.setTitle(R.string.backup_dropbox_success_title);
		alertDialogBuilder.setMessage(R.string.backup_dropbox_success_message).setCancelable(false).setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
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
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DropboxActivity.this);
		alertDialogBuilder.setTitle(R.string.backup_dropbox_error_title);
		alertDialogBuilder.setMessage(R.string.backup_dropbox_error_message).setCancelable(false).setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
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
	protected void onResume() {
		super.onResume();
		if (mDBApi != null) {
			if (mDBApi.getSession().authenticationSuccessful() && !authPreviously) {
				try {
					mDBApi.getSession().finishAuthentication();
					AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
					sp.edit().putString(Consts.BACKUP_DROPBOX_KEY, tokens.key.toString()).commit();
					sp.edit().putString(Consts.BACKUP_DROPBOX_SECRET, tokens.secret.toString()).commit();
					showDialogBox();
				} catch (IllegalStateException e) {
					Log.e("DbAuthLog", "Error authenticating", e);
					showErrorDialogBox();
				}
			}
		}
	}
}
