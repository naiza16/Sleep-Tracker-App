package com.example.sleep_trackerapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private lateinit var sleepStageText: TextView
    private lateinit var avgSleepText: TextView
    private lateinit var goalSleepText: TextView

    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var resetBtn: Button
    private lateinit var settingsBtn: Button

    private var isRunning = false
    private var totalSleepTime: Long = 0
    private var sleepSessions = 0
    private var goalSleepTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IMPORTANT
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("SleepData", MODE_PRIVATE)

        totalSleepTime = prefs.getLong("TOTAL_TIME", 0)
        sleepSessions = prefs.getInt("SESSION_COUNT", 0)
        goalSleepTime = prefs.getLong("GOAL", 0)

        chronometer = findViewById(R.id.chronometer)
        sleepStageText = findViewById(R.id.sleepStageText)
        avgSleepText = findViewById(R.id.avgSleepText)
        goalSleepText = findViewById(R.id.goalSleepText)

        startBtn = findViewById(R.id.startBtn)
        stopBtn = findViewById(R.id.stopBtn)
        resetBtn = findViewById(R.id.resetBtn)
        settingsBtn = findViewById(R.id.settingsBtn)

        updateGoalText()

        // START
        startBtn.setOnClickListener {
            if (!isRunning) {
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
                isRunning = true
            }
        }

        // STOP
        stopBtn.setOnClickListener {
            if (!isRunning) {
                Toast.makeText(this, "Start first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            chronometer.stop()
            isRunning = false

            val elapsed = SystemClock.elapsedRealtime() - chronometer.base

            totalSleepTime += elapsed
            sleepSessions++

            val editor = prefs.edit()
            editor.putLong("TOTAL_TIME", totalSleepTime)
            editor.putInt("SESSION_COUNT", sleepSessions)
            editor.apply()

            val avg = totalSleepTime / sleepSessions
            val min = avg / 60000
            val sec = (avg / 1000) % 60

            avgSleepText.text = "Avg Sleep: ${min}m ${sec}s"

            if (goalSleepTime > 0) {
                val diff = goalSleepTime - elapsed

                val m = abs(diff) / 60000
                val s = (abs(diff) / 1000) % 60

                sleepStageText.text = when {
                    diff > 0 -> "Slept ${m}m ${s}s less than goal"
                    diff < 0 -> "Exceeded goal by ${m}m ${s}s"
                    else -> "Goal achieved!"
                }
            }
        }

        // RESET
        resetBtn.setOnClickListener {
            chronometer.base = SystemClock.elapsedRealtime()
            sleepStageText.text = "Sleep Stage: Awake"
            isRunning = false
        }

        // SLEEP STAGE
        chronometer.setOnChronometerTickListener {
            val sec = (SystemClock.elapsedRealtime() - chronometer.base) / 1000

            sleepStageText.text =
                if (sec < 10) "Sleep Stage: Light Sleep"
                else "Sleep Stage: Deep Sleep"
        }

        // SETTINGS
        settingsBtn.setOnClickListener {
            val picker = TimePickerDialog(this,
                { _, hour, minute ->
                    goalSleepTime = (hour * 3600000L) + (minute * 60000L)
                    prefs.edit().putLong("GOAL", goalSleepTime).apply()
                    updateGoalText()
                }, 7, 0, true)

            picker.show()
        }
    }

    private fun updateGoalText() {
        if (goalSleepTime > 0) {
            val m = goalSleepTime / 60000
            val s = (goalSleepTime / 1000) % 60
            goalSleepText.text = "Goal: ${m}m ${s}s"
        } else {
            goalSleepText.text = "Goal: Not Set"
        }
    }
}