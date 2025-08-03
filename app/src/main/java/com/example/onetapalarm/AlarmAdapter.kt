package com.example.onetapalarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class AlarmAdapter(
    private val alarms: List<Alarm>,
    private val onToggle: (Alarm, Int) -> Unit // A function to handle the switch toggle
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    // This class holds the views for each list item.
    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.alarmTimeTextView)
        val switch: SwitchMaterial = view.findViewById(R.id.alarmSwitch)
    }

    // Creates a new view holder when the RecyclerView needs one.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    // Returns the total number of items in the list.
    override fun getItemCount() = alarms.size

    // Binds the data to the views in a given view holder.
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        // Format the time to be human-readable (e.g., 07:05)
        val timeString = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)
        holder.timeTextView.text = timeString
        holder.switch.isChecked = alarm.isEnabled

        // Set a listener to handle when the user toggles the switch
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            alarm.isEnabled = isChecked
            onToggle(alarm, position)
        }
    }
}