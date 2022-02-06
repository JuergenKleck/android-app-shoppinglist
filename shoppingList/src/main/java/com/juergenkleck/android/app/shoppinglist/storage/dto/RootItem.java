package com.juergenkleck.android.app.shoppinglist.storage.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class RootItem {

    public List<Inventory> inventories;
    public List<CartItem> cartitems;

    public RootItem() {
        inventories = new ArrayList<>();
        cartitems = new ArrayList<>();
    }

}
