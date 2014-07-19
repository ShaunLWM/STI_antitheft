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

import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import dev.blacksheep.sti_antitheft.MyLocation.LocationResult;

public class MainActivity extends PreferenceActivity {
	private static final int ADMIN_INTENT = 15;
	String device_admin_description_activity;
	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mComponentName;
	CheckBoxPreference pref_device_admin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mComponentName = new ComponentName(MainActivity.this, DeviceAdmin.class);
		addPreferencesFromResource(R.xml.preference);

		pref_device_admin = (CheckBoxPreference) findPreference("pref_device_admin");
		setDeviceAdminCheckBox();
		Preference pref_choose_files = (Preference) findPreference("pref_choose_files");
		pref_choose_files.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(MainActivity.this, FileChooserActivity.class));
				return false;
			}
		});
		Preference pref_encrypt = (Preference) findPreference("pref_encrypt");
		pref_encrypt.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				new encryptFiles().execute();
				return false;
			}
		});

		Preference pref_decrypt = (Preference) findPreference("pref_decrypt");
		pref_decrypt.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				new decryptFiles().execute();
				return false;
			}
		});
		Preference pref_wipe = (Preference) findPreference("pref_wipe");
		pref_wipe.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Utils u = new Utils(getApplicationContext());
				u.wipeStorageCommand();
				return false;
			}
		});
		Preference pref_show_command = (Preference) findPreference("pref_show_command");
		pref_show_command.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(MainActivity.this, ShowCommandActivity.class));
				return false;
			}
		});
		Preference pref_location = (Preference) findPreference("pref_location");
		pref_location.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
			    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				return false;
			}
		});
		Preference pref_sms = (Preference) findPreference("pref_sms");
		pref_sms.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Utils u = new Utils(MainActivity.this);
				Log.e("PHONE STATUS", u.phoneStatus());
				MainActivity.this.startService(new Intent(MainActivity.this, AlarmService.class).putExtra("duration", "10"));
				LocationResult locationResult = new LocationResult() {
					@Override
					public void gotLocation(Location location) {
						if (location == null) {
							Log.e("Location", "No location");
						} else {
							Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
							List<Address> addresses;

							try {
								addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
								String address = addresses.get(0).getAddressLine(0);
								String city = addresses.get(0).getAddressLine(1);
								String country = addresses.get(0).getAddressLine(2);
								String data = "Latitude : " + location.getLatitude() + "\nLongtitude : " + location.getLongitude() + "\nAddress:" + address + ", " + city + ", " + country;
								data += "\nhttps://maps.google.com/maps?q=" + location.getLatitude() + ",+" + location.getLongitude() + "+" + "(Current+phone+location)\n" + "Accuracy : "
										+ location.getAccuracy() + "m" + "\nProvider : " + location.getProvider();
								Log.e("Location", data);
								Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
							} catch (Exception e) {
								e.printStackTrace();
								String data = "Latitude : " + location.getLatitude() + "\nLongtitude : " + location.getLongitude() + "\nAddress: ERROR";
								data += "\nhttps://maps.google.com/maps?q=" + location.getLatitude() + ",+" + location.getLongitude() + "+" + "(Current+phone+location)\n" + "Accuracy : "
										+ location.getAccuracy() + "m" + "\nProvider : " + location.getProvider();
								Log.e("Location", data);
								Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
							}
						}
					}
				};
				MyLocation myLocation = new MyLocation();
				myLocation.getLocation(MainActivity.this, locationResult, "");
				return false;
			}
		});

		Preference pref_lock_phone = (Preference) findPreference("pref_lock_phone");
		pref_lock_phone.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				String body = "!lock ";
				try {
					String pass, password = "";
					if (body.contains(" ")) {
						password = body.replace("!lock ", "").trim();
					}
					Utils u = new Utils(MainActivity.this);
					if (u.isDeviceAdminEnabled()) {
						if (password.length() > 0) {
							pass = u.lockDevice(password);
						} else {
							pass = u.lockDevice("");
						}
						if (pass.length() > 0) {
							Log.e("Locking Phone", "Phone has been locked with password : " + pass + " (no starting or ending spaces)");
						} else {
							Log.e("Locking Phone", "Error locking device.");
						}
					} else {
						Log.e("Locking Phone", "Device Admin is not enabled. Unable to lock phone.");
					}
				} catch (Exception e) { // lock with default
					Log.e("Locking Phone", "Device Admin is not enabled. Unable to lock phone.");
				}
				return false;
			}
		});
		
		Preference pref_popup = (Preference) findPreference("pref_popup");
		pref_popup.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				new Utils(MainActivity.this).setPopupOnBoot(true, Consts.PHONE_STOLEN);
				startActivity(new Intent(MainActivity.this, PopupMessageActivity.class).putExtra("message", Consts.PHONE_STOLEN));
				return false;
			}
		});
		
		Preference pref_test_location = (Preference) findPreference("pref_test_location");
		pref_test_location.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				LocationLibrary.forceLocationUpdate(MainActivity.this);
				new Utils(MainActivity.this).getFluffyLocation();
			    return false;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADMIN_INTENT) {
			if (resultCode == RESULT_OK) {
				pref_device_admin.setChecked(true);
				setDeviceAdminCheckBox();
				Toast.makeText(getApplicationContext(), "Registered As Admin", Toast.LENGTH_SHORT).show();
			} else {
				pref_device_admin.setChecked(false);
				setDeviceAdminCheckBox();
				Toast.makeText(getApplicationContext(), "Failed to register as Admin", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void setDeviceAdminCheckBox() {
		if (pref_device_admin.isChecked()) {
			pref_device_admin.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					mDevicePolicyManager.removeActiveAdmin(mComponentName);
					// Toast.makeText(getApplicationContext(),
					// "Admin registration removed", Toast.LENGTH_SHORT).show();
					setDeviceAdminCheckBox();
					return false;
				}
			});
		} else {
			pref_device_admin.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
					intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, device_admin_description_activity);
					startActivityForResult(intent, ADMIN_INTENT);
					return false;
				}
			});
		}
	}

	private class encryptFiles extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
			MainActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "Files encrypted successfully", Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		protected Void doInBackground(Void... params) {
			SQLFunctions sql = new SQLFunctions(MainActivity.this);
			sql.open();
			ArrayList<String> data = sql.loadFileList();
			for (String filePath : data) {
				try {
					byte[] fileByteData = getFile(filePath);
					Utils u = new Utils(MainActivity.this);
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

	private class decryptFiles extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			SQLFunctions sql = new SQLFunctions(MainActivity.this);
			sql.open();
			ArrayList<String> data = sql.loadFileList();
			for (String filePath : data) {
				try {
					byte[] fileByteData = getFile(filePath);
					Utils u = new Utils(MainActivity.this);
					byte[] key = u.generateKey(sql.getPasswordOfFiles(filePath));
					byte[] fileToEncrypt = u.decodeFile(key, fileByteData);
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

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
			MainActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this, "Files decrypted successfully", Toast.LENGTH_SHORT).show();
				}
			});
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
}
