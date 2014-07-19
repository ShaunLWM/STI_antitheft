package dev.blacksheep.sti_antitheft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (new Utils(context).getPopupOnBoot()) {
			context.startActivity(new Intent(context, PopupMessageActivity.class).putExtra("message", new Utils(context).getPopupMessageOnBoot()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}
}