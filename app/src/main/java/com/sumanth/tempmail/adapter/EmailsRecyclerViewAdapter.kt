package com.sumanth.tempmail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.sumanth.tempmail.R
import com.sumanth.tempmail.data.EmailDataModel

class EmailrecyclerViewAdapter(private var emailList: List<EmailDataModel>) :
        RecyclerView.Adapter<EmailrecyclerViewAdapter.MyViewHolder>() {
     class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var from: TextView = view.findViewById(R.id.from)
        var subject: TextView = view.findViewById(R.id.subject)
        var body: TextView = view.findViewById(R.id.body)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val movie = emailList[position]
        holder.from.text = movie.getFrom()
        holder.subject.text = movie.getSubject()
        holder.body.text = movie.getBody()
    }
    override fun getItemCount(): Int {
        return emailList.size
    }
}