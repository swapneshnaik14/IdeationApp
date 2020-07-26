package com.nestle.retailerapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nestle.retailerapp.R;
import com.nestle.retailerapp.util.adapter.ProductAdapter;
import com.nestle.retailerapp.util.model.Product;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ProgressBar productsProgress;
    RecyclerView productsList;
    DatabaseReference mRef;

    List<Product> productList;
    ProductAdapter adapter;

    FloatingActionButton floatingActionButton;

    IntentIntegrator qrScan;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        qrScan = new IntentIntegrator(this);

        productsList = findViewById(R.id.productsRecycler);
        productsProgress = findViewById(R.id.productsProgress);
        mRef = FirebaseDatabase.getInstance().getReference();
        floatingActionButton = findViewById(R.id.fabHome);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        productsList.setLayoutManager(new LinearLayoutManager(this));
        productsList.addItemDecoration(new
                DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        productsList.setAdapter(adapter);

        getSupportActionBar().setTitle("Product List");

        mRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productsProgress.setVisibility(View.GONE);
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    productList.add(dataSnapshot.getValue(Product.class));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", error+"");
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FAB", "floating action button clicked");
                qrScan.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Snackbar.make(findViewById(R.id.fabHome), "Result Not Found", LENGTH_LONG).show();
            } else {
                Log.d("QR Content", result.getContents()+ "");

                mRef.child("sales_promoter").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            if (dataSnapshot.getKey().equals(result.getContents())) {
                                found = true;
                                startActivity(new Intent(HomeActivity.this, AddProductsActivity.class).putExtra("uid", result.getContents()));
                            }
                        }
                        if (!found) {
                            Snackbar.make(findViewById(R.id.fabHome), "Retailer is fake...", LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("Error", error+ "");
                    }
                });

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}