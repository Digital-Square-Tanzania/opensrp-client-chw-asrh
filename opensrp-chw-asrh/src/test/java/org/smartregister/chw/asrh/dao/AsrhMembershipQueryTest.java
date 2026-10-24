package org.smartregister.chw.asrh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.dao.AbstractDao;
import org.smartregister.repository.Repository;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = 28, manifest = Config.NONE)
public class AsrhMembershipQueryTest extends AsrhDao {
    private SQLiteDatabase data;
    private Repository previousRepository;
    private RecordingTree logs;

    @Before
    public void setUp() {
        previousRepository = ReflectionHelpers.getStaticField(AbstractDao.class, "repository");
        data = SQLiteDatabase.create(null);
        data.execSQL("CREATE TABLE ec_family_member (base_entity_id TEXT, unique_id TEXT,"
                + " relational_id TEXT, dob TEXT, first_name TEXT, middle_name TEXT, last_name TEXT,"
                + " gender TEXT, phone_number TEXT, other_phone_number TEXT, is_closed TEXT)");
        data.execSQL("CREATE TABLE ec_family (base_entity_id TEXT, first_name TEXT,"
                + " primary_caregiver TEXT, family_head TEXT, village_town TEXT, is_closed TEXT)");
        data.execSQL("CREATE TABLE ec_asrh_register (base_entity_id TEXT, hiv_status TEXT,"
                + " ctc_number TEXT, client_status TEXT, is_closed TEXT)");
        Repository repository = mock(Repository.class);
        net.zetetic.database.sqlcipher.SQLiteDatabase cipher =
                mock(net.zetetic.database.sqlcipher.SQLiteDatabase.class);
        when(repository.getReadableDatabase()).thenReturn(cipher);
        when(cipher.rawQuery(anyString(), nullable(String[].class)))
                .thenAnswer(call -> data.rawQuery(call.getArgument(0), call.getArgument(1)));
        setRepository(repository);
        logs = new RecordingTree();
        Timber.plant(logs);
    }

    @After
    public void tearDown() {
        Timber.uproot(logs);
        data.close();
        setRepository(previousRepository);
    }

    @Test
    public void activeEnrollmentIsMappedWhenEveryJoinedTableHasAnIsClosedColumn() {
        household("family", "0", "head", "caregiver");
        member("head", "family", "0", "1975-01-01");
        member("caregiver", "family", "0", "1980-01-01");
        member("active", "family", "0", "2007-01-01");
        enrollment("active", "0");

        List<MemberObject> members = AsrhDao.getMembers();

        assertNotNull(members);
        assertEquals(1, members.size());
        MemberObject member = members.get(0);
        assertEquals("active", member.getBaseEntityId());
        assertEquals("Name-active", member.getFirstName());
        assertEquals("Household", member.getFamilyName());
        assertEquals("Name-head Middle Family", member.getFamilyHeadName());
        assertEquals("Name-caregiver Middle Family", member.getPrimaryCareGiverName());
        assertEquals("ctc-active", member.getCtcNumber());
        assertTrue(logs.errors.isEmpty());
    }

    @Test
    public void closedAsrhEnrollmentIsExcludedEvenWhenTheFamilyAndMemberRemainOpen() {
        household("family", "0", "", "");
        member("active", "family", "0", "2007-01-01");
        member("closed", "family", "0", "1990-01-01");
        enrollment("active", "0");
        enrollment("closed", "1");

        List<MemberObject> members = AsrhDao.getMembers();

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals("active", members.get(0).getBaseEntityId());
        assertTrue(logs.errors.isEmpty());
    }

    @Test
    public void enrollmentStatusDrivesEligibilityInsteadOfUnrelatedJoinedStatusColumns() {
        household("family", "1", "head", "caregiver");
        member("head", "family", "1", "1975-01-01");
        member("caregiver", "family", "1", "1980-01-01");
        member("active", "family", "1", "2007-01-01");
        enrollment("active", "0");

        List<MemberObject> members = AsrhDao.getMembers();

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals("active", members.get(0).getBaseEntityId());
        assertTrue(logs.errors.isEmpty());
    }

    @Test
    public void olderActiveMembersRemainAvailableToTheAgeBasedClosureJob() {
        household("family", "0", "", "");
        member("older", "family", "0", "2000-01-01");
        enrollment("older", "0");
        DateTimeUtils.setCurrentMillisFixed(new DateTime("2026-10-23").getMillis());
        try {
            List<MemberObject> members = AsrhDao.getMembers();

            assertNotNull(members);
            assertEquals(1, members.size());
            assertEquals(26, members.get(0).getAge());
            assertTrue(logs.errors.isEmpty());
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void noActiveEnrollmentRetainsTheExistingNullResultWithoutAQueryError() {
        household("family", "0", "", "");
        member("closed", "family", "0", "1990-01-01");
        enrollment("closed", "1");

        assertNull(AsrhDao.getMembers());
        assertTrue(logs.errors.isEmpty());
    }

    private void household(String id, String closed, String head, String caregiver) {
        data.execSQL("INSERT INTO ec_family VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] {id, "Household", caregiver, head, "Village", closed});
    }

    private void member(String id, String familyId, String closed, String dob) {
        data.execSQL("INSERT INTO ec_family_member VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[] {id, "unique-" + id, familyId, dob, "Name-" + id, "Middle", "Family",
                        "Female", "0700000000", "", closed});
    }

    private void enrollment(String id, String closed) {
        data.execSQL("INSERT INTO ec_asrh_register VALUES (?, ?, ?, ?, ?)",
                new Object[] {id, "unknown", "ctc-" + id, "active", closed});
    }

    private static class RecordingTree extends Timber.Tree {
        private final List<String> errors = new ArrayList<>();

        @Override
        protected void log(int priority, String tag, String message, Throwable throwable) {
            if (priority == android.util.Log.ERROR) {
                errors.add(message);
            }
        }
    }
}
