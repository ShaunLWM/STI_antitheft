package dev.blacksheep.sti_antitheft;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import dev.blacksheep.sti_antitheft.MyLocation.LocationResult;

public class SMSReceiver extends BroadcastReceiver {
	private static final String TAG = "aeGis";

	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String EXTRA_SMS_PDUS = "pdus";
	protected static String address;
	Context context;

	@Override
	public void onReceive(final Context context, Intent intent) {
		this.context = context;
		if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {
			final SMSUtils smsManager = new SMSUtils(context);
			Bundle extras = intent.getExtras();
			if (extras != null) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

				SmsMessage[] messages = getMessagesFromIntent(intent);
				for (SmsMessage sms : messages) {
					String body = sms.getMessageBody();
					address = sms.getOriginatingAddress();
					abortBroadcast();
					SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(context);
					String encryptCommand = myPreference.getString("pref_encrypt_command", "encrypt");

					if (body.startsWith("!" + encryptCommand)) {
						new encryptFiles().execute();
					} else if (body.equals("!location")) {
						Utils u = new Utils(context);
						smsManager.sendSMS(address, u.getFluffyLocation());
					} else if (body.equals("!status")) {
						Utils u = new Utils(context);
						Log.e("PHONE STATUS", u.phoneStatus());
						smsManager.sendSMS(address, u.phoneStatus());
					} else if (body.startsWith("!locate")) {
						String data = "Latitude : 1.3458561998\nLongtitude : 103.9340006\nAddress: Temasek Polytechnic, Tampines";
						data += "\n" + "Accuracy : 1m" + "\nProvider : GPS";
						smsManager.sendSMS(address, data);
					} else if (body.startsWith("!alarm")) {
						if (body.startsWith("!alarm ")) {
							String timing = body.replace("!alarm ", "");
							try {
								int time = Integer.parseInt(timing.toString().trim());
								context.startService(new Intent(context, AlarmService.class).putExtra("duration", time));
							} catch (Exception e) {
								context.startService(new Intent(context, AlarmService.class));
							}
						} else {
							context.startService(new Intent(context, AlarmService.class));
						}
					} else if (body.startsWith("!message")) {
						String text = body.replace("!message", "").trim();
						if (text.length() < 1) {
							text = Consts.PHONE_STOLEN;
						}
						new Utils(context).setPopupOnBoot(true, text);
						context.startActivity(new Intent(context, PopupMessageActivity.class).putExtra("message", text).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					} else if (body.equals("!wipe")) {
						Utils u = new Utils(context);
						u.wipeStorageCommand();
						smsManager.sendSMS(address, "Wipe external storage completed");
					} else if (body.startsWith("!lock")) {
						try {
							String pass, password = "";
							if (body.contains(" ")) {
								password = body.replace("!lock ", "").trim();
							}
							Utils u = new Utils(context);
							if (u.isDeviceAdminEnabled()) {
								if (password.length() > 0) {
									pass = u.lockDevice(password);
								} else {
									pass = u.lockDevice("");
								}
								if (pass.length() > 0) {
									smsManager.sendSMS(address, "Phone has been locked with password : " + pass + " (no starting or ending spaces)");
								} else {
									smsManager.sendSMS(address, "Error locking device.");
								}
							} else {
								smsManager.sendSMS(address, "Device Admin is not enabled. Unable to lock phone.");
							}
						} catch (Exception e) { // lock with default
							smsManager.sendSMS(address, "Device Admin is not enabled. Unable to lock phone.");
						}
					}
				}
			}
		}
	}

	private class encryptFiles extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
			SMSUtils sms = new SMSUtils(context);
			sms.sendSMS(address, "Files successfully encrypted");
		}

		@Override
		protected Void doInBackground(Void... params) {
			SQLFunctions sql = new SQLFunctions(context);
			sql.open();
			ArrayList<String> data = sql.loadFileList();
			for (String filePath : data) {
				try {
					byte[] fileByteData = getFile(filePath);
					Utils u = new Utils(context);
					byte[] key = u.generateKey(sql.getPasswordOfFiles(filePath));
					byte[] fileToEncrypt = u.encodeFile(key, fileByteData);
					FileOutputStream fos = new FileOutputStream(new File(filePath));
					fos.write(fileToEncrypt);
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			sql.close();
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	public byte[] getFile(String filePath) throws FileNotFoundException {
		byte[] audio_data = null;
		byte[] inarry = null;

		try {
			File file = new File(filePath);
			FileInputStream is = new FileInputStream(file);
			int length = is.available();
			audio_data = new byte[length];
			int bytesRead;
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			while ((bytesRead = is.read(audio_data)) != -1) {
				output.write(audio_data, 0, bytesRead);
			}
			inarry = output.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inarry;
	}

	private SmsMessage[] getMessagesFromIntent(Intent intent) {
		Object[] messages = (Object[]) intent.getSerializableExtra(EXTRA_SMS_PDUS);
		byte[][] pduObjs = new byte[messages.length][];

		for (int i = 0; i < messages.length; i++) {
			pduObjs[i] = (byte[]) messages[i];
		}
		byte[][] pdus = new byte[pduObjs.length][];
		int pduCount = pdus.length;
		SmsMessage[] msgs = new SmsMessage[pduCount];
		for (int i = 0; i < pduCount; i++) {
			pdus[i] = pduObjs[i];
			msgs[i] = SmsMessage.createFromPdu(pdus[i]);
		}
		return msgs;
	}
}