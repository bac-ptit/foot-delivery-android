package com.example.myapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.Address

class AddressAdapter(
    private val addresses: List<Address>,
    private val onEditClick: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvEdit: TextView = itemView.findViewById(R.id.tvEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        holder.tvPhone.text = "SDT: ${address.phone ?: "Chưa có số điện thoại"}"
        holder.tvAddress.text = "Địa chỉ: ${address.detail}"
        holder.tvEdit.setOnClickListener {
            onEditClick(address)
        }
    }

    override fun getItemCount(): Int = addresses.size
}
