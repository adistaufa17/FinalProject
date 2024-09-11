package com.adista.finalproject.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FriendViewModel(application: Application) : AndroidViewModel(application) {
    private val friendDao: FriendDao = FriendDatabase.getDatabase(application).friendDao()
    private val allFriends: LiveData<List<Friend>> = friendDao.getAllFriends()

    fun getAllFriends(): LiveData<List<Friend>> {
        return allFriends
    }

    fun getFriendById(friendId: Int): LiveData<Friend?> {
        return friendDao.getFriendById(friendId)
    }

    fun updateFriend(friendId: Int, name: String, school: String, bio: String, photo: String) {
        viewModelScope.launch {
            val friendToUpdate = friendDao.getFriendById(friendId).value
            friendToUpdate?.let {
                val updatedFriend = Friend(friendId, name, school, bio, photo)
                friendDao.updateFriend(updatedFriend)
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            friendDao.deleteFriend(friend)
        }
    }
}