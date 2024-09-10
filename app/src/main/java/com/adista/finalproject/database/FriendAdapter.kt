package com.adista.finalproject.database

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adista.finalproject.R

class FriendAdapter(private val context: Context,
                    private var friends: List<Friend>,
                    private val listener: OnFriendClickListener
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    // Interface untuk menangani klik item
    interface OnFriendClickListener {
        fun onFriendClick(friendId: Int) // Dikirimkan ID teman
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)

        // Set OnClickListener untuk item view
        holder.itemView.setOnClickListener {
            listener.onFriendClick(friend.id) // Kirim ID teman ke listener
        }
    }

    fun getData(): List<Friend> {
        return friends
    }

    fun getFilteredList(query: String): List<Friend> {
        return friends.filter { friend ->
            friend.name.contains(query, ignoreCase = true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun getItemCount(): Int = friends.size

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_photo)
        private val nameTextView = itemView.findViewById<TextView>(R.id.tv_name)
        private val schoolTextView = itemView.findViewById<TextView>(R.id.tv_school)

        fun bind(friend: Friend) {
            // Set text fields
            val photoPath = friend.photo

            if (photoPath.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.profile)
                }
            } else {
                imageView.setImageResource(R.drawable.profile)
            }

            nameTextView.text = friend.name
            schoolTextView.text = friend.school

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFriends: List<Friend>) {
        this.friends = newFriends
        notifyDataSetChanged()
    }

}