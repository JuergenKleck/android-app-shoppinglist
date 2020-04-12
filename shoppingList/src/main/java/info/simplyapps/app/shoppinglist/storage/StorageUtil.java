package info.simplyapps.app.shoppinglist.storage;

import android.content.Context;
import android.widget.Toast;

import info.simplyapps.app.shoppinglist.Constants;
import info.simplyapps.appengine.AppEngineConstants;
import info.simplyapps.appengine.R;
import info.simplyapps.appengine.SystemHelper;
import info.simplyapps.appengine.storage.DBDriver;
import info.simplyapps.appengine.storage.dto.Configuration;

public final class StorageUtil {

    public synchronized static void prepareStorage(Context context) {
        if (DBDriver.getInstance() == null) {
            DBDriver.createInstance(new info.simplyapps.app.shoppinglist.storage.DBDriver(Constants.DATABASE, Constants.DATABASE_VERSION, context));
        }

        // try to load
        if (StoreData.getInstance() == null) {
            StoreData.createInstance(DBDriver.getInstance().read());
        }
        // create
        if (StoreData.getInstance() == null) {
            StoreData.createInstance(new StoreData());
        }
        // migration
        Configuration cMig = SystemHelper.getConfiguration(AppEngineConstants.CONFIG_MIGRATION, AppEngineConstants.DEFAULT_CONFIG_MIGRATION);
        if (!SystemHelper.hasConfiguration(AppEngineConstants.CONFIG_MIGRATION)) {
            // will store automatically if migration is lower than this value
            StoreData.getInstance().configuration.add(cMig);
        }
        StoreData.getInstance().migration = Integer.valueOf(cMig.value);

        // update
        if (StoreData.getInstance().update()) {
            // store back the migration value
            Configuration c = SystemHelper.getConfiguration(AppEngineConstants.CONFIG_MIGRATION, AppEngineConstants.DEFAULT_CONFIG_MIGRATION);
            c.value = Integer.toString(StoreData.getInstance().migration);
            DBDriver.getInstance().write(StoreData.getInstance());
            Toast.makeText(context, R.string.update_complete, Toast.LENGTH_LONG).show();
        }
        if (!SystemHelper.hasConfiguration(AppEngineConstants.CONFIG_FORCE_UPDATE)) {
            Configuration c = new Configuration(AppEngineConstants.CONFIG_FORCE_UPDATE, AppEngineConstants.DEFAULT_CONFIG_FORCE_UPDATE);
            if (DBDriver.getInstance().store(c)) {
                StoreData.getInstance().configuration.add(c);
            }
        }
        if (!SystemHelper.hasConfiguration(AppEngineConstants.CONFIG_LAST_CHECK)) {
            Configuration c = new Configuration(AppEngineConstants.CONFIG_LAST_CHECK, AppEngineConstants.DEFAULT_CONFIG_LAST_CHECK);
            if (DBDriver.getInstance().store(c)) {
                StoreData.getInstance().configuration.add(c);
            }
        }
        if (!SystemHelper.hasConfiguration(AppEngineConstants.CONFIG_ON_SERVER)) {
            Configuration c = new Configuration(AppEngineConstants.CONFIG_ON_SERVER, AppEngineConstants.DEFAULT_CONFIG_ON_SERVER);
            if (DBDriver.getInstance().store(c)) {
                StoreData.getInstance().configuration.add(c);
            }
        }
        // store additional data

        if (!SystemHelper.hasConfiguration(Constants.CONFIG_AUTOSORT)) {
            Configuration c = new Configuration(Constants.CONFIG_AUTOSORT, Constants.DEFAULT_CONFIG_AUTOSORT);
            if (DBDriver.getInstance().store(c)) {
                StoreData.getInstance().configuration.add(c);
            }
        }
        if (!SystemHelper.hasConfiguration(Constants.CONFIG_AUTOTEXTSIZE)) {
            Configuration c = new Configuration(Constants.CONFIG_AUTOTEXTSIZE, Constants.DEFAULT_CONFIG_AUTOTEXTSIZE);
            if (DBDriver.getInstance().store(c)) {
                StoreData.getInstance().configuration.add(c);
            }
        }
        if (!SystemHelper.hasConfiguration(Constants.CONFIG_TEXTSIZE)) {
            Configuration c = new Configuration(Constants.CONFIG_TEXTSIZE, Constants.DEFAULT_CONFIG_TEXTSIZE);
            if (DBDriver.getInstance().store(c)) {
                StoreData.getInstance().configuration.add(c);
            }
        }

    }

}
