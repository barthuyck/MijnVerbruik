package com.huyck.mijnverbruik.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.huyck.mijnverbruik.DagMeetGegevens
import com.huyck.mijnverbruik.repository.CommunicatieMetFirebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


class MainActivityViewModel : ViewModel() {
    val TAG = "bartview"
    private val dataVoorGrafiek = MutableLiveData<DagMeetGegevens>()
    //private val Datarepository = CommunicatieMetFirebase()

    init {
        Log.d(TAG, "init doorlopen")
        // dummy data voor als er geen gebruiker is ingelogd
        dataVoorGrafiek.value =
            DagMeetGegevens("2000-01-01T01:00:00.000001", 0.0, mutableListOf(12.3, 16.3, 10.0))

        val nu = LocalDateTime.now()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val useruid = user.uid.toString()
            val username = user.displayName
            Log.d(TAG, "Gebruiker " + username + " met userid " + useruid + " is ingelogd")
            //dataVoorGrafiek.value =
            leesDagData(nu, useruid)
            Log.d(TAG, "datagelezen")
        } else {
            Log.d(TAG, "Er is geen Gebruiker ingelogd")
        }
    }

    // voor observer
    fun getDatagrafiek(): MutableLiveData<DagMeetGegevens> {
        return dataVoorGrafiek
    }

    // Ophalen data in firebase
    fun leesDagData(datumVanInteresse: LocalDateTime, db_useruid: String) {
        // dummy data
        var ldata =
            DagMeetGegevens("2001-07-01T01:00:00.000001", 0.1, mutableListOf(0.0, 3.14, 1.0))

        val db_dag_ref = "D" + datumVanInteresse.format(DateTimeFormatter.ofPattern("D"))
        val db = FirebaseFirestore.getInstance()

        //val docRef = db.collection("meetdata").document("D125")
        val docRef = db.collection("users").document(db_useruid).collection("meetgegevens")
            .document(db_dag_ref)

        //ophalen data
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    ldata.datum = document.getString("datum")
                    //ldata.literwaterperdag = document.getDouble("literwaterperdag")
                    //Log.d(TAG, "DocumentSnapshot data: ${ldata.literwaterperdag}")
                    @Suppress("UNCHECKED_CAST") var tmplwpk: List<Double> =
                        document.get("literwaterperkwartier") as List<Double>;
                    ldata.literwaterperkwartier = tmplwpk;
                    val iterator = tmplwpk.iterator()
                    var som: Double = 0.0
                    iterator.forEach {
                        som = som + it
                    }
                    ldata.literwaterperdag = som
                    // update mutable data
                    dataVoorGrafiek.value = ldata
                } else {
                    Log.d(TAG, "No such document - db kon niet worden gelezen")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        Log.d(TAG, "ldata: 1 ste element" + ldata.literwaterperkwartier[1].toString())
        Log.d(TAG, "ldata: literwaterperdag : " + ldata.literwaterperdag.toString())
        Log.d(TAG, "ldata: datum : " + ldata.literwaterperdag.toString())


    }

    // Reset data in firebase
    fun ResetDagData() {
        // dummy data
        var ldata =
            DagMeetGegevens("2001-07-01T01:00:00.000001", 0.1, mutableListOf(0.0, 3.14, 1.0))
        // update mutable data
        dataVoorGrafiek.value = ldata

    }


    /* fun GetDataVoorGrafiek(datum : LocalDateTime, userid : String): MutableLiveData<DagMeetGegevens> {
         Log.d(TAG, "GetDataVoorGrafiek doorlopen")
         val tempdata = DagMeetGegevens("2019-07-01T01:00:00.000001", 3.0 , mutableListOf(12.3,16.3,10.0))
         Log.d(TAG, "stap1")
         dataVoorGrafiek.value = tempdata
         Log.d(TAG,"dataVoorGrafiek.value.literwaterperdag:" + dataVoorGrafiek.value?.literwaterperdag.toString())
         Log.d(TAG, "stap2")
         dataVoorGrafiek.value = Datarepository.LeesDagData(datum,userid)
         Log.d(TAG, "stap3")
         Log.d(TAG,"dataVoorGrafiek.value.literwaterperdag:" + dataVoorGrafiek.value.toString())
         return this.dataVoorGrafiek
     } */
}
