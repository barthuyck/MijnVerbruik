package com.huyck.mijnverbruik

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_main.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat
import android.content.Intent


import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProviders
import com.firebase.ui.auth.AuthUI
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth

import com.huyck.mijnverbruik.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.content_main.*

import java.time.LocalDateTime
import java.util.Arrays
import kotlin.collections.ArrayList
import kotlin.math.abs


class MainActivity : AppCompatActivity() {

    lateinit var grafiek: LineChart
    val TAG = "barthoofdje"
    lateinit var model: MainActivityViewModel
    //var dagdata = DagMeetGegevens("2019-07-01T01:00:00.000001", 15.5, mutableListOf(1.5, 16.0, 10.2))
    lateinit var nu : LocalDateTime

    private lateinit var mDetector: GestureDetectorCompat



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        mDetector = GestureDetectorCompat(this, MyGestureListener())

        nu = LocalDateTime.now()


        //grafiek = findViewById<View>(R.id.chart) as BarChart
        grafiek = findViewById<View>(R.id.chart) as LineChart

        model = ViewModelProviders.of(this)[MainActivityViewModel::class.java]
        model.getDatagrafiek().observe(this, Observer<DagMeetGegevens> { geg ->
            // update UI
            maaklijngrafiek(geg)
        })

        /*fab.setOnClickListener { view ->
            make(view, "Replace with your own action", LENGTH_LONG)
                .setAction("Action", null).show()
        }*/

        UpdateDagGegevens(nu,true)

    }

    fun UpdateDagGegevens(dag: LocalDateTime, toast : Boolean)
    {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val useruid = user.uid.toString()
            if (toast){
                val username = user.displayName
                Toast.makeText(this, username + " " + getString(R.string.toast_loggedin),Toast.LENGTH_LONG).show()
            }
            Log.d(TAG, "Gebruiker met userid " + useruid + " is ingelogd")
            model.leesDagData(dag, useruid)
        } else {
            Log.d(TAG, "Er is geen Gebruiker ingelogd")
            if (toast) {
                Toast.makeText(this, getString(R.string.toast_notloggedin), Toast.LENGTH_LONG)
                    .show()
            }
        }
        val tview_datum: TextView = findViewById(R.id.tv_datum) as TextView
        val formatterstr = DateTimeFormatter.ofPattern("E d/M/Y")
        tview_datum.text = nu.format(formatterstr)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            event1: MotionEvent,
            event2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val Xdiff = event1.x - event2.x
            val Ydiff = event1.y - event2.y
            if (abs(Xdiff) > abs(Ydiff)){
                if ((abs(Xdiff) > 100) && (abs(velocityX) > 100))
                {
                    if (Xdiff > 0) {
                        //todo onClickVorig()
                    }
                    else{
                        //todo onClickVolgende()

                    }

                }
            }

            //Log.d(TAG, "onFling: $event1 $event2")
            return true
        }


    }

    fun onClickDatum(view: View){
        val dag = nu.dayOfMonth
        val maand = nu.monthValue-1
        val jaar = nu.year
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { vview, year, monthOfYear, dayOfMonth ->
            var nieuwedag : LocalDateTime = LocalDateTime.of(year.toInt(),monthOfYear.toInt()+1,dayOfMonth.toInt(),1,1)
            if (nieuwedag > LocalDateTime.now()) {
                nieuwedag = LocalDateTime.now()
            }
            nu = nieuwedag
            UpdateDagGegevens(nieuwedag,false)

        }, jaar, maand, dag)
        dpd.show()
    }

    fun onClickVorig(view: View) {
        nu = nu.minusDays(1)
        UpdateDagGegevens(nu,false)

    }

    fun onClickVolgende(view: View) {
        nu = nu.plusDays(1)
        if (nu > LocalDateTime.now()) {
            nu = LocalDateTime.now()
            //Toast.makeText(this, "te grote datum",Toast.LENGTH_SHORT).show()
        }
        UpdateDagGegevens(nu,false)
    }

    fun maaklijngrafiek(dagMeetGegevens: DagMeetGegevens) {
        val plotdata = dagMeetGegevens.literwaterperkwartier
        val numbersIterator = plotdata.iterator()
        var loper = 0

        val entries = ArrayList<Entry>()
        entries.add(Entry(0.0F, 0.0F))
        while (numbersIterator.hasNext()) {
            entries.add(Entry(loper.toFloat(), numbersIterator.next().toFloat()))
            loper++
        }
        entries.add(Entry(0.0F, 0.0F))

        val dataSet : LineDataSet  = LineDataSet(entries, getString(R.string.grafiek_label)); // add entries to dataset

        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(true)
        dataSet.setDrawFilled(true);
        //dataSet.fillColor = Color.BLUE

        val xAxis = grafiek.getXAxis()
        xAxis.setPosition(XAxisPosition.BOTTOM)
        xAxis.setTextSize(10f)
        //xAxis.setTextColor(Color.RED)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)
        xAxis.setLabelCount(8)
        xAxis.setGranularity(4f); // minimum axis-step (interval) is 1
        xAxis.valueFormatter = MyValueFormatter()


        val yAxis = grafiek.axisLeft
        yAxis.axisMinimum = 0.0F
        val yAxisr = grafiek.axisRight
        yAxisr.isEnabled = false

        val lineData: LineData  = LineData(dataSet);
        val formatterstr = DateTimeFormatter.ofPattern("d/M/Y H:mm")

        setTitle(getString(R.string.grafiek_titel))



        grafiek.setData(lineData)
        grafiek.getDescription().setEnabled(false)
        grafiek.fitScreen()
        grafiek.setMaxVisibleValueCount(30)

        grafiek.invalidate() // refresh

        val grafiekdatum = LocalDateTime.parse(dagMeetGegevens.datum, DateTimeFormatter.ISO_DATE_TIME)
        val tmp: String = getString(R.string.app_waterverbruik) + " " + dagMeetGegevens.literwaterperdag.toString() + " l\n" +
                getString(R.string.app_updatetijdgrafiek) + " " + grafiekdatum.format(formatterstr)
        tvverbruik.text = tmp

    }

    // Barchart !
    /*fun maakgrafiek(dagMeetGegevens: DagMeetGegevens) {

        val plotdata = dagMeetGegevens.literwaterperkwartier
        //val formatteriso = DateTimeFormatter.ISO_DATE_TIME
        //val formatterstr = DateTimeFormatter.ofPattern("E d/M/Y")
        //val grafiekdatum = LocalDateTime.parse(dagMeetGegevens.datum, formatteriso)
        //val grafiekdatumstring = grafiekdatum.format(formatterstr)

        val numbersIterator = plotdata.iterator()
        var loper = 0

        val entries = ArrayList<BarEntry>()
        while (numbersIterator.hasNext()) {
            entries.add(BarEntry(loper.toFloat(), numbersIterator.next().toFloat()))
            loper++
        }

        /*List<Double>;
         entries.add(BarEntry(0f, 30f))
         entries.add(BarEntry(1f, 80f))
         entries.add(BarEntry(2f, 60f))

         entries.add(BarEntry(80f, 70f))
         entries.add(BarEntry(95f, 60f))
         //entries.add(BarEntry(96f, 30f))*/


        val Yset = BarDataSet(entries, "waterverbruik (liter/15min)")


        val xAxis = grafiek.getXAxis()
        xAxis.setPosition(XAxisPosition.BOTTOM)
        xAxis.setTextSize(10f)
        //xAxis.setTextColor(Color.RED)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)
        xAxis.setLabelCount(8)
        xAxis.setGranularity(12f); // minimum axis-step (interval) is 1
        xAxis.valueFormatter = MyValueFormatter()


        val data = BarData(Yset)
        data.barWidth = 0.9f // set custom bar width
        grafiek.setData(data)
        grafiek.setFitBars(true) // make the x-axis fit exactly all bars


        setTitle("Waterverbruik")
        val tmp: String =
            "Verbruik tot nu toe: " + dagMeetGegevens.literwaterperdag.toString() + " l"
        tvverbruik.text = tmp
        grafiek.getDescription().setEnabled(false)
        grafiek.setMaxVisibleValueCount(30)
        grafiek.invalidate() // refresh
    }*/

    class MyValueFormatter : ValueFormatter() {
        private val format = DecimalFormat("###,##0.0")

        // override this for BarChart
        override fun getBarLabel(barEntry: BarEntry?): String {
            return format.format(barEntry?.y)
        }

        // override this for custom formatting of XAxis or YAxis labels
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            var tijd = LocalTime.of(0, 0, 0)
            val loper = value.toInt()
            tijd = tijd.plusMinutes(15 * loper.toLong())
            return tijd.format(DateTimeFormatter.ofPattern("H:mm"));
            // return //format.format(value)
        }
        // ... override other methods for the other chart types
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            menu!!.findItem(R.id.login_settings).setVisible(false)
            menu.findItem(R.id.logout_settings).setVisible(true)
        }
        else{
            menu!!.findItem(R.id.login_settings).setVisible(true)
            menu.findItem(R.id.logout_settings).setVisible(false)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.login_settings ->{
                createSignInIntent()
                true
            }
            R.id.logout_settings ->{
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = Arrays.asList(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
        // [END auth_fui_create_intent]
    }

    // [START auth_fui_result]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            //val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val nu = LocalDateTime.now()
                UpdateDagGegevens(nu,true)
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    // [END auth_fui_result]

    fun signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {

            }
        model.ResetDagData()

        // [END auth_fui_signout]
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }





}
