package com.eryaz.aceesswifidevices

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eryaz.aceesswifidevices.databinding.RecyclerRowBinding

class WifiAdapter(val list:ArrayList<ScanResult>):RecyclerView.Adapter<WifiAdapter.WifiHolder>() {
    class WifiHolder(val binding:RecyclerRowBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiHolder {
        val binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return WifiHolder(binding)
    }

    override fun onBindViewHolder(holder: WifiHolder, position: Int) {

        holder.binding.textView.text = list.get(position).SSID
    }

    override fun getItemCount(): Int {
       return list.size
    }
}