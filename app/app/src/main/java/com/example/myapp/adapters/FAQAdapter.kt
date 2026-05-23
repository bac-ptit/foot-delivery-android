package com.example.myapp.screens.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.FAQ

class FAQAdapter(private val faqs: List<FAQ>) : 
    RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    class FAQViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

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

    override fun getItemCount() = faqs.size
}
