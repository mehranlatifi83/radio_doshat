package ir.codelighthouse.vpn;

import android.os.Handler;
import android.view.View;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import ir.codelighthouse.radio_doshat.MainActivity;
import ir.codelighthouse.radio_doshat.R;
import ir.codelighthouse.radio_doshat.player.PlayerManager;
import ir.codelighthouse.ui.UIUpdater;

public class VpnStatusChecker {
	public static void checkForVpnIsConected(MainActivity mainActivity) {
		final Handler handler = new Handler();
		Runnable runnable = () -> {
			if (mainActivity.isAppActive) {
				fetchIsUserInIran(mainActivity);
			}
			handler.postDelayed(() -> checkForVpnIsConected(mainActivity), 15000);
		};
		handler.post(runnable);
	}

	private static void fetchIsUserInIran(MainActivity mainActivity) {
		new Thread(() -> {
			try {
				// ارسال درخواست به ip-api.com و دریافت پاسخ به صورت متنی
				String json = Jsoup.connect(mainActivity.getString(R.string.url_get_country)).ignoreContentType(true).execute().body();
				// تبدیل رشته پاسخ به یک شیء JSONObject برای تجزیه آسان
				JSONObject jsonObject = new JSONObject(json);

				// استخراج مقدار کشور از JSON
				String country = jsonObject.getString("country");
				// بررسی اینکه آیا کشور ایران است یا خیر
				if (mainActivity.isVpnConected == (country.equals("Iran"))) {
					if (!country.equals("Iran")) {
						mainActivity.runOnUiThread(() -> mainActivity.playButton.setVisibility(View.GONE));
						mainActivity.runOnUiThread(() -> mainActivity.player.stop());
						UIUpdater.showErrorMessage(mainActivity, mainActivity.getString(R.string.vpn_error));
						mainActivity.isVpnConected = true;
					} else {
						mainActivity.runOnUiThread(() -> mainActivity.statusBar.setText(""));
						mainActivity.runOnUiThread(() -> PlayerManager.playUrl(mainActivity));
						mainActivity.runOnUiThread(() -> mainActivity.playButton.setVisibility(View.VISIBLE));
						mainActivity.isVpnConected = false;
					}
				}
			} catch (Exception e) {
			}
		}).start();
	}
}
