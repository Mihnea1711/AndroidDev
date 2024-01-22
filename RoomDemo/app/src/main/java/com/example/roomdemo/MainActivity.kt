package com.example.roomdemo

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdemo.databinding.ActivityMainBinding
import com.example.roomdemo.databinding.DialogUpdateBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // get the employeeDAO instance from the application
        val employeeDAO = (application as EmployeeApp).db.employeeDAO()
        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDAO = employeeDAO)
        }

        lifecycleScope.launch {
            employeeDAO.fetchAll().collect {employeeList ->
                val arrayListOfEmployees = ArrayList(employeeList)
                setupListOfItems(arrayListOfEmployees, employeeDAO)
            }
        }
    }

    private fun addRecord(employeeDAO: IEmployeeDAO) {
        val employeeName: String = binding?.etName?.text.toString()
        val employeeEmail: String = binding?.etEmailId?.text.toString()

        if (employeeName.isNotEmpty() && employeeEmail.isNotEmpty()) {
            lifecycleScope.launch {
                employeeDAO.insert(EmployeeNTT(name = employeeName, email = employeeEmail))

                // use applicationContext or runOnUiThread to display toasts
                Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_SHORT).show()

                // empty fields after inserting record
                binding?.etName?.text?.clear()
                binding?.etEmailId?.text?.clear()
            }
        } else {
            Toast.makeText(applicationContext, "Name or Email cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListOfItems(employeeList: ArrayList<EmployeeNTT>, employeeDAO: IEmployeeDAO) {
        if (employeeList.isNotEmpty()) {
            val itemAdapter = ItemAdapter(
                employeeList,
                {
                  updateID -> updateRecordDialog(updateID, employeeDAO)
                },
                {
                    deleteID -> deleteRecordDialog(deleteID, employeeDAO)
                }
            )

            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id: Int, employeeDAO: IEmployeeDAO) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDAO.fetchEmployeeByID(id).collect {
                if (it != null) {
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmailId.setText(it.email)
                }
            }
        }

        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                lifecycleScope.launch {
                    employeeDAO.update(EmployeeNTT(id, name, email))
                    Toast.makeText(applicationContext, "Record updated.", Toast.LENGTH_SHORT).show()
                    updateDialog.dismiss()
                }
            } else {
                Toast.makeText(applicationContext, "Name or Email cannot be blank.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    private fun deleteRecordDialog(id: Int, employeeDAO: IEmployeeDAO) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDAO.delete(EmployeeNTT(id))
                Toast.makeText(applicationContext, "Record deleted successfully", Toast.LENGTH_SHORT).show()
            }
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)

        alertDialog.show()
    }
}