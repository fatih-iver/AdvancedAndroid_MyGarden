package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

public class GridWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(getApplicationContext());
    }
}


class GridRemoteViewsFactory implements RemoteViewsFactory {

    Context mContext;
    Cursor mCursor;

    public GridRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        if(mCursor != null) { mCursor.close(); }

        Uri contentUri = PlantContract.PlantEntry.CONTENT_URI;

        mCursor = mContext.getContentResolver().query(contentUri,null,null,null, PlantContract.PlantEntry.COLUMN_CREATION_TIME);

    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if(mCursor == null || mCursor.getCount() == 0) { return null; }
        mCursor.moveToPosition(i);

        int plantIdIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int lastWateredTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int creationTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

        long plantId = mCursor.getLong(plantIdIndex);
        long lastWateredTime = mCursor.getLong(lastWateredTimeIndex);
        long creationTime = mCursor.getLong(creationTimeIndex);
        int plantType = mCursor.getInt(plantTypeIndex);

        long timeNow = System.currentTimeMillis();

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        int imageResourceId = PlantUtils.getPlantImageRes(mContext, timeNow - creationTime, timeNow - lastWateredTime, plantType);
        remoteViews.setImageViewResource(R.id.widget_plant_image, imageResourceId);
        remoteViews.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        remoteViews.setViewVisibility(R.id.widget_water_button, View.GONE);

        Bundle bundle = new Bundle();
        bundle.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        Intent fillIntent = new Intent();
        fillIntent.putExtras(bundle);
        remoteViews.setOnClickFillInIntent(R.id.widget_plant_image, fillIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
