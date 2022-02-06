package com.juergenkleck.android.app.shoppinglist;

import java.text.Collator;
import java.util.Comparator;

import com.juergenkleck.android.app.shoppinglist.storage.dto.CartItem;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class SortComparator implements Comparator<CartItem> {

    public int compare(CartItem lhs, CartItem rhs) {
        return Collator.getInstance().compare(lhs.name.toLowerCase(), rhs.name.toLowerCase());
    }

}
