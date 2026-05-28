package com.example.myapp.screens.adapters

/**
 * @file FAQAdapter.kt
 * @brief Adapter hiển thị danh sách câu hỏi thường gặp (FAQ) trong RecyclerView.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.FAQ

/**
 * Adapter hiển thị danh sách câu hỏi thường gặp (FAQ).
 * Hỗ trợ chức năng ẩn/hiện câu trả lời khi nhấn vào câu hỏi.
 *
 * @property faqs Danh sách các cặp câu hỏi - câu trả lời.
 */
class FAQAdapter(private val faqs: List<FAQ>) :
    RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    /**
     * ViewHolder chứa các view hiển thị một mục FAQ.
     *
     * @property tvQuestion TextView hiển thị nội dung câu hỏi.
     * @property tvAnswer TextView hiển thị nội dung câu trả lời.
     */
    class FAQViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
    }

    /**
     * Tạo ViewHolder mới bằng cách inflate layout item_faq.
     *
     * @param parent ViewGroup cha chứa RecyclerView.
     * @param viewType Loại view (không sử dụng trong trường hợp này).
     * @return Đối tượng FAQViewHolder mới được tạo.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    /**
     * Gán dữ liệu câu hỏi và câu trả lời vào ViewHolder.
     * Thiết lập sự kiện nhấn để toggle hiển thị câu trả lời.
     *
     * @param holder ViewHolder cần gán dữ liệu.
     * @param position Vị trí của item trong danh sách.
     */
    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val faq = faqs[position]
        holder.tvQuestion.text = faq.question
        holder.tvAnswer.text = faq.answer
        
        // Toggle answer visibility when clicking on question
        holder.itemView.setOnClickListener {
            if (holder.tvAnswer.visibility == View.VISIBLE) {
                holder.tvAnswer.visibility = View.GONE
            } else {
                holder.tvAnswer.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Trả về tổng số mục FAQ trong danh sách.
     *
     * @return Số lượng câu hỏi thường gặp.
     */
    override fun getItemCount() = faqs.size
}
