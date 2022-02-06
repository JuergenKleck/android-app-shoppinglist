package com.juergenkleck.android.app.shoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.juergenkleck.android.app.shoppinglist.listener.ListenerCollection;
import com.juergenkleck.android.app.shoppinglist.screens.InventoryScreen;
import com.juergenkleck.android.app.shoppinglist.storage.DBDriver;
import com.juergenkleck.android.app.shoppinglist.storage.StorageUtil;
import com.juergenkleck.android.app.shoppinglist.storage.dto.CartItem;
import com.juergenkleck.android.appengine.screens.GenericScreenTemplate;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class ShoppingListScreen extends GenericScreenTemplate {

    private TableLayout lTable;
    private Dialog editDialog;
    private CartItem editItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lTable = findViewById(R.id.tableLayoutShoppingList);
        createEditDialog();
        Button bAdd = findViewById(R.id.btn_add);
        bAdd.setOnClickListener(onButtonAdd);
        Button bInventory = findViewById(R.id.btn_inventory);
        bInventory.setOnClickListener(onButtonInventory);
    }

    @Override
    protected void onResume() {
        updateLists();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createEditDialog() {
        // prepare edit dialog
        editDialog = new Dialog(this, R.style.Theme_AppCompat_Dialog);
        editDialog.setContentView(R.layout.editdialog_ac);

        Button bOk = editDialog.findViewById(R.id.btn_ok);
        Button bCancel = editDialog.findViewById(R.id.btn_cancel);
        bOk.setOnClickListener(onEditDialogOk);
        bCancel.setOnClickListener(onEditDialogCancel);
    }

    public void openEditDialog() {
        // clear text
        AutoCompleteTextView mText = editDialog.findViewById(R.id.autoCompleteTextView);
        List<String> strings = new ArrayList<>();

        for (CartItem tmpItem : SystemHelper.getCartItems()) {
            strings.add(tmpItem.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, strings);
        mText.setAdapter(adapter);

        mText.setText("");
        if (editItem != null) {
            mText.setText(editItem.name);
        }
        // popup window
        editDialog.show();
    }

    public void openInventory() {
        Intent wordIntent = new Intent(this, InventoryScreen.class);
        wordIntent.setData(getIntent().getData());
        startActivity(wordIntent);
    }

    public void updateLists() {
        if (CartUtils.isAutoSort(getApplicationContext())) {
            actionSort();
        }
        lTable.removeAllViews();
        for (CartItem entry : SystemHelper.getCartItems()) {

            // not from this list
            if (!entry.inCart) {
                continue;
            }

            View row;
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            row = inflater.inflate(R.layout.shoppinglistitem, lTable, false);
            if (row != null) {
                ImageButton btnDel;
                TextView txtField;
                TextView idField;
                boolean inBoughtList = entry.inCartBought;

                // Get reference to buttons
                btnDel = row.findViewById(R.id.imageButton_deleteCart);

                btnDel.setOnClickListener(ListenerCollection.mDeleteFromShoppingList);

                // Get reference to TextView
                txtField = row.findViewById(R.id.shoppinglistitem_textview);
                idField = row.findViewById(R.id.shoppinglistitem_id);
                //set value into the list
                txtField.setText(entry.name);
                if (!CartUtils.isAutoTextSize(getApplicationContext()) && CartUtils.getTextSize(getApplicationContext()) > 0) {
                    txtField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CartUtils.getTextSize(getApplicationContext()));
                }

                txtField.setOnClickListener(ListenerCollection.mAddToBoughtListener);
                txtField.setOnLongClickListener(onLongClickListener);
                idField.setText(Long.toString(entry.id));

                // check if this is already listed in the shoppinglist
                if (inBoughtList) {
                    txtField.setTextColor(Color.GRAY);
                    txtField.setPaintFlags(txtField.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    txtField.setTextColor(Color.WHITE);
                    txtField.setPaintFlags(txtField.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                lTable.addView(row);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shoppinglistmenu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shoppinglist_clear: {
                dialogClear();
                return true;
            }
            case R.id.menu_shoppinglist_help: {
                actionHelp();
                return true;
            }
            case R.id.menu_shoppinglist_about: {
                actionAbout();
                return true;
            }
        }
        return false;
    }


    OnClickListener onButtonInventory = v -> actionInventory();

    OnClickListener onButtonAdd = v -> actionAdd();

    OnClickListener onEditDialogOk = new OnClickListener() {
        public void onClick(View v) {
            AutoCompleteTextView mText = ((View) v.getParent()).findViewById(R.id.autoCompleteTextView);
            String text = mText.getText().toString();
            if (CartUtils.notEmpty(text)) {
                CartItem item = null;
                for (CartItem tmpItem : SystemHelper.getCartItems()) {
                    if (tmpItem.name.equalsIgnoreCase(text)) {
                        item = tmpItem;
                        break;
                    }
                }
                // delete if item is not the edititem
                if (item != null && editItem != null && item.id != editItem.id) {
                    DBDriver.getInstance().delete(editItem);
                    SystemHelper.getCartItems().remove(editItem);
                    editItem = null;
                }
                if (item == null) {
                    item = new CartItem();
                    if (editItem != null) {
                        item = editItem;
                    } else {
                        item.list = Constants.SHOPPING_CART_LIST;
                    }
                    item.name = text;
                    item.inCart = true;
                    item.inCartBought = false;

                    if (DBDriver.getInstance().store(item)) {
                        if (editItem == null) {
                            SystemHelper.getCartItems().add(item);
                        }
                        updateLists();
                    } else {
                        Toast.makeText(ShoppingListScreen.this, R.string.save_failed, Toast.LENGTH_LONG).show();
                    }
                } else {
                    item.name = text;
                    item.inCart = true;
                    item.inCartBought = false;

                    if (DBDriver.getInstance().store(item)) {
                        updateLists();
                    } else {
                        Toast.makeText(ShoppingListScreen.this, R.string.save_failed, Toast.LENGTH_LONG).show();
                    }
                }
            }
            editItem = null;
            editDialog.dismiss();
        }
    };

    OnClickListener onEditDialogCancel = new OnClickListener() {
        public void onClick(View v) {
            editItem = null;
            editDialog.dismiss();
        }
    };

    private void actionClear() {
        List<CartItem> removal = new ArrayList<>();
        for (CartItem item : SystemHelper.getCartItems()) {
            // delete items from this list only
            if (item.list == Constants.SHOPPING_CART_LIST) {
                removal.add(item);
            }
            // clear other items
            if (item.inCart) {
                item.inCart = false;
                item.inCartBought = false;
                DBDriver.getInstance().store(item);
            }
        }
        for (CartItem item : removal) {
            DBDriver.getInstance().delete(item);
        }
        SystemHelper.getCartItems().removeAll(removal);
        updateLists();
    }

    private void actionInventory() {
        openInventory();
    }

    private void actionAdd() {
        editItem = null;
        openEditDialog();
    }

    private void actionHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.help_shoppinglist)
                .setCancelable(false)
                .setNeutralButton(R.string.btn_ok, (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void actionSort() {
        if (SystemHelper.getCartItems() != null && SystemHelper.getCartItems().size() > 1) {
            Collections.sort(SystemHelper.getCartItems(), new SortComparator());
        }
    }

    private void actionAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about_app);
        builder.setMessage(R.string.about_text)
                .setCancelable(false);
        builder.setNeutralButton(R.string.btn_ok, (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void dialogClear() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.btn_clear))
                .setMessage(getString(R.string.delete_list_text))
                .setPositiveButton(getString(R.string.btn_ok), (dialog, which) -> {
                    actionClear();
                    dialog.cancel();
                })
                .setNegativeButton(getString(R.string.btn_cancel), (dialog, which) -> dialog.cancel())
                .create();
        alert.show();
    }

    OnLongClickListener onLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            TextView tv = ((View) v.getParent()).findViewById(R.id.shoppinglistitem_id);
            long itemId = Long.parseLong(tv.getText().toString());

            for (CartItem tmpItem : SystemHelper.getCartItems()) {
                if (tmpItem.id == itemId) {
                    editItem = tmpItem;
                    break;
                }
            }

            openEditDialog();
            return false;
        }
    };

    @Override
    public int getScreenLayout() {
        return R.layout.shoppinglistscreen;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void prepareStorage(Context context) {
        StorageUtil.prepareStorage(getApplicationContext());
    }

    @Override
    public void onPermissionResult(String permission, boolean granted) {

    }

    @Override
    public void onScreenCreate(Bundle bundle) {

    }

}