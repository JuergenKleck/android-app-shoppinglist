package com.juergenkleck.android.app.shoppinglist.screens;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.juergenkleck.android.app.shoppinglist.CartUtils;
import com.juergenkleck.android.app.shoppinglist.R;
import com.juergenkleck.android.app.shoppinglist.SortComparator;
import com.juergenkleck.android.app.shoppinglist.SystemHelper;
import com.juergenkleck.android.app.shoppinglist.storage.DBDriver;
import com.juergenkleck.android.app.shoppinglist.storage.StorageUtil;
import com.juergenkleck.android.app.shoppinglist.storage.dto.CartItem;
import com.juergenkleck.android.app.shoppinglist.storage.dto.Inventory;
import com.juergenkleck.android.appengine.screens.GenericScreenTemplate;

/**
 * Android app - ShoppingList
 *
 * Copyright 2022 by Juergen Kleck <develop@juergenkleck.com>
 */
public class InventoryScreen extends GenericScreenTemplate {

    private ViewGroup lTable;
    private ViewGroup lTableTabs;
    private Dialog editDialog;
    private Button bAdd;
    private ScrollView vInventoryList;
    private Button bConfigure;

    private Inventory activeList;
    private CartItem editItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lTable = findViewById(R.id.tableLayoutInventoryList);
        lTableTabs = findViewById(R.id.tableLayoutInventorySelect);
        vInventoryList = findViewById(R.id.ScrollViewInventoryList);
        bConfigure = findViewById(R.id.btn_configure);
        bConfigure.setOnClickListener(onButtonConfigure);
        bAdd = findViewById(R.id.btn_inventoryadd);
        bAdd.setOnClickListener(onButtonAdd);

        Button bBack = findViewById(R.id.btn_inventoryback);
        bBack.setOnClickListener(onButtonBack);

        createEditDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeList = null;
        this.updateTabs();
        this.updateLists();
    }

    private void createEditDialog() {
        editDialog = new Dialog(this, R.style.Theme_AppCompat_Dialog);
        editDialog.setContentView(R.layout.editdialog);

        Button bOk = editDialog.findViewById(R.id.btn_ok);
        Button bCancel = editDialog.findViewById(R.id.btn_cancel);
        bOk.setOnClickListener(onEditDialogOk);
        bCancel.setOnClickListener(onEditDialogCancel);
    }

    public void openEditDialog() {
        // clear text
        EditText mText = editDialog.findViewById(R.id.editText1);
        mText.setText("");
        if (editItem != null) {
            mText.setText(editItem.name);
        }
        // popup window
        editDialog.show();
    }

    public void updateTabs() {
        Collections.sort(SystemHelper.getInventories(), CartUtils.getSortInventory());

        lTableTabs.removeAllViews();
        int i = 0;
        Inventory active = null;
        // pre-scan
        for (Inventory entry : SystemHelper.getInventories()) {
            if (activeList == null) {
                active = entry;
                activeList = entry;
                break;
            }
            if (activeList.name.equals(entry.name)) {
                active = entry;
                break;
            }
        }
        // in case the active list was deleted
        if (active == null && SystemHelper.getInventories().size() > 0) {
            activeList = SystemHelper.getInventories().get(0);
        }
        for (Inventory entry : SystemHelper.getInventories()) {
            boolean isActive = activeList.name.equals(entry.name);
            if (isActive) {
                active = entry;
            }

            TextView txtField = new TextView(getApplicationContext());
            txtField.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            txtField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            txtField.setOnClickListener(mSelectTab);
            txtField.setText(entry.name);
            txtField.setGravity(Gravity.CENTER);

            int drawable = isActive ? R.drawable.tab_active : R.drawable.tab;
            setBackgroundImage(txtField, drawable);

            txtField.setTextColor(isActive ? Color.BLACK : Color.WHITE);

            lTableTabs.addView(txtField);

            if (i == 0) {
                i += 1;
            } else {
                i = 0;
            }
        }
    }

    private void setBackgroundImage(TextView txtField, int drawable) {
        setBackgroundImage16(txtField, drawable);
    }

    private void setBackgroundImage16(TextView txtField, int drawable) {
        txtField.setBackground(getApplicationContext().getResources().getDrawable(drawable));
    }

    public void updateLists() {
        if (lTable == null || activeList == null) {
            bAdd.setEnabled(false);
            vInventoryList.setVisibility(View.GONE);
            bConfigure.setVisibility(View.VISIBLE);
            return;
        }
        if (!bAdd.isEnabled()) {
            bAdd.setEnabled(true);
        }
        vInventoryList.setVisibility(View.VISIBLE);
        bConfigure.setVisibility(View.GONE);
        if (CartUtils.isAutoSort(getApplicationContext())) {
            actionSort();
        }
        lTable.removeAllViews();
        for (CartItem entry : SystemHelper.getCartItems()) {

            // not from this list
            if (entry.list != activeList.list) {
                continue;
            }

            View row;
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            row = inflater.inflate(R.layout.inventorylistitem, lTable, false);
            if (row != null) {
                ImageButton btnDel;
                TextView txtField;
                TextView idField;

                boolean inShoppingList = entry.inCart;

                // Get reference to buttons
                btnDel = row.findViewById(R.id.imageButton_delete);

                btnDel.setOnClickListener(mDeleteFromInventoryListener);

                // Get reference to TextView
                txtField = row.findViewById(R.id.inventorylistitem_textview);
                idField = row.findViewById(R.id.inventorylistitem_id);
                //set value into the list
                txtField.setText(entry.name);
                if (!CartUtils.isAutoTextSize(getApplicationContext()) && CartUtils.getTextSize(getApplicationContext()) > 0) {
                    txtField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CartUtils.getTextSize(getApplicationContext()));
                }
                idField.setText(Long.toString(entry.id));
                txtField.setOnClickListener(mAddToShoppingListener);
                txtField.setOnLongClickListener(onLongClickListener);

                // check if this is already listed in the shoppinglist
                if (inShoppingList) {
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
        inflater.inflate(R.menu.inventorymenu, menu);
        return true;
    }

    OnClickListener mAddToShoppingListener = v -> {
        TextView tv = ((View) v.getParent()).findViewById(R.id.inventorylistitem_id);
        long itemId = Long.parseLong(tv.getText().toString());

        CartItem item = null;
        for (CartItem entry : SystemHelper.getCartItems()) {
            if (entry.id == itemId) {
                item = entry;
                break;
            }
        }
        if (item != null) {
            item.inCart = true;
            item.inCartBought = false;
            if (DBDriver.getInstance().store(item)) {
                updateLists();
            }
        }

    };

    //get the row the clicked button is in
    OnClickListener mDeleteFromInventoryListener = this::actionDeleteFromConfiguration;

    private void actionDeleteFromConfiguration(final View v) {
        final LinearLayout vwParentRow = (LinearLayout) v.getParent();
        final TextView child = (TextView) vwParentRow.getChildAt(1);
        final String selected = child.getText().toString();

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(getApplicationContext().getString(R.string.delete_item))
                .setMessage(MessageFormat.format(getApplicationContext().getString(R.string.delete_item_text), selected))
                .setPositiveButton(getApplicationContext().getString(R.string.btn_ok), (dialog, which) -> {

                    TextView tv = (TextView) vwParentRow.getChildAt(0);
                    long itemId = Long.parseLong(tv.getText().toString());

                    CartItem item = null;
                    for (CartItem entry : SystemHelper.getCartItems()) {
                        if (entry.id == itemId) {
                            item = entry;
                            break;
                        }
                    }
                    if (item != null) {
                        if (DBDriver.getInstance().delete(item)) {
                            SystemHelper.getCartItems().remove(item);
                            updateLists();
                        }
                    }

                    dialog.cancel();
                })
                .setNegativeButton(v.getContext().getString(R.string.btn_cancel), (dialog, which) -> dialog.cancel());
        AlertDialog d = b.create();
        d.show();
    }

    OnClickListener onButtonBack = v -> actionBack();

    OnClickListener onButtonAdd = v -> actionAdd();

    OnClickListener onButtonConfigure = v -> actionConfigure();

    OnClickListener onButtonClear = v -> actionClear();

    OnClickListener onEditDialogOk = new OnClickListener() {
        public void onClick(View v) {
            EditText mText = ((View) v.getParent()).findViewById(R.id.editText1);
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
                        item.list = activeList.list;
                    }
                    item.name = text;
                    item.inCart = false;
                    item.inCartBought = false;

                    if (DBDriver.getInstance().store(item)) {
                        if (editItem == null) {
                            SystemHelper.getCartItems().add(item);
                        }
                        updateLists();
                    } else {
                        Toast.makeText(InventoryScreen.this, R.string.save_failed, Toast.LENGTH_LONG).show();
                    }
                } else {
                    item.name = text;
                    item.list = activeList.list;

                    if (DBDriver.getInstance().store(item)) {
                        updateLists();
                    } else {
                        Toast.makeText(InventoryScreen.this, R.string.save_failed, Toast.LENGTH_LONG).show();
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

    OnClickListener mSelectTab = new OnClickListener() {
        public void onClick(View v) {
            TextView child = (TextView) v;
            String selected = child.getText().toString();
            Inventory item = null;
            for (Inventory entry : SystemHelper.getInventories()) {
                if (entry.name.equalsIgnoreCase(selected)) {
                    item = entry;
                    break;
                }
            }
            if (item != null) {
                activeList = item;
            }
            updateTabs();
            updateLists();
        }
    };


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_inventory_clear: {
                dialogClear();
                return true;
            }
            case R.id.menu_inventory_help: {
                actionHelp();
                return true;
            }
            case R.id.menu_inventory_config: {
                actionConfigure();
                return true;
            }
        }
        return false;
    }

    private void actionConfigure() {
        Intent wordIntent = new Intent(this, ConfigureScreen.class);
        wordIntent.setData(getIntent().getData());
        startActivity(wordIntent);
    }

    private void actionSort() {
        if (SystemHelper.getCartItems() != null && SystemHelper.getCartItems().size() > 1) {
            Collections.sort(SystemHelper.getCartItems(), new SortComparator());
        }
    }

    private void dialogClear() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.btn_clear))
                .setMessage(getString(R.string.delete_inventory_list_text))
                .setPositiveButton(getString(R.string.btn_ok), (dialog, which) -> {
                    actionClear();
                    dialog.cancel();
                })
                .setNegativeButton(getString(R.string.btn_cancel), (dialog, which) -> dialog.cancel())
                .create();
        alert.show();
    }

    private void actionClear() {
        List<CartItem> removal = new ArrayList<>();
        for (CartItem item : SystemHelper.getCartItems()) {
            // delete items from this list only
            if (item.list == activeList.list) {
                removal.add(item);
            }
        }
        for (CartItem item : removal) {
            DBDriver.getInstance().delete(item);
        }
        SystemHelper.getCartItems().removeAll(removal);
        updateLists();
    }

    private void actionAdd() {
        openEditDialog();
    }

    private void actionBack() {
        this.finish();
    }

    private void actionHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.help_inventory)
                .setCancelable(false)
                .setNeutralButton(R.string.btn_ok, (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    OnLongClickListener onLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            TextView tv = ((View) v.getParent()).findViewById(R.id.inventorylistitem_id);
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
        return R.layout.inventoryscreen;
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