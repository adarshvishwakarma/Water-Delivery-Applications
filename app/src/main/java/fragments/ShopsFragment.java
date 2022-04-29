package fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import ui.cart.CartItemActivity;
import ui.cart.EmptyCartActivity;
import ui.main.MainShopPageActivity;
import com.example.WaterWallha.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Objects;

import models.ShopsDetail;
import ui.location.ChangeLocationActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.nikartm.support.ImageBadgeView;

public class ShopsFragment extends Fragment {

    private FirebaseUser mCurrentUser;
    private FirebaseFirestore db;
    private String address;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView mShopRecyclerView;
    private ImageBadgeView mImageBadgeView;
    private String[] waterNames = {"Water Jar","Chilled Water","Water Bottle"};
    private String waterJar = "https://firebasestorage.googleapis.com/v0/b/waterd-4e52d.appspot.com/o/categories%2F20l_water_jar.jpg?alt=media&token=fa2714bb-fbdb-4beb-b77f-6641ec2df642";  //"https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fbiryani.jpg?alt=media&token=6eceb101-07c1-49ff-95a3-e540b1e0fb35"
    private String ChilledWater = "https://firebasestorage.googleapis.com/v0/b/waterd-4e52d.appspot.com/o/categories%2Fcool_jar.jpg?alt=media&token=e7b00471-3c42-4f1c-8a25-d2b81b05cf07";
    private String WaterBottle = "https://firebasestorage.googleapis.com/v0/b/waterd-4e52d.appspot.com/o/categories%2Fbottle.jpg?alt=media&token=661569bf-24fa-413d-9eca-4a7bcef9f816";
//    private String rollsImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Frolls.jpg?alt=media&token=d52c1f03-0558-44a8-a7c3-90b8fda6d77f";
//    private String burgersImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fburgers.jpg?alt=media&token=f6f2ca46-fc84-4ed6-84f2-2d199686f45e";
//    private String pizzaImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fpizza.jpg?alt=media&token=3239cc9d-c2e6-4a8c-8a76-e8ee307ec72e";
//    private String cakesImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fcake.jpeg?alt=media&token=1ded9af6-25a6-4deb-8b1a-0681d7a154d5";
//    private String chineseImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fchinese.jpg?alt=media&token=b407830e-0e8d-47d2-8fd0-de43b34bb4d2";
//    private String dessertsImg = "https://firebasestorage.googleapis.com/v0/b/munche-be7a5.appspot.com/o/cuisine_images%2Fdesserts.jpg?alt=media&token=209f4592-7b36-4768-9b71-f35d5c05ef74";
    private String[] waterImages = {waterJar,ChilledWater,WaterBottle};

    public ShopsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        init(view);
        fetchLocation(view);
        getItemsInCartNo();
        getShopDetails();

        return view;
    }

    private void init(View view) {
        LinearLayout mAddressContainer = view.findViewById(R.id.addressContainer);
        GridView mCategoryWaterView = view.findViewById(R.id.categoryGridView);
        CategoryImageAdapter adapter = new CategoryImageAdapter();
        mCategoryWaterView.setAdapter(adapter);
        mAddressContainer.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ChangeLocationActivity.class);
            intent.putExtra("INT", "ONE");
            startActivity(intent);
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        Toolbar mToolbar = view.findViewById(R.id.customToolBar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);
        mShopRecyclerView = view.findViewById(R.id.shop_recyclerView);
        linearLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false);
        mShopRecyclerView.setLayoutManager(linearLayoutManager);
        mShopRecyclerView.setHasFixedSize(true);
        mImageBadgeView = view.findViewById(R.id.imageBadgeView);
    }

    private void fetchLocation(View view) {
        if (mCurrentUser != null) {
            DocumentReference docRef = db.collection("UserList").document(mCurrentUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;
                    address = String.valueOf(documentSnapshot.get("address"));
                    TextView textView = view.findViewById(R.id.userLocation);
                    textView.setText(address);
                }
            });
        }
    }

    private void getItemsInCartNo() {
        db.collection("UserList").document(mCurrentUser.getUid()).collection("CartItems").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                int count = 0;
                for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {
                    count++;
                }
                mImageBadgeView.setBadgeValue(count);
                mImageBadgeView.setOnClickListener(view -> {
                    if (mImageBadgeView.getBadgeValue() != 0){
                        sendUserToCheckOut();
                    }else {
                        sendUserToEmptyCart();
                    }
                });

            }
        });
    }

    public  class CategoryImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return waterImages.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View view1 = getLayoutInflater().inflate(R.layout.custom_category_layout,null);
            //getting view in row_data
            TextView name = view1.findViewById(R.id.categoryName);
            ImageView image = view1.findViewById(R.id.categoryImage);

            name.setText(waterNames[i]);
            Glide.with(requireContext())
                    .load(waterImages[i])
                    .apply(new RequestOptions()
                            .override(200,200))
                    .centerCrop()
                    .into(image);
            return view1;
        }
    }

    private void getShopDetails() {
        Query query = db.collection("ShopList");
        FirestoreRecyclerOptions<ShopsDetail> menuItemModel = new FirestoreRecyclerOptions.Builder<ShopsDetail>()
                .setQuery(query, ShopsDetail.class)
                .build();
        FirestoreRecyclerAdapter<ShopsDetail, ShopsItemViewHolder> shopAdapter = new FirestoreRecyclerAdapter<ShopsDetail, ShopsItemViewHolder>(menuItemModel) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull ShopsItemViewHolder holder, int position, @NonNull ShopsDetail model) {
                if (mCurrentUser != null) {
                    DocumentReference docRef = db.collection("UserList").document(mCurrentUser.getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            Double mUserLat = (Double) documentSnapshot.get("latitude");
                            Double mUserLong = (Double) documentSnapshot.get("longitude");
                            Double mResLat = model.getLatitude();
                            Double mResLong = model.getLongitude();
                            int prepTime = Integer.parseInt(Objects.requireNonNull(model.getShop_prep_time()).replace(" Mins", ""));
                            Location userLocation = new Location("");
                            userLocation.setLatitude(mUserLat);
                            userLocation.setLongitude(mUserLong);
                            Location shopLocation = new Location("");
                            shopLocation.setLatitude(mResLat);
                            shopLocation.setLongitude(mResLong);

                            int distanceInMeters = (int) (userLocation.distanceTo(shopLocation));
                            int AvgDrivingSpeedPerKm = 666;
                            int estimatedDriveTimeInMinutes = distanceInMeters / AvgDrivingSpeedPerKm;
                            String deliveryTime = String.valueOf(estimatedDriveTimeInMinutes + prepTime);
                            holder.mShopName.setText(model.getShop_name());
                            String SUID = model.getShop_uid();
                            holder.mAverageDeliveryTime.setText(deliveryTime + " mins");

                            Glide.with(requireActivity())
                                    .load(model.getShop_spotimage())
                                    .into(holder.mShopSpotImage);
                            holder.mAveragePrice.setText("\u20B9" + model.getAverage_price() + " Per Person | ");
                            holder.itemView.setOnClickListener(view -> {

                                Intent intent = new Intent(getActivity(), MainShopPageActivity.class);
                                intent.putExtra("SUID", SUID);
                                intent.putExtra("NAME", model.getShop_name());
                                intent.putExtra("DISTANCE", String.valueOf(distanceInMeters / 1000));
                                intent.putExtra("TIME", deliveryTime);
                                intent.putExtra("PRICE", model.getAverage_price());
                                intent.putExtra("SHOP_IMAGE", model.getShop_spotimage());
                                intent.putExtra("SHOP_NUM", model.getShop_phonenumber());
                                startActivity(intent);
                                requireActivity().overridePendingTransition(0, 0);

                            });
                        }
                    });
                }

            }

            @NonNull
            @Override
            public ShopsItemViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.shop_main_detail, group, false);
                return new ShopsItemViewHolder(view);
            }

            @Override
            public void onError(@NonNull @NotNull FirebaseFirestoreException e) {

            }
        };
        shopAdapter.startListening();
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mShopRecyclerView);
        shopAdapter.notifyDataSetChanged();
        mShopRecyclerView.setAdapter(shopAdapter);
    }

    public static class ShopsItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.shopName)
        TextView mShopName;
        @BindView(R.id.shopImage)
        ImageView mShopSpotImage;
        @BindView(R.id.average_price)
        TextView mAveragePrice;
        @BindView(R.id.average_time)
        TextView mAverageDeliveryTime;

        public ShopsItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void sendUserToCheckOut() {
        Intent intent = new Intent(getActivity(), CartItemActivity.class);
        startActivity(intent);
        requireActivity().overridePendingTransition(0,0);
    }

    private void sendUserToEmptyCart() {
        Intent intent = new Intent(getActivity(), EmptyCartActivity.class);
        startActivity(intent);
        requireActivity().overridePendingTransition(0,0);
    }

}