package com.ekosp.indoweb.epesantren.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.helper.parseDateToddMMyyyy
import com.ekosp.indoweb.epesantren.model.data_laporan.Rekap
import java.text.SimpleDateFormat
import java.util.*

class ListRekapKehadiranAdapter(
    private val context: Context,
    private val dataKehadiran: MutableList<Rekap>,
    val listener: ClickListener
) :
    RecyclerView.Adapter<ListRekapKehadiranAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tgl_kehadiran: TextView
        var tvTime: TextView
        var status: TextView
        var parentLayout: ConstraintLayout

        init {
            tgl_kehadiran = view.findViewById<View>(R.id.txt_tgl_kehadiran) as TextView
            tvTime = view.findViewById<View>(R.id.tv_time) as TextView
            status = view.findViewById<View>(R.id.txt_status) as TextView
            parentLayout = view.findViewById<View>(R.id.parentLy) as ConstraintLayout
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_kehadiran, parent, false)

        return MyViewHolder(view)
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = dataKehadiran[position]

        val tglRekap = parseDateToddMMyyyy(data.hari)

        holder.tgl_kehadiran.text = tglRekap

        val inputCome = data.detail.jam_datang
        val inputHome = data.detail.jam_pulang
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val come: String = if (inputCome.isEmpty()) {
            ""
        } else {
            val outputCome: Date = inputFormat.parse(inputCome)
            outputFormat.format(outputCome)
        }

        val home: String = if (inputHome.isEmpty()) {
            ""
        } else {
            val outputHome: Date = inputFormat.parse(inputHome)
            outputFormat.format(outputHome)
        }

        if (home.isEmpty()) {
            holder.tvTime.text = come
        } else {
            holder.tvTime.text = buildString {
                append(come)
                append(" — ")
                append(home)
            }
        }

        when (data.status){
            "Hadir" -> holder.status.setTextColor(ContextCompat.getColor(context,R.color.green_pesantren_2))
            "Sakit" -> holder.status.setTextColor(ContextCompat.getColor(context,R.color.red))
            "Cuti" -> holder.status.setTextColor(ContextCompat.getColor(context,R.color.lime_dark))
            "Keperluan Lain" -> holder.status.setTextColor(ContextCompat.getColor(context,R.color.black))
        }

        //holder.status.text = data.status
        holder.status.text = "Detail"

        holder.parentLayout.setOnClickListener {
//            Toast.makeText(context, "Menu dalam pengembangan", Toast.LENGTH_SHORT).show()
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
