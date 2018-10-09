package ch.epfl.sweng.favours.database;

import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class User extends DatabaseHandler {

    private static User user = new User();
    public static User getMain(){
        return user;
    }

    public enum StringFields implements DatabaseStringField{firstName, lastName, email, sex, basedLocation}

    public User(){
        super(StringFields.values());
        if(FirebaseAuth.getInstance().getUid() != null)
            updateFromDb();
    }

    public void updateOnDb(){
        Map<String, Object> toSend = new HashMap<>();

        convertTypedMapToObjectMap(stringData, toSend);

        // Do the same here if other types of datas

        Database.getInstance().getUserCollection().document(FirebaseAuth.getInstance().getUid()).set(toSend)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateFromDb();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                /* Feedback of an error here - Impossible to update user informations */
            }
        });
    }

    public void updateFromDb(){
        Database.getInstance().getUserCollection().document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.getData() != null) parseStringData(StringFields.values(),document.getData());
                    // Do the same for rest of datas

                } else {
                    /* Feedback of an error here - Impossible to get user informations from server */
                }
            }
        });
    }
}
