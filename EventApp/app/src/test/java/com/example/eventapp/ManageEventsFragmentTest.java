package com.example.eventapp;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ApplicationProvider;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class ManageEventsFragmentTest {

    private MockedStatic<FirebaseHelper> firebaseHelperMock;
    private FirebaseFirestore mockFirestore;
    private CollectionReference mockEventAttendees;
    private DocumentReference mockEventDoc;
    private CollectionReference mockAttendeesCollection;

    @Before
    public void setup() {

        // Mock Firebase static helper
        firebaseHelperMock = Mockito.mockStatic(FirebaseHelper.class);

        mockFirestore = Mockito.mock(FirebaseFirestore.class);
        mockEventAttendees = Mockito.mock(CollectionReference.class);
        mockEventDoc = Mockito.mock(DocumentReference.class);
        mockAttendeesCollection = Mockito.mock(CollectionReference.class);

        // Return firestore
        firebaseHelperMock.when(FirebaseHelper::getFirestore).thenReturn(mockFirestore);

        // eventAttendees -> document(eventId) -> collection("attendees")
        Mockito.when(mockFirestore.collection("eventAttendees"))
                .thenReturn(mockEventAttendees);

        Mockito.when(mockEventAttendees.document(Mockito.anyString()))
                .thenReturn(mockEventDoc);

        Mockito.when(mockEventDoc.collection("attendees"))
                .thenReturn(mockAttendeesCollection);

        // Return an empty QuerySnapshot
        QuerySnapshot emptySnapshot = Mockito.mock(QuerySnapshot.class);
        Mockito.when(emptySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        Task<QuerySnapshot> emptyTask = Tasks.forResult(emptySnapshot);

        Mockito.when(mockAttendeesCollection.get()).thenReturn(emptyTask);
    }

    @After
    public void teardown() {
        firebaseHelperMock.close();
    }

    /**
     * Wrapper to safely launch fragment without ambiguous method call.
     */
    private ManageEventsFragment launch(Bundle args) {
        FragmentScenario<ManageEventsFragment> scenario =
                FragmentScenario.launchInContainer(
                        ManageEventsFragment.class,
                        args,
                        R.style.Theme_EventApp
                );

        final ManageEventsFragment[] holder = new ManageEventsFragment[1];
        scenario.onFragment(f -> holder[0] = f);
        return holder[0];
    }

    @Test
    public void fragmentLaunchesSuccessfully() {
        Bundle args = new Bundle();
        args.putString("eventId", "123");

        ManageEventsFragment frag = launch(args);

        assertNotNull(frag.requireView());
    }

    @Test
    public void recyclerViewIsPresent() {
        Bundle args = new Bundle();
        args.putString("eventId", "123");

        ManageEventsFragment frag = launch(args);

        View rv = frag.requireView().findViewById(R.id.recyclerViewList);
        assertNotNull(rv);
    }

    @Test
    public void clickingEditEvent_doesNotCrash() {

        Bundle args = new Bundle();
        args.putString("eventId", "123");
        args.putString("title", "Test");
        args.putString("desc", "D");
        args.putString("date", "01/01/2025");
        args.putString("time", "12:00");
        args.putString("location", "Campus");
        args.putString("imageUrl", "url");

        ManageEventsFragment frag = launch(args);

        // Mock NavController
        NavController mockNav = Mockito.mock(NavController.class);

        Navigation.setViewNavController(frag.requireView(), mockNav);

        View btn = frag.requireView().findViewById(R.id.btnEditEvent);
        btn.performClick();

        // Verify navigation occurred
        Mockito.verify(mockNav).navigate(
                Mockito.eq(R.id.action_manageEventsFragment_to_createEventFragment),
                Mockito.any(Bundle.class)
        );

        assertTrue(true);
    }
}
