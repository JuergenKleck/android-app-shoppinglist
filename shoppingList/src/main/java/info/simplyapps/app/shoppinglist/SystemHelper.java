package info.simplyapps.app.shoppinglist;

import java.util.List;

import info.simplyapps.app.shoppinglist.storage.StoreData;
import info.simplyapps.app.shoppinglist.storage.dto.CartItem;
import info.simplyapps.app.shoppinglist.storage.dto.Inventory;

public class SystemHelper extends info.simplyapps.appengine.SystemHelper {

//	public static boolean isDemo() {
//		return !SystemHelper.hasPurchase(Constants.EXPANSION_FULL_ID);
//	}

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
