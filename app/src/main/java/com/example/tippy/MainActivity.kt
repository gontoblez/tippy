package com.example.tippy

import android.animation.ArgbEvaluator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.round
import kotlin.system.exitProcess

private const val INITIAL_TIP_PERCENT = 15

class MainActivity : AppCompatActivity() {

    private lateinit var main: ConstraintLayout
    private lateinit var etBaseAmount: EditText
    private lateinit var etSplitBy: EditText
    private lateinit var seekBarTip: SeekBar
    private lateinit var tipPercent: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTipDescription: TextView
    private lateinit var btnRoundUp: Button
    private lateinit var btnRoundDown: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // initializing variables
        main = findViewById(R.id.main)
        etBaseAmount = findViewById(R.id.etBaseAmount)
        seekBarTip = findViewById(R.id.seekBarTip)
        tipPercent = findViewById(R.id.tipPercent)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipDescription = findViewById(R.id.tvTipDescription)
        etSplitBy = findViewById(R.id.etSplitBy)
        btnRoundUp = findViewById(R.id.btnRoundUp)
        btnRoundDown = findViewById(R.id.btnRoundDown)
        var rounded = false

        thread {
            while (true) {
                Thread.sleep(180000)
                Snackbar.make(main, getString(R.string.bored_msg), 10000)
                    .setBackgroundTint(getColor(R.color.secondary_purple))
                    .setTextColor(getColor(R.color.darkActionBar))
                    .setActionTextColor(getColor(R.color.background_blue_dark))
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE) // transition type
                    .setAction(getString(R.string.quit)) {
                        exitProcess(0) // exit application
                    }.show()
            }
        }


        // set default values
        seekBarTip.progress = INITIAL_TIP_PERCENT
        tipPercent.text = "$INITIAL_TIP_PERCENT%"//text between quotes to change data type to String (.toString())
        tvTipAmount.text = "00.00"
        tvTotalAmount.text = "00.00"
        updateTipDescription(INITIAL_TIP_PERCENT)

        seekBarTip.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tipPercent.text = "$progress%"
                computeTipAndTotal()
                updateTipDescription(progress)
                rounded = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}    // left empty bc idc

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}    // left empty bc idc

        })

        etBaseAmount.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                computeTipAndTotal()
                rounded = false
            }

        })

        etSplitBy.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                computeTipAndTotal()
                rounded = false
            }

        })

        btnRoundUp.setOnClickListener(object: OnClickListener{
            override fun onClick(v: View?) {
                if (etBaseAmount.text.isEmpty()) {
                    longToast(getString(R.string.cannot_round_wo_ba))
                    return
                } else if (rounded) {
                    longToast(getString(R.string.alrdy_rnd))
                    return
                }
                val roundedTotal = round(calculateTotal()+0.4)
                val newTip = "%.2f".format(Locale.US, calculateTip() + roundedTotal - calculateTotal())
                tvTipAmount.text = newTip
                tvTotalAmount.text = "%.2f".format(Locale.US, roundedTotal)  // use english numbers
                rounded = true
            }
        })

        btnRoundDown.setOnClickListener(object: OnClickListener{
            override fun onClick(v: View?) {
                if (etBaseAmount.text.isEmpty()) {
                    longToast(getString(R.string.cannot_round_wo_ba))
                    return
                } else if (rounded) {
                    longToast(getString(R.string.alrdy_rnd))
                    return
                } else if (seekBarTip.progress==0) {
                    longToast(getString(R.string.cannot_rd_wo_tip))
                    return
                }
                val roundedTotal = round(calculateTotal()-0.4)
                val newTip = "%.2f".format(Locale.US, calculateTip() + roundedTotal - calculateTotal())
                tvTipAmount.text = newTip
                tvTotalAmount.text = "%.2f".format(Locale.US, roundedTotal) // use english numbers
                rounded = true
            }
        })
    }
    private fun computeTipAndTotal() {
        if (etBaseAmount.text.isEmpty()) {
            tvTipAmount.text = "00.00"
            tvTotalAmount.text = "00.00"
            return
        }
        // 3. update the ui
        tvTipAmount.text = "%.2f".format(Locale.US, calculateTip())   // use english numbers
        tvTotalAmount.text = "%.2f".format(Locale.US, calculateTotal()) // use english numbers
    }

    // get tip
    private fun calculateTip(): Double {
        val baseAmount = etBaseAmount.text.toString().toDouble()
        val tipPercent = seekBarTip.progress.toDouble()
        val tipAmount = baseAmount * tipPercent / 100

        if (etSplitBy.text.isNotEmpty()) {
            val people = etSplitBy.text.toString().toInt()
            return tipAmount / people
        }
        return tipAmount
    }

    private fun calculateTotal(): Double {
        val baseAmount = etBaseAmount.text.toString().toDouble()
        val totalAmount = baseAmount + calculateTip()
        if (etSplitBy.text.isNotEmpty()) {
            val people = etSplitBy.text.toString().toInt()
            return totalAmount / people
        }
        return totalAmount
    }

    private fun Context.longToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateTipDescription(tipPercent: Int) {
        val tipDescription = when (tipPercent) {
            in 0..4 -> getString(R.string.poor)
            in 5..14 -> getString(R.string.acceptable)
            in 15..20 -> getString(R.string.good)
            in 20..24 -> getString(R.string.great)
            else -> getString(R.string.amazing)
        }
        tvTipDescription.text = tipDescription
        // update color based on tip percent
        val color = if (resources.getString(R.string.mode)=="Day") {
            ArgbEvaluator().evaluate(
            tipPercent.toFloat() / seekBarTip.max,
            ContextCompat.getColor(this, R.color.color_worst_tip),
            ContextCompat.getColor(this, R.color.color_best_tip)
        ) as Int
        } else {
            ArgbEvaluator().evaluate(
                tipPercent.toFloat() / seekBarTip.max,
                ContextCompat.getColor(this, R.color.color_worst_tip_dark),
                ContextCompat.getColor(this, R.color.color_best_tip_dark)
            ) as Int
        }
        tvTipDescription.setTextColor(color)
    }

}