package com.adista.finalproject.database

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adista.finalproject.R
import com.adista.finalproject.databinding.ItemFriendBinding
import java.io.FileNotFoundException

class FriendAdapter(private var friends: List<Friend>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.binding.tvName.text = friend.name
        holder.binding.tvSchool.text = friend.school

        if (friend.photo.isNotEmpty()) {
            try {
                val bitmap = BitmapFactory.decodeFile(friend.photo)
                holder.binding.ivPhoto.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            // Fallback image jika tidak ada gambar
            holder.binding.ivPhoto.setImageResource(R.drawable.profile)
        }
    }


    override fun getItemCount(): Int {
        return friends.size
    }

    // Update data method for adapter
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}
