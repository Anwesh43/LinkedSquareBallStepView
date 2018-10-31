package com.anwesh.uiprojects.linkedsquareballstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.squareballstepview.SquareBallStepsView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SquareBallStepsView.create(this)
    }
}
