package com.ekosp.indoweb.adminsekolah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ItemListPelajaranTahunBinding
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.PelajaranLaporanTahunData

class PelajaranTahunanAdapter(
    val context: Context,
    private val data: MutableList<PelajaranLaporanTahunData>
) :
    RecyclerView.Adapter<PelajaranTahunanAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemListPelajaranTahunBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding =
            ItemListPelajaranTahunBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = data[position]

        val total = data.hadir.toInt() + data.ijin.toInt() + data.sakit.toInt() + data.lain.toInt()

        (context.resources.getString(R.string.total) + total).also {
            holder.binding.total.text = it
        }

        holder.binding.tvMonth.text = data.bulan
        holder.binding.valuePresent.text = data.hadir
        holder.binding.valuePermit.text = data.ijin
        holder.binding.valueSick.text = data.sakit
        holder.binding.valueOther.text = data.lain

    }

    override fun getItemCount(): Int {
        return data.size
    }
}