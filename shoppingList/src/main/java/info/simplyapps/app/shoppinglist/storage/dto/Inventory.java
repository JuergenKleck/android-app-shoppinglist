package info.simplyapps.app.shoppinglist.storage.dto;

import java.io.Serializable;

import info.simplyapps.appengine.storage.dto.BasicTable;

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
