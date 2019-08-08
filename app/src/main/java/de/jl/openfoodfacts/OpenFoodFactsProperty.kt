package de.jl.openfoodfacts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class OpenFoodFactsProperty(@Json(name = "code") val code: String,
                                 @Json(name = "status") val status: Int,
                                 @Json(name = "product") val product: Product) {

}