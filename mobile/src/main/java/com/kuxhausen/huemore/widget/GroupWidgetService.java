package com.kuxhausen.huemore.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GroupWidgetService extends RemoteViewsService {

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new GroupStackRemoteViewsFactory(this.getApplicationContext(), intent);
  }
}


/**
 * This is the factory that will provide data to the collection widget.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class GroupStackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

  private Context mContext;
  private Cursor mCursor;
  private int mAppWidgetId;

  public GroupStackRemoteViewsFactory(Context context, Intent intent) {
    mContext = context;
    mAppWidgetId =
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                           AppWidgetManager.INVALID_APPWIDGET_ID);
  }

  public void onCreate() {
    // Since we reload the cursor in onDataSetChanged() which gets called immediately after
    // onCreate(), we do nothing here.
  }

  public void onDestroy() {
    if (mCursor != null) {
      mCursor.close();
    }
  }

  public int getCount() {
    return mCursor.getCount();
  }

  public RemoteViews getViewAt(int position) {
    // Get the data for this position from the content provider

    // int rowID = -1;
    String groupName = "";
    if (mCursor.moveToPosition(position)) {
      groupName = mCursor.getString(0);
      // rowID = mCursor.getInt(1);
    }

    final int itemId = R.layout.widget_group_row;
    RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
    rv.setTextViewText(R.id.groupTextView, groupName);

    {
      // Set the click intent
      final Intent fillInIntent = new Intent();
      final Bundle extras = new Bundle();
      extras.putString(InternalArguments.MOOD_NAME,
                       mContext.getResources().getString(R.string.cap_off));
      extras.putString(InternalArguments.GROUP_NAME, groupName);
      fillInIntent.putExtras(extras);
      rv.setOnClickFillInIntent(R.id.offButton, fillInIntent);
    }
    {
      // Set the click intent
      final Intent fillInIntent = new Intent();
      final Bundle extras = new Bundle();
      extras.putString(InternalArguments.MOOD_NAME,
                       mContext.getResources().getString(R.string.cap_on));
      extras.putString(InternalArguments.GROUP_NAME, groupName);
      fillInIntent.putExtras(extras);
      rv.setOnClickFillInIntent(R.id.onButton, fillInIntent);
    }

    return rv;
  }

  public RemoteViews getLoadingView() {
    return null;
  }

  public int getViewTypeCount() {
    return 1;
  }

  public long getItemId(int position) {
    return position;
  }

  public boolean hasStableIds() {
    return true;
  }

  public void onDataSetChanged() {
    final long identityToken = Binder.clearCallingIdentity();
    // Refresh the cursor
    if (mCursor != null) {
      mCursor.close();
    }

    ContentResolver r = mContext.getContentResolver();
    String[] columns = {GroupColumns.COL_GROUP_NAME, GroupColumns._ID};
    mCursor = r.query(GroupColumns.URI, columns, null, null, null);
    Binder.restoreCallingIdentity(identityToken);
  }
}
