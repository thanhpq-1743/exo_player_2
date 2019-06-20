package hvcnbcvt_uddd.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var check = false
    private val STATE_RESUME_WINDOW = "resuilt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        if (savedInstanceState != null) {
            check = savedInstanceState.getBoolean(STATE_RESUME_WINDOW)
        }
        if(!check){
            supportFragmentManager.beginTransaction().add(R.id.activity_home, VideoFragment()).commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putBoolean(STATE_RESUME_WINDOW, check)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        check = true
    }
}