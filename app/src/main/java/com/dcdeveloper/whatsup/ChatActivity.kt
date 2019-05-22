package com.dcdeveloper.whatsup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*


class ChatActivity : AppCompatActivity() {
    val mAuth = FirebaseAuth.getInstance()
    var realSender: String? = null
    var emailSelected: String? = null
    var usernameSelected: String? = null
    var profileUrlSelected: String? = null
    var uidSelected: String? = null

    val adapter = GroupAdapter<ViewHolder>()

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = uidSelected
        val ref = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)
                if (message != null){

                    if (message.senderId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(message.message!!, LatestMessagesActivity.currentProfileImg!!))
                    } else {
                        adapter.add(ChatToItem(message.message!!,profileUrlSelected!!))
                    }
                }

                chatListView.scrollToPosition(adapter.itemCount-1)
            }
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun performSendMessage() {

        val textMessage = chatEditText.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = uidSelected
        val reference = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("user-messages/$toId/$fromId").push()
        val messageId = reference.key!!

        if (fromId == null) return

        val message = Message(textMessage,fromId,toId,messageId)

        reference.setValue(message)
            .addOnSuccessListener {
                chatEditText.text.clear()
                chatListView.scrollToPosition(adapter.itemCount -1)
            }

        toReference.setValue(message)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId/$toId")
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toId/$fromId")
        latestMessageRef.setValue(message)
        latestMessageToRef.setValue(message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        usernameSelected = intent.getStringExtra(NewMessageActivity.USERNAME_KEY)
        emailSelected = intent.getStringExtra(NewMessageActivity.EMAIL_KEY)
        profileUrlSelected = intent.getStringExtra(NewMessageActivity.PROFILEURL_KEY)
        uidSelected = intent.getStringExtra(NewMessageActivity.UID_KEY)

        title = "Chat with " + usernameSelected

        chatListView.adapter = adapter

        if (mAuth.currentUser != null){
            realSender = mAuth.currentUser!!.email.toString()
        } else {
            mAuth.signOut()
        }

        listenForMessages()

        sendButton.setOnClickListener {
            performSendMessage()
        }

    }
}

class ChatFromItem(val text: String, val userProfileUrl:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.FromTextView.text  = text
        val targetImageView = viewHolder.itemView.fromImageView
        Picasso.get().load(userProfileUrl).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String, val userProfileUrl:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.toTextView.text  = text
        val targetImageView = viewHolder.itemView.toImageView
        Picasso.get().load(userProfileUrl).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}