package com.huyck.mijnverbruik


import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

class DagMeetGegevens(
    var datum : String?,
    var literwaterperdag : Double?,
    var literwaterperkwartier: List<Double>//ArrayList<Double>
)
