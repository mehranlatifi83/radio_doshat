package ir.codelighthouse.ui;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import ir.codelighthouse.radio_doshat.MainActivity;
import ir.codelighthouse.radio_doshat.R;
import ir.codelighthouse.radio_doshat.player.PlayerManager;

public class UIUpdater {
	public static void showErrorMessage(MainActivity mainActivity, String message) {
		mainActivity.runOnUiThread(() -> {
			mainActivity.statusBar.setText(message);
			Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show();
		});
	}

	public static void showToast(MainActivity mainActivity, String message) {
		mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show());
	}

	public static void initializeUIComponents(MainActivity mainActivity) {
		mainActivity.playButton = mainActivity.findViewById(R.id.playButton);
		mainActivity.playButton.setVisibility(View.GONE);
		mainActivity.statusBar = mainActivity.findViewById(R.id.statusBar);
		mainActivity.playButton.setOnClickListener(v -> PlayerManager.togglePlayback(mainActivity));
	}

	public static void openLink(MainActivity mainActivity, String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		mainActivity.startActivity(browserIntent);
	}

	public static void navigateToBatteryOptimizationSettings(MainActivity mainActivity) {
		Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
				.setData(Uri.parse("package:" + mainActivity.getPackageName()));
		mainActivity.startActivity(intent);
	}

	public static void showBatteryOptimizationDialog(MainActivity mainActivity) {
		new AlertDialog.Builder(mainActivity)
				.setTitle(mainActivity.getString(R.string.battery_optimization_title))
				.setMessage(mainActivity.getString(R.string.battery_optimization_message))
				.setPositiveButton(mainActivity.getString(R.string.ok_button), (dialog, which) -> navigateToBatteryOptimizationSettings(mainActivity))
				.setNegativeButton(mainActivity.getString(R.string.cancel_button), null)
				.show();
	}
}
