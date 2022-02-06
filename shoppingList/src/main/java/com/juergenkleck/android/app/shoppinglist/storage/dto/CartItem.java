package com.juergenkleck.android.app.shoppinglist.storage.dto;

import java.io.Serializable;

import com.juergenkleck.android.appengine.storage.dto.BasicTable;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class CartItem extends BasicTable implements Serializable {

    /**
     * serial id
     */
    private static final long serialVersionUID = -7101789009229738387L;

    // the optional quantity
    public String quantity;
    // the cart item
    public String name;
    // the associated list or -1 for no list
    public long list;
    // multi state to display as selected and in shopping cart
    public boolean inCart;
    // state to display it is in real cart
    public boolean inCartBought;

}
