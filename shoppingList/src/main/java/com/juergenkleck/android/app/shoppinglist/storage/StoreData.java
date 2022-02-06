package com.juergenkleck.android.app.shoppinglist.storage;

import java.util.ArrayList;
import java.util.List;

import com.juergenkleck.android.app.shoppinglist.Constants;
import com.juergenkleck.android.app.shoppinglist.storage.dto.CartItem;
import com.juergenkleck.android.app.shoppinglist.storage.dto.Inventory;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class StoreData extends com.juergenkleck.android.appengine.storage.StoreData {

    private static final long serialVersionUID = 5696810296031292822L;

    public List<CartItem> shoppingCart;
    public List<Inventory> inventories;

    public StoreData() {
        shoppingCart = new ArrayList<CartItem>();
        inventories = new ArrayList<Inventory>();
    }

    public static StoreData getInstance() {
        return (StoreData) com.juergenkleck.android.appengine.storage.StoreData.getInstance();
    }

    /**
     * Update to the latest release
     */
    public boolean update() {
        boolean persist = false;

        // Release 4 - 1.1.0
        if (migration < 4) {
            // transfer the id to the list value for data backup
            for (Inventory i : inventories) {
                i.list = Long.valueOf(i.id).intValue();
            }
            persist = true;
        }

        // Release 5 - 1.1.1
        if (migration < 5) {
            persist = true;
        }

        // Release 6 - 1.1.2
        if (migration < 6) {
            persist = true;
        }

        // Release 7 - 1.2.0
        if (migration < 7) {
            persist = true;
        }

        // Release 8 - 1.3.0
        if (migration < 8) {
            persist = true;
            if (inventories.isEmpty()) {
                Inventory i = new Inventory();
                i.name = Constants.INVENTORY_DEFAULT_NAME;
                i.order = Constants.INVENTORY_DEFAULT_ORDER;
                i.list = 0;
                inventories.add(i);
            }
        }

        migration = 8;
        return persist;
    }

}
