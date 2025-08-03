package com.example.onetapalarm // Make sure this matches your package name!

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onetapalarm.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarms = mutableListOf<Alarm>()
    private lateinit var alarmManager: AlarmManager

    private val gson = Gson()
    private val prefsName = "AlarmAppPrefs"
    private val alarmsKey = "alarms"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        setupRecyclerView()
        loadAlarms()

        // This now calls our new permission-checking function first
        binding.addAlarmFab.setOnClickListener {
            checkPermissionsAndShowTimePicker()
        }
    }

    // This function checks for the required permissions before showing the time picker.
    private fun checkPermissionsAndShowTimePicker() {
        // Check for notification permission first (for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // You can add a more friendly dialog here to explain why you need notifications
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
                Snackbar.make(binding.root, "Please enable notifications to receive alarms.", Snackbar.LENGTH_LONG).show()
                return // Stop here until permission is granted
            }
        }

        // Now check for the exact alarm permission (for Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Permission is not granted, guide the user to the settings screen.
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                Snackbar.make(binding.root, "Please grant permission to schedule exact alarms.", Snackbar.LENGTH_LONG).show()
                return // Stop here until permission is granted
            }
        }

        // If all permissions are granted, proceed to show the time picker.
        showTimePickerDialog()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(alarms) { alarm, position ->
            if (alarm.isEnabled) {
                scheduleAlarm(alarm)
            } else {
                cancelAlarm(alarm)
            }
            saveAlarms()
            alarmAdapter.notifyItemChanged(position)
        }
        binding.alarmRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = alarmAdapter
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val newAlarm = Alarm(
                    id = Random.nextInt(Int.MAX_VALUE),
                    hour = selectedHour,
                    minute = selectedMinute,
                    isEnabled = true
                )

                alarms.add(newAlarm)
                alarms.sortBy { it.hour * 60 + it.minute }
                val newIndex = alarms.indexOf(newAlarm)
                alarmAdapter.notifyItemInserted(newIndex)

                scheduleAlarm(newAlarm)
                saveAlarms()
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun scheduleAlarm(alarm: Alarm) {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm(alarm: Alarm) {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
    // Converts the alarm list to a JSON string and saves it in SharedPreferences.
    private fun saveAlarms() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val alarmsJson = gson.toJson(alarms)
        prefs.edit {
            putString(alarmsKey, alarmsJson)
        }
    }

    // Loads the JSON string from SharedPreferences and converts it back to an alarm list.
    private fun loadAlarms() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val alarmsJson = prefs.getString(alarmsKey, null)

        if (alarmsJson != null) {
            val type = object : TypeToken<MutableList<Alarm>>() {}.type
            val loadedAlarms: MutableList<Alarm> = gson.fromJson(alarmsJson, type)

            alarms.clear()
            alarms.addAll(loadedAlarms)

            @SuppressLint("NotifyDataSetChanged")
            alarmAdapter.notifyDataSetChanged()
        }
    }
}