package org.codewiz.droid64.util;

import android.os.Bundle;
import android.os.ResultReceiver;

public class IMMResult extends ResultReceiver {
    public int result = -1;
    public IMMResult() {
        super(null);
    }

    @Override
    public void onReceiveResult(int r, Bundle data) {
        result = r;
    }

    // poll result value for up to 500 milliseconds
    public int getResult() {
        try {
            int sleep = 0;
            while (result == -1 && sleep < 500) {
                Thread.sleep(100);
                sleep += 100;
            }
        } catch (InterruptedException e) {
            ;
        }
        return result;
    }
}