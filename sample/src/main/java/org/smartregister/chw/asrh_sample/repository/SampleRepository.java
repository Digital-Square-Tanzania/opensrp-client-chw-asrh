package org.smartregister.chw.asrh_sample.repository;

import android.content.Context;
import android.text.TextUtils;

import net.zetetic.database.sqlcipher.SQLiteDatabase;

import org.smartregister.AllConstants;
import org.smartregister.chw.asrh_sample.application.SampleApplication;
import org.smartregister.chw.asrh_sample.BuildConfig;
import org.smartregister.chw.asrh.AsrhLibrary;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.SettingsRepository;
import org.smartregister.repository.UniqueIdRepository;

import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * Created by cozej4 on 17/08/2023.
 */
public class SampleRepository extends Repository {

    private static final String TAG = SampleRepository.class.getCanonicalName();
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;
    private Context context;
    private String password = "Sample_PASS";

    public SampleRepository(Context context, org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(), SampleApplication.createCommonFtsObject(), openSRPContext.sharedRepositoriesArray());
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        EventClientRepository.createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
        EventClientRepository.createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());

        createConfigurableViewsTable(database);

        UniqueIdRepository.createTable(database);
        SettingsRepository.onUpgrade(database);


        AsrhLibrary.getInstance().visitRepository().createTable(database);
        AsrhLibrary.getInstance().visitDetailsRepository().createTable(database);

        onUpgrade(database, 1, BuildConfig.DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w(SampleRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    //upgradeToVersion2(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }


    @Override
    public SQLiteDatabase getReadableDatabase() {
        return openReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return openWritableDatabase();
    }

    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        if (!TextUtils.isEmpty(password)) {
            this.password = password;
        }
        return openReadableDatabase();
    }

    private synchronized SQLiteDatabase openReadableDatabase() {
        if (TextUtils.isEmpty(password)) {
            Timber.e("Database password is empty.");
        }
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                if (readableDatabase != null) {
                    readableDatabase.close();
                }
                readableDatabase = super.getReadableDatabase();
            }
            return readableDatabase;
        } catch (Exception e) {
            Timber.e("Database Error. " + e.getMessage());
            return null;
        }

    }

    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (!TextUtils.isEmpty(password)) {
            this.password = password;
        }
        return openWritableDatabase();
    }

    private synchronized SQLiteDatabase openWritableDatabase() {
        if (TextUtils.isEmpty(password)) {
            Timber.e("Database password is empty.");
        }
        if (writableDatabase == null || !writableDatabase.isOpen()) {
            if (writableDatabase != null) {
                writableDatabase.close();
            }
            writableDatabase = super.getWritableDatabase();
        }
        return writableDatabase;
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }

    private void createConfigurableViewsTable(SQLiteDatabase database) {
        try {
            Method createTable = ConfigurableViewsRepository.class.getMethod("createTable", database.getClass());
            createTable.invoke(null, database);
        } catch (NoSuchMethodException e) {
            Timber.w("ConfigurableViewsRepository.createTable signature mismatch; skipping table creation.");
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
