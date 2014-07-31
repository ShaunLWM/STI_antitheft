package dev.blacksheep.sti_antitheft;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.securepreferences.SecurePreferences;

public class Utils {

	private Context context;

	public Utils(Context con) {
		this.context = con;
	}
	
	public void storeSimInfo() {
		SecurePreferences sp = new SecurePreferences(context);
		TelephonyManager telemamanger = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Log.e("SIM", telemamanger.getSimSerialNumber());
		sp.edit().putString(Consts.STORE_SIM_SERIAL, telemamanger.getSimSerialNumber()).commit();
	}

	public boolean compareSimInfo() {
		SecurePreferences sp = new SecurePreferences(context);
		TelephonyManager telemamanger = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (!sp.getString(Consts.STORE_SIM_SERIAL, "").equals(telemamanger.getSimSerialNumber())) {
			return false;
		}
		return true;
	}
	public void setPopupOnBoot(boolean yesOrNo, String message) {
		SecurePreferences sp = new SecurePreferences(context);
		sp.edit().putBoolean("popup", yesOrNo).commit();
		sp.edit().putString("popup_message", message);
	}

	public boolean getPopupOnBoot() {
		SecurePreferences sp = new SecurePreferences(context);
		return sp.getBoolean("popup", false);
	}

	public String getPopupMessageOnBoot() {
		SecurePreferences sp = new SecurePreferences(context);
		return sp.getString("popup_message", Consts.PHONE_STOLEN);
	}

	public byte[] generateKey(String password) throws Exception {
		byte[] keyStart = password.getBytes("UTF-8");
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
		sr.setSeed(keyStart);
		kgen.init(128, sr);
		SecretKey skey = kgen.generateKey();
		return skey.getEncoded();
	}

	public byte[] encodeFile(byte[] key, byte[] fileData) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(fileData);
		return encrypted;
	}

	public byte[] decodeFile(byte[] key, byte[] fileData) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(fileData);
		return decrypted;
	}

	public String getDeviceId() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String deviceId = tm.getDeviceId();
		if (deviceId != null) {
			return deviceId + "(D)";
		} else {
			return android.os.Build.SERIAL + "(S)";
		}
	}

	public String phoneStatus() {
		String text = "";
		TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);

		String imeiSIM1 = telephonyInfo.getImeiSIM1();
		text += "IMEI : " + imeiSIM1 + "\n";
		text += "UDID : " + getDeviceId() + "\n";
		Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		/*
		 * double scale = batteryIntent.getIntExtra("scale", -1); double level =
		 * -1; if (rawlevel >= 0 && scale > 0) { level = rawlevel / scale; }
		 */
		text += "Battery : " + String.valueOf(rawlevel) + "%\n";
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi.getConnectionInfo();

		if (wifi.isWifiEnabled()) {
			try {
				text += "WiFi SSID : " + wifiInfo.getSSID() + "\n";
			} catch (Exception e) {
				text += "Wifi SSID : Error\n";
			}
		} else {
			text += "Wifi : Off\n";
		}

		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			text += "GPS : On\n";
		} else {
			text += "GPS : Off\n";
		}

		if (isDataEnabled()) {
			text += "Data : On\n";
		} else {
			text += "Data : Off\n";
		}
		return text;
	}

	public void wifiState(boolean on) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (on) {
			wifiManager.setWifiEnabled(true);
		} else {
			wifiManager.setWifiEnabled(false);
		}
	}

	public boolean isDataEnabled() {
		ConnectivityManager connManager1 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mMobile = connManager1.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mMobile.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	public void gpsState(boolean on) {
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		if (on) {
			intent.putExtra("enabled", true);
		} else {
			intent.putExtra("enabled", false);

		}
		context.sendBroadcast(intent);
		final Intent poke = new Intent();
		poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
		poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
		poke.setData(Uri.parse("3"));
		context.sendBroadcast(poke);
	}

	public void setMobileDataEnabled(boolean ON) throws Exception {
		final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		final Object iConnectivityManager = iConnectivityManagerField.get(conman);
		final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);
		setMobileDataEnabledMethod.invoke(iConnectivityManager, ON);
	}

	public boolean isDeviceAdminEnabled() {
		final DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (mDPM.isAdminActive(new ComponentName(context, DeviceAdmin.class))) {
			return true;
		}
		return false;
	}

	public String lockDevice(String newPass) {
		Log.e("Locking Device", "Locking device");
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		SecurePreferences preferences = new SecurePreferences(context);
		String password = preferences.getString("password", "");
		if (newPass.trim().length() > 0) { // if user specify
			Log.e("Locking Device", "Specified Password : " + newPass);
			devicePolicyManager.resetPassword(newPass, 0);
			devicePolicyManager.lockNow();
			return newPass;
		} else if (newPass.equals("") && password.length() > 0) {
			Log.e("Locking Device", "Default Master Password : " + password);
			devicePolicyManager.resetPassword(password, 0);
			devicePolicyManager.lockNow();
			return password;
		} else { // if user never specify in text AND nothing in pref
			password = randomNumberGenerator();
			devicePolicyManager.resetPassword(password, 0);
			devicePolicyManager.lockNow();
			Log.e("Locking Device", "Random Password : " + password);
			return password;
		}
	}

	private String randomNumberGenerator() {
		String password = "";
		Random randomGenerator = new Random();
		for (int i = 1; i <= 5; ++i) { // generate 5 digits
			int randomInt = randomGenerator.nextInt(10); // 0 to 9
			password += randomInt;
		}
		return password;
	}

	public void wipeStorageCommand() {
		new wipeSDCard().execute();
	}

	private void wipeDirectory(String name) {
		File directoryFile = new File(name);
		File[] filenames = directoryFile.listFiles();
		if (filenames != null && filenames.length > 0) {
			for (File tempFile : filenames) {
				if (tempFile.isDirectory()) {
					wipeDirectory(tempFile.toString());
					tempFile.delete();
				} else {
					tempFile.delete();
				}
			}
		} else {
			directoryFile.delete();
		}
	}

	public class wipeSDCard extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			File deleteMatchingFile = new File(Environment.getExternalStorageDirectory().toString());
			try {
				File[] filenames = deleteMatchingFile.listFiles();
				if (filenames != null && filenames.length > 0) {
					for (File tempFile : filenames) {
						Log.e("DELETING", tempFile.toString());
						if (tempFile.isDirectory()) {
							wipeDirectory(tempFile.toString());
							tempFile.delete();
						} else {
							tempFile.delete();
						}
					}
				} else {
					deleteMatchingFile.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public String getFluffyLocation() {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		List<Address> addresses;
		String data = "";
		LocationLibrary.forceLocationUpdate(context);
		LocationInfo locationInfo = new LocationInfo(context);
		if (locationInfo.anyLocationDataReceived()) {
			data += LocationInfo.formatTimeAndDay(locationInfo.lastLocationUpdateTimestamp, true) + "\n";
			data += "Lat:" + Float.toString(locationInfo.lastLat) + " - Long:" + Float.toString(locationInfo.lastLong) + "\n";
			data += "Acc:" + Integer.toString(locationInfo.lastAccuracy) + "m" + "\n";
			try {
				addresses = geocoder.getFromLocation(locationInfo.lastLat, locationInfo.lastLong, 1);
				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getAddressLine(1);
				String country = addresses.get(0).getAddressLine(2);
				data += country + ", " + city + ", " + address;
			} catch (Exception e) {

			}
		} else {
			LocationInfo latestInfo = new LocationInfo(context);
			data += LocationInfo.formatTimeAndDay(latestInfo.lastLocationUpdateTimestamp, true) + "\n";
			data += "Lat:" + Float.toString(latestInfo.lastLat) + " - Long:" + Float.toString(latestInfo.lastLong) + "\n";
			data += "Acc:" + Integer.toString(latestInfo.lastAccuracy) + "m" + "\n";
			try {
				addresses = geocoder.getFromLocation(latestInfo.lastLat, latestInfo.lastLong, 1);
				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getAddressLine(1);
				String country = addresses.get(0).getAddressLine(2);
				data += country + ", " + city + ", " + address;
			} catch (Exception e) {

			}
		}
		Log.e("Location", data);
		Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
		return data;
	}
}
