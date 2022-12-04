package id.indoweb.elazis.presensi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.ItemListKehadiranBinding

import id.indoweb.elazis.presensi.helper.parseDateToddMMyyyy
import id.indoweb.elazis.presensi.model.data_laporan.Rekap
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

        val tglRecap = parseDateToddMMyyyy(data.hari)
        holder.binding.tvDatePresence.text = tglRecap

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
                    R.color.colorPrimaryDark
                )
            )
            "Sakit" -> holder.binding.tvDetail.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
            "Cuti" -> holder.binding.tvDetail.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.lime_dark
                )
            )
            "Keperluan Lain" -> holder.binding.tvDetail.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
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
