package ch.epfl.sweng.favors.database;

import android.app.Activity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import java.util.HashMap;
import ch.epfl.sweng.favors.utils.ExecutionMode;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FirebaseDatabaseTest {
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock private FirebaseFirestore fakeDb;
    @Mock private CollectionReference fakeCollection;
    @Mock private DocumentReference fakeDoc;
    @Mock private DocumentSnapshot fakeDocSnap;
    @Mock private Activity fakeActivity;
    @Mock private ListenerRegistration fakeListenerRegistration;

    private Task<DocumentSnapshot> fakeTask;
    private Task<Void> fakeSetTask;
    private HashMap<String, Object> data;
    private String FAKE_DOC_ID = "klsafdjdlksdf";
    private String NEW_TITLE = "New favor title";
    private String FAKE_COLLECTION = "favors";
    private EventListener<QuerySnapshot> fakeListener;

    @Before
    public void Before() {
        ExecutionMode.getInstance().setTest(true);
        FirebaseDatabase.getInstance();
        data = new HashMap<>();
        fakeTask = Tasks.forResult(fakeDocSnap);
        fakeSetTask = Tasks.forResult(null);
        when(fakeDb.collection(FAKE_COLLECTION)).thenReturn(fakeCollection);
        when(fakeCollection.document(FAKE_DOC_ID)).thenReturn(fakeDoc);
        when(fakeCollection.addSnapshotListener(fakeActivity,fakeListener)).thenReturn(fakeListenerRegistration);
        when(fakeDoc.get()).thenReturn(fakeTask);
        when(fakeDoc.set(any())).thenReturn(fakeSetTask);
        when(fakeDocSnap.getData()).thenReturn(data);
        FirebaseDatabase.setFirebaseTest(fakeDb);
    }

    @Test
    public void updateFromAndOnDbTest() {
        Favor favor = new Favor(FAKE_DOC_ID);
        FirebaseDatabase.getInstance().updateFromDb(favor).addOnCompleteListener(t -> {
            favor.set(Favor.StringFields.title, NEW_TITLE);
            FirebaseDatabase.getInstance().updateOnDb(favor);
            assertEquals("New favor title", favor.get(Favor.StringFields.title));
        });
    }

    @Test
    public void addSnapshotListenerTest(){
        assertEquals(fakeListenerRegistration,FirebaseDatabase.getInstance().addSnapshotListener(fakeActivity,FAKE_COLLECTION,fakeListener));
    }

    @After
    public void After(){
        FakeDatabase.getInstance().addExtraToDb();
        FakeDatabase.getInstance().removeExtraFromDB();
        Database.cleanUpAll();
    }
}