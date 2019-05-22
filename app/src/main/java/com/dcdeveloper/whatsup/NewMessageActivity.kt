package com.dcdeveloper.whatsup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        title = "Select User"

        fetchUsers()

    }

    companion object {
        val USERNAME_KEY = "USERNAME_KEY"
        val EMAIL_KEY = "EMAIL_KEY"
        val PROFILEURL_KEY = "PROFILEURL_KEY"
        val UID_KEY = "UID_KEY"
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()


                p0.children.forEach {
                    val userEmail = it.child("email")?.value as String
                    val userProfileUrl = it.child("profileImageUrl")?.value as String
                    val userName = it.child("username")?.value as String
                    val userUid = it.key as String
                    Log.i("profileImage", userProfileUrl)
                    Log.i("email", userEmail)
                    Log.i("username", userName)
                    Log.i("uid", userUid)
                    adapter.add(UserItem(userProfileUrl,userName,userEmail, userUid))
                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatActivity::class.java)
                    intent.putExtra(USERNAME_KEY, userItem.userName)
                    intent.putExtra(EMAIL_KEY, userItem.userEmail)
                    intent.putExtra(PROFILEURL_KEY, userItem.userProfileUrl)
                    intent.putExtra(UID_KEY, userItem.userUid)
                    startActivity(intent)

                    finish()
                }

                newMessageRecyclerView.adapter = adapter

            }


        })
    }

}

class UserItem(val userProfileUrl:String, val userName:String, val userEmail:String, val userUid: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.usernameTextView.text= userName

        Picasso.get().load(userProfileUrl).into(viewHolder.itemView.profileCircleImageView)

    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

}