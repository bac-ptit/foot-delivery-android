package com.example.myapp.adapters

/**
 * @file AddressAdapter.kt
 * @brief Adapter hiển thị danh sách địa chỉ trong RecyclerView.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.Address

/**
 * Adapter hiển thị danh sách địa chỉ trong RecyclerView.
 *
 * Mỗi item hiển thị số điện thoại, địa chỉ chi tiết và nút chỉnh sửa.
 *
 * @property addresses Danh sách địa chỉ cần hiển thị.
 * @property onEditClick Callback được gọi khi người dùng nhấn nút chỉnh sửa một địa chỉ.
 */
class AddressAdapter(
    private val addresses: List<Address>,
    private val onEditClick: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    /**
     * ViewHolder chứa các view hiển thị thông tin của một địa chỉ.
     *
     * @property tvPhone TextView hiển thị số điện thoại.
     * @property tvAddress TextView hiển thị địa chỉ chi tiết.
     * @property tvEdit TextView đóng vai trò nút chỉnh sửa.
     */
    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvEdit: TextView = itemView.findViewById(R.id.tvEdit)
    }

    /**
     * Tạo ViewHolder mới bằng cách inflate layout item_address.
     *
     * @param parent ViewGroup cha chứa RecyclerView.
     * @param viewType Loại view (không sử dụng trong trường hợp này).
     * @return Đối tượng AddressViewHolder mới được tạo.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    /**
     * Gán dữ liệu địa chỉ vào ViewHolder tại vị trí cho trước.
     * Hiển thị số điện thoại, địa chỉ và gán sự kiện nhấn nút chỉnh sửa.
     *
     * @param holder ViewHolder cần gán dữ liệu.
     * @param position Vị trí của item trong danh sách.
     */
    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        holder.tvPhone.text = "SDT: ${address.phone ?: "Chưa có số điện thoại"}"
        holder.tvAddress.text = "Địa chỉ: ${address.detail}"
        holder.tvEdit.setOnClickListener {
            onEditClick(address)
        }
    }

    /**
     * Trả về tổng số địa chỉ trong danh sách.
     *
     * @return Số lượng địa chỉ.
     */
    override fun getItemCount(): Int = addresses.size
}
