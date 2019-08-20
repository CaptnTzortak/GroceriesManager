package de.jl.openfoodfacts

import com.squareup.moshi.Json

data class Product(@Json(name = "id") val id: String,
                   @Json(name = "brands") val brands: String?,
                   @Json(name = "quantity") val quantity: String?,
                   @Json(name = "product_name") val product_name: String?,
                   @Json(name = "generic_name") val generic_name: String?,
                   @Json(name = "link") val link: String?,
                   @Json(name = "image_url") val image_url: String?
)