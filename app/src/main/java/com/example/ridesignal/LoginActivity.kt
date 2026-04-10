package com.example.ridesignal
import LoginFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.ui.layout.layout
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.ridesignal.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.authFragmentContainer, LoginFragment())
                .commit()
        }
    }
}