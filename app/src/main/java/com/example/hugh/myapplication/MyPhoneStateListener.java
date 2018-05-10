package com.example.hugh.myapplication;

import android.app.Activity;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hugh on 2018/5/10.
 */

public class MyPhoneStateListener extends PhoneStateListener {
    protected List<MyPhoneStateListenerListener> listeners = new ArrayList<>();
    private Object lock = new Object();
    public static int sMark = -1;
    private TelephonyManager tel;
    //    中国移动的是 46000
    //    中国联通的是 46001
    //    中国电信的是 46003
    private String STRNetworkOperator[] = { "46000", "46001", "46003" };
    private boolean is3Ghave = false;
    public static int sPosition;
    int signal;
    public MyPhoneStateListener(Activity activity) {
        tel = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
    }

    private SignalStrength signalStrength;
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        String signalInfo = signalStrength.toString();
        String[] params = signalInfo.split(" ");

        if(sMark <0)
        {
            getmark();
        }
        if (sMark == 0) {
            if(tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                //4G网络 最佳范围   >-90dBm 越大越好
                signal = Integer.parseInt(params[11]);
            }else{
                signal = signalStrength.getGsmSignalStrength();
            }
        } else if (sMark == 1) {
            signal = signalStrength.getCdmaDbm();
        } else if (sMark == 2) {
            signal = signalStrength.getEvdoDbm();
        }else {
            sPosition = 0;
        }
        notifyStateToAll();
    }


    public void addListener(MyPhoneStateListener.MyPhoneStateListenerListener l) {
        synchronized(lock) {
            listeners.add(l);
        }
    }

    public void removeListeners() {
        synchronized(lock) {
            listeners.clear();
        }
    }

    private void notifyStateToAll() {
        synchronized(lock) {
            for (MyPhoneStateListener.MyPhoneStateListenerListener listener : listeners) {
                notifyState(listener);
            }

        }
    }
    private void notifyState(MyPhoneStateListener.MyPhoneStateListenerListener listener) {
        if (listener!=null){
            listener.onSignalStrengthsChanged(sPosition);
        }

    }

    public interface MyPhoneStateListenerListener {
        public void onSignalStrengthsChanged(int singnaStrength);
    }

    private void getmark()//得到当前电话卡的归属运营商
    {
        String strNetworkOperator = tel.getNetworkOperator();
        if (strNetworkOperator != null) {
            for (int i = 0; i < 3; i++) {
                if (strNetworkOperator.equals(STRNetworkOperator[i])) {
                    sMark = i;
                    break;
                }
            }
        } else {
            sMark = -1;
        }
    }
}
