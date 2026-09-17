package com.abhinav.sipplanner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Search hit from GET /mf/search?q= */
@Serializable
data class SchemeSearchDto(
    @SerialName("schemeCode") val schemeCode: Int,
    @SerialName("schemeName") val schemeName: String,
)

/** Full response from GET /mf/{schemeCode} */
@Serializable
data class SchemeDetailDto(
    @SerialName("meta") val meta: SchemeMetaDto? = null,
    @SerialName("data") val data: List<NavEntryDto> = emptyList(),
    @SerialName("status") val status: String? = null,
)

@Serializable
data class SchemeMetaDto(
    @SerialName("fund_house") val fundHouse: String? = null,
    @SerialName("scheme_type") val schemeType: String? = null,
    @SerialName("scheme_category") val schemeCategory: String? = null,
    @SerialName("scheme_code") val schemeCode: Int? = null,
    @SerialName("scheme_name") val schemeName: String? = null,
)

/** NAV is delivered as a string, and date as dd-MM-yyyy. Parsing lives in the mapper. */
@Serializable
data class NavEntryDto(
    @SerialName("date") val date: String,
    @SerialName("nav") val nav: String,
)
