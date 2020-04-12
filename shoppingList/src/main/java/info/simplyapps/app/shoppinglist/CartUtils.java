package info.simplyapps.app.shoppinglist;

import android.content.Context;

import java.util.Comparator;

import info.simplyapps.app.shoppinglist.storage.dto.Inventory;
import info.simplyapps.appengine.storage.dto.Configuration;

public final class CartUtils {

    public static boolean notEmpty(String s) {
        return s != null && s.length() > 0;
    }

//	public static boolean hasOriginalVersion(Context mContext) {
//		boolean hasOriginal = false;
//		PackageManager pm = Application.class.cast(mContext).getPackageManager();
//		if(pm != null) {
//			try {
//				PackageInfo pi = pm.getPackageInfo("info.jnkleck.android.shoppinglist", 0);
//				if(pi != null) {
//					hasOriginal = true;
//				}
//			} catch (NameNotFoundException e) {
//				hasOriginal = false;
//			}
//		}
//		return hasOriginal;
//	}

    public static int getTextSize(Context context) {
        Configuration config = SystemHelper.getConfiguration(Constants.CONFIG_TEXTSIZE, Constants.DEFAULT_CONFIG_TEXTSIZE);
        return Integer.valueOf(config.value);
    }

    public static boolean isAutoTextSize(Context context) {
        Configuration config = SystemHelper.getConfiguration(Constants.CONFIG_AUTOTEXTSIZE, Constants.DEFAULT_CONFIG_AUTOTEXTSIZE);
        return Boolean.valueOf(config.value);
    }

    public static boolean isAutoSort(Context context) {
        Configuration config = SystemHelper.getConfiguration(Constants.CONFIG_AUTOSORT, Constants.DEFAULT_CONFIG_AUTOSORT);
        return Boolean.valueOf(config.value);
    }

    public static SortInventory getSortInventory() {
        return new CartUtils().new SortInventory();
    }

    public final class SortInventory implements Comparator<Inventory> {

        @Override
        public int compare(Inventory lhs, Inventory rhs) {
            return Integer.valueOf(lhs.order).compareTo(Integer.valueOf(rhs.order));
        }

    }

}
