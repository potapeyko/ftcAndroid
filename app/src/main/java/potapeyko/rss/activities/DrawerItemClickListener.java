package potapeyko.rss.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import potapeyko.rss.sql.DB;


final class DrawerItemClickListener implements ListView.OnItemClickListener {
    private final DrawerLayout drawerLayout;
    private final ListView drawerList;
    private final MainActivity mainActivity;

    DrawerItemClickListener(DrawerLayout drawerLayout, ListView drawerList, MainActivity mainActivity) {
        this.drawerLayout = drawerLayout;
        this.drawerList = drawerList;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    private void selectItem(int position) {
        drawerLayout.closeDrawer(drawerList);

        switch (position) {
            case 0: {
                NewChanelActivityMy.start(mainActivity);
                break;
            }
            case 1: {
                ChanelChangeActivityMy.start(mainActivity);
                break;
            }
            case 2: {
                DB db = null;
                if (mainActivity != null) {
                    try {
                        db = new DB(mainActivity);

                        db.open();
                        db.deleteChanelById(mainActivity.getChanelId());


                    } catch (Exception r) {
                        r.printStackTrace();
                    } finally {
                        if (db != null) {
                            db.close();
                            Intent intent = new Intent(mainActivity, MainActivity.class);
                            mainActivity.startActivity(intent);
                            mainActivity.finish();
                        }
                    }
                }
                break;
            }
            case 3: {
//                настройки канала... что это игде
                break;
            }
            case 4: {
                //                настройки приложения... что это игде
                break;
            }
            case 5: {
                AboutAppActivityMy.start(mainActivity);
                break;
            }
            default:
                break;
        }
    }
}
