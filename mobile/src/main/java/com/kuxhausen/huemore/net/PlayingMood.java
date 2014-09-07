package com.kuxhausen.huemore.net;

import android.util.Log;
import android.util.Pair;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.QueueEvent;
import com.kuxhausen.huemore.timing.Conversions;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 * used to store activity data about an ongoing mood and format the data for consumption by
 * visualizations/notefications
 */
public class PlayingMood {


  private Mood mMood;
  private String mMoodName;
  private Group mGroup;
  private long mStartMiliTime;
  private long mLastTickedMiliTime;

  public PlayingMood(Mood m, String moodName, Group g, long startMiliTime) {
    if (m == null || g == null || startMiliTime < 1l) {
      throw new IllegalArgumentException();
    }

    mMood = m;
    if (moodName != null) {
      mMoodName = moodName;
    } else {
      mMoodName = "?";
    }
    mGroup = g;
    mStartMiliTime = startMiliTime;
    mLastTickedMiliTime = startMiliTime - 1;

    if (mMood.getTimeAddressingRepeatPolicy()) {
      throw new UnsupportedOperationException();
    }

  }

  public boolean hasFutureEvents(long nowMiliTime) {
    return false;
  }

  public long getNextEventInCurrentMillis(long nowMiliTime) {
    return -1;
  }

  public List<Pair<List<Long>, BulbState>> getEventsSinceThrough(long sinceMiliTime,
                                                                 long throughMiliTime) {
    return null;
  }

  public List<Pair<List<Long>, BulbState>> tick(long throughMiliTime) {
    long sinceTime = mLastTickedMiliTime;
    long throughTime = throughMiliTime;

    mLastTickedMiliTime = throughMiliTime;

    return getEventsSinceThrough(sinceTime, throughTime);
  }

  public String getMoodName() {
    return mMoodName;
  }

  public String getGroupName() {
    return mGroup.getName();
  }

  public String toString() {
    return getGroupName() + " \u2190 " + getMoodName();
  }

  public Group getGroup() {
    return mGroup;
  }

  public Mood getMood() {
    return mMood;
  }

  //TODO delete everything beneath this line


  // if the next even is happening in less than 1/2 seconds, stay awake for it
  private final static long IMMIMENT_EVENT_WAKE_THRESHOLD_IN_MILISEC = 1000l;

  private DeviceManager mDeviceManager;

  private BrightnessManager mBrightnessManager;

  private PriorityQueue<QueueEvent> queue = new PriorityQueue<QueueEvent>();
  private Long moodLoopIterationEndMiliTime = 0L;

  /**
   * @param systemElapsedRealtime usually SystemClock.elapsedRealtime();
   */
  public PlayingMood(DeviceManager dm, Group g, Mood m, String mName,
                     Long timeStartedInRealtimeElapsedMilis,
                     long systemElapsedRealtime) {
    assert g != null;

    mDeviceManager = dm;
    mGroup = g;
    mMood = m;
    if (mName != null) {
      mMoodName = mName;
    } else {
      mMoodName = "?";
    }

    mBrightnessManager = dm.obtainBrightnessManager(g);

    if (timeStartedInRealtimeElapsedMilis != null) {
      if (mMood.isInfiniteLooping()) {
        // if entire loops could have occurred since the timeStarm started, fast forward to the
        // currently ongoing loop
        while (timeStartedInRealtimeElapsedMilis < (systemElapsedRealtime + (
            mMood.loopIterationTimeLength * 100))) {
          timeStartedInRealtimeElapsedMilis += (mMood.loopIterationTimeLength * 100);
        }
      }
    }

    loadMoodIntoQueue(timeStartedInRealtimeElapsedMilis, systemElapsedRealtime);
  }

  /**
   * @param timeLoopStartedInRealtimeElapsedMilis if null, parameter ignored
   * @param systemElapsedRealtime                 usually SystemClock.elapsedRealtime();
   */
  private void loadMoodIntoQueue(Long timeLoopStartedInRealtimeElapsedMilis,
                                 long systemElapsedRealtime) {
    Log.d("mood", "loadMoodWithOffset" + ((timeLoopStartedInRealtimeElapsedMilis != null)
                                          ? timeLoopStartedInRealtimeElapsedMilis : "null"));

    ArrayList<Long>[] channels = new ArrayList[mMood.getNumChannels()];
    for (int i = 0; i < channels.length; i++) {
      channels[i] = new ArrayList<Long>();
    }

    ArrayList<Long> bulbBaseIds = mGroup.getNetworkBulbDatabaseIds();
    for (int i = 0; i < bulbBaseIds.size(); i++) {
      channels[i % mMood.getNumChannels()].add(bulbBaseIds.get(i));
    }

    if (mMood.timeAddressingRepeatPolicy) {
      Stack<QueueEvent> pendingEvents = new Stack<QueueEvent>();

      long earliestEventStillApplicable = Long.MIN_VALUE;

      for (int i = mMood.events.length - 1; i >= 0; i--) {
        Event e = mMood.events[i];
        for (Long bNum : channels[e.channel]) {
          QueueEvent qe = new QueueEvent(e);
          qe.bulbBaseId = bNum;

          qe.miliTime = Conversions.miliEventTimeFromMoodDailyTime(e.time);
          if (qe.miliTime > systemElapsedRealtime) {
            pendingEvents.add(qe);
          } else if (qe.miliTime >= earliestEventStillApplicable) {
            earliestEventStillApplicable = qe.miliTime;
            qe.miliTime = systemElapsedRealtime;
            pendingEvents.add(qe);
          }
        }
      }

      if (earliestEventStillApplicable == Long.MIN_VALUE && mMood.events.length > 0) {
        // haven't found a previous state to start with, time to roll over and add last evening
        // event
        Event e = mMood.events[mMood.events.length - 1];
        for (Long bNum : channels[e.channel]) {
          QueueEvent qe = new QueueEvent(e);
          qe.bulbBaseId = bNum;
          qe.miliTime = systemElapsedRealtime;
          pendingEvents.add(qe);
        }
      }

      while (!pendingEvents.empty()) {
        queue.add(pendingEvents.pop());
      }
    } else {
      for (Event e : mMood.events) {

        for (Long bNum : channels[e.channel]) {
          QueueEvent qe = new QueueEvent(e);
          qe.bulbBaseId = bNum;

          // if no preset mood start time, use present time
          if (timeLoopStartedInRealtimeElapsedMilis == null) {
            timeLoopStartedInRealtimeElapsedMilis = systemElapsedRealtime;
          }
          qe.miliTime = timeLoopStartedInRealtimeElapsedMilis + (e.time * 100l);

          Log.d("mood", "qe event offset" + (qe.miliTime - systemElapsedRealtime));

          // if event in future or present (+/- 100ms, add to queue
          if (qe.miliTime + 100 > systemElapsedRealtime) {
            queue.add(qe);
          }
        }
      }
    }
    moodLoopIterationEndMiliTime =
        systemElapsedRealtime + (mMood.loopIterationTimeLength * 100l);
  }

  /**
   * @param systemElapsedRealtime usually SystemClock.elapsedRealtime();
   * @return false if done playing
   */
  public boolean onTick(long systemElapsedRealtime) {
    if (queue.peek() != null && queue.peek().miliTime <= systemElapsedRealtime) {
      while (queue.peek() != null && queue.peek().miliTime <= systemElapsedRealtime) {
        QueueEvent e = queue.poll();
        if (mDeviceManager.getNetworkBulb(e.bulbBaseId) != null) {
          mBrightnessManager.setState(mDeviceManager.getNetworkBulb(e.bulbBaseId), e.event.state);
        }
      }
    } else if (queue.peek() == null && mMood.isInfiniteLooping()
               && systemElapsedRealtime > moodLoopIterationEndMiliTime) {
      loadMoodIntoQueue(null, systemElapsedRealtime);
    } else if (queue.peek() == null && !mMood.isInfiniteLooping()) {
      return false;
    }
    return true;
  }

  public boolean hasImminentPendingWork() {
    //TODO fix
    return true;/*
    if(!queue.isEmpty()){
      Log.d("mood",queue.peek().miliTime + "");
      Log.d("mood",(queue.peek().miliTime - SystemClock.elapsedRealtime())+"");
    }

    // IF queue has imminent events or queue about to be reloaded
    if (!queue.isEmpty() && (queue.peek().miliTime - SystemClock.elapsedRealtime()) < IMMIMENT_EVENT_WAKE_THRESHOLD_IN_MILISEC){
      return true;
    } else if(mood.isInfiniteLooping() && (moodLoopIterationEndMiliTime - SystemClock.elapsedRealtime()) < IMMIMENT_EVENT_WAKE_THRESHOLD_IN_MILISEC){
      return true;
    }
    return false;*/
  }

  /**
   * @return next event time is millis referenced in SystemClock.elapsedRealtime()
   */
  public long getNextEventTime() {
    if (!queue.isEmpty()) {
      return queue.peek().miliTime;
    } else if (mMood.isInfiniteLooping()) {
      return moodLoopIterationEndMiliTime;
    }
    return Long.MAX_VALUE;
  }
}
