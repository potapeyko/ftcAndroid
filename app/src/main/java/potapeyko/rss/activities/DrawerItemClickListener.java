package potapeyko.rss.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import potapeyko.rss.sql.DB;


final class DrawerItemClickListener implements ListView.OnItemClickListener {
    private final DrawerLayout drawerLayout;
    private final ListView drawerList;
    private final MainActivity mainActivity;

    private static final int ADD_CHANNEL_ITEM = 0;
    private static final int CHANGE_CHANNEL_ITEM = 1;
    private static final int DELETE_CHANNEL_ITEM = 2;
    private static final int SETTINGS_ITEM = 3;
    private static final int ABOUT_ITEM = 4;


    DrawerItemClickListener(DrawerLayout drawerLayout, ListView drawerList, MainActivity mainActivity) {
        this.drawerLayout = drawerLayout;
        this.drawerList = drawerList;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    private void selectItem(final int position) {
        drawerLayout.closeDrawer(drawerList);

        switch (position) {
            case ADD_CHANNEL_ITEM: {
                NewChanelActivityMy.start(mainActivity);
                break;
            }
            case CHANGE_CHANNEL_ITEM: {
                ChannelChangeActivity.start(mainActivity);
                break;
            }
            case DELETE_CHANNEL_ITEM: {
                DB db = null;
                if (mainActivity != null && mainActivity.getChanelId()!=-1) {
                    try {
                        db = new DB(mainActivity);
                        db.open();
                        db.deleteChanelById(mainActivity.getChanelId());
                        final SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                        final SharedPreferences.Editor ed = sPref.edit();
                        ed.putLong(MainActivity.CHANEL_ID, -1);
                        ed.apply();
                    } catch (Throwable r) {
                        r.printStackTrace();
                    } finally {
                        if (db != null) {
                            db.close();
                        }
                        ChannelChangeActivity.start(mainActivity);
                    }
                }
                break;
            }

            case SETTINGS_ITEM: {
                SettingsActivity.start(mainActivity);
                break;
            }
            case ABOUT_ITEM: {
                AboutAppActivityMy.start(mainActivity);
                break;
            }
            default:
                break;
        }
    }
}
