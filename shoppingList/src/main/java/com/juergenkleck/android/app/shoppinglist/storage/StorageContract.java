package com.juergenkleck.android.app.shoppinglist.storage;

import android.provider.BaseColumns;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public final class StorageContract extends com.juergenkleck.android.appengine.storage.StorageContract {

    // Prevents the StorageContract class from being instantiated.
    private StorageContract() {
    }

    public static abstract class TableInventory implements BaseColumns {
        public static final String TABLE_NAME = "inventory";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ORDER = "sortorder";
        public static final String COLUMN_LIST = "list";
    }

    public static abstract class TableCartItem implements BaseColumns {
        public static final String TABLE_NAME = "cartitem";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_LIST = "list";
        public static final String COLUMN_INCART = "incart";
        public static final String COLUMN_INCART_BOUGHT = "incartbought";
    }
}
