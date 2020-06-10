package com.mcdev.memery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.iammert.library.ui.multisearchviewlib.MultiSearchView;
import com.mcdev.memery.databinding.ActivitySearchBinding;

import org.jetbrains.annotations.NotNull;

public class SearchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActivitySearchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        binding.multiSearchView.setSearchViewListener(new MultiSearchView.MultiSearchViewListener() {
            @Override
            public void onTextChanged(int i, @NotNull CharSequence charSequence) {

            }

            @Override
            public void onSearchComplete(int i, @NotNull CharSequence charSequence) {

            }

            @Override
            public void onSearchItemRemoved(int i) {

            }

            @Override
            public void onItemSelected(int i, @NotNull CharSequence charSequence) {

            }
        });
    }
}