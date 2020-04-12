package info.simplyapps.app.shoppinglist.storage;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import info.simplyapps.app.shoppinglist.R;
import info.simplyapps.app.shoppinglist.SystemHelper;
import info.simplyapps.app.shoppinglist.storage.dto.CartItem;
import info.simplyapps.app.shoppinglist.storage.dto.Inventory;
import info.simplyapps.app.shoppinglist.storage.dto.RootItem;

public class FileDriver {

    private static final String ns = null;

    public static final String FILENAME = "simplyapps.shoppinglist.xml";

    public static void readWriteExternalFile(Activity activity, boolean write, String fullpath) {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (write && mExternalStorageAvailable && mExternalStorageWriteable) {
            // persist file
            if (hasExternalStoragePrivateFile(fullpath)) {
                deleteExternalStoragePrivateFile(fullpath);
            }
            createExternalStoragePrivateFile(activity, fullpath);

        } else if (!write && mExternalStorageAvailable) {
            // read file
            if (hasExternalStoragePrivateFile(fullpath)) {
                readExternalStoragePrivateFile(activity, fullpath);
            } else {
                Toast.makeText(activity, R.string.import_notfound, Toast.LENGTH_LONG).show();
            }
        }

    }

    private static void createExternalStoragePrivateFile(Activity activity, String fullpath) {
        // Create a path where we will place our private file on external
        // storage.
        File file = new File(fullpath, FILENAME);

        try {
            StringBuilder sb = new StringBuilder();

            openTag(sb, "rootitem");
            for (Inventory item : SystemHelper.getInventories()) {
                writeInventory(sb, item);
            }
            for (CartItem item : SystemHelper.getCartItems()) {
                if (item.list > -1) {
                    writeCartItem(sb, item);
                }
            }
            closeTag(sb, "rootitem");

            OutputStream os = new FileOutputStream(file);
            os.write(sb.toString().getBytes());
            os.close();
            String done = activity.getResources().getString(R.string.export_done);
            Toast.makeText(activity, done + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
            Log.w("ExternalStorage", "Error writing " + file.getAbsolutePath(), e);
        }
    }

    private static void readExternalStoragePrivateFile(Activity activity, String fullpath) {
        File file = new File(fullpath, FILENAME);
        try {
            FileInputStream fis = new FileInputStream(file);
            RootItem rootItem = parse(fis);

            // clean database and prepare inserting of new items
            synchronized (StoreData.getInstance()) {
                // clean existing data
                for (CartItem item : SystemHelper.getCartItems()) {
                    DBDriver.getInstance().delete(item);
                }
                for (Inventory item : SystemHelper.getInventories()) {
                    DBDriver.getInstance().delete(item);
                }
                SystemHelper.getCartItems().clear();
                SystemHelper.getInventories().clear();
                // create new data
                for (Inventory item : rootItem.inventories) {
                    if (DBDriver.getInstance().store(item)) {
                        SystemHelper.getInventories().add(item);
                    }
                }
                for (CartItem item : rootItem.cartitems) {
                    if (item.list > -1) {
                        if (DBDriver.getInstance().store(item)) {
                            SystemHelper.getCartItems().add(item);
                        }
                    }
                }
            }
            Toast.makeText(activity, R.string.import_done, Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
            Log.w("ExternalStorage", "Error reading " + file.getAbsolutePath(), e);
        } catch (XmlPullParserException e) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
            Log.w("ExternalStorage", "Error reading " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
            Log.w("ExternalStorage", "Error reading " + file.getAbsolutePath(), e);
        }

    }

    private static void deleteExternalStoragePrivateFile(String fullpath) {
        // Get path for the file on external storage. If external
        // storage is not currently mounted this will fail.
        File file = new File(fullpath, FILENAME);
        if (file != null) {
            file.delete();
        }
    }

    private static boolean hasExternalStoragePrivateFile(String fullpath) {
        // Get path for the file on external storage. If external
        // storage is not currently mounted this will fail.
        File file = new File(fullpath, FILENAME);
        boolean exists = false;
        if (file != null) {
            exists = file.exists();
        }
        return exists;
    }

    // We don't use namespaces

    private static RootItem parse(InputStream in) throws XmlPullParserException,
            IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readRootItem(parser);
        } finally {
            in.close();
        }
    }

    private static RootItem readRootItem(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        RootItem entries = new RootItem();

        parser.require(XmlPullParser.START_TAG, ns, "rootitem");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("inventory")) {
                entries.inventories.add(readInventory(parser));
            } else if (name.equals("cartitem")) {
                entries.cartitems.add(readCartItem(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // Processes link tags in the xml.
    private static Inventory readInventory(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        Inventory value = new Inventory();
        parser.require(XmlPullParser.START_TAG, ns, "inventory");
        String tag = parser.getName();
        if (tag.equals("inventory")) {
            value.order = Integer.valueOf(parser.getAttributeValue(null, "order"));
            value.list = Integer.valueOf(parser.getAttributeValue(null, "list"));
            value.name = readText(parser);
//			parser.nextTag();
        }

        parser.require(XmlPullParser.END_TAG, ns, "inventory");
        return value;
    }

    // Processes link tags in the xml.
    private static CartItem readCartItem(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        CartItem value = new CartItem();
        parser.require(XmlPullParser.START_TAG, ns, "cartitem");
        String tag = parser.getName();
        if (tag.equals("cartitem")) {
            value.quantity = parser.getAttributeValue(null, "quantity");
            value.list = Long.valueOf(parser.getAttributeValue(null, "list"));
            value.name = readText(parser);
        }

        parser.require(XmlPullParser.END_TAG, ns, "cartitem");
        return value;
    }

    // For the tags title and summary, extracts their text values.
    private static String readText(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested
    // tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps
    // going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being
    // 0).
    private static void skip(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static void writeInventory(StringBuilder sb, Inventory item) {
        openAttributeTag(sb, "inventory");
        writeAttribute(sb, "order", item.order);
        writeAttribute(sb, "list", item.list);
        finishAttributeTag(sb, "inventory");
        sb.append(item.name);
        closeTag(sb, "inventory");
    }

    private static void writeCartItem(StringBuilder sb, CartItem item) {
        openAttributeTag(sb, "cartitem");
        writeAttribute(sb, "quantity", item.quantity);
        writeAttribute(sb, "list", item.list);
        finishAttributeTag(sb, "cartitem");
        sb.append(item.name);
        closeTag(sb, "cartitem");
    }

    private static void writeAttribute(StringBuilder sb, String attribute,
                                       Object value) {
        sb.append(" ");
        sb.append(attribute);
        sb.append("=\"");
        sb.append(value);
        sb.append("\"");
    }

    private static void openTag(StringBuilder sb, String tag) {
        sb.append("<");
        sb.append(tag);
        sb.append(">");
    }

    private static void openAttributeTag(StringBuilder sb, String tag) {
        sb.append("<");
        sb.append(tag);
    }

    private static void finishAttributeTag(StringBuilder sb, String tag) {
        sb.append(">");
    }

    private static void closeTag(StringBuilder sb, String tag) {
        sb.append("</");
        sb.append(tag);
        sb.append(">");
    }

}
