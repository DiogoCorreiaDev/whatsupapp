package com.dcdeveloper.whatsup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    val latestMessagesMap = HashMap<String, Message>()

    companion object {
        var currentUsername: String? = ""
        var currentEmail: String? = ""
        var currentProfileImg: String? = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        latestMessagesRecyclerView.adapter = adapter
        latestMessagesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatActivity::class.java)

            val row = item as LatestMessageRow

            intent.putExtra(NewMessageActivity.USERNAME_KEY, row.chatPartnerUsername)
            intent.putExtra(NewMessageActivity.UID_KEY, row.chatPartnerId)
            intent.putExtra(NewMessageActivity.PROFILEURL_KEY, row.chatPartnerProfile)
            startActivity(intent)
        }

        listenForLatestMessages()
        fetchCurrentUserInfo()
        verifyUserIsLoggedIn()

        Log.i("PROFILE IMAGE", currentProfileImg)


    }

    class LatestMessageRow(val chatMessage: Message): Item<ViewHolder>() {
        var chatPartnerUsername: String ? = null
        var chatPartnerId: String ? = null
        var chatPartnerProfile: String ? = null

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latestMessageTextView.text = chatMessage.message
            if(chatMessage.senderId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.recipientId!!
            } else {
                chatPartnerId = chatMessage.senderId!!
            }

            val ref = FirebaseDatabase.getInstance().getReference("users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUsername = p0.child("username").value as String
                    chatPartnerProfile = p0.child("profileImageUrl").value as String
                    viewHolder.itemView.latestMessageUsernameTextView.text = chatPartnerUsername

                    val targetImageView = viewHolder.itemView.latestMessageImageView
                    Picasso.get().load(chatPartnerProfile).into(targetImageView)
                }

            })



        }

    }


    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
            Log.i("map", it.toString())
        }
    }

    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(Message::class.java) ?: return
                Log.i("map", chatMessage.toString())
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun fetchCurrentUserInfo(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                currentUsername = p0.child("username").value as String
                currentEmail = p0.child("email").value as String
                currentProfileImg = p0.child("profileImageUrl").value as String
            }

        })
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }
}
