package thomas.com.NoExpiry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class FoodAdapter extends FirestoreRecyclerAdapter<Food, FoodAdapter.FoodHolder>{

    private OnItemClickListener listener;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FoodAdapter(@NonNull FirestoreRecyclerOptions<Food> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FoodHolder holder, int position, @NonNull Food model) {
        // tell adapter what we need to put in the card layout
        holder.textViewTitle.setText(model.getTitle()); // put title in the node to textViewTitle
        holder.textViewDescription.setText(model.getDescription());
        holder.textViewExpiryDate.setText(model.getExpiryDate());
        int today = 20190217;
        int compare = model.getExDate();
        if (compare - today < 0) {
            holder.foodStatus.setBackgroundResource(R.color.red);
        } else if (compare - today < 3) {
            holder.foodStatus.setBackgroundResource(R.color.yellow);
        } else {
            holder.foodStatus.setBackgroundResource(R.color.green);
        }
    }

    @NonNull
    @Override
    public FoodHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // viewGroup is the recycling view
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item, viewGroup, false);

        return new FoodHolder(view);
    }

    // swipe to delete recycler view
    public void deleteItem(int position) {
        // need document reference to delete database item
        // we finds the position
        getSnapshots().getSnapshot(position).getReference().delete();

    }

    // inner class
    class FoodHolder extends RecyclerView.ViewHolder {

        // items from card view
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewExpiryDate;
        ImageView foodStatus;

        public FoodHolder(@NonNull View itemView) {
            super(itemView);
            // referencing food_item.xml
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewExpiryDate = itemView.findViewById(R.id.text_view_expiryDate);
            foodStatus = itemView.findViewById(R.id.foodStatus);

            // set click event in the recycle view item
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    // call interface to send the click
                    // just make sure we don't click on an item right after we delete an item
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }

                }
            });
        }
    }

    // create an interface and a method to send click from adapter to activity
    public interface OnItemClickListener {
        // define interface method
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        this.listener = listener;

    }
}


