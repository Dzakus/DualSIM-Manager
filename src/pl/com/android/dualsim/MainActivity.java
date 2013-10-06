package pl.com.android.dualsim;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements OnCheckedChangeListener {

	private static final int ColorGreen = Color.parseColor("#669900");
	private static final int ColorRed = Color.parseColor("#CC0000");
	private final String TAG = this.getClass().getSimpleName();
	/*
	 * //private List<Telephony.SIMInfo> mSiminfoList = new ArrayList(); private
	 * List<Object> mSiminfoList = new ArrayList(); private Object
	 * mTelephonyManagerEx; private BroadcastReceiver mSimReceiver = new
	 * BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) { String
	 * action = intent.getAction(); // TODO Auto-generated method stub if
	 * ((action.equals("android.intent.action.SIM_INFO_UPDATE")) || (action
	 * .equals("android.intent.action.SIM_SETTING_INFO_CHANGED")) ||
	 * (action.equals("android.intent.action.SIM_NAME_UPDATE")) || (action
	 * .equals("android.intent.action.SIM_INDICATOR_STATE_CHANGED")) ||
	 * (action.equals("android.intent.action.AIRPLANE_MODE"))) { // Update();
	 * 
	 * } } }; private IntentFilter mIntentFilter; private TelephonyManager
	 * mTelephonyManager; private TextView mSimMode;
	 */
	private SimManagement mSimManagement;
	private Switch mSwitchSim1;
	private Switch mSwitchSim2;
	private TextView mWarning;
	private TextView mSimMode;
	private TextView mSteteSim1;
	private TextView mSteteSim2;
	private LinearLayout mDebugView;
	private CheckBox mTest;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SimManagement.DUAL_SIM_MODE)
					|| action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
				UpdateView();
			}
		}
	};
	private IntentFilter mIntentFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSimManagement = SimManagement.getInstance(this);
		setContentView(R.layout.activity_main);

		mWarning = (TextView) findViewById(R.id.unsupported);

		mSwitchSim1 = (Switch) findViewById(R.id.switch1);
		mSwitchSim2 = (Switch) findViewById(R.id.switch2);
		mSwitchSim1.setOnCheckedChangeListener(this);
		mSwitchSim2.setOnCheckedChangeListener(this);
		mTest = (CheckBox) findViewById(R.id.test1);
		mTest.setOnCheckedChangeListener(this);

		mIntentFilter = new IntentFilter(SimManagement.DUAL_SIM_MODE);
		mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

		if (BuildConfig.DEBUG) {
			mSimMode = (TextView) findViewById(R.id.simMode);
			mSteteSim1 = (TextView) findViewById(R.id.stateSim1);
			mSteteSim2 = (TextView) findViewById(R.id.stateSim2);
			mDebugView = (LinearLayout) findViewById(R.id.debugview);
			mDebugView.setVisibility(View.VISIBLE);
		}
		// initIntentFilter();

		if (!mSimManagement.isSupported()) {
			mWarning.setVisibility(View.VISIBLE);
			mSwitchSim1.setVisibility(View.INVISIBLE);
			mSwitchSim2.setVisibility(View.INVISIBLE);
		}
		DebugView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mIntentFilter);
		UpdateView();
		DebugView();

	}

	@Override
	protected void onPause() {
		super.onPause();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mTest) {
			mSimManagement.setTest(isChecked);
			Log.i(TAG, "buttonView == mTest : isChecked = " + isChecked);
			return;
		}

		int slot = (buttonView == mSwitchSim1 ? 1 : 2);
		Log.i(TAG, "onCheckedChanged: slot: " + slot + " state: " + isChecked);
		mSimManagement.changeState(slot, isChecked);
		DebugView();
	}

	private void DebugView() {
		if (BuildConfig.DEBUG) {
			mSimMode.setText("state:" + mSimManagement.getSimMode());
			int color;
			color = (mSimManagement.getState(1) ? ColorGreen : ColorRed);
			mSteteSim1.setBackgroundColor(color);
			color = (mSimManagement.getState(2) ? ColorGreen : ColorRed);
			mSteteSim2.setBackgroundColor(color);

		}
	}

	private void UpdateView() {
		mSwitchSim1.setChecked(mSimManagement.getState(1));
		mSwitchSim2.setChecked(mSimManagement.getState(2));

	}
	/*
	 * private void initIntentFilter() { mIntentFilter = new IntentFilter(
	 * "android.intent.action.SIM_INDICATOR_STATE_CHANGED");
	 * mIntentFilter.addAction("");
	 * 
	 * mIntentFilter.addAction("android.intent.action.SIM_INFO_UPDATE");
	 * mIntentFilter
	 * .addAction("android.intent.action.SIM_SETTING_INFO_CHANGED");
	 * mIntentFilter.addAction("android.intent.action.SIM_NAME_UPDATE");
	 * mIntentFilter
	 * .addAction("android.intent.action.SIM_INDICATOR_STATE_CHANGED");
	 * mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE"); }
	 * 
	 * 
	 * @SuppressWarnings({ "rawtypes", "unused" }) private void getSimInfo() {
	 * mSiminfoList.clear(); List simList; Method method_getInsertedSIMList;
	 * Field field_SimInfo_mSlot; Field field_SimInfo_mDisplayName; Field
	 * field_SimInfo_mNumber; Field field_SimInfo_mColor; Field
	 * field_SimInfo_mDispalyNumberFormat; Field field_SimInfo_mSimId; try {
	 * method_getInsertedSIMList = Class.forName(
	 * "android.provider.Telephony$SIMInfo").getMethod( "getInsertedSIMList",
	 * Class.forName("android.content.Context")); Class classSimInfo = Class
	 * .forName("android.provider.Telephony$SIMInfo"); field_SimInfo_mSlot =
	 * classSimInfo.getField("mSlot"); field_SimInfo_mDisplayName =
	 * classSimInfo.getField("mDisplayName"); field_SimInfo_mNumber =
	 * classSimInfo.getField("mNumber"); field_SimInfo_mColor =
	 * classSimInfo.getField("mColor");; field_SimInfo_mDispalyNumberFormat =
	 * classSimInfo.getField("mDispalyNumberFormat");; field_SimInfo_mSimId =
	 * classSimInfo.getField("mSimId");; simList = (List)
	 * method_getInsertedSIMList.invoke(null, this); } catch (Exception e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); return; }
	 * 
	 * // android.provider.Telephony$SIMInfo
	 * 
	 * if (simList.size() == 2) { Object simInfo1Obj = (Object) simList.get(0);
	 * int simInfo1Slot = GetFieldInt(field_SimInfo_mSlot, simInfo1Obj); Object
	 * simInfo2Obj = (Object) simList.get(1); int simInfo2Slot =
	 * GetFieldInt(field_SimInfo_mSlot, simInfo2Obj);
	 * 
	 * if (simInfo1Slot > simInfo2Slot) Collections.swap(simList, 0, 1);
	 * mIsSlot1Insert = true; mIsSlot2Insert = true; } else if (simList.size()
	 * == 1) { Object simInfo1Obj = mSiminfoList.get(0); int simInfo1Slot =
	 * GetFieldInt(field_SimInfo_mSlot, simInfo1Obj); if (simInfo1Slot == 0)
	 * mIsSlot1Insert = true; else mIsSlot2Insert = true; }
	 * 
	 * for (int j = 0; j < simList.size(); j++) { // Telephony.SIMInfo Object
	 * tempSiminfo; tempSiminfo = (Object) simList.get(j);
	 * GetFieldInt(field_SimInfo_mSlot, tempSiminfo);
	 * 
	 * Log.i("SimManagementSettings", "siminfo.mDisplayName = " +
	 * GetFieldInt(field_SimInfo_mDisplayName,tempSiminfo));
	 * Log.i("SimManagementSettings", "siminfo.mNumber = " +
	 * GetFieldInt(field_SimInfo_mNumber,tempSiminfo));
	 * Log.i("SimManagementSettings", "siminfo.mSlot = " +
	 * GetFieldInt(field_SimInfo_mSlot, tempSiminfo));
	 * Log.i("SimManagementSettings", "siminfo.mColor = "
	 * +GetFieldInt(field_SimInfo_mColor,tempSiminfo));
	 * Log.i("SimManagementSettings",
	 * "siminfo.mDispalyNumberFormat = "+GetFieldInt
	 * (field_SimInfo_mDispalyNumberFormat,tempSiminfo));
	 * Log.i("SimManagementSettings", "siminfo.mSimId = "
	 * +GetFieldInt(field_SimInfo_mSimId,tempSiminfo));
	 * 
	 * 
	 * mSiminfoList.add(tempSiminfo); } }
	 * 
	 * public int GetFieldInt(Field f, Object obj) { int value; try { value =
	 * f.getInt(obj); } catch (Exception e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); return 0; } return value; }
	 */
}
