package potapeyko.rss.activities;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


final class DrawerItemClickListener implements ListView.OnItemClickListener {
    private final DrawerLayout DrawerLayout;
    private final ListView DrawerList;
    private final Activity mainActivity;
    DrawerItemClickListener(DrawerLayout mDrawerLayout, ListView mDrawerList, Activity mainActivity) {
        this.DrawerLayout = mDrawerLayout;
        this.DrawerList =mDrawerList;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    private void selectItem(int position) {
        DrawerLayout.closeDrawer(DrawerList);

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
                //                диалог или сразу удаление
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
