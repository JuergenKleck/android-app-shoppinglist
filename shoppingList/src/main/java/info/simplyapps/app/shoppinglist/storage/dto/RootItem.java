package info.simplyapps.app.shoppinglist.storage.dto;

import java.util.ArrayList;
import java.util.List;

public class RootItem {

    public List<Inventory> inventories;
    public List<CartItem> cartitems;

    public RootItem() {
        inventories = new ArrayList<Inventory>();
        cartitems = new ArrayList<CartItem>();
    }

}
