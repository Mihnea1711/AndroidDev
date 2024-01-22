package com.example.workoutapp

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.workoutapp.databinding.ActivityExerciseBinding
import com.example.workoutapp.databinding.ActivityMainBinding
import com.example.workoutapp.databinding.DialogCustomBackConfirmationBinding
import java.util.Locale

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var exerciseBinding: ActivityExerciseBinding? = null

    private var restTimer: CountDownTimer? = null
    private var restProgress: Int = 0
    private val totalRestDuration: Long = 1

    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress: Int = 0
    private val totalExerciseDuration: Long = 1

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition: Int = -1

    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null

    private var exerciseAdapter: ExerciseStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseBinding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(exerciseBinding?.root)

        setSupportActionBar(exerciseBinding?.tbExercise)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        tts = TextToSpeech(this, this)

        exerciseList = Constants.defaultExerciseList()

        exerciseBinding?.tbExercise?.setNavigationOnClickListener {
            customDialogBackBtn()
        }

        setupRestView()
        setupExerciseRecyclerView()
    }

    private fun customDialogBackBtn() {
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)

        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)

        dialogBinding.tvYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }

        dialogBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun setupExerciseRecyclerView() {
        exerciseBinding ?.rvExerciseStatus?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)

        exerciseBinding?.rvExerciseStatus?.adapter = exerciseAdapter
    }

    private fun setupRestView() {
        try {
            val soundURI = Uri.parse("android.resource://com.example.workoutapp/" + R.raw.press_start)
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()

        } catch (e: Exception) {
            Log.e("error player", e.message.toString())
        }

        exerciseBinding?.flRestView?.visibility = View.VISIBLE
        exerciseBinding?.tvTitle?.visibility = View.VISIBLE
        exerciseBinding?.tvExerciseName?.visibility = View.INVISIBLE
        exerciseBinding?.flExerciseView?.visibility = View.INVISIBLE
        exerciseBinding?.ivExerciseImage?.visibility = View.INVISIBLE
        exerciseBinding?.tvUpcomingExerciseLabel?.visibility = View.VISIBLE
        exerciseBinding?.tvUpcomingExerciseName?.visibility = View.VISIBLE
        exerciseBinding?.tvUpcomingExerciseName?.text = exerciseList!![currentExercisePosition + 1].getName()

        if(restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        setRestProgressBar()

    }

    private fun setupExerciseView() {
        exerciseBinding?.flRestView?.visibility = View.INVISIBLE
        exerciseBinding?.tvTitle?.visibility = View.INVISIBLE
        exerciseBinding?.tvExerciseName?.visibility = View.VISIBLE
        exerciseBinding?.flExerciseView?.visibility = View.VISIBLE
        exerciseBinding?.ivExerciseImage?.visibility = View.VISIBLE
        exerciseBinding?.tvUpcomingExerciseLabel?.visibility = View.INVISIBLE
        exerciseBinding?.tvUpcomingExerciseName?.visibility = View.INVISIBLE

        if(exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())

        exerciseBinding?.ivExerciseImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        exerciseBinding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }

    private fun setRestProgressBar() {
        exerciseBinding?.progressBar?.progress = restProgress

        exerciseTimer = object: CountDownTimer(totalRestDuration * 1000, 1000) {
            override fun onTick(p0: Long) {
                restProgress++
                exerciseBinding?.progressBar?.progress = totalRestDuration.toInt() - restProgress
                exerciseBinding?.tvTimer?.text = (totalRestDuration - restProgress).toString()
            }

            override fun onFinish() {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()

                setupExerciseView()
            }
        }.start()
    }

    private fun setExerciseProgressBar() {
        exerciseBinding?.progressBarExercise?.progress = exerciseProgress

        restTimer = object: CountDownTimer(totalExerciseDuration * 1000, 1000) {
            override fun onTick(p0: Long) {
                exerciseProgress++
                exerciseBinding?.progressBarExercise?.progress = totalExerciseDuration.toInt() - exerciseProgress
                exerciseBinding?.tvTimerExercise?.text = (totalExerciseDuration - exerciseProgress).toString()
            }

            override fun onFinish() {
                if(currentExercisePosition < exerciseList?.size!! - 1) {
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseAdapter!!.notifyDataSetChanged()
                    setupRestView()
                } else {
                    finish()

                    val intent = Intent(this@ExerciseActivity, FinishActivity::class.java)
                    startActivity(intent)
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        if(restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        if(exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }

        if(player != null) {
            player!!.stop()
        }

        exerciseBinding = null
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }

        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}