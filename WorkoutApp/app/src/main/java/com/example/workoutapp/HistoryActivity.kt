package com.example.workoutapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workoutapp.databinding.ActivityHistoryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private var binding: ActivityHistoryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.tbHistory)
        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
            actionbar.title = "HISTORY"
        }

        binding?.tbHistory?.setNavigationOnClickListener {
            onBackPressed()
        }

        val historyDAO = (application as WorkoutApp).db.historyDAO()
        getAllDates(historyDAO)
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun getAllDates(historyDAO: IHistoryDAO) {
        lifecycleScope.launch {
            historyDAO.fetchHistory().collect { historyList ->
                if (historyList.isNotEmpty()) {
                    binding?.tvHistory?.visibility = View.VISIBLE
                    binding?.rvHistory?.visibility = View.VISIBLE
                    binding?.tvNoDataAvailable?.visibility = View.INVISIBLE

                    binding?.rvHistory?.layoutManager = LinearLayoutManager(this@HistoryActivity)

                    val history = ArrayList<String>()
                    for (item: HistoryNTT in historyList) {
                        history.add(item.date)
                    }

                    val historyAdapter = HistoryAdapter(history)

                    binding?.rvHistory?.adapter = historyAdapter

                } else {
                    binding?.tvHistory?.visibility = View.GONE
                    binding?.rvHistory?.visibility = View.GONE
                    binding?.tvNoDataAvailable?.visibility = View.VISIBLE
                }
            }
        }
    }
}