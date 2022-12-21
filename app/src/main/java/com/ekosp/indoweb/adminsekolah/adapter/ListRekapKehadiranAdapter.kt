package com.ekosp.indoweb.adminsekolah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ItemListKehadiranBinding
import com.ekosp.indoweb.adminsekolah.model.data_laporan.Rekap
import java.text.SimpleDateFormat
import java.util.*

class ListRekapKehadiranAdapter(
    private val context: Context,
    private val dataKehadiran: MutableList<Rekap>,
    private val listener: ClickListener
) :
    RecyclerView.Adapter<ListRekapKehadiranAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemListKehadiranBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding =
            ItemListKehadiranBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = dataKehadiran[position]

        val localeID = Locale("in", "ID")
        val inputPattern = "dd MMMM yyyy"
        val outputPattern = "dd MMM yyyy"
        val inputDateFormat = SimpleDateFormat(inputPattern, localeID)
        val outputDateFormat = SimpleDateFormat(outputPattern, localeID)
        val date = inputDateFormat.parse(data.hari)
        val resultDate = date?.let { outputDateFormat.format(it) }
        holder.binding.tvDatePresence.text = resultDate

        val inputCome = data.detail.jam_datang
        val inputReturn = data.detail.jam_pulang
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val come: String = if (inputCome.isEmpty()) {
            ""
        } else {
            val outputCome: Date = inputFormat.parse(inputCome)!!
            outputFormat.format(outputCome)
        }

        val home: String = if (inputReturn.isEmpty()) {
            ""
        } else {
            val outputReturn: Date = inputFormat.parse(inputReturn)!!
            outputFormat.format(outputReturn)
        }

        if (home.isEmpty()) {
            holder.binding.tvTime.text = come
        } else {
            holder.binding.tvTime.text = buildString {
                append(come)
                append(" — ")
                append(home)
            }
        }

        when (data.status) {
            "Hadir" -> holder.binding.tvDetail.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.green_pesantren_2
                )
            )
            "Ijin" -> holder.binding.tvDetail.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
            "Sakit" -> holder.binding.tvDetail.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.lime_dark
                )
            )
            "Lain-lain" -> holder.binding.tvDetail.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
        }

        if (data.status == "Ijin" || data.status == "Sakit" || data.status == "Lain-lain") {
            holder.binding.tvTime.visibility = View.GONE
        }

        holder.binding.parentLy.setOnClickListener {
            listener.selectKehadiran(data)
        }
    }

    override fun getItemCount(): Int {
        return dataKehadiran.size
    }

    interface ClickListener {
        fun selectKehadiran(data: Rekap)
    }
}