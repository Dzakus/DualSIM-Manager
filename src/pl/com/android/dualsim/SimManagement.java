package pl.com.android.dualsim;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class SimManagement {
	private static SimManagement instance;
	private final String TAG = this.getClass().getSimpleName();

	public static final int ALL_RADIO_OFF = 0;
	public static final int ALL_RADIO_ON = 3;
	public static final int SIM_SLOT_1_RADIO_ON = 1;
	public static final int SIM_SLOT_2_RADIO_ON = 2;
	public static final String DUAL_SIM_MODE = "android.intent.action.DUAL_SIM_MODE";
	private int mSimMode = ALL_RADIO_ON;
	private boolean mSlot1State = false;
	private boolean mSlot2State = false;
	private Context mCtx;
	private boolean mTest = true;

	public SimManagement(Context context) {
		mCtx = context.getApplicationContext();
		loadState();
	}

	public static synchronized SimManagement getInstance(Context context) {
		if (instance == null) {
			instance = new SimManagement(context);
		}
		return instance;
	}

	public boolean getState(int slot) {

		switch (slot) {
		case 1:
			return mSlot1State;
		case 2:
			return mSlot2State;
		default:
			throw new IllegalArgumentException("slot must 1 or 2");
		}

	}

	private void loadState() {
		mSimMode = Settings.System.getInt(mCtx.getContentResolver(),
				"dual_sim_mode_setting", -1);

		mSlot1State = (mSimMode == SIM_SLOT_1_RADIO_ON)
				|| (mSimMode == ALL_RADIO_ON);
		mSlot2State = (mSimMode == SIM_SLOT_2_RADIO_ON)
				|| (mSimMode == ALL_RADIO_ON);

		if (BuildConfig.DEBUG)
			Log.i(TAG, "loadState: " + mSimMode + " slot1:" + mSlot1State
					+ " slot2:" + mSlot2State);

	}

	public int getSimMode() {
		return mSimMode;
	}

	private void saveState() {
		int simMode = ALL_RADIO_OFF;

		if (mSlot1State && mSlot2State)
			simMode = ALL_RADIO_ON;
		if (mSlot1State && !mSlot2State)
			simMode = SIM_SLOT_1_RADIO_ON;
		if (!mSlot1State && mSlot2State)
			simMode = SIM_SLOT_2_RADIO_ON;

		mSimMode = simMode;
		Settings.System.putInt(mCtx.getContentResolver(),
				"dual_sim_mode_setting", simMode);
		if(BuildConfig.DEBUG){
			Log.i(TAG,"saveState: simMode: " + simMode);
		}
	}

	public void changeState(int slot, boolean state) {
		switch (slot) {
		case 1:
			mSlot1State = state;
			break;
		case 2:
			mSlot2State = state;
			break;
		default:
			throw new IllegalArgumentException("slot must 1 or 2");
		}

		saveState();

		if (mTest) {
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", false);
			mCtx.sendBroadcast(intent);
		} else {
			Intent intent2 = new Intent("android.intent.action.DUAL_SIM_MODE");
			intent2.putExtra("mode", mSimMode);
			mCtx.sendBroadcast(intent2);
		}
	}

	public void switchState(int slot) {
		Log.i(TAG, "switchState: " + slot);
		switch (slot) {
		case 1:
			changeState(slot, !mSlot1State);
			break;
		case 2:
			changeState(slot, !mSlot2State);
			break;
		default:
			throw new IllegalArgumentException("slot must 1 or 2");
		}

	}

	public boolean isSupported() {
		if (BuildConfig.DEBUG)
			return true;
		try {
			Class.forName("com.mediatek.telephony.TelephonyManagerEx");

			/*
			 * Method method_TelephonyManagerEx_getDefault =
			 * class_TelephonyManagerEx .getMethod("getDefault", null);
			 * 
			 * mTelephonyManagerEx =
			 * method_TelephonyManagerEx_getDefault.invoke( null, null);
			 */
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean isTest() {
		return mTest;
	}

	public void setTest(boolean mTest) {
		this.mTest = mTest;
	}
}
