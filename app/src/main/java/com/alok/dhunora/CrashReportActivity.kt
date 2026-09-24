package com.alok.dhunora

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Diagnostic screen: shows the launch crash stack trace so the user can send it to the developer. */
class CrashReportActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trace = intent.getStringExtra(EXTRA_TRACE) ?: "no trace captured"

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val copyBtn = Button(this).apply {
            text = "Copy crash log"
            setOnClickListener {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("dhunora-crash", trace))
                Toast.makeText(this@CrashReportActivity, "Copied! Paste it in chat.", Toast.LENGTH_LONG).show()
            }
        }

        val text = TextView(this).apply {
            this.text = "Dhunora crashed on launch.\nCopy the log below and send it in chat:\n\n$trace"
            textSize = 11f
            setPadding(24, 24, 24, 24)
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply { addView(text) }

        layout.addView(copyBtn)
        layout.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(layout)
    }

    companion object {
        const val EXTRA_TRACE = "trace"
    }
}
