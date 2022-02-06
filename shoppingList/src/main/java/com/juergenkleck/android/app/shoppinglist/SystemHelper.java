package com.juergenkleck.android.app.shoppinglist;

import java.util.List;

import com.juergenkleck.android.app.shoppinglist.storage.StoreData;
import com.juergenkleck.android.app.shoppinglist.storage.dto.CartItem;
import com.juergenkleck.android.app.shoppinglist.storage.dto.Inventory;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class SystemHelper extends com.juergenkleck.android.appengine.SystemHelper {

    public synchronized static final List<Inventory> getInventories() {
        return StoreData.getInstance().inventories;
    }

    public synchronized static final List<CartItem> getCartItems() {
        return StoreData.getInstance().shoppingCart;
    }

    public synchronized static final void addInventory(Inventory i) {
        if (StoreData.getInstance() != null && StoreData.getInstance().inventories != null) {
            StoreData.getInstance().inventories.add(i);
        }
    }

    public synchronized static final void addCartItem(CartItem ci) {
        if (StoreData.getInstance() != null && StoreData.getInstance().shoppingCart != null) {
            StoreData.getInstance().shoppingCart.add(ci);
        }
    }

}
