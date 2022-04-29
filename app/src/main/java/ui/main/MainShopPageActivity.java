package ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.WaterWallha.MainActivity;
import com.example.WaterWallha.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import models.ShopsMenuItems;
import ui.cart.CartItemActivity;
import ui.review.ReviewsActivity;

import static android.view.View.GONE;

public class MainShopPageActivity extends AppCompatActivity {

    private String uid,mShopUid, mShopName, mShopDistance, mShopPrice, mShopDeliveryTime, mShopImage, mShopNum;
    private FirebaseFirestore db;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mMenuItemRecyclerView;
    private NestedScrollView mRootView;
    private LottieAnimationView mFavoriteAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_shop_page);

        Intent intent = getIntent();
        if (intent != null){
            mShopUid = intent.getStringExtra("SUID");
            mShopName = intent.getStringExtra("NAME");
            mShopDistance = intent.getStringExtra("DISTANCE");
            mShopPrice = intent.getStringExtra("PRICE");
            mShopDeliveryTime = intent.getStringExtra("TIME");
            mShopImage = intent.getStringExtra("SHOP_IMAGE");
            mShopNum = intent.getStringExtra("SHOP_NUM");
        }

        init();
        getMenuItems();

    }

    @SuppressLint("SetTextI18n")
    private void init() {
        mRootView = findViewById(R.id.content1);
        db = FirebaseFirestore.getInstance();
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        TextView mShopNameToolBar = findViewById(R.id.shopTitleToolbar);
        TextView mShopNameText = findViewById(R.id.mainShopName);
        TextView mReviewsText = findViewById(R.id.reviewText);
        mReviewsText.setOnClickListener(view -> {
            Intent intent = new Intent(this, ReviewsActivity.class);
            intent.putExtra("SUID", mShopUid);
            intent.putExtra("NAME", mShopName);
            intent.putExtra("PRICE", mShopPrice);
            intent.putExtra("NUM", mShopNum);
            startActivity(intent);
        });
        mFavoriteAnim = findViewById(R.id.favoriteAnim);
        checkFavRes();
        mFavoriteAnim.setOnClickListener(view -> {
            if (mFavoriteAnim.getProgress() >= 0.1f){
                mFavoriteAnim.setSpeed(-1);
                mFavoriteAnim.playAnimation();
                delFavRes();
            }else if(mFavoriteAnim.getProgress() == 0.0f){
                mFavoriteAnim.setSpeed(1);
                mFavoriteAnim.playAnimation();
                favRes();
            }
        });
        TextView mResDistanceText = findViewById(R.id.mainShopDistance);
        TextView mResAvgPriceText = findViewById(R.id.shopAvgPrice);
        TextView mResDeliveryTimeText = findViewById(R.id.shopDeliveryTime);
        mShopNameToolBar.setText(mShopName);
        mShopNameText.setText(mShopName);
        mResDeliveryTimeText.setText(mShopDeliveryTime + " mins");
        mResAvgPriceText.setText("\u20b9" + mShopPrice);
        mResDistanceText.setText(mShopDistance + " kms");
        ImageView mBackBtnView = findViewById(R.id.backBtn);
        mBackBtnView.setOnClickListener(view -> {
            this.onBackPressed();
            this.overridePendingTransition(0,0);
        });

        mMenuItemRecyclerView = findViewById(R.id.menuItemRecylerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMenuItemRecyclerView.setLayoutManager(linearLayoutManager);

    }

    private void getMenuItems() {
        Query query = db.collection("Menu").document(mShopUid).collection("MenuItems");
        FirestoreRecyclerOptions<ShopsMenuItems> menuItemModel = new FirestoreRecyclerOptions.Builder<ShopsMenuItems>()
                .setQuery(query, ShopsMenuItems.class)
                .build();
        FirestoreRecyclerAdapter<ShopsMenuItems, MenuItemHolder> adapter = new FirestoreRecyclerAdapter<ShopsMenuItems, MenuItemHolder>(menuItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull MenuItemHolder holder, int position, @NonNull ShopsMenuItems model) {
                if (model.is_active()!=null && model.is_active().equals("no")) {
                    holder.mItemName.setText(model.getName());
                    holder.mItemCategory.setText(model.getCategory());
                    String specImage = String.valueOf(model.getSpecification());
                    if (specImage.equals("Mineral")) {
                        Glide.with(Objects.requireNonNull(getApplicationContext()))
                                .load(R.drawable.veg_symbol).into(holder.waterSpecification);
                    } else {
                        Glide.with(Objects.requireNonNull(getApplicationContext()))
                                .load(R.drawable.non_veg_symbol).into(holder.waterSpecification);
                    }
                    holder.mItemPrice.setText("\u20B9 " + model.getPrice());
                    holder.mItemAddBtn.setClickable(false);
                    holder.mNotAvailableText.setVisibility(View.VISIBLE);
                }else
                 if (model.is_active()!=null && model.is_active().equals("yes")) {
                    holder.mItemName.setText(model.getName());
                    holder.mItemCategory.setText(model.getCategory());
                    String specImage = String.valueOf(model.getSpecification());
                    if (specImage.equals("Mineral")) {
                        Glide.with(Objects.requireNonNull(getApplicationContext()))
                                .load(R.drawable.veg_symbol).into(holder.waterSpecification);
                    } else {
                        Glide.with(Objects.requireNonNull(getApplicationContext()))
                                .load(R.drawable.non_veg_symbol).into(holder.waterSpecification);
                    }
                    holder.mItemPrice.setText("\u20B9 " + model.getPrice());
                    holder.itemView.setOnClickListener(v -> {
                    });

                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                    holder.mItemAddBtn.setOnClickListener(view -> {
                        String selectedItemName = holder.mItemName.getText().toString();
                        addItemToCart(selectedItemName, model, uid);
                        holder.mItemAddBtn.setVisibility(GONE);
                        holder.mRemoveItemBtn.setVisibility(View.VISIBLE);
                    });

                    holder.mRemoveItemBtn.setOnClickListener(view -> {
                        String selectedItemName = holder.mItemName.getText().toString();
                        removeItemFromCart(selectedItemName, uid);
                        holder.mRemoveItemBtn.setVisibility(GONE);
                        holder.mItemAddBtn.setVisibility(View.VISIBLE);
                    });

                    DocumentReference docRef = db.collection("UserList").document(uid).collection("CartItems").document(holder.mItemName.getText().toString());
                    docRef.get().addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (Objects.requireNonNull(documentSnapshot).exists()) {

                                holder.mItemAddBtn.setVisibility(GONE);
                                holder.mRemoveItemBtn.setVisibility(View.VISIBLE);

                            }
                        }

                    });
                }
            }

            @NotNull
            @Override
            public MenuItemHolder onCreateViewHolder(@NotNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.shop_menuitems_view, group, false);
                return new MenuItemHolder(view);
            }

            @Override
            public void onError(@NotNull FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        mMenuItemRecyclerView.setAdapter(adapter);

    }

    private void addItemToCart(String selectedItemName, ShopsMenuItems model, String uid){
        Map<String ,String> cartItemMap = new HashMap<>();
        cartItemMap.put("select_name", selectedItemName);
        cartItemMap.put("select_price", model.getPrice());
        cartItemMap.put("select_specification", model.getSpecification());
        cartItemMap.put("item_count", "1");
        db.collection("UserList").document(uid).collection("CartItems")
                .document(selectedItemName).
                set(cartItemMap)
                .addOnSuccessListener(aVoid -> {

                    db.collection("UserList").document(uid).collection("CartItems").get().addOnCompleteListener(task -> {

                        if (task.isSuccessful()){
                            int count = 0;
                            for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                                count++;
                            }
                            Snackbar snackbar = Snackbar
                                    .make(mRootView, "Added " + count + " items in  your cart", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Cart", view -> {
                                        Snackbar snackbar1 = Snackbar.make(mRootView, "Message is restored!", Snackbar.LENGTH_SHORT);
                                        Intent intent = new Intent(this, CartItemActivity.class);
                                        startActivity(intent);
                                        snackbar1.dismiss();
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            snackbar.show();
                        }
                    });
                }).addOnFailureListener(e -> {
        });

        HashMap<String, Object> resNameMap = new HashMap<>();
        resNameMap.put("shop_cart_name", mShopName);
        resNameMap.put("shop_cart_uid", mShopUid);
        resNameMap.put("shop_delivery_time", mShopDeliveryTime);
        resNameMap.put("shop_cart_spotimage", mShopImage);

        db.collection("UserList").document(uid).update(resNameMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Added Item Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Adding Item Failed", Toast.LENGTH_SHORT).show();
        });

    }

    private void removeItemFromCart(String selectedItemName,String uid) {
        db.collection("UserList").document(uid).collection("CartItems").document(selectedItemName).delete().addOnCompleteListener(task ->
                Toast.makeText(getApplicationContext(), "Item Removed From Cart", Toast.LENGTH_SHORT).show());
    }

    public static class MenuItemHolder extends RecyclerView.ViewHolder {

        TextView  mItemName , mItemPrice , mItemCategory , mNotAvailableText ;
        ImageView waterSpecification;
        Button mItemAddBtn , mRemoveItemBtn;

        public MenuItemHolder(@NonNull View itemView) {
            super(itemView);

            mItemName = itemView.findViewById(R.id.menuItemName);
            waterSpecification = itemView.findViewById(R.id.waterMark);
            mItemPrice = itemView.findViewById(R.id.menuItemPrice);
            mItemCategory = itemView.findViewById(R.id.menuItemCategory);
            mItemAddBtn = itemView.findViewById(R.id.addMenuItemBtn);
            mRemoveItemBtn = itemView.findViewById(R.id.removeMenuItemBtn);
            mNotAvailableText = itemView.findViewById(R.id.notAvailableText);
        }

//        @BindView(R.id.menuItemName)
//        TextView mItemName;
//        @BindView(R.id.foodMark)
//        ImageView foodSpecification;
//        @BindView(R.id.menuItemPrice)
//        TextView mItemPrice;
//        @BindView(R.id.menuItemCategory)
//        TextView mItemCategory;
//        @BindView(R.id.addMenuItemBtn)
//        Button mItemAddBtn;
//        @BindView(R.id.removeMenuItemBtn)
//        Button mRemoveItemBtn;
//        @BindView(R.id.notAvailableText)
//        TextView mNotAvailableText;
//
//        public MenuItemHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
    }

    private void checkFavRes() {
        db.collection("UserList")
                .document(uid)
                .collection("FavoriteShops")
                .document(mShopUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot docRef = task.getResult();
                        if (Objects.requireNonNull(docRef).exists()){
                            mFavoriteAnim.setProgress(0.1f);
                            mFavoriteAnim.resumeAnimation();
                        }else {
                            mFavoriteAnim.setProgress(0.0f);
                        }
                    }
                });
    }

    private void favRes() {
        Map<String, Object> favResMap = new HashMap<>();
        favResMap.put("shop_uid" , mShopUid);
        favResMap.put("shop_name", mShopName);
        favResMap.put("shop_image", mShopImage);
        favResMap.put("shop_price", mShopPrice);

        db.collection("UserList")
                .document(uid)
                .collection("FavoriteShops")
                .document(mShopUid).set(favResMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), mShopName + " has marked as favorite", Toast.LENGTH_SHORT).show();
        });
    }

    private void delFavRes() {
        db.collection("UserList")
                .document(uid)
                .collection("FavoriteShops")
                .document(mShopUid).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Removed as favorite", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}