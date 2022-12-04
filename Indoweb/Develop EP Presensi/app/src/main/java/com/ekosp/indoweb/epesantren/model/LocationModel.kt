package com.ekosp.indoweb.epesantren.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationModel(
    var longitude: Double?,
    var latitude: Double?
) : Parcelable