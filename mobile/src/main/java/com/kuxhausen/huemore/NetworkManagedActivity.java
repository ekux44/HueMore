package com.kuxhausen.huemore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;

import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.net.ConnectivityService.LocalBinder;
import com.kuxhausen.huemore.net.DeviceManager.OnStateChangedListener;
import com.kuxhausen.huemore.net.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.state.Group;

import java.util.ArrayList;

public abstract class NetworkManagedActivity extends ActionBarActivity implements
                                                                       OnConnectionStatusChangedListener,
                                                                       OnStateChangedListener,
                                                                       OnServiceConnectedListener {

  public void setGroup(Group g) {
    if (mBound) {
      mService.getDeviceManager().onGroupSelected(g, null);
    }
  }

  private ConnectivityService mService = new ConnectivityService();
  private boolean mBound = false;
  private ArrayList<OnServiceConnectedListener> serviceListeners =
      new ArrayList<OnServiceConnectedListener>();

  // register for a one time on service connected message
  public void registerOnServiceConnectedListener(OnServiceConnectedListener l) {
    if (mBound) {
      l.onServiceConnected();
    } else {
      serviceListeners.add(l);
    }
  }

  public ConnectivityService getService() {
    if (mBound) {
      return mService;
    } else {
      return null;
    }
  }

  public boolean boundToService() {
    return mBound;
  }

  @Override
  public void onServiceConnected() {
    // override in subclass if needed
  }

  @Override
  public void onConnectionStatusChanged() {
    // override in subclass if needed
  }

  @Override
  public void onStateChanged() {
    // override in subclass if needed
  }

  @Override
  public void onResume() {
    super.onResume();
    // Bind to LocalService
    Intent intent = new Intent(this, ConnectivityService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    this.registerOnServiceConnectedListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();

    // Unbind from the service
    if (mBound) {
      mService.getDeviceManager().removeOnConnectionStatusChangedListener(this);
      mService.getDeviceManager().removeStateListener(NetworkManagedActivity.this);
      mService.getDeviceManager().removeBrightnessListener(this);
      unbindService(mConnection);
      mBound = false;
    }
  }

  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      LocalBinder binder = (LocalBinder) service;
      mService = binder.getService();
      mBound = true;
      mService.getDeviceManager().addOnConnectionStatusChangedListener(NetworkManagedActivity.this);
      mService.getDeviceManager().registerStateListener(NetworkManagedActivity.this);
      mService.getDeviceManager().registerBrightnessListener(NetworkManagedActivity.this);

      for (OnServiceConnectedListener l : serviceListeners) {
        l.onServiceConnected();
      }
      serviceListeners.clear();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };
}
