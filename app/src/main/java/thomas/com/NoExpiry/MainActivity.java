package thomas.com.NoExpiry;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    // connect adapter to recycler view
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Food Collection
    private CollectionReference foodRef = db.collection("FoodCollection");
    private FoodAdapter adapter;


    /**
     * OnCreate will be the first thing android will call during your activity launch
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {

        Query query = foodRef.orderBy("exDate", Query.Direction.ASCENDING);
        // create fire store recycler option
        FirestoreRecyclerOptions<Food> options = new FirestoreRecyclerOptions.Builder<Food>()
                .setQuery(query, Food.class)
                .build();
        // assign adapter variable
        adapter = new FoodAdapter(options);
        // reference recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true); // performance reasons
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // for swipe delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            // for drag and drop (we don't need)
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            // swipe
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        // use anonymous class to create the interface
        adapter.setOnItemClickListener(new FoodAdapter.OnItemClickListener() {
            // get the snapshots
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                // we can recreate object
                Food food = documentSnapshot.toObject(Food.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();
                //documentSnapshot.getReference().update();
                // Toast.makeText(MainActivity.this, "Position: " + position + "ID: " + id, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // tell adapter to listen to fire base
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.startListening();
    }

    public void haha(View view) {
        startActivity(new Intent(MainActivity.this, FormActivity.class));
    }
}
