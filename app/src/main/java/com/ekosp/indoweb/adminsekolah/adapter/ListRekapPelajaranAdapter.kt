package com.ekosp.indoweb.adminsekolah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ItemListKehadiranBinding
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.RekapPelajaran
import java.text.SimpleDateFormat
import java.util.*

class ListRekapPelajaranAdapter(
    private val context: Context,
    private val dataLessonReport: MutableList<RekapPelajaran>,
    private val listener: ClickListener
) :
    RecyclerView.Adapter<ListRekapPelajaranAdapter.MyViewHolder>() {

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
        val data = dataLessonReport[position]

        val localeID = Locale("in", "ID")
        val inputPattern = "dd MMMM yyyy"
        val outputPattern = "dd MMM yyyy"
        val inputDateFormat = SimpleDateFormat(inputPattern, localeID)
        val outputDateFormat = SimpleDateFormat(outputPattern, localeID)
        val convertDate = inputDateFormat.parse(data.tanggal)
        val resultDate = convertDate?.let { outputDateFormat.format(it) }

        holder.binding.tvDatePresence.text = resultDate
        holder.binding.tvTime.text = data.kelas

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

        holder.binding.parentLy.setOnClickListener {
            listener.selectLessonReport(data)
        }
    }

    override fun getItemCount(): Int {
        return dataLessonReport.size
    }

    interface ClickListener {
        fun selectLessonReport(data: RekapPelajaran)
    }
}