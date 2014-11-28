package com.caller.info;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {
	private static boolean incomingCall = false;
	private static WindowManager windowManager;
	private static ViewGroup windowLayout;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
			String phoneState = intent
					.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				//
				/*
				 * try { //√р€зноватый хак, рекомендуемый многими примерами в
				 * сети, но не об€зательный Thread.sleep(1000); } catch
				 * (InterruptedException ie) { //ну и ладно }
				 */
				String phoneNumber = intent
						.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				incomingCall = true;
				Log.debug("Show window: " + phoneNumber);
				showWindow(context, phoneNumber);

			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				// “елефон находитс€ в режиме
				// звонка (набор номера / разговор) -
				// закрываем окно, что бы не мешать
				if (incomingCall) {
					Log.debug("Close window.");
					closeWindow();
					incomingCall = false;
				}
			} else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				// “елефон находитс€ в ждущем
				// режиме - это событие наступает по
				// окончанию разговора
				// или в ситуации
				// "отказалс€ поднимать трубку и сбросил звонок"
				if (incomingCall) {
					Log.debug("Close window.");
					closeWindow();
					incomingCall = false;
				}
			}
		}
	}

	Context mContext;

	private void showWindow(Context context, String phone) {
		mContext = context;
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.TOP;

		windowLayout = (ViewGroup) layoutInflater.inflate(R.layout.info, null);

		TextView textViewNumber = (TextView) windowLayout
				.findViewById(R.id.textViewNumber);
		Button buttonClose = (Button) windowLayout
				.findViewById(R.id.buttonClose);
		textViewNumber.setText(phone);
		buttonClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeWindow();
			}
		});

		Button answerButton = (Button) windowLayout
				.findViewById(R.id.buttonAnswer);
		answerButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				Toast.makeText(mContext, "call", Toast.LENGTH_LONG).show();
				Intent answer = new Intent(Intent.ACTION_MEDIA_BUTTON);
				answer.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
						KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
				mContext.sendOrderedBroadcast(answer, null);
				closeWindow();

			}
		});

		Button rejectButton = (Button) windowLayout
				.findViewById(R.id.buttonDown);
		rejectButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {				
				TelephonyManager telephonyManager = (TelephonyManager)mContext.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
				Class clazz;
				try {
					clazz = Class.forName(telephonyManager.getClass().getName());
					Method method = clazz.getDeclaredMethod("getITelephony");
					method.setAccessible(true);
					ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
					telephonyService.endCall();

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			/*	Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
				buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
						KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
				mContext.getApplicationContext().sendOrderedBroadcast(
						buttonDown, "android.permission.CALL_PRIVILEGED"); // closeWindow();
*/
			}
		});

		windowManager.addView(windowLayout, params);
	}

	private void closeWindow() {
		if (windowLayout != null) {
			windowManager.removeView(windowLayout);
			windowLayout = null;
		}
	}

	public static void disconnectPhoneItelephony(Context context) {
		ITelephony telephonyService;
		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class c = Class.forName(telephony.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(telephony);

			telephonyService.endCall();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public void disconnectCall() {
		try {

			String serviceManagerName = "android.os.ServiceManager";
			String serviceManagerNativeName = "android.os.ServiceManagerNative";
			String telephonyName = "com.android.internal.telephony.ITelephony";
			Class<?> telephonyClass;
			Class<?> telephonyStubClass;
			Class<?> serviceManagerClass;
			Class<?> serviceManagerNativeClass;
			Method telephonyEndCall;
			Object telephonyObject;
			Object serviceManagerObject;
			telephonyClass = Class.forName(telephonyName);
			telephonyStubClass = telephonyClass.getClasses()[0];
			serviceManagerClass = Class.forName(serviceManagerName);
			serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
			Method getService = // getDefaults[29];
			serviceManagerClass.getMethod("getService", String.class);
			Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
					"asInterface", IBinder.class);
			Binder tmpBinder = new Binder();
			tmpBinder.attachInterface(null, "fake");
			serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
			IBinder retbinder = (IBinder) getService.invoke(
					serviceManagerObject, "phone");
			Method serviceMethod = telephonyStubClass.getMethod("asInterface",
					IBinder.class);
			telephonyObject = serviceMethod.invoke(null, retbinder);
			telephonyEndCall = telephonyClass.getMethod("endCall");
			telephonyEndCall.invoke(telephonyObject);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}