package info.simplyapps.app.shoppinglist.screens;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import info.simplyapps.app.shoppinglist.CartUtils;
import info.simplyapps.app.shoppinglist.Constants;
import info.simplyapps.app.shoppinglist.R;
import info.simplyapps.app.shoppinglist.SystemHelper;
import info.simplyapps.app.shoppinglist.storage.DBDriver;
import info.simplyapps.app.shoppinglist.storage.FileDriver;
import info.simplyapps.app.shoppinglist.storage.StorageUtil;
import info.simplyapps.app.shoppinglist.storage.StoreData;
import info.simplyapps.app.shoppinglist.storage.dto.CartItem;
import info.simplyapps.app.shoppinglist.storage.dto.Inventory;
import info.simplyapps.appengine.screens.GenericScreenTemplate;
import info.simplyapps.appengine.storage.dto.Configuration;

public class ConfigureScreen extends GenericScreenTemplate {

    static final String TAG = "ShoppingList";

    private TableLayout lTable;
    private Dialog editDialog;
    private Dialog backupDialog;
    private EditText backupPath;
    private long listId = -1l;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lTable = (TableLayout) findViewById(R.id.tableLayoutConfigureList);

        createEditDialog();
        createBackupDialog();

        Button bAdd = (Button) findViewById(R.id.btn_add_config);
        bAdd.setOnClickListener(onButtonAdd);
        Button bClear = (Button) findViewById(R.id.btn_back_config);
        bClear.setOnClickListener(onButtonBack);

        Button bBackup = (Button) findViewById(R.id.btn_backup);
        bBackup.setOnClickListener(onButtonBackup);

        findViewById(R.id.btn_backup).setOnClickListener(onButtonBackup);

        CheckBox cbSort = (CheckBox) findViewById(R.id.configure_autosort);
        cbSort.setOnClickListener(onClickAutoSort);
        cbSort.setChecked(CartUtils.isAutoSort(getApplicationContext()));
        CheckBox cbTextSize = (CheckBox) findViewById(R.id.configure_autotextsize);
        cbTextSize.setOnClickListener(onClickAutoTextSize);
        cbTextSize.setChecked(CartUtils.isAutoTextSize(getApplicationContext()));

        SeekBar sbTextSize = (SeekBar) findViewById(R.id.configure_seektextsize);
        sbTextSize.setOnSeekBarChangeListener(onTextSizeChangeListener);

        if (CartUtils.getTextSize(getApplicationContext()) > 0) {
            TextView tv = (TextView) findViewById(R.id.configure_displaytextsize);
            tv.setText(Integer.toString(CartUtils.getTextSize(getApplicationContext())) + Constants.CONFIG_TEXT_UNIT);
            sbTextSize.setProgress(CartUtils.getTextSize(getApplicationContext()) - Constants.CONFIG_TEXT_ADD);
        }

        updateLists();
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
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View border = inflater.inflate(R.layout.editdialog, null);
        editDialog = new Dialog(this, R.style.Theme_Dialog);
        editDialog.setContentView(R.layout.editdialog);

        Button bOk = (Button) editDialog.findViewById(R.id.btn_ok);
        Button bCancel = (Button) editDialog.findViewById(R.id.btn_cancel);
        bOk.setOnClickListener(onEditDialogOk);
        bCancel.setOnClickListener(onEditDialogCancel);
    }

    public void openEditDialog() {
        // clear text
        EditText mText = (EditText) editDialog.findViewById(R.id.editText1);
        mText.setText("");
        if (listId > 0l) {
            for (Inventory item : SystemHelper.getInventories()) {
                if (item.id == listId) {
                    mText.setText(item.name);
                    break;
                }
            }
        }
        // popup window
        editDialog.show();
    }

    private void createBackupDialog() {

        // prepare backup dialog
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View border = inflater.inflate(R.layout.backupdialog, null);
        backupDialog = new Dialog(this, R.style.Theme_Dialog);
        backupDialog.setContentView(R.layout.backupdialog);

        Button bExport = (Button) backupDialog.findViewById(R.id.backup_export);
        Button bImport = (Button) backupDialog.findViewById(R.id.backup_import);
        Button bCancel = (Button) backupDialog.findViewById(R.id.backup_cancel);
        bExport.setOnClickListener(onBackupDialogExport);
        bImport.setOnClickListener(onBackupDialogImport);
        bCancel.setOnClickListener(onBackupDialogCancel);

        backupPath = (EditText) backupDialog.findViewById(R.id.backup_path);
    }

    public void openBackupDialog() {
        backupPath.setText(Environment.getExternalStorageDirectory().getPath());
        backupDialog.show();
    }

    public void updateLists() {
        lTable.removeAllViews();
        for (Inventory entry : SystemHelper.getInventories()) {

            View row = null;
            if (row == null) {
                // ROW INFLATION
                LayoutInflater inflater = (LayoutInflater) this.getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.configureitem, lTable, false);
            }
            if (row != null) {
                ImageButton btnDel;
                ImageButton btnEdit;
                TextView txtField;

                // Get reference to buttons
                btnDel = (ImageButton) row.findViewById(R.id.imageButton_deleteConfig);
                btnDel.setOnClickListener(onDeleteFromConfiguration);

                btnEdit = (ImageButton) row.findViewById(R.id.imageButton_editConfig);
                btnEdit.setOnClickListener(onEditConfiguration);

                // Get reference to TextView
                txtField = (TextView) row.findViewById(R.id.configurelistitem_textview);
                //set value into the list
                txtField.setText(entry.name);
                if (!CartUtils.isAutoTextSize(getApplicationContext()) && CartUtils.getTextSize(getApplicationContext()) > 0) {
                    txtField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CartUtils.getTextSize(getApplicationContext()));
                }

                txtField.setTextColor(Color.WHITE);
                lTable.addView(row);
            }
        }
    }

    OnClickListener onButtonAdd = new OnClickListener() {
        public void onClick(View v) {
            actionAdd();
        }
    };

    OnClickListener onButtonBackup = new OnClickListener() {
        public void onClick(View v) {
            actionBackup();
        }
    };

    OnClickListener onButtonBack = new OnClickListener() {
        public void onClick(View v) {
            actionBack();
        }
    };

    OnClickListener onClickAutoSort = new OnClickListener() {
        public void onClick(View v) {
            actionAutoSort();
        }
    };
    OnClickListener onClickAutoTextSize = new OnClickListener() {
        public void onClick(View v) {
            actionAutoTextSize();
        }
    };

    OnClickListener onDeleteFromConfiguration = new OnClickListener() {
        public void onClick(View v) {
            //get the row the clicked button is in
            actionDeleteFromConfiguration(v);
        }
    };

    private void actionDeleteFromConfiguration(final View v) {
        final LinearLayout vwParentRow = (LinearLayout) v.getParent();
        final TextView child = (TextView) vwParentRow.getChildAt(0);
        final String selected = child.getText().toString();

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(getApplicationContext().getString(R.string.delete_inventory))
                .setMessage(MessageFormat.format(getApplicationContext().getString(R.string.delete_inventory_text), selected))
                .setPositiveButton(getApplicationContext().getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Inventory item = null;
                        for (Inventory entry : SystemHelper.getInventories()) {
                            if (entry.name.equalsIgnoreCase(selected)) {
                                item = entry;
                                break;
                            }
                        }
                        if (item != null) {
                            List<CartItem> removals = new ArrayList<CartItem>();
                            for (CartItem entry : SystemHelper.getCartItems()) {
                                if (entry.list == item.list) {
                                    DBDriver.getInstance().delete(entry);
                                    removals.add(entry);
                                }
                            }
                            SystemHelper.getCartItems().removeAll(removals);
                            if (DBDriver.getInstance().delete(item)) {
                                SystemHelper.getInventories().remove(item);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.delete_failed, Toast.LENGTH_LONG).show();
                            }
                        }
                        TableLayout l = (TableLayout) v.getParent().getParent();
                        l.removeView(vwParentRow);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(v.getContext().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog d = b.create();
        d.show();
    }

    OnClickListener onEditConfiguration = new OnClickListener() {
        public void onClick(View v) {
            listId = -1l;
            LinearLayout vwParentRow = (LinearLayout) v.getParent();
            TextView child = (TextView) vwParentRow.getChildAt(0);
            String selected = child.getText().toString();
            for (Inventory entry : SystemHelper.getInventories()) {
                if (entry.name.equalsIgnoreCase(selected)) {
                    listId = entry.id;
                    break;
                }
            }
            //get the row the clicked button is in
            openEditDialog();
        }
    };

    private void readWriteExternalFile(boolean write) {
        FileDriver.readWriteExternalFile(this, write, backupPath.getText().toString());
    }

    OnSeekBarChangeListener onTextSizeChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            TextView tv = (TextView) findViewById(R.id.configure_displaytextsize);
            tv.setText(Integer.toString(progress + Constants.CONFIG_TEXT_ADD) + Constants.CONFIG_TEXT_UNIT);
            actionChangeTextSize(progress + Constants.CONFIG_TEXT_ADD);
            updateLists();
        }
    };


    OnClickListener onEditDialogOk = new OnClickListener() {
        public void onClick(View v) {
            EditText mText = (EditText) editDialog.findViewById(R.id.editText1);
            String text = mText.getText().toString();
            if (CartUtils.notEmpty(text)) {
                if (listId > -1l) {
                    // edit entry
                    for (Inventory item : SystemHelper.getInventories()) {
                        if (item.id == listId) {
                            item.name = text;
                            if (DBDriver.getInstance().store(item)) {
                                updateLists();
                            }
                            break;
                        }
                    }
                } else {
                    // add entry
                    boolean inList = false;
                    for (Inventory item : SystemHelper.getInventories()) {
                        if (item.name.equalsIgnoreCase(text)) {
                            inList = true;
                            break;
                        }
                    }
                    if (!inList) {
                        Inventory item = new Inventory();
                        item.name = text;
                        item.order = getNextOrder();
                        item.list = getNextList();
                        if (DBDriver.getInstance().store(item)) {
                            SystemHelper.addInventory(item);
                            updateLists();
                        }
                    }
                }
            }
            editDialog.dismiss();
        }
    };

    private int getNextOrder() {
        int last = 0;
        for (Inventory item : SystemHelper.getInventories()) {
            if (last < item.order) {
                last = item.order;
            }
        }
        return ++last;
    }

    private int getNextList() {
        int last = 0;
        for (Inventory item : SystemHelper.getInventories()) {
            if (last < item.list) {
                last = item.list;
            }
        }
        return ++last;
    }

    OnClickListener onEditDialogCancel = new OnClickListener() {
        @Override
        public void onClick(View v) {
            editDialog.dismiss();
        }
    };

    OnClickListener onBackupDialogImport = new OnClickListener() {
        public void onClick(View v) {
            readWriteExternalFile(false);
            DBDriver.getInstance().write(StoreData.getInstance());
            updateLists();
            backupDialog.dismiss();
        }
    };

    OnClickListener onBackupDialogExport = new OnClickListener() {
        public void onClick(View v) {
            readWriteExternalFile(true);
            backupDialog.dismiss();
        }
    };

    OnClickListener onBackupDialogCancel = new OnClickListener() {
        public void onClick(View v) {
            backupDialog.dismiss();
        }
    };

    private void actionAdd() {
        listId = -1l;
        openEditDialog();
    }

    private void actionBack() {
        this.finish();
    }

    private void actionBackup() {
        openBackupDialog();
    }

    private void actionAutoSort() {
        Configuration config = SystemHelper.getConfiguration(Constants.CONFIG_AUTOSORT, Constants.DEFAULT_CONFIG_AUTOSORT);
        config.value = Boolean.toString(!Boolean.valueOf(config.value).booleanValue());
        DBDriver.getInstance().store(config);
    }

    private void actionAutoTextSize() {
        Configuration config = SystemHelper.getConfiguration(Constants.CONFIG_AUTOTEXTSIZE, Constants.DEFAULT_CONFIG_AUTOTEXTSIZE);
        config.value = Boolean.toString(!Boolean.valueOf(config.value).booleanValue());
        DBDriver.getInstance().store(config);
        updateLists();
    }

    private void actionChangeTextSize(int newSize) {
        Configuration config = SystemHelper.getConfiguration(Constants.CONFIG_TEXTSIZE, Constants.DEFAULT_CONFIG_TEXTSIZE);
        config.value = Integer.toString(newSize);
        DBDriver.getInstance().store(config);
    }


    @Override
    public int getScreenLayout() {
        return R.layout.configurescreen;
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