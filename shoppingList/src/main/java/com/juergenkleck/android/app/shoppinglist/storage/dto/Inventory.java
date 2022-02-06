package com.juergenkleck.android.app.shoppinglist.storage.dto;

import java.io.Serializable;

import com.juergenkleck.android.appengine.storage.dto.BasicTable;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class Inventory extends BasicTable implements Serializable {

    /**
     * serial id
     */
    private static final long serialVersionUID = -213755492728977917L;

    // the list name
    public String name;
    // the sort order
    public int order;
    // the list ident
    public int list;

}
