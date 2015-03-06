package dev.blacksheep.sti_antitheft.services;

import dev.blacksheep.sti_antitheft.PopupMessageActivity;
import dev.blacksheep.sti_antitheft.classes.SMSUtils;
import dev.blacksheep.sti_antitheft.classes.Utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class BootCompleteService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager phoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = phoneManager.getLine1Number();
		//Toast.makeText(context, phoneNumber, Toast.LENGTH_SHORT).show();
		if (new Utils(context).getPopupOnBoot()) {
			context.startActivity(new Intent(context, PopupMessageActivity.class).putExtra("message", new Utils(context).getPopupMessageOnBoot()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		if (!new Utils(context).compareSimInfo()) {
			SMSUtils sms = new SMSUtils(context);
			SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(context);
			String pref_sim_change_number = myPreference.getString("pref_sim_change_number", "");
			if (phoneNumber == null) {
				phoneNumber = "error";
			}
			if (pref_sim_change_number.length() > 0) {
				sms.sendSMS(pref_sim_change_number, "New SIM card insert. SIM Details: " + new Utils(context).fullSimInfo());
			}
			new Utils(context).setPopupOnBoot(true, "SIM Card Change Detected.");
			context.startActivity(new Intent(context, PopupMessageActivity.class).putExtra("message", "SIM Card Change Detected.").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}
}