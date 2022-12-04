package id.indoweb.elazis.presensi.utils;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.instacart.library.truetime.TrueTimeRx;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Eko S. Purnomo on 12/17/2019.
 * Email me at ekosetyopurnomo@gmail.com
 * Visit me on ekosp.com
 */
public class App extends MultiDexApplication {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        initRxTrueTime();
    }

    /**
     * Initialize the TrueTime using RxJava.
     */
    private void initRxTrueTime() {
        DisposableSingleObserver<Date> disposable = TrueTimeRx.build()
                .withConnectionTimeout(31_428)
                .withRetryCount(100)
                .withSharedPreferencesCache(this)
                .withLoggingEnabled(true)
                .initializeRx("0.id.pool.ntp.org")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Date>() {
                    @Override
                    public void onSuccess(Date date) {
                        Log.d(TAG, "Success initialized TrueTime :" + date.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
                    }
                });
    }
}