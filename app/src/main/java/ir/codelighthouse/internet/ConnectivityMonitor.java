package ir.codelighthouse.internet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import ir.codelighthouse.radio_doshat.MainActivity;
import ir.codelighthouse.radio_doshat.R;
import ir.codelighthouse.radio_doshat.player.MediaManager;
import ir.codelighthouse.radio_doshat.player.PlayerManager;
import ir.codelighthouse.ui.UIUpdater;
import ir.codelighthouse.vpn.VpnStatusChecker;

public class ConnectivityMonitor {
	public static void setupNetworkChangeReceiver(final MainActivity mainActivity) {
		mainActivity.networkChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (checkInternetConnection(mainActivity)) {
					VpnStatusChecker.checkForVpnIsConected(mainActivity);
					if (!mainActivity.isVpnConected) {
						if (mainActivity.player != null && mainActivity.player.isPlaying()) {
							mainActivity.playButton.setText(mainActivity.getString(R.string.pause));
						} else {
							PlayerManager.initializePlayer(mainActivity);
							MediaManager.checkForSongChange(mainActivity);
							mainActivity.playButton.setVisibility(View.VISIBLE);
							mainActivity.statusBar.setText("");
						}
					}
				} else {
					mainActivity.playButton.setVisibility(View.GONE);
					UIUpdater.showErrorMessage(mainActivity, mainActivity.getString(R.string.internet_error));
					if (mainActivity.player != null && mainActivity.player.isPlaying()) {
						mainActivity.player.pause();
					}
				}
			}
		};
	}

	public static void registerNetworkChangeReceiver(MainActivity mainActivity) {
		mainActivity.registerReceiver(mainActivity.networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	public static void unregisterNetworkChangeReceiver(MainActivity mainActivity) {
		mainActivity.unregisterReceiver(mainActivity.networkChangeReceiver);
	}

	public static boolean checkInternetConnection(MainActivity mainActivity) {
		ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}
}
