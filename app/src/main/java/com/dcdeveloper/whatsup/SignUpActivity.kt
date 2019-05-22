package com.dcdeveloper.whatsup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.emailEditText
import kotlinx.android.synthetic.main.activity_main.passwordEditText
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

class SignUpActivity : AppCompatActivity() {

    val mAuth = FirebaseAuth.getInstance()
    var selectedPhotoUri: Uri? = null
    var username: String? = null
    var email: String? = null
    var password: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        title="Sign up for WhatsUp"

        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        signupButton.setOnClickListener {
        signUp()
        }

        loginTextView.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circleImageView.setImageBitmap(bitmap)
            selectPhotoButton.alpha= 0f
        }
    }

    private fun signUp() {
        username = usernameEditText.text.toString()
        email = emailEditText.text.toString()
        password = passwordEditText.text.toString()

        if (email!!.isEmpty() || password!!.isEmpty() || email!!.isEmpty()){
            Toast.makeText(this, "Please fill in all text fields.", Toast.LENGTH_SHORT).show()
        } else {
            mAuth.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        uploadImageToFirebaseStorage()

                        Toast.makeText(this, "Account succesfully created. Please log in.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create account. "+it.message.toString(), Toast.LENGTH_LONG)
                        .show()
                }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener{
                Log.i("REGISTER", "IMAGE SUCCESFULLY UPLOADED")
                ref.downloadUrl.addOnSuccessListener {
                    Log.i("REGISTER",it.toString())
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){

        val uid = FirebaseAuth.getInstance().uid ?: ""

        FirebaseDatabase.getInstance().getReference().child("users").child(uid!!)
            .child("email").setValue(email)
        FirebaseDatabase.getInstance().getReference().child("users").child(uid!!)
            .child("username").setValue(username)
        FirebaseDatabase.getInstance().getReference().child("users").child(uid!!)
            .child("profileImageUrl").setValue(profileImageUrl)
        val intent = Intent(this, LatestMessagesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}
