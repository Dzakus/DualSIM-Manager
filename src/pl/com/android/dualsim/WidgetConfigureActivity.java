package pl.com.android.dualsim;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * The configuration screen for the {@link WidgetProvider WidgetOneSim} AppWidget.
 */
public class WidgetConfigureActivity extends ListActivity {
	private final String TAG = this.getClass().getSimpleName();
	int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	public EditText mWidgetText;
	private static final String PREFS_NAME = "com.example.dualsim.Widget";
	private static final String PREF_PREFIX_KEY_TITLE = "widget_title_";
	private static final String PREF_PREFIX_KEY_SIMID = "widget_simid_";

	public WidgetConfigureActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setResult(RESULT_CANCELED);

		setContentView(R.layout.widget_configure);
		mWidgetText = (EditText) findViewById(R.id.widget_text);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}
		
		String widgetText = loadTitlePref(this, mWidgetId);
		mWidgetText.setText(widgetText);

		//TODO Pobieranie informacji z telefonu. Maybe w³asny adapter
		String[] data = new String[] { "Karta pierwsza", "Karta druga" };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, data);

		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		final Context context = WidgetConfigureActivity.this;
		int simId = position + 1; 
		String widgetText = mWidgetText.getText().toString();
		saveTitlePref(context, mWidgetId, widgetText);
		saveSimIdPref(context, mWidgetId, simId);

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		WidgetProvider.updateAppWidget(context, appWidgetManager, mWidgetId);

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	// Write the prefix to the SharedPreferences object for this widget

	static void saveSimIdPref(Context context, int appWidgetId, int simid) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, Context.MODE_MULTI_PROCESS).edit();
		prefs.putInt(PREF_PREFIX_KEY_SIMID + appWidgetId, simid);
		prefs.apply();
		prefs.commit();
	}

	static int loadSimIdPref(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_MULTI_PROCESS);
		int value = prefs.getInt(PREF_PREFIX_KEY_SIMID + appWidgetId, -1);
		if(value == -1) {
			value = prefs.getInt(PREF_PREFIX_KEY_SIMID + appWidgetId, -1);
		}
		if(value == -1) {
			value = prefs.getInt(PREF_PREFIX_KEY_SIMID + appWidgetId, -1);
		}
		if(value == -1) {
			value = prefs.getInt(PREF_PREFIX_KEY_SIMID + appWidgetId, 1);
		}
		
		return value;
	}

	static void deleteSimIdPref(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, Context.MODE_MULTI_PROCESS).edit();
		prefs.remove(PREF_PREFIX_KEY_SIMID + appWidgetId);
		prefs.commit();
	}

	static void saveTitlePref(Context context, int appWidgetId, String text) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, Context.MODE_MULTI_PROCESS).edit();
		prefs.putString(PREF_PREFIX_KEY_TITLE + appWidgetId, text);
		prefs.commit();
	}

	static String loadTitlePref(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String titleValue = prefs.getString(
				PREF_PREFIX_KEY_TITLE + appWidgetId, null);
		if (titleValue != null) {
			return titleValue;
		} else {
			return context.getResources().getString(R.string.default_title);
		}
	}

	static void deleteTitlePref(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, 0).edit();
		prefs.remove(PREF_PREFIX_KEY_TITLE + appWidgetId);
		prefs.commit();
	}
}
