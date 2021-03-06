package ch.epfl.sweng.favors.database;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

import ch.epfl.sweng.favors.database.fields.DatabaseField;
import ch.epfl.sweng.favors.main.FavorsMain;

import static ch.epfl.sweng.favors.main.FavorsMain.TAG;

/**
 * Firebase is used as our backend, specifically Firestore
 *
 * the important methods of this class are the
 * updateFromDB,
 * update(TO)OnDB and
 * deleteFromDB
 *
 */
public class FirebaseDatabase extends Database {

    private static FirebaseDatabase db = null;
    private static FirebaseFirestore dbFireStore = null;

    private FirebaseDatabase(){
        dbFireStore = FirebaseFirestore.getInstance();
    }


    /**
     * @return The current FirebaseDatabase or a new one if not yet instantiated
     */
    public static FirebaseDatabase getInstance(){
        if(db == null){
            db = new FirebaseDatabase();
        }

        return db;
    }

    public static void setFirebaseTest(FirebaseFirestore newFireStore) {
        dbFireStore = newFireStore;
    }

    /**
     * Updates a database entry (Favor, User, Chat) that was locally modified or created
     * by the user on his phone.
     * As always Firestore tasks are asynchronous and there is logging capability to
     * debug the status of a push to the database
     *
     * @param databaseEntity DatabaseEntity to update on the DB
     * @return asynchronous task completing the store on the remote db
     */
    @Override
    public Task updateOnDb(DatabaseEntity databaseEntity){
        if(databaseEntity.documentID == null){
            // Do the same here if other types of datas

            return dbFireStore.collection(databaseEntity.collection).add(databaseEntity.getEncapsulatedObjectOfMaps())
                    .addOnSuccessListener(docRef -> {
                        databaseEntity.documentID = docRef.getId();
                        updateFromDb(databaseEntity);
                    }).addOnFailureListener(e -> {
                Log.d(TAG,"failure to push favor to database");
                /* Feedback of an error here - Impossible to update user informations*/
            });
        }else {
            return dbFireStore.collection(databaseEntity.collection).document(databaseEntity.documentID).set(databaseEntity.getEncapsulatedObjectOfMaps())
                    .addOnSuccessListener(aVoid -> updateFromDb(databaseEntity));
        }
        /* Feedback of an error here - Impossible to update user informations */
    }

    /**
     * Gets a database entry from the firestore database
     * since all of the requests are asynchronous
     * the provision is made in the form of a task that will
     * load the updated db entry into the specified DatabaseEntity
     * which could be a remotely changed favor, chat or even user
     *
     * @param databaseEntity DatabaseEntity that must be updated with the db version
     * @return an asynchronous task that will update the db entry passed as argument
     */
    @Override
    public Task updateFromDb(DatabaseEntity databaseEntity){
        if(databaseEntity.documentID == null){return Tasks.forCanceled();}
        return dbFireStore.collection(databaseEntity.collection).document(databaseEntity.documentID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        databaseEntity.updateLocalData(document.getData());
                    } else {
                        Toast.makeText(FavorsMain.getContext(), "An error occured while requesting " +
                                "data from database",Toast.LENGTH_LONG).show();
                    }
                });

    }

    /**
     * Deletes an entry from the DB
     * this can either be a favor, a user or chat
     *
     * @param databaseEntity - the Object referencing the database entry
     * @return Task that will delete the entry
     */
    @Override
    public Task deleteFromDatabase(DatabaseEntity databaseEntity) {
        if(databaseEntity == null) return Tasks.forResult(false);
        return dbFireStore.collection(databaseEntity.collection).document(databaseEntity.documentID).delete();
    }

    @Override
    public ListenerRegistration addSnapshotListener(Activity activity,String collection, EventListener<QuerySnapshot> listener) {
        return dbFireStore.collection(collection).addSnapshotListener(activity,listener);
    }

    class ListRequestFb<T extends DatabaseEntity> implements OnCompleteListener{

        ObservableArrayList<T> list;
        T firstElement;
        Class<T> clazz;

        public  ListRequestFb(ObservableArrayList<T> list, Class<T> clazz){
            super();
            this.list = list;
            this.clazz = clazz;
        }
        public  ListRequestFb(T extratctedFirstElement, Class<T> clazz){
            super();
            this.firstElement = extratctedFirstElement;
            this.clazz = clazz;
        }

        @Override
        public void onComplete(@NonNull Task task) {
            if (!task.isSuccessful()) {
                Log.d(TAG, "get failed with ", task.getException());
            }

            if(task.getResult() instanceof DocumentSnapshot){
                DocumentSnapshot document = (DocumentSnapshot) task.getResult();
                if(firstElement != null ){
                    firstElement.reset();
                    firstElement.set(document.getId(), document.getData());
                }
            }

            else if(task.getResult() instanceof QuerySnapshot){
                ArrayList<T> temp = new ArrayList<>();
                for (QueryDocumentSnapshot document : (QuerySnapshot) task.getResult()) {
                    try {
                        if (firstElement != null) {
                            firstElement.reset();
                            firstElement.set(document.getId(), document.getData());
                        }
                        if (list != null) {
                            T documentObject = clazz.newInstance();
                            documentObject.set(document.getId(), document.getData());
                            temp.add(documentObject);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Illegal access exception");
                    }
                }
                if(list != null) list.update(temp);
            }

        }
    }

    Query addParametersToQuery(Query query, Integer limit, DatabaseField orderBy){
        if(orderBy != null){
            if(orderBy == Favor.ObjectFields.creationTimestamp){
                query = query.orderBy(orderBy.toString(), Query.Direction.DESCENDING);
            }
            else if(orderBy == ChatMessage.LongFields.messageDate){
                query = query.orderBy(orderBy.toString(), Query.Direction.DESCENDING);
            }else {
                query = query.orderBy(orderBy.toString());
            }
        }
        if(limit != null){
            query = query.limit(limit);
        }
        return query;
    }

    @Override
    protected <T extends DatabaseEntity> void getAll(ObservableArrayList<T> list, Class<T> clazz,
                                                     String collection,
                                                     Integer limit,
                                                     DatabaseField orderBy){
        Query query = dbFireStore.collection(collection);
        query = addParametersToQuery(query, limit, orderBy);
        query.get().addOnCompleteListener(new ListRequestFb<T>(list, clazz));
    }


    @Override
    protected  <T extends DatabaseEntity> void getList(ObservableArrayList<T> list, Class<T> clazz,
                                                       String collection,
                                                       Map<DatabaseField, Object> mapEquals,
                                                       Map<DatabaseField, Object> mapLess,
                                                       Map<DatabaseField, Object> mapMore,
                                                       Map<DatabaseField, Object> mapContains,
                                                       Integer limit,
                                                       DatabaseField orderBy){


        Query query = dbFireStore.collection(collection);

        if(mapEquals != null) for(Map.Entry<DatabaseField, Object> el : mapEquals.entrySet()){
            query = query.whereEqualTo(el.getKey().toString(), el.getValue());
        }
        if(mapLess != null) for(Map.Entry<DatabaseField, Object> el : mapLess.entrySet()){
            query = query.whereLessThan(el.getKey().toString(), el.getValue());
        }
        if(mapMore != null) for(Map.Entry<DatabaseField, Object> el : mapMore.entrySet()){
            query = query.whereGreaterThan(el.getKey().toString(), el.getValue());
        }
        if(mapContains != null) for(Map.Entry<DatabaseField, Object> el : mapContains.entrySet()){
            query = query.whereArrayContains(el.getKey().toString(), el.getValue());
        }

        query = addParametersToQuery(query, limit, orderBy);
        query.get().addOnCompleteListener(new ListRequestFb<T>(list, clazz));
    }

    protected <T extends DatabaseEntity> void getList(ObservableArrayList<T> list, Class<T> clazz,
                                                      String collection,
                                                      DatabaseField element,
                                                      Object value,
                                                      Integer limit,
                                                      DatabaseField orderBy){
        if(element == null || value == null){return;}
        Query query = dbFireStore.collection(collection).whereEqualTo(element.toString(), value);
        query = addParametersToQuery(query, limit, orderBy);
        query.get().addOnCompleteListener(new ListRequestFb<T>(list, clazz));


    }

    protected <T extends DatabaseEntity> void getLiveList(ObservableArrayList<T> list, Class<T> clazz,
                                                          String collection,
                                                          DatabaseField element,
                                                          Object value,
                                                          Integer limit,
                                                          DatabaseField orderBy){
        if(element == null || value == null){return;}
        Query query = dbFireStore.collection(collection).whereEqualTo(element.toString(), value);
        query = addParametersToQuery(query, limit, orderBy);
        query.addSnapshotListener((value1, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            ArrayList<T> temp = new ArrayList<>();
            for (QueryDocumentSnapshot document : value1) {
                try {
                    if (list != null) {
                        T documentObject = clazz.newInstance();
                        documentObject.set(document.getId(), document.getData());
                        temp.add(documentObject);
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "Illegal access exception");
                }
            }
            if(list != null) list.update(temp);
        });

    }

    @Override
    protected  <T extends DatabaseEntity> void getElement(T toUpdate, Class<T> clazz, String collection,
                                                          String value){
        if(value == null || toUpdate == null){return;}
        DocumentReference query = dbFireStore.collection(collection).document(value);
        query.get().addOnCompleteListener(new ListRequestFb<T>(toUpdate, clazz));

    }


    @Override
    protected  <T extends DatabaseEntity> void getElement(T toUpdate, Class<T> clazz, String collection,
                                                          DatabaseField element, Object value){
        if(value == null || toUpdate == null){return;}
        Query query = dbFireStore.collection(collection).whereEqualTo(element.toString(), value);
        query.get().addOnCompleteListener(new ListRequestFb<T>(toUpdate, clazz));

    }


    public void cleanUp(){
        db = null;
        dbFireStore = null;
    }
}
