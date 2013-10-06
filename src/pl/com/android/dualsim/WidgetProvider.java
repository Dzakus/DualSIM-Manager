package pl.com.android.dualsim;

import java.util.Arrays;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality. App Widget Configuration
 * implemented in {@link WidgetConfigureActivity WidgetConfigureActivity}
 */
public class WidgetProvider extends AppWidgetProvider {
	private final String TAG = this.getClass().getSimpleName();
	private static final String BTN_CLICKED = "Button_Clicked";
	private static SimManagement mSimManagement;

	private enum WidgetState {
		OFF(R.drawable.ic_widget_off, R.drawable.ind_bar_off), ON(
				R.drawable.ic_widget_on, R.drawable.ind_bar_on);

		/**
		 * The drawable resources associated with this widget state.
		 */
		private final int mDrawImgRes;
		private final int mDrawIndRes;

		private WidgetState(int drawImgRes, int drawIndRes) {
			mDrawImgRes = drawImgRes;
			mDrawIndRes = drawIndRes;
		}

		public int getImgDrawable() {
			return mDrawImgRes;
		}

		public int getIndDrawable() {
			return mDrawIndRes;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onReceive: action=" + intent.getAction());
		String action = intent.getAction();
		if (action.equals(BTN_CLICKED)) {

			int widgetId = intent.getIntExtra("widgetId", 0);
			int widgetSimId = intent.getIntExtra("simID", 0);

			if (BuildConfig.DEBUG)
				Log.i(TAG, "Btn_clicked: widgetId=" + widgetId
						+ " widgetSimId=" + widgetSimId);

			mSimManagement = SimManagement.getInstance(context);
			mSimManagement.switchState(widgetSimId);
		} else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)
				|| action.equals(SimManagement.DUAL_SIM_MODE))
			updateAllStates(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			WidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
			WidgetConfigureActivity.deleteSimIdPref(context, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}

	public void updateAllStates(Context context) {
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
				context, this.getClass()));
		for (int widgetId : widgetIds)
			updateAppWidget(context, appWidgetManager, widgetId);
	}

	static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int widgetId) {

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);

		String widgetText = WidgetConfigureActivity.loadTitlePref(
				context, widgetId);
		int widgetSimId = WidgetConfigureActivity.loadSimIdPref(context,
				widgetId);
		if (mSimManagement == null)
			mSimManagement = SimManagement.getInstance(context);

		boolean on = mSimManagement.getState(widgetSimId);

		if (on) {
			views.setImageViewResource(R.id.img_torch,
					WidgetState.ON.getImgDrawable());
			views.setImageViewResource(R.id.ind_torch,
					WidgetState.ON.getIndDrawable());
		} else {
			views.setImageViewResource(R.id.img_torch,
					WidgetState.OFF.getImgDrawable());
			views.setImageViewResource(R.id.ind_torch,
					WidgetState.OFF.getIndDrawable());
		}
		PendingIntent pi = getLaunchPendingIntent(context, widgetId,
				widgetSimId);
		views.setOnClickPendingIntent(R.id.btn, pi);

		views.setTextViewText(R.id.ind_text, widgetText);
		if (BuildConfig.DEBUG & false)
			Log.i("WidgetProvider", "updateAppWidget: widgetId=" + widgetId
					+ " widgetText=" + widgetText + " widgetSimId="
					+ widgetSimId);
		appWidgetManager.updateAppWidget(widgetId, views);
	}

	private static PendingIntent getLaunchPendingIntent(Context context,
			int widgetId, int simId) {
		if (BuildConfig.DEBUG & false)
			Log.i("WidgetProvider", "getLaunchPendingIntent: widgetId="
					+ widgetId + " simId=" + simId);
		Intent launchIntent = new Intent();
		launchIntent.setClass(context, WidgetProvider.class);
		launchIntent.setAction(BTN_CLICKED);
		launchIntent.putExtra("widgetId", widgetId);
		launchIntent.putExtra("simID", simId);

		PendingIntent pi = PendingIntent.getBroadcast(context, widgetId,
				launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pi;
	}
}
