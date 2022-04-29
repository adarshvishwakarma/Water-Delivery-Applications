package ui.cart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.WaterWallha.MainActivity;
import com.example.WaterWallha.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import models.CartItemDetail;
import ui.location.ChangeLocationActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import ui.order.CheckoutActivity;

public class CartItemActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter<CartItemDetail, CartItemHolder> itemAdapter;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mCartItemRecyclerView;
    private TextView mShopCartName;
    private TextView mUserAddressText;
    private TextView mTotalAmountText;
    private String uid, userAddress,suid,userName,userPhoneNum,shopDeliveryTime,shopSpotImage;
    private String extraIns = "none";
    private String USER_LIST = "UserList";
    private String CART_ITEMS = "CartItems";
    private EditText mExtraInstructionsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_item);

        init();
        setShopName();
        getCartItems();

    }

    @SuppressLint("SetTextI18n")
    private void init() {
        db = FirebaseFirestore.getInstance();
        TextView mToolBarText = findViewById(R.id.confirmOrderText);
        mToolBarText.setText("Confirm Order");
        mShopCartName = findViewById(R.id.shopCartName);
        TextView mChangeAddressText = findViewById(R.id.changeAddressText);
        mChangeAddressText.setOnClickListener(view -> {

            Intent intent = new Intent(getApplicationContext(), ChangeLocationActivity.class);
            intent.putExtra("INT","TWO");
            startActivity(intent);
            finish();

        });
        ImageView mCartBackBtn = findViewById(R.id.cartBackBtn);
        mCartBackBtn.setOnClickListener(view -> {
            this.onBackPressed();
        });
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        mCartItemRecyclerView = findViewById(R.id.cartItemRecyclerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mCartItemRecyclerView.setLayoutManager(linearLayoutManager);
        mExtraInstructionsText = findViewById(R.id.extraInstructionEdiText);
        mUserAddressText = findViewById(R.id.userDeliveryAddress);
        mTotalAmountText = findViewById(R.id.totAmount);
        Button mCheckoutBtn = findViewById(R.id.payAmountBtn);
        mCheckoutBtn.setOnClickListener(view -> {
            calculateTotalPriceAndMove();
        });
    }

    private void setShopName() {
        db.collection(USER_LIST).document(uid).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;
                String resName = (String) documentSnapshot.get("shop_cart_name");
                suid = String.valueOf(documentSnapshot.get("shop_cart_uid"));
                shopDeliveryTime = String.valueOf(documentSnapshot.get("shop_delivery_time"));
                shopSpotImage = String.valueOf(documentSnapshot.get("shop_cart_spotimage"));
                userName = String.valueOf(documentSnapshot.get("name"));
                userPhoneNum = String.valueOf(documentSnapshot.get("phonenumber"));
                userAddress = String.valueOf(documentSnapshot.get("address"));
                mShopCartName.setText(resName);
                mUserAddressText.setText(userAddress);

            }else {
                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getCartItems() {
        Query query = db.collection(USER_LIST).document(uid).collection(CART_ITEMS);
        FirestoreRecyclerOptions<CartItemDetail> cartItemModel = new FirestoreRecyclerOptions.Builder<CartItemDetail>()
                .setQuery(query, CartItemDetail.class)
                .build();
        itemAdapter = new FirestoreRecyclerAdapter<CartItemDetail, CartItemHolder>(cartItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull CartItemHolder holder, int item, @NonNull CartItemDetail model) {

                String specImage = model.getSelect_specification();
                if (specImage.equals("Mineral")){
                    Glide.with(Objects.requireNonNull(getApplicationContext()))
                            .load(R.drawable.veg_symbol).into(holder.mWaterMarkImg);
                }else {
                    Glide.with(Objects.requireNonNull(getApplicationContext()))
                            .load(R.drawable.non_veg_symbol).into(holder.mWaterMarkImg);
                }
                holder.mItemCartName.setText(model.getSelect_name());
                String itemCount = model.getItem_count();
                holder.mQtyPicker.setNumber(itemCount);
                int getItemPrice = Integer.parseInt(model.getSelect_price());
                int getItemCount = Integer.parseInt(model.getItem_count());
                int finalPrice = getItemPrice * getItemCount;
                holder.mItemCartPrice.setText("\u20b9 " + finalPrice);

                holder.mQtyPicker.setOnValueChangeListener((view, oldValue, newValue) -> {

                    String updatedPrice = String.valueOf(newValue * Integer.parseInt(model.getSelect_price()));
                    holder.mItemCartPrice.setText("\u20b9 " + updatedPrice);

                    Map<String, Object> updatedPriceMap = new HashMap<>();
                    updatedPriceMap.put("item_count", String.valueOf(newValue));

                    db.collection(USER_LIST)
                            .document(uid)
                            .collection(CART_ITEMS)
                            .document(model.getSelect_name())
                            .update(updatedPriceMap)
                            .addOnCompleteListener(task ->
                                    Log.d("SUCCESS", "SUCESSSSSSS"));

                    if (newValue == 0){
                        db.collection(USER_LIST).document(uid).collection(CART_ITEMS).document(holder.mItemCartName.getText().toString())
                                .delete().addOnSuccessListener(aVoid -> {
                            Toast.makeText(getApplicationContext(), "Item Removed", Toast.LENGTH_SHORT).show();
                        });
                    }
                });

                calculateTotalPrice();
            }

            @NonNull
            @Override
            public CartItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cart_items_layout, parent, false);

                return new CartItemHolder(view);
            }
            @Override
            public void onError(@NotNull FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };
        itemAdapter.startListening();
        itemAdapter.notifyDataSetChanged();
        mCartItemRecyclerView.setAdapter(itemAdapter);

    }

    public static class CartItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.waterMarkCart)
        ImageView mWaterMarkImg;
        @BindView(R.id.itemNameCart)
        TextView mItemCartName;
        @BindView(R.id.itemPriceCart)
        TextView mItemCartPrice;
        @BindView(R.id.quantityPicker)
        ElegantNumberButton mQtyPicker;

        public CartItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void moveIfCartEmpty() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void calculateTotalPrice() {
        mCartItemRecyclerView.postDelayed(() -> {
            if (itemAdapter.getItemCount() == 0){
                moveIfCartEmpty();
            }else {
                if (Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(0)).itemView.findViewById(R.id.itemPriceCart) != null){
                    int totPrice = 0;
                    for (int i = 0; i < Objects.requireNonNull(mCartItemRecyclerView.getAdapter()).getItemCount() ; i++){
                        TextView textView = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.itemPriceCart);
                        String priceText = textView.getText().toString().replace("\u20b9 " , "");
                        int price = Integer.parseInt(priceText);
                        totPrice = price + totPrice;
                    }
                    mTotalAmountText.setText("Amount Payable: \u20b9" + totPrice);
                }
            }
        },5);

    }

    private void calculateTotalPriceAndMove() {
        mCartItemRecyclerView.postDelayed(() -> {
            if (itemAdapter.getItemCount() == 0){
                moveIfCartEmpty();
            }else {
                if (Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(0)).itemView.findViewById(R.id.itemPriceCart) != null){
                    int totPrice = 0;
                    String[] itemsArr = new String[Objects.requireNonNull(mCartItemRecyclerView.getAdapter()).getItemCount()];
                    String[] orderedItemsArr = new String[mCartItemRecyclerView.getAdapter().getItemCount()];
                    for (int i = 0; i < Objects.requireNonNull(mCartItemRecyclerView.getAdapter()).getItemCount() ; i++){
                        TextView textView = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.itemPriceCart);
                        TextView textView2 = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.itemNameCart);
                        ElegantNumberButton elegantNumberButton = Objects.requireNonNull(mCartItemRecyclerView.findViewHolderForAdapterPosition(i)).itemView.findViewById(R.id.quantityPicker);
                        String itemCount = elegantNumberButton.getNumber();
                        String priceText = textView.getText().toString().replace("\u20b9 " , "");
                        int price = Integer.parseInt(priceText);
                        totPrice = price + totPrice;
                        itemsArr[i] = textView2.getText().toString();
                        orderedItemsArr[i] = itemCount + " x " + textView2.getText().toString();
                    }

                    if(!TextUtils.isEmpty(mExtraInstructionsText.getText())){
                        extraIns = mExtraInstructionsText.getText().toString();
                    }

                    if (shopSpotImage != null){
                        Intent intent = new Intent(CartItemActivity.this, CheckoutActivity.class);
                        intent.putExtra("TOTAL_AMOUNT", String.valueOf(totPrice));
                        intent.putExtra("ITEM_NAMES", itemsArr);
                        intent.putExtra("ITEM_ORDERED_NAME", orderedItemsArr);
                        intent.putExtra("SHOP_NAME", mShopCartName.getText().toString());
                        intent.putExtra("SHOP_UID", suid);
                        intent.putExtra("USER_ADDRESS",userAddress);
                        intent.putExtra("USER_NAME", userName);
                        intent.putExtra("USER_UID",uid);
                        intent.putExtra("EXTRA_INS", extraIns);
                        intent.putExtra("USER_PHONE", userPhoneNum);
                        intent.putExtra("DELIVERY_TIME", shopDeliveryTime);
                        intent.putExtra("SHOP_IMAGE", shopSpotImage);
                        startActivity(intent);
                        this.overridePendingTransition(0,0);
                    }
                }
            }
        },5);
    }

}