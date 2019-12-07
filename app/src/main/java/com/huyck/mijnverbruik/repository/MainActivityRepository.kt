package com.huyck.mijnverbruik.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.huyck.mijnverbruik.DagMeetGegevens
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CommunicatieMetFirebase {
    lateinit var user : FirebaseUser
    val TAG: String = "bartrepo"
    //var data = DagMeetGegevens()
    var data = DagMeetGegevens("2019-07-01T01:00:00.000001", 3.0 , mutableListOf(12.3,16.3,10.0))

    init {
        val user = FirebaseAuth.getInstance().currentUser
        val nu = LocalDateTime.now()

        if (user!= null){
            val useruid = user.uid.toString()
            val username = user.displayName
            Log.d(TAG, "Gebruiker " + username + " met userid " + useruid + " is ingelogd")
            data = LeesDagData(nu,useruid)
            Log.d(TAG, "datagelezen")
        }
        else
        {

            Log.d(TAG, "Er is geen Gebruiker ingelogd")

        }
    }


    fun LeesDagData (datumVanInteresse : LocalDateTime, db_useruid : String): DagMeetGegevens {
        val db_dag_ref = "D" + datumVanInteresse.format(DateTimeFormatter.ofPattern("D"))
        val db = FirebaseFirestore.getInstance()

        //val docRef = db.collection("meetdata").document("D125")
        val docRef = db.collection("users").document(db_useruid).collection("meetgegevens").document(db_dag_ref)


        var ldata = DagMeetGegevens("2019-07-01T01:00:00.000001", 0.6 , mutableListOf(0.0,3.14,1.0))

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    ldata.datum = document.getString("datum")
                    //ldata.literwaterperdag = document.get("literwaterperdag") as Double
                    //ldata.literwaterperkwartier = document.get("literwaterperkwartier") as List<Double>;

                } else {
                    Log.d(TAG, "No such document - db kon niet worden gelezen")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        Log.d(TAG, "ldata: 1 ste element" + ldata.literwaterperkwartier[1].toString())
        Log.d(TAG, "ldata: literwaterperdag : " + ldata.literwaterperdag.toString() )
        Log.d(TAG, "ldata: datum : " + ldata.literwaterperdag.toString() )
        return ldata
    }

}