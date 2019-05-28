package com.kuxhausen.huemore.persistence;

import android.net.Uri;
import android.provider.BaseColumns;

import com.kuxhausen.huemore.BuildConfig;

/**
 * Convenience definitions for Database Hander and Preferences
 */
public final class Definitions {

  public static final String SLASH = "/";

  public static final class AlarmColumns implements BaseColumns {

    public static final String TABLE_NAME = "alarms";
    public static final String PATH_ALARMS = "alarms";

    /**
     * The content:// style URL for this table
     */
    public static final Uri ALARMS_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_ALARMS);

    /**
     * String, must be valid entry in Groups table
     */
    public static final String COL_GROUP_ID = "COL_GROUP_ID";

    /* row id of mood in moods database
     */
    public static final String COL_MOOD_ID = "COL_MOOD_ID";

    /**
     * 0-255 or null
     */
    public static final String COL_BRIGHTNESS = "COL_BRIGHTNESS";

    /**
     * int, 0 = false,  1 = true
     */
    public static final String COL_IS_ENABLED = "COL_IS_ENABLED";

    /**
     * int, encoding by DaysOfWeek class
     */
    public static final String COL_REPEAT_DAYS = "COL_REPEAT_DAYS";

    /**
     * int
     */
    public static final String COL_YEAR = "COL_YEAR";

    /**
     * int
     */
    public static final String COL_MONTH = "COL_MONTH";

    /**
     * int, day of month
     */
    public static final String COL_DAY_OF_MONTH = "COL_DAY_OF_MONTH";


    /**
     * int, hour of day (24 hour time)
     */
    public static final String COL_HOUR_OF_DAY = "COL_HOUR_OF_DAY";

    /**
     * int
     */
    public static final String COL_MINUTE = "COL_MINUTE";

    // This class cannot be instantiated
    private AlarmColumns() {
    }
  }

  public static final class GroupColumns implements BaseColumns {

    public static final String TABLE_NAME = "groups";

    public static final String PATH_GROUPS = "groups";
    public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_GROUPS);

    public static final String COL_GROUP_NAME = "D_COL_GROUP_NAME";
    public static final String COL_GROUP_LOWERCASE_NAME = "D_COL_GROUP_LOWERCASE_NAME";
    public static final String COL_GROUP_PRIORITY = "D_COL_GROUP_PRIORITY";
    public static final String COL_GROUP_FLAGS = "D_COL_GROUP_FLAGS";

    public final static int PRIORITY_UNSTARRED = 1, PRIORITY_STARRED = 2;

    public final static int FLAG_NORMAL = 0, FLAG_ALL = 1;

    // This class cannot be instantiated
    private GroupColumns() {
    }
  }

  public static final class GroupBulbColumns implements BaseColumns {

    public static final String TABLE_NAME = "groupbulbs";

    public static final String PATH_GROUPBULBS = "groupbulbs";
    public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_GROUPBULBS);

    /**
     * Points to the Group table entry for the Group this is part of
     */
    public static final String COL_GROUP_ID = "D_COL_GROUP_ID";

    /**
     * order within group in which bulbs should be used when applying mood (lowest number = first)
     */
    public static final String COL_BULB_PRECEDENCE = "D_COL_BULB_PRECEDENCE";

    /**
     * Points to the NetBulb table entry for this bulb
     */
    public static final String COL_NET_BULB_ID = "D_COL_NET_BULB_ID";

    // This class cannot be instantiated
    private GroupBulbColumns() {
    }
  }

  public static final class MoodColumns implements BaseColumns {

    public static final String TABLE_NAME = "moods";

    public static final String PATH_MOODS = "moods";
    public static final Uri MOODS_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_MOODS);

    public static final String COL_MOOD_VALUE = "Dstate";
    public static final String COL_MOOD_NAME = "Dmood";
    public static final String COL_MOOD_LOWERCASE_NAME = "D_COL_MOOD_LOWERCASE_NAME";
    public static final String COL_MOOD_PRIORITY = "D_COL_MOOD_PRIORITY";

    public final static int UNSTARRED_PRIORITY = 1, STARRED_PRIORITY = 2;

    // This class cannot be instantiated
    private MoodColumns() {
    }
  }

  public static final class NetBulbColumns implements BaseColumns {

    public static final String TABLE_NAME = "netbulbs";

    public static final String PATH = "netbulbs";
    public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH);

    public static final String NAME_COLUMN = "D_NAME_COLUMN";
    public static final String DEVICE_ID_COLUMN = "D_DEVICE_ID_COLUMN";
    public static final String TYPE_COLUMN = "D_TYPE_COLUMN";
    public static final String JSON_COLUMN = "D_JSON_COLUMN";
    /**
     * holds a values 0-100 *
     */
    public static final String CURRENT_MAX_BRIGHTNESS = "D_CURRENT_MAX_BRIGHTNESS";
    /**
     * Points to the NetConnection table entry for this bulb
     */
    public static final String CONNECTION_DATABASE_ID = "Dconnection_database_id";

    public static final class NetBulbType {

      public static final int DEBUG = 0;
      public static final int PHILIPS_HUE = 1;
      public static final int LIFX = 2;
    }

    // This class cannot be instantiated
    private NetBulbColumns() {
    }
  }

  public static final class NetConnectionColumns implements BaseColumns {

    public static final String TABLE_NAME = "netconnection";

    public static final String PATH = "netconnection";
    public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH);

    public static final String NAME_COLUMN = "D_NAME_COLUMN";
    public static final String DEVICE_ID_COLUMN = "D_DEVICE_ID_COLUMN";
    // uses NetBulbType
    public static final String TYPE_COLUMN = "D_TYPE_COLUMN";
    public static final String JSON_COLUMN = "D_JSON_COLUMN";

    // This class cannot be instantiated
    private NetConnectionColumns() {
    }
  }

  /*
   * This table is used to store ongoing/playing moods details while the app power naps
   */
  public static final class PlayingMood implements BaseColumns {

    public static final String TABLE_NAME = "playingmood";

    public static final String PATH = "playingmood";
    public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH);

    /**
     * json encoded group object
     */
    public static final String COL_GROUP_VALUE = "D_GROUP_VALUE_COLUMN";
    /**
     * the mood name, may not exist in the mood table
     */
    public static final String COL_MOOD_NAME = "D_MOOD_NAME_COLUMN";
    /**
     * the URL-ENCODE mood value
     */
    public static final String COL_MOOD_VALUE = "D_MOOD_VALUE_COLUMN";

    public static final String COL_MOOD_BRI = "D_COL_INITIAL_MAX_BRI";

    public static final String COL_INTERNAL_PROGRESS = "D_COL_INTERNAL_PROGRESS";

    /**
     * the original mood start time measured in miliseconds using SystemClock elapsedRealTime()
     */
    public static final String COL_MILI_TIME_STARTED = "D_MILI_TIME_START_COLUMN";

    // This class cannot be instantiated
    private PlayingMood() {
    }
  }

  public static final class InternalArguments {

    public static final String GROUP_NAME = "Group_Name";
    public static final String MOOD_NAME = "Mood_Name";
    public static final String ENCODED_MOOD = "Encoded_Mood";
    public static final String BRIDGES = "Bridges";
    public static final String MD5 = "MD5";
    public static final String FRAG_MANAGER_DIALOG_TAG = "dialog";
    public static final String FALLBACK_USERNAME_HASH = "f01623452466afd4eba5c1ed0a0a9395";
    public final static String HUE_STATE = "HueState";
    public final static String PREVIOUS_STATE = "PreviousState";
    public final static String ALARM_ID = "AlarmId";
    public final static String DECODER_ERROR_UPGRADE = "DecoderErrorUpgrade";
    public static final String DURATION_TIME = "DurationTime";
    public static final String HELP_PAGE = "HelpPage";
    public static final String ROW = "Row";
    public static final String COLUMN = "Column";
    public static final String NET_BULB_DATABASE_ID = "NET_BULB_DATABASE_ID";
    public static final String MAX_BRIGHTNESS = "MAX_BRIGHTNESS";
    public static final String NAV_DRAWER_TITLE = "NAV_DRAWER_TITLE";
    public static final String GROUPBULB_TAB = "GROUPBULB_TAB";
    public static final String FLAG_SHOW_NAV_DRAWER = "FLAG_SHOW_NAV_DRAWER";
    public static final String FLAG_CANCEL_PLAYING = "FLAG_CANCEL_PLAYING";
    public static final String VOICE_INPUT = "VOICE_INPUT";
    public static final String VOICE_INPUT_LIST = "VOICE_INPUT_LIST";
    public static final String VOICE_INPUT_CONFIDENCE_ARRAY = "VOICE_INPUT_CONFIDENCE_ARRAY";
    public static final String CLICK_ACTION = "com.kuxhausen.huemore.CLICK_ACTION";
    public static final String ALARM_HANDLER_THREAD = "ALARM_HANDLER_THREAD";
    public static final String ALARM_INTENT_ACTION = "com.kuxhausen.huemore.alarm";
    public static final String DAYS_OF_WEEK_AS_BYTE = "DAYS_OF_WEEK_AS_BYTE";
    public static final String GROUP_ID = "GROUP_ID";
  }

  public static final class PreferenceKeys {

    public static final String DEFAULT_TO_GROUPS = "default_to_groups";
    public static final String DEFAULT_TO_MOODS = "default_to_moods";
    public static final String FIRST_RUN = "First_Run";
    public static final String DONE_WITH_WELCOME_DIALOG = "DONE_WITH_WELCOME_DIALOG";
    public static final String HAS_SHOWN_COMMUNITY_DIALOG = "HAS_SHOWN_COMMUNITY_DIALOG";
    public static final String UPDATE_OPT_OUT = "Update_Opt_Out";
    public static final String NUMBER_OF_CONNECTED_BULBS = "Number_Of_Connected_Bulbs";
    public static final String VERSION_NUMBER = "Version_Number";
    public static final String UNNAMED_GROUP_NUMBER = "UNNAMED_GROUP_NUMBER";
    public static final String UNNAMED_MOOD_NUMBER = "UNNAMED_MOOD_NUMBER";
    public static final String CACHED_EXECUTING_ENCODED_MOOD = "CACHED_EXECUTING_ENCODED_MOOD";
    public static final String ALARM_GLOBAL_ID = "ALARM_GLOBAL_ID";
  }

  /**
   * These preference keys were used in previous versions and might still exist on users devices
   */
  public static final class DeprecatedPreferenceKeys {

    public static final String BRIDGE_IP_ADDRESS = "Bridge_IP_Address";
    public static final String LOCAL_BRIDGE_IP_ADDRESS = "Local_Bridge_IP_Address";
    public static final String INTERNET_BRIDGE_IP_ADDRESS = "Internet_Bridge_IP_Address";
    public static final String HASHED_USERNAME = "Hashed_Username";
  }

  public static final class NotificationChannelIds {
    public static final String PlayingMoodsChannel = "PLAYING_MOODS";
  }

  public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.database";
  private static final String SCHEME = "content://";


  // This class cannot be instantiated
  private Definitions() {
  }
}
