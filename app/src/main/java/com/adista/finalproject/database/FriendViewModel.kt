package com.adista.finalproject.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class FriendViewModel(application: Application) : AndroidViewModel(application) {

    private val friendDao: FriendDao = FriendDatabase.getDatabase(application).friendDao()
    private val allFriends: LiveData<List<Friend>> = friendDao.getAllFriends()

    fun getAllFriends(): LiveData<List<Friend>> {
        return allFriends
    }
}
