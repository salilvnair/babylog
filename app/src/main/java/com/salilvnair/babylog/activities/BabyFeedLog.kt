package com.salilvnair.babylog.activities

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.Query
import com.happyplaces.utils.SwipeToDeleteCallback
import com.salilvnair.babylog.R
import com.salilvnair.babylog.adapter.BabyLogAdapter
import com.salilvnair.babylog.database.FireBaseDataHandler
import com.salilvnair.babylog.databinding.ActivityBabyFeedLogBinding
import com.salilvnair.babylog.model.BabyLogModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BabyFeedLog : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityBabyFeedLogBinding
    var showAllToggle = true
    private lateinit var docRef: Query
    private var keptItems = ArrayList<BabyLogModel>()
    private var allKeptItems = ArrayList<BabyLogModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBabyFeedLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        refreshBabyLogs(false)
        if(keptItems.size == 0) {
            disableClearBtn()
        }
        binding.toolbarBtnShowAllLogs.setOnClickListener(this)
        binding.toolbarBtnDelete.setOnClickListener(this)
    }

    private fun enableClearBtnOnDataLoad() {
        binding.toolbarBtnDelete.isEnabled = true
        binding.toolbarBtnDelete.compoundDrawableTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                        this,
                        R.color.white
                )
        )
    }

    private fun disableClearBtn() {
        binding.toolbarBtnDelete.isEnabled = false
        binding.toolbarBtnDelete.compoundDrawableTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                        this,
                        R.color.material_on_background_disabled
                )
        )
    }

    private fun refreshBabyLogs(cache: Boolean) {
        keptItems.clear()
        var dbHandler = FireBaseDataHandler();
        docRef = dbHandler.db()?.collection("babylogs").orderBy(
            "lastFed",
            Query.Direction.DESCENDING
        )
        docRef?.get()?.addOnSuccessListener { collectionSnapshot ->
            val log = collectionSnapshot
            var id = 1
            collectionSnapshot.forEach { s ->
                val babyLogModel = s.toObject(BabyLogModel::class.java)
                babyLogModel.id = id
                babyLogModel.key = s.id
                Log.e("LOADED_DATA", "$babyLogModel")
                keptItems.add(babyLogModel)
                id += 1
            }
            enableClearBtnOnDataLoad()
            showAllBabyLogs(cache)
        }
    }


    private fun showAllBabyLogs(cache: Boolean) {
        if(!cache) {
            allKeptItems = ArrayList(keptItems)
        }
        if(keptItems.isNotEmpty()) {
            binding.rvBabyLogs.visibility = View.VISIBLE
            val filteredKeptItems = filteredTodaysBabyLogs(keptItems)
            var babyLogs = ArrayList<BabyLogModel>(filteredKeptItems)
            if(babyLogs.size == keptItems.size) {
                disableClearBtn()
            }
            if(!showAllToggle) {
                babyLogs = keptItems
            }
            setupKeptItemsRecyclerView(babyLogs)
        }
        else {
            binding.rvBabyLogs.visibility = View.GONE
        }
    }


    private fun filteredTodaysBabyLogs(keptItems: List<BabyLogModel>): List<BabyLogModel> {
        return keptItems.filter { item ->
            val sdf = SimpleDateFormat("yyyyMMdd")
            sdf.format(item.lastFed) == sdf.format(Date())
        };
    }

    private fun deleteOldLogs(oldItems: ArrayList<BabyLogModel>) {
        val dbHandler = FireBaseDataHandler()
        var batch = dbHandler.db().batch()
        oldItems.forEach { item ->
            var docRef = dbHandler.db()?.collection("babylogs").document(item.key!!)
            batch.delete(docRef)
        }
        batch.commit().addOnSuccessListener(OnSuccessListener<Void?> {
            Toast
                .makeText(this, "Old logs deleted successfully!!", Toast.LENGTH_LONG)
                .show()
        })
    }

    private fun setupKeptItemsRecyclerView(babyLogs: ArrayList<BabyLogModel>) {
        binding.rvBabyLogs.layoutManager = LinearLayoutManager(this)
        binding.rvBabyLogs.setHasFixedSize(true)
        val itemAdapter = BabyLogAdapter(this, babyLogs)
        binding.rvBabyLogs.adapter = itemAdapter


        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvBabyLogs.adapter as BabyLogAdapter
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvBabyLogs)
    }

    override fun onClick(v: View?) {
        when(v) {
            binding.toolbarBtnShowAllLogs -> {
                if (!showAllToggle) binding.toolbarBtnShowAllLogs.text = "Today's Log"
                else binding.toolbarBtnShowAllLogs.text = "All Logs"
                showAllToggle = !showAllToggle;
                showAllBabyLogs(true)
            }
            binding.toolbarBtnDelete -> {
                confirmAndDelete()
            }
        }
    }
    
    private fun deleteOldLogsAndReInitScreen() {
        val newKeptItems = filteredTodaysBabyLogs(allKeptItems)
        allKeptItems.removeAll(newKeptItems)
        deleteOldLogs(allKeptItems)
        refreshBabyLogs(false)
        disableClearBtn()
    }

    private fun confirmAndDelete() {
        val builder = AlertDialog.Builder(this)
        builder
                .setMessage("All Old logs will be deleted, Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    // Delete selected note from database
                    deleteOldLogsAndReInitScreen()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
        val alert = builder.create()
        alert.show()
    }
}