package com.fahm781.rigcraft

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class RegistrationActivity : AppCompatActivity() {

    lateinit var editTextEmail: TextInputEditText
    lateinit var editTextPassword: TextInputEditText
    lateinit var editTextPassword2: TextInputEditText
    lateinit var buttonRegister: Button
    lateinit var progressBar: ProgressBar

    //create a Firebase Authentication object
    lateinit var auth: FirebaseAuth
    lateinit var textView: TextView

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //send the user to MainActivity for now
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        auth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextPassword2 = findViewById(R.id.confirmPassword)
        buttonRegister = findViewById(R.id.buttonReg)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.lgnNow)

        textView.setOnClickListener {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonRegister.setOnClickListener {
            progressBar.visibility = ProgressBar.VISIBLE
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val password2 = editTextPassword2.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter an email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty() || password2.isEmpty()) {
                Toast.makeText(this, "Enter a Password into the field(s)", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            //check if password matches the confirm password
            if (password != password2) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = ProgressBar.GONE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //  Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(
                            baseContext,
                            "Account Created.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val user = auth.currentUser

                    } else {
                        // If sign in fails, display a message to the user.
                        //  Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }


        }
    }
}
