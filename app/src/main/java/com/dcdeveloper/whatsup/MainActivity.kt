package com.dcdeveloper.whatsup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnKeyListener  {

    var signupTextView: TextView? = null
    var emailEditText: EditText? = null
    var passwordEditText: EditText? = null
    var logoView: ImageView? = null
    var backgroundLayout: ConstraintLayout? = null
    val mAuth = FirebaseAuth.getInstance()

    fun logIn() {
        // Move to next Activity
        val intent = Intent(this, LatestMessagesActivity::class.java)
        startActivity(intent)
    }

    override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
            loginClicked(view)
        }
        return false
    }


    fun loginClicked(view: View) {
        emailEditText = findViewById(R.id.emailEditText) as EditText
        passwordEditText = findViewById(R.id.passwordEditText) as EditText

        if (emailEditText?.getText().toString().matches("".toRegex())
            || passwordEditText?.getText().toString().matches("".toRegex())) {
            Toast.makeText(this, "A username and a password are required.", Toast.LENGTH_SHORT).show()
        } else {
            //Login
            mAuth.signInWithEmailAndPassword(emailEditText?.text.toString(), passwordEditText?.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        logIn()
                    } else {
                        Toast.makeText(this, "Login Failed. Try Again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signupTextView = findViewById(R.id.signupTextView)
        signupTextView!!.setOnClickListener(this)

        logoView = findViewById(R.id.logoView)
        backgroundLayout = findViewById(R.id.backgroundLayout)
        logoView!!.setOnClickListener(this)
        backgroundLayout?.setOnClickListener(this)
        passwordEditText?.setOnKeyListener(this)

        if (mAuth.currentUser != null) {
            logIn()
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.signupTextView) {

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)


        } else if (view.id == R.id.logoView || view.id == R.id.backgroundLayout) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}