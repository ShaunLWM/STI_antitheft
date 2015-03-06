package dev.blacksheep.sti_antitheft.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.securepreferences.SecurePreferences;

import dev.blacksheep.sti_antitheft.Consts;
import dev.blacksheep.sti_antitheft.classes.SMSUtils;

public class BackupService extends Service {
	private static final String TAG = "BackupService";
	boolean backupDropbox = false;
	String dbxKey, dbxSecret, timeStamp, picName, audioFile;
	private DropboxAPI<AndroidAuthSession> mDBApi;

	String address;
	boolean backupPic = false, backupAudio = false;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		address = extras.getString("address");
		Log.e("BackUpService", "Address : " + address);
		if (extras.containsKey("camera") && extras.containsKey("picname")) {
			backupPic = true;
			picName = extras.getString("picname");
		} else if (extras.containsKey("audio") && extras.containsKey("audioFile")) {
			backupAudio = true;
			audioFile = extras.getString("audioFile");
		}
		initialiseSequence();
		return 1;
	}

	private void initialiseSequence() {
		SecurePreferences sp = new SecurePreferences(getApplicationContext());
		dbxKey = sp.getString(Consts.BACKUP_DROPBOX_KEY, "");
		dbxSecret = sp.getString(Consts.BACKUP_DROPBOX_SECRET, "");
		if (dbxKey.length() > 0 && dbxSecret.length() > 0) {
			backupDropbox = true;
		}

		if (backupDropbox) {
			if (backupPic) {
				new backupPicture().execute();
			} else if (backupAudio) {
				new backupAudio().execute();
			} else {
				new startBackup().execute();
			}
		} else {
			Log.e(TAG, "Nothing to backup");
			smsBack("Nothing to backup. Both your Dropbox and Google Drive have not been enabled");
			stopSelf();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	private void smsBack(String content) {
		SMSUtils s = new SMSUtils(getApplicationContext());
		s.sendSMS(address, content);
	}

	private String doLogs() {
		timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + "_Dropbox";

		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		String fileName = timeStamp + ".txt";
		File file = new File(baseDir + File.separator + fileName);

		try {
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			// myOutWriter.append(new
			// Utils(getApplicationContext()).getCallLog());
			myOutWriter.close();
			fOut.close();
			// Log.e(TAG, new Utils(getApplicationContext()).getCallLog());
			return baseDir + File.separator + fileName;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public class backupAudio extends AsyncTask<Void, Void, Void> {
		String text = "";

		@Override
		protected Void doInBackground(Void... arg0) {
			String audio = Environment.getExternalStorageDirectory().toString() + File.separator + audioFile;
			File file = new File(audio);
			if (file.exists()) {
				Log.e("EXIST", "FILE EXIST!!");
			} else {
				Log.e("EXIST", "FILE DOTN EXIST");
			}
			if (backupDropbox) {
				Log.e(TAG, "Backing up to Dropbox");
				AndroidAuthSession session = buildSession();
				mDBApi = new DropboxAPI<AndroidAuthSession>(session);
				FileInputStream inputStream = null;
				Log.e(TAG, file.getAbsolutePath());
				try {
					inputStream = new FileInputStream(file);
					try {
						Log.e("Audio File Size", file.length() + "");
						Entry response = mDBApi.putFile(audioFile, inputStream, file.length(), null, null);
						if (response.rev.length() > 0) {
							Log.e("FILE REV", response.rev);
							Log.e("File Byte", response.bytes + "");
							// Log.e(TAG, "Successfully uploaded!");
							text += "Audio uploaded to Dropbox - Apps/AIRemote/" + audioFile;
						} else {
							text += "Audio failed to upload to Dropbox";
						}
					} catch (DropboxException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}

				}
			}
			if (file.delete()) {
				Log.e(TAG, "File deleted");
			} else {
				Log.e(TAG, "Cannot delete file");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (text.equals("")) {
				text = "You did not setup any backup services :/";
			}
			smsBack(text);
		}
	}

	public class backupPicture extends AsyncTask<Void, Void, Void> {
		String text = "";

		@Override
		protected Void doInBackground(Void... arg0) {
			String pic = Environment.getExternalStorageDirectory().toString() + File.separator + picName;
			File file = new File(pic);
			if (backupDropbox) {
				Log.e(TAG, "Backing up to Dropbox");
				AndroidAuthSession session = buildSession();
				mDBApi = new DropboxAPI<AndroidAuthSession>(session);
				FileInputStream inputStream = null;
				Log.e(TAG, file.getAbsolutePath());
				try {
					inputStream = new FileInputStream(file);
					try {
						Entry response = mDBApi.putFile(picName, inputStream, file.length(), null, null);
						if (response.rev.length() > 0) {
							Log.e("FILE REV", response.rev);
							// Log.e(TAG, "Successfully uploaded!");
							text += "Picture uploaded to Dropbox - Apps/AIRemote/" + picName;
						} else {
							text += "Picture failed to upload to Dropbox";
						}
					} catch (DropboxException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
						}
					}

				}
			}

			if (file.delete()) {
				Log.e(TAG, "File deleted");
			} else {
				Log.e(TAG, "Cannot delete file");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (text.equals("")) {
				text = "You did not setup any backup services :/";
			}
			smsBack(text);
		}
	}

	public class startBackup extends AsyncTask<Void, Void, Void> {
		String text = "";

		@Override
		protected Void doInBackground(Void... arg0) {
			String logs = doLogs();
			File file = new File(logs);
			if (logs.length() > 0) { // if can create
				Log.e(TAG, "File path - " + logs);
				String[] l = logs.split("/");
				if (backupDropbox) {
					Log.e(TAG, "Backing up to Dropbox");
					AndroidAuthSession session = buildSession();
					mDBApi = new DropboxAPI<AndroidAuthSession>(session);
					FileInputStream inputStream = null;
					Log.e(TAG, file.getAbsolutePath());
					try {
						inputStream = new FileInputStream(file);
						try {
							Entry response = mDBApi.putFile(l[l.length - 1], inputStream, file.length(), null, null);
							if (response.rev.length() > 0) {
								text += "Logs uploads to Dropbox - /Apps/SheepTheft/" + l[l.length - 1];
							} else {
								text += "Logs failed to upload to Dropbox";
							}
						} catch (DropboxException e) {
							e.printStackTrace();
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} finally {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (IOException e) {
							}
						}

					}
				}
				if (file.delete()) {
					Log.e(TAG, "File deleted");
				} else {
					Log.e(TAG, "Cannot delete file");
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (text.equals("")) {
				text = "You did not setup any backup services :/";
			}
			smsBack(text);
		}
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(Consts.DROPBOX_APP_KEY, Consts.DROPBOX_APP_SECRET);
		// Log.e(TAG, dbxKey + "|" + dbxSecret);
		return new AndroidAuthSession(appKeyPair, AccessType.APP_FOLDER, new AccessTokenPair(dbxKey, dbxSecret));
	}

}
