package de.jl.openfoodfacts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class Product(@Json(name = "id") val id: String,
                   @Json(name = "brands") val brands: String,
                   @Json(name = "quantity") val quantity: String,
                   @Json(name = "generic_name") val generic_name: String,
                   @Json(name = "link") val link: String,
                   @Json(name = "categories") val categories: String,
                   @Json(name = "stores") val stores: String,
                   @Json(name = "image_nutrition_url") val image_nutrition_url: String,
                   @Json(name = "image_url") val image_url: String,
                   @Json(name = "countries") val countries: String
)