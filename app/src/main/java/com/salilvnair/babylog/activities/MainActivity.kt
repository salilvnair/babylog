package com.salilvnair.babylog.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Query
import com.salilvnair.babylog.database.FireBaseDataHandler
import com.salilvnair.babylog.databinding.ActivityEditTimeBinding
import com.salilvnair.babylog.databinding.ActivityMainBinding
import com.salilvnair.babylog.model.BabyLogModel
import com.salilvnair.babylog.model.BabyThyroidRecordModel
import com.salilvnair.babylog.util.DateUtil
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var changeTimeBinding: ActivityEditTimeBinding
    private lateinit var docRef: Query
    private lateinit var thyDocRef: Query
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addItemBtn.setOnClickListener(this)
        binding.changeTimeBtn.setOnClickListener(this)
        binding.llLatestFed.setOnClickListener(this)
        binding.swipeRefresh.setOnRefreshListener {
            Log.i("SWIPE_REFRESH", "onRefresh called from SwipeRefreshLayout")
            initData()
            initThyData()
            binding.swipeRefresh.isRefreshing = false
        }
        var dbHandler = FireBaseDataHandler();

        docRef = dbHandler.db()?.collection("babylogs").orderBy("lastFed",Query.Direction.DESCENDING).limit(1)
        thyDocRef = dbHandler.db()?.collection("thyroidlogs").limit(1)
        initData()
        initThyData()
    }

    private fun initThyData() {
        if(!DateUtil.dateTimeAfter("06:00")) {
            return
        }
        thyDocRef?.get()?.addOnSuccessListener { collectionSnapshot ->
            if(collectionSnapshot.isEmpty) {
                showThyDialog(null)
            }
            collectionSnapshot.forEach { s ->
                val babyThyModel = s.toObject(BabyThyroidRecordModel::class.java)
                val today = DateUtil.formattedDate(Date())
                val thyDay = DateUtil.formattedDate(babyThyModel.date)
                babyThyModel.key = s.id
                if(today != thyDay) {
                    babyThyModel.completed = false
                    babyThyModel.date = Date()
                }
                if(!babyThyModel.completed) {
                    showThyDialog(babyThyModel)
                }
            }
        }
    }

    private fun initData() {
        docRef?.get()?.addOnSuccessListener { collectionSnapshot ->
            val log = collectionSnapshot
            collectionSnapshot.forEach { s ->
                val babyLogModel = s.toObject(BabyLogModel::class.java)
                Log.e("LOADED_DATA", "${s.id} ${s.data["lastFed"]}")
                val lastFedText = lastFedTime(babyLogModel.lastFed, Date())
                binding.tvLastFeedTime.text = lastFedText
            }
        }
    }

    private fun lastFedTime(d1: Date?, d2: Date): String {
        val differenceInTime = d2.time - d1!!.time
        
        val differenceInMinutes: Long = (TimeUnit.MILLISECONDS
            .toMinutes(differenceInTime)
                % 60)

        val differenceInHours: Long = (TimeUnit.MILLISECONDS
            .toHours(differenceInTime)
                % 24)
        return "$differenceInHours hr, $differenceInMinutes min ago"
    }

    private fun addCurrentTimeAndFetchLastFeedTime(date: Date): BabyLogModel {
        val dbHandler = FireBaseDataHandler()
        val model = BabyLogModel(0,"", date)
        dbHandler.add(model)
        //val recordedTime = DatabaseHandler.formattedDateTime(date)
//        Toast
//            .makeText(this, "Feed time logged: $recordedTime", Toast.LENGTH_LONG)
//            .show()
        return model
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.addItemBtn -> {
                val model = addCurrentTimeAndFetchLastFeedTime(Date())
                val lastFed = model.lastFed;
                val lastFedText = lastFedTime(lastFed, Date())
                binding.tvLastFeedTime.text = lastFedText
            }
            binding.changeTimeBtn -> {
                showEditTimeDialog()
            }
            binding.llLatestFed -> {
                val intent = Intent(this@MainActivity, BabyFeedLog::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showThyDialog(model: BabyThyroidRecordModel?) {
        val alertDialog: AlertDialog  = AlertDialog.Builder(this).create();
        alertDialog.setTitle("Did you give thyroid tablet today!");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", DialogInterface.OnClickListener() { _: DialogInterface, i: Int ->
            val dbHandler = FireBaseDataHandler()
            var thyModel = BabyThyroidRecordModel("",true, Date())
            if(model != null) {
                thyModel = model
            }
            thyModel.completed = true
            dbHandler.recordThyroidTabletCheck(thyModel)
            alertDialog.dismiss()
        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", DialogInterface.OnClickListener() { _: DialogInterface, i: Int ->
            val dbHandler = FireBaseDataHandler()
            var thyModel = BabyThyroidRecordModel("",false, Date())
            if(model != null) {
                thyModel = model
            }
            dbHandler.recordThyroidTabletCheck(thyModel)
            alertDialog.dismiss()
        });

        //alertDialog.setView(binding.root);
        alertDialog.show();
    }


    private fun showEditTimeDialog() {
        changeTimeBinding = ActivityEditTimeBinding.inflate(layoutInflater)
        val alertDialog: AlertDialog  = AlertDialog.Builder(this).create();
        alertDialog.setTitle("Change Last Feed Time");
        //alertDialog.setIcon("Icon id here");
        alertDialog.setCancelable(false);
        //alertDialog.setMessage("Change the time of the last feed");


        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", DialogInterface.OnClickListener() { _: DialogInterface, i: Int ->
            val newDateTime = getDateFromTimePicker(changeTimeBinding.timePicker1)
            val dbHandler = FireBaseDataHandler()
            docRef?.get()?.addOnSuccessListener { collectionSnapshot ->
                collectionSnapshot.forEach { s ->
                    val babyLogModel = s.toObject(BabyLogModel::class.java)
                    babyLogModel.lastFed = newDateTime
                    babyLogModel.key = s.id
                    dbHandler.update(babyLogModel)
                }
            }
            alertDialog.dismiss()
        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", DialogInterface.OnClickListener() { _: DialogInterface, i: Int ->
            alertDialog.dismiss()
        });

        alertDialog.setView(changeTimeBinding.root);
        alertDialog.show();
    }

    private fun getDateFromTimePicker(picker: TimePicker): Date {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR);
        val month = calendar.get(Calendar.MONTH);
        val day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, picker.hour, picker.minute)
        return calendar.time
    }
}