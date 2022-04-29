package fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.WaterWallha.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

import models.FavoriteShopsDetails;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteFragment extends Fragment {

    private View view;
    private FirebaseFirestore db;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mFavoriteShopRecyclerView;
    private String uid;

    public FavouriteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favourite, container, false);

        init();
        getFavoriteShop();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        ImageView mGoBackBtn = view.findViewById(R.id.cartBackBtn);
        mGoBackBtn.setOnClickListener(view -> {
            Fragment fragment = new ShopsFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        TextView mFavoriteShopToolBarText = view.findViewById(R.id.confirmOrderText);
        mFavoriteShopToolBarText.setText("Your Favorite Shops");
        db = FirebaseFirestore.getInstance();
        mFavoriteShopRecyclerView = view.findViewById(R.id.favoriteShopRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mFavoriteShopRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void getFavoriteShop() {
        Query query = db.collection("UserList").document(uid).collection("FavoriteShops");
        FirestoreRecyclerOptions<FavoriteShopsDetails> favResModel = new FirestoreRecyclerOptions.Builder<FavoriteShopsDetails>()
                .setQuery(query, FavoriteShopsDetails.class)
                .build();
        FirestoreRecyclerAdapter<FavoriteShopsDetails, FavoriteShopMenuHolder> adapter = new FirestoreRecyclerAdapter<FavoriteShopsDetails, FavoriteShopMenuHolder>(favResModel) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull FavoriteShopMenuHolder holder, int i, @NonNull FavoriteShopsDetails model) {
                Glide.with(requireActivity())
                        .load(model.getShop_image())
                        .placeholder(R.drawable.shop_image_placeholder)
                        .into(holder.mFavShopImage);

                holder.mFavShopName.setText(model.getShop_name());
                holder.mFavShopPrice.setText("\u20b9" + model.getShop_price() + " Average");
            }

            @NonNull
            @Override
            public FavoriteShopMenuHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.custom_favorite_shop_layout, group, false);
                return new FavoriteShopMenuHolder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        mFavoriteShopRecyclerView.setAdapter(adapter);

    }

    public static class FavoriteShopMenuHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.favShopImage)
        ImageView mFavShopImage;
        @BindView(R.id.favShopName)
        TextView mFavShopName;
        @BindView(R.id.favoriteShopPrice)
        TextView mFavShopPrice;

        public FavoriteShopMenuHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}