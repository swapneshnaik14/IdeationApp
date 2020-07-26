package com.nestle.retailerapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nestle.retailerapp.R;
import com.nestle.retailerapp.util.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddProductsActivity extends AppCompatActivity {

    List<String> products;
    HashMap<String,Integer> productQuantityMap;
    HashMap<String,Integer> productPrevQuantityMap;
    HashMap<String,Integer> productPriceMap;
    DatabaseReference mRef;

    TextView productSummary,addProductsTitle;
    Button submitProducts, addProduct;

    Spinner productDropdown;
    TextInputLayout countProduct;
    LinearLayout linearSummary;

    ProgressBar addProductsProgress;
    String uid;
    String city;

    void visibleAllViewsAndDisableProgress() {
        linearSummary.setVisibility(View.VISIBLE);
        addProduct.setVisibility(View.VISIBLE);
        productDropdown.setVisibility(View.VISIBLE);
        countProduct.setVisibility(View.VISIBLE);
        addProductsProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        uid = getIntent().getStringExtra("uid");

        getSupportActionBar().setTitle("Distribute Products");

        productSummary = findViewById(R.id.productSummary);
        submitProducts = findViewById(R.id.submitProducts);
        addProduct = findViewById(R.id.addProduct);
        linearSummary = findViewById(R.id.linearSummary);

        addProductsTitle = findViewById(R.id.addProductsTitle);
        productDropdown = findViewById(R.id.productDropdown);
        countProduct = findViewById(R.id.countProduct);
        addProductsProgress = findViewById(R.id.addProductsProgress);

        products = new ArrayList<>();
        productQuantityMap = new HashMap<>();
        productPrevQuantityMap = new HashMap<>();
        productPriceMap = new HashMap<>();
        mRef = FirebaseDatabase.getInstance().getReference();

        products.add("Select Product");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, products);
        productDropdown.setAdapter(adapter);
        mRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Product p = dataSnapshot.getValue(Product.class);
                    productPriceMap.put(p.getName(), p.getPrice());
                    products.add(p.getName());
                    adapter.notifyDataSetChanged();
                }

                mRef.child("retailers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("city").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot3) {
                        city = snapshot3.getValue(String.class);
                        mRef.child("sales").child(city).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                for(String p: productPriceMap.keySet()) {
                                    if (snapshot2.hasChild(p)) {
                                        if (snapshot2.child(p).hasChild(uid)) {
                                            productPrevQuantityMap.put(p, snapshot2.child(p).child(uid).getValue(Integer.class));
                                        }
                                    }
                                }

                                visibleAllViewsAndDisableProgress();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        submitProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProducts.setText("Submitting");
                HashMap<String, Integer> combinedMap = new HashMap<>();
                for(String k: productQuantityMap.keySet()) {
                    combinedMap.put(k, productQuantityMap.get(k));
                }
                for(String k: productPrevQuantityMap.keySet()) {
                    if (combinedMap.containsKey(k)) {
                        combinedMap.put(k, combinedMap.get(k) + productPrevQuantityMap.get(k));
                    } else {
                        combinedMap.put(k, productPrevQuantityMap.get(k));
                    }
                }
                final Iterator hmIterator = combinedMap.entrySet().iterator();
                while (hmIterator.hasNext()) {
                    Map.Entry mapElement = (Map.Entry)hmIterator.next();
                    mRef.child("sales").child(city).child(mapElement.getKey().toString()).child(uid).setValue(mapElement.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!hmIterator.hasNext()) {
                                startActivity(new Intent(AddProductsActivity.this, HomeActivity.class));
                            }
                        }
                    });
                }

            }
        });

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productDropdown.getSelectedItem().toString().equals("Select Product")) {
                    Snackbar.make(v,"Please select a product", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(countProduct.getEditText().getText().toString()) || Integer.parseInt(countProduct.getEditText().getText().toString()) < 1) {
                    Snackbar.make(v,"Product count must be positive", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    String pr = productDropdown.getSelectedItem().toString();
                    int co = Integer.parseInt(countProduct.getEditText().getText().toString());
                    if(productQuantityMap.containsKey(pr)) {
                        int pre = productQuantityMap.get(pr);
                        productQuantityMap.put(pr, co+pre);
                    } else {
                        productQuantityMap.put(pr, co);
                    }
                    updateTextFromHashmap();
                }
            }
        });

    }

    public void updateTextFromHashmap() {
        String result= "";
        for (String key: productQuantityMap.keySet()) {
            result = result + key + " : " + productQuantityMap.get(key) + "\n";
        }
        productSummary.setText(result);
        productDropdown.setSelection(0);
        countProduct.getEditText().setText("");
    }
}