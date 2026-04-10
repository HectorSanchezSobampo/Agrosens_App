package com.example.myapplication.data.local.db

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.data.localdb.DBHelper
import com.example.myapplication.domain.model.User

object DBService {

    private lateinit var dbHelper: DBHelper

    fun init(context: Context) {
        dbHelper = DBHelper(context)
    }

    fun createUser(user: User) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("firstName", user.firstName)
            put("lastName", user.lastName)
            put("email", user.email)
            put("password", user.password)
        }
        db.insert("users", null, values)
    }

    fun getUserByEmail(email: String): User? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email = ?",
            arrayOf(email)
        )

        cursor.use {
            return if (it.moveToFirst()) {
                User(
                    id = it.getInt(0),
                    firstName = it.getString(1),
                    lastName = it.getString(2),
                    email = it.getString(3),
                    password = it.getString(4)
                )
            } else null
        }
    }
}
