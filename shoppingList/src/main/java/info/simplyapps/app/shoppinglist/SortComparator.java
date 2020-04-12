package info.simplyapps.app.shoppinglist;

import java.text.Collator;
import java.util.Comparator;

import info.simplyapps.app.shoppinglist.storage.dto.CartItem;

public class SortComparator implements Comparator<CartItem> {

    public int compare(CartItem lhs, CartItem rhs) {
        return Collator.getInstance().compare(lhs.name.toLowerCase(), rhs.name.toLowerCase());
    }

}
