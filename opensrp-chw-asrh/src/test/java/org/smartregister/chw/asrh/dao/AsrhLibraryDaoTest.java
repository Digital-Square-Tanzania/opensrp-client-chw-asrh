package org.smartregister.chw.asrh.dao;

import net.zetetic.database.sqlcipher.SQLiteDatabase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.smartregister.repository.Repository;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SQLiteDatabase.class)
public class AsrhLibraryDaoTest extends AsrhDao {

    @Mock
    private Repository repository;

    private SQLiteDatabase database;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        database = PowerMockito.mock(SQLiteDatabase.class);
        setRepository(repository);
    }

    @Test
    public void testIsRegisteredForMalaria() {
        Mockito.doReturn(database).when(repository).getReadableDatabase();
        boolean registered = AsrhDao.isRegisteredForAsrh("12345");
        Mockito.verify(database).rawQuery(Mockito.anyString(), Mockito.any());
        Assert.assertFalse(registered);
    }
}
