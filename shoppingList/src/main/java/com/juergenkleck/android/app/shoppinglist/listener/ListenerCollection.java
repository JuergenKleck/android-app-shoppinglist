package com.juergenkleck.android.app.shoppinglist.listener;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Collections;

import com.juergenkleck.android.app.shoppinglist.CartUtils;
import com.juergenkleck.android.app.shoppinglist.SystemHelper;
import com.juergenkleck.android.app.shoppinglist.storage.DBDriver;
import com.juergenkleck.android.app.shoppinglist.storage.dto.CartItem;
import com.juergenkleck.android.app.shoppinglist.storage.dto.Inventory;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class ListenerCollection {

    public static OnClickListener mAddToBoughtListener = new OnClickListener() {
        public void onClick(View v) {
            LinearLayout vwParentRow = (LinearLayout) v.getParent();
            TextView child = (TextView) vwParentRow.getChildAt(1);
            String selected = child.getText().toString();
            CartItem item = null;
            for (CartItem entry : SystemHelper.getCartItems()) {
                if (entry.name.equalsIgnoreCase(selected)) {
                    item = entry;
                    break;
                }
            }
            if (item != null) {
                item.inCartBought = !item.inCartBought;
                DBDriver.getInstance().store(item);
            }
            boolean inBoughtList = item != null ? item.inCartBought : false;
            if (inBoughtList) {
                child.setTextColor(Color.GRAY);
                child.setPaintFlags(child.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                child.setTextColor(Color.WHITE);
                child.setPaintFlags(child.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

        }
    };

    public static OnClickListener mDeleteFromShoppingList = new OnClickListener() {
        public void onClick(View v) {
            //get the row the clicked button is in
            LinearLayout vwParentRow = (LinearLayout) v.getParent();
            TextView child = (TextView) vwParentRow.getChildAt(1);
            String selected = child.getText().toString();
            CartItem item = null;
            for (CartItem entry : SystemHelper.getCartItems()) {
                if (entry.name.equalsIgnoreCase(selected)) {
                    item = entry;
                    break;
                }
            }
            if (item != null) {
                item.inCart = false;
                item.inCartBought = false;
                DBDriver.getInstance().store(item);
            }
            TableLayout l = (TableLayout) v.getParent().getParent();
            l.removeView(vwParentRow);
        }
    };


    public static OnClickListener mMoveUpInConfiguration = new OnClickListener() {
        public void onClick(View v) {
            //get the row the clicked button is in
            LinearLayout vwParentRow = (LinearLayout) v.getParent();
            TextView child = (TextView) vwParentRow.getChildAt(0);
            String selected = child.getText().toString();
            Inventory item = null;
            for (Inventory entry : SystemHelper.getInventories()) {
                if (entry.name.equalsIgnoreCase(selected)) {
                    item = entry;
                    break;
                }
            }
            Collections.sort(SystemHelper.getInventories(), CartUtils.getSortInventory());
            int previous = item.order;
            item.order -= 1;
            if (item.order < 0) {
                item.order = 0;
            }
            for (Inventory entry : SystemHelper.getInventories()) {
                if (!entry.name.equalsIgnoreCase(selected) && item.order == entry.order) {
                    entry.order = previous;
                    break;
                }
            }
            Collections.sort(SystemHelper.getInventories(), CartUtils.getSortInventory());
            for (int i = 0; i < SystemHelper.getInventories().size(); i++) {
                SystemHelper.getInventories().get(i).order = i;
            }

            for (Inventory entry : SystemHelper.getInventories()) {
                DBDriver.getInstance().store(entry);
            }
        }
    };

    public static OnClickListener mMoveDownInConfiguration = new OnClickListener() {
        public void onClick(View v) {
            //get the row the clicked button is in
            LinearLayout vwParentRow = (LinearLayout) v.getParent();
            TextView child = (TextView) vwParentRow.getChildAt(0);
            String selected = child.getText().toString();
            Inventory item = null;
            for (Inventory entry : SystemHelper.getInventories()) {
                if (entry.name.equalsIgnoreCase(selected)) {
                    item = entry;
                    break;
                }
            }
            Collections.sort(SystemHelper.getInventories(), CartUtils.getSortInventory());
            int previous = item.order;
            item.order += 1;
            if (item.order < 0) {
                item.order = 0;
            }
            for (Inventory entry : SystemHelper.getInventories()) {
                if (!entry.name.equalsIgnoreCase(selected) && item.order == entry.order) {
                    entry.order = previous;
                    break;
                }
            }
            Collections.sort(SystemHelper.getInventories(), CartUtils.getSortInventory());
            for (int i = 0; i < SystemHelper.getInventories().size(); i++) {
                SystemHelper.getInventories().get(i).order = i;
            }

            for (Inventory entry : SystemHelper.getInventories()) {
                DBDriver.getInstance().store(entry);
            }
        }
    };
}
