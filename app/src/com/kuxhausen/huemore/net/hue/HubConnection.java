package com.kuxhausen.huemore.net.hue;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import alt.android.os.CountDownTimer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.net.NetworkBulb.ConnectivityState;
import com.kuxhausen.huemore.network.BulbAttributesSuccessListener.OnBulbAttributesReturnedListener;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.network.ConnectionMonitor;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetConnectionColumns;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.api.Bulb;
import com.kuxhausen.huemore.state.api.BulbAttributes;

public class HubConnection implements Connection, OnBulbAttributesReturnedListener, ConnectionMonitor, OnBulbListReturnedListener{

	private static final String[] columns = {NetConnectionColumns._ID, NetConnectionColumns.TYPE_COLUMN, NetConnectionColumns.NAME_COLUMN, NetConnectionColumns.DEVICE_ID_COLUMN, NetConnectionColumns.JSON_COLUMN};
	private static final String[] bulbColumns = {NetBulbColumns._ID, NetBulbColumns.CONNECTION_DEVICE_ID_COLUMN, NetBulbColumns.TYPE_COLUMN, NetBulbColumns.NAME_COLUMN, NetBulbColumns.DEVICE_ID_COLUMN, NetBulbColumns.JSON_COLUMN};
	private static final Integer type = NetBulbColumns.NetBulbType.PHILIPS_HUE;
	private static final Gson gson = new Gson();
	
	private Long mBaseId;
	private String mName, mDeviceId;
	private HubData mData;
	private Context mContext;
	private LinkedHashSet<HueBulb> mChangedQueue;
	
	private ArrayList<HueBulb> mBulbList;
	
	public HubConnection(Context c, Long baseId, String name, String deviceId, HubData data, DeviceManager dm){
		mContext = c;
		mBaseId = baseId;
		mName = name;
		mDeviceId = deviceId;
		mData = data;
		
		mBulbList = new ArrayList<HueBulb>();
		mChangedQueue = new LinkedHashSet<HueBulb>();
		
		String[] selectionArgs = {""+NetBulbColumns.NetBulbType.PHILIPS_HUE};//, mDeviceId};
		//TODO fix selection once hub id fixed
		Cursor cursor = c.getContentResolver().query(NetBulbColumns.URI, bulbColumns, null,null,null);///*NetBulbColumns.TYPE_COLUMN + " = ?"/* AND "+NetBulbColumns.CONNECTION_DEVICE_ID_COLUMN + " = ?"*/, selectionArgs, null);
		cursor.moveToPosition(-1);// not the same as move to first!
		while (cursor.moveToNext()) {
			Long bulbBaseId = cursor.getLong(0);
			String bulbName = cursor.getString(3);
			String bulbDeviceId = cursor.getString(4);
			HueBulbData bulbData = gson.fromJson(cursor.getString(5), HueBulbData.class);
			mBulbList.add(new HueBulb(c,bulbBaseId,bulbName,bulbDeviceId,bulbData, this));
		}
		
		
		//junk?
		mDeviceManager = dm;
		volleyRQ = Volley.newRequestQueue(mContext);
		restartCountDownTimer();
	}
	
	public void saveConnection(){
		// TODO Auto-generated method stub
	}	
	
	@Override
	public void initializeConnection(Context c) {
		
		NetworkMethods.PreformGetBulbList(mContext, getRequestQueue(), this, this);
	}
	
	@Override
	public void onDestroy() {
		if(countDownTimer!=null)
			countDownTimer.cancel();
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		volleyRQ.cancelAll(InternalArguments.PERMANENT_NETWORK_REQUEST);
	}
	
	public static ArrayList<HubConnection> loadHubConnections(Context c, DeviceManager dm){
		ArrayList<HubConnection> hubs = new ArrayList<HubConnection>();
		
		String[] selectionArgs = {""+NetBulbColumns.NetBulbType.PHILIPS_HUE};
		Cursor cursor = c.getContentResolver().query(NetConnectionColumns.URI, columns, NetConnectionColumns.TYPE_COLUMN + " = ?", selectionArgs, null);
		cursor.moveToPosition(-1);// not the same as move to first!
		while (cursor.moveToNext()) {
			Long baseId = cursor.getLong(0);
			String name = cursor.getString(2);
			String deviceId = cursor.getString(3);
			HubData data = gson.fromJson(cursor.getString(4), HubData.class);
			hubs.add(new HubConnection(c,baseId,name,deviceId,data, dm));
		}
		
		//initialize all connections
		for(HubConnection h : hubs)
			h.initializeConnection(c);
		
		return hubs;
	}
	

	@Override
	public ArrayList<NetworkBulb> getBulbs() {
		ArrayList<NetworkBulb> result = new ArrayList<NetworkBulb>(mBulbList.size());
		result.addAll(mBulbList);
		return result;
	}
	

	@Override
	public void setHubConnectionState(boolean connected){
		mDeviceManager.onConnectionChanged();
		if(!connected){
			//TODO rate limit
			NetworkMethods.PreformGetBulbList(mContext, getRequestQueue(), this, null);
		}
	}
	
	protected LinkedHashSet<HueBulb> getChangedQueue(){
		return mChangedQueue;
	}
	
	private DeviceManager mDeviceManager;
	private RequestQueue volleyRQ;
	private static CountDownTimer countDownTimer;
	private final static int TRANSMITS_PER_SECOND = 10;
	public enum KnownState {Unknown, ToSend, Getting, Synched};	
	
	
	public RequestQueue getRequestQueue() {
		return volleyRQ;
	}
	
	public synchronized void onGroupSelected(Group selectedGroup, Integer optionalBri){
//		
//		groupIsAlerting = false;
//		groupIsColorLooping = false;
//		maxBrightness = null;
//		bulbBri = new int[selectedGroup.groupAsLegacyArray.length];
//		bulbRelBri = new int[selectedGroup.groupAsLegacyArray.length];
//		bulbKnown = new KnownState[selectedGroup.groupAsLegacyArray.length];
//		for(int i = 0; i < bulbRelBri.length; i++){
//			bulbRelBri[i] = MAX_REL_BRI;
//			bulbKnown[i] = KnownState.Unknown;
//		}
//		
//		this.groupName = groupName;
//		
//		if(optionalBri==null){
//			for(int i = 0; i< selectedGroup.groupAsLegacyArray.length; i++){
//				bulbKnown[i] = KnownState.Getting;
//				NetworkMethods.PreformGetBulbAttributes(mContext, getRequestQueue(), this, this, selectedGroup.groupAsLegacyArray[i]);
//			}
//		} else {
//			maxBrightness = optionalBri;
//			for(int i = 0; i< selectedGroup.groupAsLegacyArray.length; i++)
//				bulbKnown[i] = KnownState.ToSend;
//			mDeviceManager.onStateChanged();
//		}
	}
	
	/** doesn't notify listeners **/
	public synchronized void setBrightness(int brightness, Group selectedGroup){
		
//		maxBrightness = brightness;
//		if(selectedGroup.groupAsLegacyArray!=null){
//			for(int i = 0; i< selectedGroup.groupAsLegacyArray.length; i++){
//				bulbBri[i] = (maxBrightness * bulbRelBri[i])/MAX_REL_BRI; 
//				bulbKnown[i] = KnownState.ToSend;
//			}
//		}
	}
	
	public void onAttributesReturned(BulbAttributes result, int bulbNumber) {
//		//figure out which bulb in group (if that group is still selected)
//		int index = calculateBulbPositionInGroup(bulbNumber, mDeviceManager.getSelectedGroup());
//		//if group is still expected this, save 
//		if(index>-1 && bulbKnown[index]==KnownState.Getting){
//			bulbKnown[index] = KnownState.Synched;
//			bulbBri[index] = result.state.bri;
//			
//			//if all expected get brightnesses have returned, compute maxbri and notify listeners
//			boolean anyOutstandingGets = false;
//			for(KnownState ks : bulbKnown)
//				anyOutstandingGets |= (ks == KnownState.Getting);
//			if(!anyOutstandingGets){
//				//todo calc more intelligent bri when mood known
//				int briSum = 0;
//				for(int bri : bulbBri)
//					briSum +=bri;
//				maxBrightness = briSum/mDeviceManager.getSelectedGroup().groupAsLegacyArray.length;
//				
//				for(int i = 0; i< mDeviceManager.getSelectedGroup().groupAsLegacyArray.length; i++){
//					bulbBri[i]= maxBrightness;
//					bulbRelBri[i] = MAX_REL_BRI;
//				}
//				
//				mDeviceManager.onStateChanged();
//			}	
//		}	
	}
	
	
	public void restartCountDownTimer() {
		
		if (countDownTimer != null)
			countDownTimer.cancel();

		// runs at the rate to execute 15 op/sec
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE, (1000 / TRANSMITS_PER_SECOND)) {

			@Override
			public void onFinish() {
			}

			@Override
			public void onTick(long millisUntilFinished) {
				if(mChangedQueue.size()>0){
					HueBulb changedFirst = mChangedQueue.iterator().next();
					mChangedQueue.remove(changedFirst);
					NetworkMethods.PreformTransmitGroupMood(mContext, getRequestQueue(), HubConnection.this, changedFirst.getHubBulbNumber(), changedFirst.desiredState);
				}
			}
		};
		countDownTimer.start();
	}

	@Override
	public void onListReturned(Bulb[] result) {
		outer: for(int i = 0; i<result.length; i++){
			Bulb fromHue = result[i];
			
			for(int j = 0; j<mBulbList.size(); j++){	
				NetworkBulb fromMemory = mBulbList.get(j);
				
				//check to see if this bulb is already in our database
				if(fromMemory instanceof HueBulb
						&& ((HueBulb)fromMemory).getHubBulbNumber()==fromHue.number){
					if(!fromMemory.getName().equals(fromHue.name)){
						//same bulb but has been renamed by another device
						//must update our version
						
						ContentValues cv = new ContentValues();
						cv.put(NetBulbColumns.NAME_COLUMN, fromHue.name);
						String[] selectionArgs = {""+fromHue.number};
						mContext.getContentResolver().update(NetBulbColumns.URI, cv, NetBulbColumns.DEVICE_ID_COLUMN + " = ?", selectionArgs);
					}
					continue outer;
				}
			}
			//if we reach this point, must not already be in memory, so add to database and memory
			String bulbName = fromHue.name;
			String bulbDeviceId = fromHue.number+"";
			
			ContentValues cv = new ContentValues();
			cv.put(NetBulbColumns.NAME_COLUMN, bulbName);
			cv.put(NetBulbColumns.DEVICE_ID_COLUMN, bulbDeviceId);
			cv.put(NetBulbColumns.CONNECTION_DEVICE_ID_COLUMN, mDeviceId);
			cv.put(NetBulbColumns.JSON_COLUMN, gson.toJson(new HueBulbData()));
			cv.put(NetBulbColumns.TYPE_COLUMN, NetBulbColumns.NetBulbType.PHILIPS_HUE);
			String[] selectionArgs = {""+fromHue.number};
			long bulbBaseId = Long.parseLong(mContext.getContentResolver().insert(NetBulbColumns.URI, cv).getLastPathSegment());
			
			mBulbList.add(new HueBulb(mContext,bulbBaseId,bulbName,bulbDeviceId, new HueBulbData(), this));
			
		}
		
		
	}

	public ConnectivityState getConnectivityState() {
		return ConnectivityState.Unknown;
	}
}
