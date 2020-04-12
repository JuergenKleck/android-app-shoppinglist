package info.simplyapps.app.shoppinglist.storage.dto;

import java.io.Serializable;

import info.simplyapps.appengine.storage.dto.BasicTable;

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
