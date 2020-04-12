package info.simplyapps.app.shoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import info.simplyapps.app.shoppinglist.listener.ListenerCollection;
import info.simplyapps.app.shoppinglist.screens.ConfigureScreen;
import info.simplyapps.app.shoppinglist.screens.InventoryScreen;
import info.simplyapps.app.shoppinglist.storage.DBDriver;
import info.simplyapps.app.shoppinglist.storage.StorageUtil;
import info.simplyapps.app.shoppinglist.storage.dto.CartItem;
import info.simplyapps.appengine.screens.GenericScreenTemplate;

public class ShoppingListScreen extends GenericScreenTemplate {

    private TableLayout lTable;
    private Dialog editDialog;
    private CartItem editItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//		if(CartUtils.hasOriginalVersion(getApplicationContext()) && !isFullVersion()) {
//            if(DBDriver.store(getApplicationContext(), Constants.EXPANSION_FULL_ID)) {
//            	ShoppingListScreen.storeData.purchases.add(Constants.EXPANSION_FULL_ID);
//            	Toast.makeText(this, R.string.migration, Toast.LENGTH_LONG).show();
//            }
//		}

        // DEBUG MODE
//        if(SystemHelper.isDemo()) { 
//            Purchases purchase = new Purchases(Constants.EXPANSION_FULL_ID);    
//            SystemHelper.addPurchase(purchase);
//            DBDriver.getInstance().store(purchase);
//        }		
        // DEBUG MODE

        lTable = (TableLayout) findViewById(R.id.tableLayoutShoppingList);

        createEditDialog();

        Button bAdd = (Button) findViewById(R.id.btn_add);
        bAdd.setOnClickListener(onButtonAdd);
        Button bInventory = (Button) findViewById(R.id.btn_inventory);
        bInventory.setOnClickListener(onButtonInventory);
    }

//    public static boolean isFullVersion() {
//    	return !SystemHelper.isDemo();
//    }

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
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View border = inflater.inflate(R.layout.editdialog2, null);
        editDialog = new Dialog(this, R.style.Theme_Dialog);
        editDialog.setContentView(R.layout.editdialog2);

        Button bOk = (Button) editDialog.findViewById(R.id.btn_ok);
        Button bCancel = (Button) editDialog.findViewById(R.id.btn_cancel);
        bOk.setOnClickListener(onEditDialogOk);
        bCancel.setOnClickListener(onEditDialogCancel);

    }

    public void openEditDialog() {
        // clear text
        AutoCompleteTextView mText = (AutoCompleteTextView) editDialog.findViewById(R.id.autoCompleteTextView);
        List<String> strings = new ArrayList<>();

        for (CartItem tmpItem : SystemHelper.getCartItems()) {
            strings.add(tmpItem.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, strings);
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

            View row = null;
            if (row == null) {
                // ROW INFLATION
                LayoutInflater inflater = (LayoutInflater) this.getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.shoppinglistitem, lTable, false);
            }
            if (row != null) {
                ImageButton btnDel;
                TextView txtField;
                TextView idField;
                boolean inBoughtList = entry.inCartBought;

                // Get reference to buttons
                btnDel = (ImageButton) row.findViewById(R.id.imageButton_deleteCart);

                btnDel.setOnClickListener(ListenerCollection.mDeleteFromShoppingList);

                // Get reference to TextView
                txtField = (TextView) row.findViewById(R.id.shoppinglistitem_textview);
                idField = (TextView) row.findViewById(R.id.shoppinglistitem_id);
                //set value into the list
                txtField.setText(entry.name);
                if (!CartUtils.isAutoTextSize(getApplicationContext()) && CartUtils.getTextSize(getApplicationContext()) > 0) {
                    txtField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CartUtils.getTextSize(getApplicationContext()));
//					LayoutParams params = new LayoutParams(CartUtils.getTextSize(getApplicationContext())+Constants.CONFIG_IMAGE_ADD, CartUtils.getTextSize(getApplicationContext())+Constants.CONFIG_IMAGE_ADD);
//					btnDel.setLayoutParams(params);
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


    OnClickListener onButtonInventory = new OnClickListener() {
        public void onClick(View v) {
            actionInventory();
        }
    };

    OnClickListener onButtonAdd = new OnClickListener() {
        public void onClick(View v) {
            actionAdd();
        }
    };

    OnClickListener onEditDialogOk = new OnClickListener() {
        public void onClick(View v) {
            AutoCompleteTextView mText = (AutoCompleteTextView) View.class.cast(v.getParent()).findViewById(R.id.autoCompleteTextView);
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
        List<CartItem> removal = new ArrayList<CartItem>();
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
//    	if(!isFullVersion()) {
//    		if(SystemHelper.getCartItems().size() > Constants.DEMO_LIMITATION) {
//    			dialogDemo();
//    		} else {
//        		openEditDialog();
//    		}
//    	} else {
        editItem = null;
        openEditDialog();
//    	}
    }

    private void actionHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.help_shoppinglist)
                .setCancelable(false)
                .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void actionSort() {
        if (SystemHelper.getCartItems() != null && SystemHelper.getCartItems().size() > 1) {
            Collections.sort(SystemHelper.getCartItems(), new SortComparator());
        }
    }

    private void openConfigurationScreen() {
        Intent wordIntent = new Intent(this, ConfigureScreen.class);
        wordIntent.setData(this.getIntent().getData());
        this.startActivity(wordIntent);
    }

    private void actionAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about_app);
        builder.setMessage(R.string.about_text)
                .setCancelable(false);
//    	if(isFullVersion()) {
        builder.setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
//    	} else {
//    		builder.setPositiveButton(getString(R.string.btn_buy), new DialogInterface.OnClickListener() {
//        		public void onClick(DialogInterface dialog, int which) {
//        			openConfigurationScreen();
//        		}
//        	});
//        	builder.setNegativeButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
//        		public void onClick(DialogInterface dialog, int which) {
//        			dialog.cancel();
//        		}
//        	});
//    	}

        AlertDialog alert = builder.create();
        alert.show();
    }

//    private void dialogDemo() {
//    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
//    	builder.setTitle(R.string.demolimit_title);
//    	builder.setMessage(R.string.demolimit_text)
//    	       .setCancelable(false);
//    	builder.setPositiveButton(getString(R.string.btn_buy), new DialogInterface.OnClickListener() {
//    		public void onClick(DialogInterface dialog, int which) {
//    			openConfigurationScreen();
//    		}
//    	});
//    	builder.setNegativeButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
//    		public void onClick(DialogInterface dialog, int which) {
//    			dialog.cancel();
//    		}
//    	});
//
//    	AlertDialog alert = builder.create();
//    	alert.show();
//    }

    private void dialogClear() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.btn_clear))
                .setMessage(getString(R.string.delete_list_text))
                .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        actionClear();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        alert.show();
    }

    OnLongClickListener onLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            TextView tv = (TextView) ((View) v.getParent()).findViewById(R.id.shoppinglistitem_id);
            long itemId = Long.valueOf(tv.getText().toString()).longValue();

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

}