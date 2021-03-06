package com.mcdev.memery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.iammert.library.ui.multisearchviewlib.MultiSearchView
import com.mcdev.memery.databinding.ActivitySearchieBinding
import kotlinx.android.synthetic.main.activity_searchie.*
import kotlinx.android.synthetic.main.activity_searchie.view.*


//import android.util.Log
//import androidx.databinding.DataBindingUtil
//import com.iammert.library.ui.multisearchview.databinding.ActivityMainBinding
//import com.iammert.library.ui.multisearchviewlib.MultiSearchView



/*This class is just the kotlin equivalent for the multi search view..currently not using this class bcz i found a way to implement the multi search view in java*/
class Searchie : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_searchie)
        val binding: ActivitySearchieBinding = DataBindingUtil.setContentView(this, R.layout.activity_searchie)

        binding.multiSearchView.setSearchViewListener(object : MultiSearchView.MultiSearchViewListener{
            override fun onItemSelected(index: Int, s: CharSequence) {
                Log.v("TEST", "onItemSelected: index: $index, query: $s")
            }

            override fun onTextChanged(index: Int, s: CharSequence) {
                Log.v("TEST", "changed: index: $index, query: $s")
            }

            override fun onSearchComplete(index: Int, s: CharSequence) {
                Log.v("TEST", "complete: index: $index, query: $s")
            }

            override fun onSearchItemRemoved(index: Int) {
                Log.v("TEST", "remove: index: $index")
            }

        })
    }
}