package com.juergenkleck.android.app.shoppinglist;

import android.content.Context;

import java.util.Comparator;

import com.juergenkleck.android.app.shoppinglist.storage.dto.Inventory;
import com.juergenkleck.android.appengine.storage.dto.Configuration;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public final class CartUtils {

    public static boolean notEmpty(String s) {
        return s != null && s.length() > 0;
    }

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
