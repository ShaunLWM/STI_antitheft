package dev.blacksheep.sti_antitheft.classes;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSUtils {
	/*
	 * Available Uri string String strUriInbox =
	 * "content://sms/inbox";//SMS_INBOX:1 String strUriFailed =
	 * "content://sms/failed";//SMS_FAILED:2 String strUriQueued =
	 * "content://sms/queued";//SMS_QUEUED:3 String strUriSent =
	 * "content://sms/sent";//SMS_SENT:4 String strUriDraft =
	 * "content://sms/draft";//SMS_DRAFT:5 String strUriOutbox =
	 * "content://sms/outbox";//SMS_OUTBOX:6 String strUriUndelivered =
	 * "content://sms/undelivered";//SMS_UNDELIVERED String strUriAll =
	 * "content://sms/";//SMS_ALL String strUriConversations =
	 * "content://sms/conversations";//you can delete one conversation by
	 * thread_id String strUriAll = "content://sms"//you can delete one message
	 * by _id
	 */
	private static final String TAG = "SMSUtils";
	Context con;

	public SMSUtils(Context con) {
		this.con = con;

	}

	public boolean deleteSMS() {
		Uri inboxUri = Uri.parse("content://sms/");
		boolean pass = false;
		try {
			Cursor c = con.getContentResolver().query(inboxUri, null, null, null, null);
			while (c.moveToNext()) {
				try {
					String pid = c.getString(0);
					String uri = "content://sms/" + pid;
					con.getContentResolver().delete(Uri.parse(uri), null, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			pass = true;
			c.close();
		} catch (Exception e) {
			pass = false;
			e.printStackTrace();
		}
		return pass;
	}

	public boolean deleteSMS(String number) {
		Log.e(TAG, "Deleting SMS with " + number);
		boolean pass = false;
		try {
			Uri uriSms = Uri.parse("content://sms/");
			Cursor c = con.getContentResolver().query(uriSms, new String[] { "_id", "thread_id", "address" }, null, null, null);
			Log.e(TAG, "Cursor");
			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(0);
					String address = c.getString(2);

					if (address.equals(number)) {
						con.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
						pass = true;
					}
				} while (c.moveToNext());
			}
			c.close();
		} catch (Exception e) {
			pass = false;
			Log.e(TAG, e.getMessage());
		}
		return pass;
	}

	public ArrayList<String> getSmsDetails(String type, boolean unreadOnly, int amount) {
		ArrayList<String> allSMS = new ArrayList<String>();
		String str = "";
		if (unreadOnly) {
			str = "read = 0";
		}
		try {
			Uri uriSms = Uri.parse("content://sms/" + type);
			Cursor c = con.getContentResolver().query(uriSms, new String[] { "_id", "address", "body" }, str, null, null);
			int count = 0;
			if (c != null && c.moveToFirst()) {
				do {
					String address = c.getString(1);
					String body = c.getString(2);
					allSMS.add(address + " : " + body + "\n");
					count++;
					if (count == amount) {
						break;
					}
				} while (c.moveToNext());
			}
			c.close();
		} catch (Exception e) {
		}

		return allSMS;
	}

	public void sendSMS(String address, String content) {
		SmsManager sms = SmsManager.getDefault();
		if (content.length() > 160) {
			ArrayList<String> parts = sms.divideMessage(content);
			sms.sendMultipartTextMessage(address, null, parts, null, null);
		} else {
			sms.sendTextMessage(address, null, content, null, null);
		}

	}
}
