package com.example.workoutapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.workoutapp.databinding.ActivityBmiBinding
import java.math.BigDecimal
import java.math.RoundingMode

class BMIActivity : AppCompatActivity() {
    companion object {
        private const val METRIC_UNITS_VIEW = "METRIC_UNIT_VIEW"
        private const val US_UNITS_VIEW = "US_UNITS_VIEW"
    }

    private var binding: ActivityBmiBinding? = null
    private var currentVisibleView: String = METRIC_UNITS_VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.tbBMI)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "CALCULATE BMI"
        }

        binding?.tbBMI?.setNavigationOnClickListener {
            onBackPressed()
        }

        makeVisibleMetricUnits()

        binding?.rgUnits?.setOnCheckedChangeListener { _, checkedId: Int ->
            if (checkedId == R.id.rbMetric) {
                makeVisibleMetricUnits()
            } else {
                makeVisibleUSUnits()
            }
        }

        binding?.btnCalculate?.setOnClickListener {
            when (currentVisibleView) {
                METRIC_UNITS_VIEW -> {
                    if (validateMetricUnits()) {
                        val weight: Float = binding?.etWeight?.text.toString().toFloat()
                        val height: Float = binding?.etHeight?.text.toString()
                            .toFloat() / 100              //convert to meters

                        val bmi = calculateMetricBMI(weight, height)

                        // display bmi result
                        displayBMIResults(bmi)
                    } else {
                        Toast.makeText(
                            this@BMIActivity,
                            "Please enter valid values for weight and height",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                US_UNITS_VIEW -> {
                    if (validateUSUnits()) {
                        val weightLBS: Float = binding?.etWeightPounds?.text.toString().toFloat()
                        val heightFeet: Float = binding?.etHeightFeet?.text.toString().toFloat()
                        val heightInch: Float = binding?.etHeightInch?.text.toString().toFloat()

                        val bmi = calculateUSBMI(weightLBS, heightFeet, heightInch)

                        // display bmi result
                        displayBMIResults(bmi)
                    } else {
                        Toast.makeText(
                            this@BMIActivity,
                            "Please enter valid values for weight and height",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun calculateMetricBMI(weight: Float, height: Float): Float {
        return weight / (height * height)
    }

    private fun calculateUSBMI(weight: Float, heightFeet: Float, heightInch: Float): Float {
        val totalHeightInInches = 12 * heightFeet + heightInch
        return 703 * weight / (totalHeightInInches * totalHeightInInches)
    }

    private fun displayBMIResults(bmi: Float) {
        val bmiLabel: String
        val bmiDescription: String

        if (bmi.compareTo(15f) <= 0) {
            bmiLabel = "Very severely underweight"
            bmiDescription = "Oops! You really need to take better care of yourself! Eat more!"
        } else if (bmi.compareTo(15f) > 0 && bmi.compareTo(16f) <= 0
        ) {
            bmiLabel = "Severely underweight"
            bmiDescription = "Oops!You really need to take better care of yourself! Eat more!"
        } else if (bmi.compareTo(16f) > 0 && bmi.compareTo(18.5f) <= 0
        ) {
            bmiLabel = "Underweight"
            bmiDescription = "Oops! You really need to take better care of yourself! Eat more!"
        } else if (bmi.compareTo(18.5f) > 0 && bmi.compareTo(25f) <= 0
        ) {
            bmiLabel = "Normal"
            bmiDescription = "Congratulations! You are in a good shape!"
        } else if (bmi.compareTo(25f) > 0 && bmi.compareTo(30f) <= 0
        ) {
            bmiLabel = "Overweight"
            bmiDescription = "Oops! You really need to take care of your yourself! Workout maybe!"
        } else if (bmi.compareTo(30f) > 0 && bmi.compareTo(35f) <= 0
        ) {
            bmiLabel = "Obese Class | (Moderately obese)"
            bmiDescription = "Oops! You really need to take care of your yourself! Workout maybe!"
        } else if (bmi.compareTo(35f) > 0 && bmi.compareTo(40f) <= 0
        ) {
            bmiLabel = "Obese Class || (Severely obese)"
            bmiDescription = "OMG! You are in a very dangerous condition! Act now!"
        } else {
            bmiLabel = "Obese Class ||| (Very Severely obese)"
            bmiDescription = "OMG! You are in a very dangerous condition! Act now!"
        }

        val bmiValue = BigDecimal(bmi.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toString()

        binding?.llBMIResult?.visibility = View.VISIBLE
        binding?.tvBMIValue?.text = bmiValue
        binding?.tvBMIType?.text = bmiLabel
        binding?.tvBMIDescription?.text = bmiDescription

    }

    private fun validateMetricUnits(): Boolean {
        var isValid = true

        if (binding?.etWeight?.text.toString().isEmpty()) {
            isValid = false
        } else if (binding?.etHeight?.text.toString().isEmpty()) {
            isValid = false
        }

        return isValid
    }

    private fun validateUSUnits(): Boolean {
        var isValid = true

        if (binding?.etWeightPounds?.text.toString().isEmpty()) {
            isValid = false
        } else if (binding?.etHeightFeet?.text.toString().isEmpty()) {
            isValid = false
        } else if (binding?.etHeightInch?.text.toString().isEmpty()) {
            isValid = false
        }

        return isValid
    }

    private fun makeVisibleMetricUnits() {
        currentVisibleView = METRIC_UNITS_VIEW

        binding?.tilWeight?.visibility = View.VISIBLE
        binding?.tilHeight?.visibility = View.VISIBLE

        binding?.tilWeightPounds?.visibility = View.GONE
        binding?.tilHeightFeet?.visibility = View.GONE
        binding?.tilHeightInch?.visibility = View.GONE

        binding?.etHeight?.text!!.clear()
        binding?.etWeight?.text!!.clear()

        binding?.llBMIResult?.visibility = View.INVISIBLE
    }

    private fun makeVisibleUSUnits() {
        currentVisibleView = US_UNITS_VIEW

        binding?.tilWeight?.visibility = View.INVISIBLE
        binding?.tilHeight?.visibility = View.INVISIBLE

        binding?.tilWeightPounds?.visibility = View.VISIBLE
        binding?.tilHeightFeet?.visibility = View.VISIBLE
        binding?.tilHeightInch?.visibility = View.VISIBLE

        binding?.etHeightFeet?.text!!.clear()
        binding?.etHeightInch?.text!!.clear()
        binding?.etWeightPounds?.text!!.clear()

        binding?.llBMIResult?.visibility = View.INVISIBLE
    }
}