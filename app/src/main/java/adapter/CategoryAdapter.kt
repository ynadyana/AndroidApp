package com.quizapp.tork.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.quizapp.tork.QuizActivity
import com.quizapp.tork.R
import com.quizapp.tork.model.Category

class CategoryAdapter(private val catList: List<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // ViewHolder to hold and bind views for each item in the RecyclerView
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.img)
        val cate: TextView = view.findViewById(R.id.category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for individual RecyclerView items
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val category = catList[position]

        // Load the category image using Glide with placeholders
        Glide.with(context)
            .load(category.image) // Load the image from the URL
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                    .error(R.drawable.error_image) // Error image if the URL is invalid
                    .centerCrop() // Ensures the image fits nicely into the ImageView
            )
            .into(holder.img)

        // Set category title
        holder.cate.text = category.cat_title

        // Handle click on the category item to start QuizActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, QuizActivity::class.java)
            intent.putExtra("catId", category.cat_id) // Pass the category ID to QuizActivity
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        // Return the size of the category list
        return catList.size
    }
}
