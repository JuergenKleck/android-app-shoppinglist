package info.simplyapps.app.shoppinglist.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import info.simplyapps.app.shoppinglist.storage.dto.CartItem;
import info.simplyapps.app.shoppinglist.storage.dto.Inventory;
import info.simplyapps.appengine.storage.Migrate;
import info.simplyapps.appengine.storage.MigrationHelper;
import info.simplyapps.appengine.storage.dto.BasicTable;
import info.simplyapps.appengine.storage.dto.Purchases;

public class DBDriver extends info.simplyapps.appengine.storage.DBDriver {

    private static final String SQL_CREATE_INVENTORY =
            "CREATE TABLE " + StorageContract.TableInventory.TABLE_NAME + " (" +
                    StorageContract.TableInventory._ID + " INTEGER PRIMARY KEY," +
                    StorageContract.TableInventory.COLUMN_NAME + TYPE_TEXT + COMMA_SEP +
                    StorageContract.TableInventory.COLUMN_ORDER + TYPE_TEXT + COMMA_SEP +
                    StorageContract.TableInventory.COLUMN_LIST + TYPE_TEXT +
                    " );";
    private static final String SQL_CREATE_CARTITEM =
            "CREATE TABLE " + StorageContract.TableCartItem.TABLE_NAME + " (" +
                    StorageContract.TableCartItem._ID + " INTEGER PRIMARY KEY," +
                    StorageContract.TableCartItem.COLUMN_NAME + TYPE_TEXT + COMMA_SEP +
                    StorageContract.TableCartItem.COLUMN_QUANTITY + TYPE_TEXT + COMMA_SEP +
                    StorageContract.TableCartItem.COLUMN_LIST + TYPE_INT + COMMA_SEP +
                    StorageContract.TableCartItem.COLUMN_INCART + TYPE_INT + COMMA_SEP +
                    StorageContract.TableCartItem.COLUMN_INCART_BOUGHT + TYPE_INT +
                    " );";

    private static final String SQL_DELETE_INVENTORY =
            "DROP TABLE IF EXISTS " + StorageContract.TableInventory.TABLE_NAME;
    private static final String SQL_DELETE_CARTITEM =
            "DROP TABLE IF EXISTS " + StorageContract.TableCartItem.TABLE_NAME;

    public DBDriver(String dataBaseName, int dataBaseVersion, Context context) {
        super(dataBaseName, dataBaseVersion, context);
    }

    public static DBDriver getInstance() {
        return (DBDriver) info.simplyapps.appengine.storage.DBDriver.getInstance();
    }

    @Override
    public void createTables(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY);
        db.execSQL(SQL_CREATE_CARTITEM);
    }

    @Override
    public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion != oldVersion && newVersion == 2) {
            // inventory - added list column
            // cartitem - nothing
            // configuration - nothing
            // extension - new
            // purchases - added id column
            final String alterInventory = "ALTER TABLE " + StorageContract.TableInventory.TABLE_NAME +
                    " ADD COLUMN " + StorageContract.TableInventory.COLUMN_LIST + TYPE_TEXT + ";";
            db.execSQL(alterInventory);
            db.execSQL(SQL_CREATE_EXTENSIONS);

            // convert purchases
            List<String> old = new ArrayList<String>();
            String[] projection = {StorageContract.TablePurchases.COLUMN_NAME};
            String sortOrder = StorageContract.TablePurchases.COLUMN_NAME + " ASC";
            Cursor c = null;
            try {
                c = openCursor(db, projection, sortOrder, StorageContract.TablePurchases.TABLE_NAME);
                boolean hasResults = c.moveToFirst();
                while (hasResults) {
                    old.add(c.getString(c.getColumnIndexOrThrow(StorageContract.TablePurchases.COLUMN_NAME)));
                    hasResults = c.moveToNext();
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            db.execSQL(SQL_DELETE_PURCHASES);
            db.execSQL(SQL_CREATE_PURCHASES);

            for (String purchase : old) {
                Purchases p = new Purchases(purchase);
                MigrationHelper.add(new Migrate(0, 4, p));
            }

        }
    }

    @Override
    public String getExtendedTable(BasicTable data) {
        return Inventory.class.isInstance(data) ? StorageContract.TableInventory.TABLE_NAME :
                CartItem.class.isInstance(data) ? StorageContract.TableCartItem.TABLE_NAME : null;
    }

    @Override
    public void storeExtended(info.simplyapps.appengine.storage.StoreData data) {
        store(StoreData.class.cast(data).inventories);
        storeCartItems(StoreData.class.cast(data).shoppingCart);
    }

    @Override
    public void readExtended(info.simplyapps.appengine.storage.StoreData data, SQLiteDatabase db) {
        readInventory(StoreData.class.cast(data), db);
        readCartItem(StoreData.class.cast(data), db);
    }

    @Override
    public info.simplyapps.appengine.storage.StoreData createStoreData() {
        return new StoreData();
    }

    public void clearCartItems(List<CartItem> dataList) {
        for (CartItem data : dataList) {
            delete(data);
        }
    }

    public boolean storeCartItems(List<CartItem> dataList) {
        boolean stored = true;
        for (CartItem data : dataList) {
            stored &= store(data);
        }
        return stored;
    }

    public void clear(List<Inventory> dataList) {
        for (Inventory data : dataList) {
            delete(data);
        }
    }

    public boolean store(List<Inventory> dataList) {
        boolean stored = true;
        for (Inventory data : dataList) {
            stored &= store(data);
        }
        return stored;
    }

    public boolean store(CartItem data) {
        ContentValues values = new ContentValues();
        values.put(StorageContract.TableCartItem.COLUMN_NAME, data.name);
        values.put(StorageContract.TableCartItem.COLUMN_QUANTITY, data.quantity);
        values.put(StorageContract.TableCartItem.COLUMN_LIST, data.list);
        values.put(StorageContract.TableCartItem.COLUMN_INCART, data.inCart);
        values.put(StorageContract.TableCartItem.COLUMN_INCART_BOUGHT, data.inCartBought);
        return persist(data, values, StorageContract.TableCartItem.TABLE_NAME);
    }

    public boolean store(Inventory data) {
        ContentValues values = new ContentValues();
        values.put(StorageContract.TableInventory.COLUMN_NAME, data.name);
        values.put(StorageContract.TableInventory.COLUMN_ORDER, data.order);
        values.put(StorageContract.TableInventory.COLUMN_LIST, data.list);
        return persist(data, values, StorageContract.TableInventory.TABLE_NAME);
    }

    private void readInventory(StoreData data, SQLiteDatabase db) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StorageContract.TableInventory._ID,
                StorageContract.TableInventory.COLUMN_NAME,
                StorageContract.TableInventory.COLUMN_ORDER,
                StorageContract.TableInventory.COLUMN_LIST
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = StorageContract.TableInventory.COLUMN_ORDER + " ASC";
        String selection = null;
        String[] selectionArgs = null;
        Cursor c = db.query(
                StorageContract.TableInventory.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        boolean hasResults = c.moveToFirst();
        while (hasResults) {
            Inventory i = new Inventory();
            i.id = c.getLong(c.getColumnIndexOrThrow(StorageContract.TableInventory._ID));
            i.name = c.getString(c.getColumnIndexOrThrow(StorageContract.TableInventory.COLUMN_NAME));
            i.order = c.getInt(c.getColumnIndexOrThrow(StorageContract.TableInventory.COLUMN_ORDER));
            i.list = c.getInt(c.getColumnIndexOrThrow(StorageContract.TableInventory.COLUMN_LIST));
            data.inventories.add(i);
            hasResults = c.moveToNext();
        }
        c.close();
    }

    private void readCartItem(StoreData data, SQLiteDatabase db) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StorageContract.TableCartItem._ID,
                StorageContract.TableCartItem.COLUMN_NAME,
                StorageContract.TableCartItem.COLUMN_QUANTITY,
                StorageContract.TableCartItem.COLUMN_LIST,
                StorageContract.TableCartItem.COLUMN_INCART,
                StorageContract.TableCartItem.COLUMN_INCART_BOUGHT
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = StorageContract.TableCartItem._ID + " ASC";
        String selection = null;
        String[] selectionArgs = null;
        Cursor c = db.query(
                StorageContract.TableCartItem.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        boolean hasResults = c.moveToFirst();
        while (hasResults) {
            CartItem i = new CartItem();
            i.id = c.getLong(c.getColumnIndexOrThrow(StorageContract.TableCartItem._ID));
            i.name = c.getString(c.getColumnIndexOrThrow(StorageContract.TableCartItem.COLUMN_NAME));
            i.quantity = c.getString(c.getColumnIndexOrThrow(StorageContract.TableCartItem.COLUMN_QUANTITY));
            i.list = c.getInt(c.getColumnIndexOrThrow(StorageContract.TableCartItem.COLUMN_LIST));
            i.inCart = c.getInt(c.getColumnIndexOrThrow(StorageContract.TableCartItem.COLUMN_INCART)) == 1;
            i.inCartBought = c.getInt(c.getColumnIndexOrThrow(StorageContract.TableCartItem.COLUMN_INCART_BOUGHT)) == 1;
            data.shoppingCart.add(i);
            hasResults = c.moveToNext();
        }
        c.close();
    }

}
