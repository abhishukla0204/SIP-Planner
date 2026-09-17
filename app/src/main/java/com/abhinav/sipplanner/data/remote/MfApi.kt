package com.abhinav.sipplanner.data.remote

import com.abhinav.sipplanner.data.remote.dto.SchemeDetailDto
import com.abhinav.sipplanner.data.remote.dto.SchemeSearchDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * api.mfapi.in — a free, keyless, public wrapper over AMFI's daily NAV publication.
 *
 * No auth header, no quota, no billing account. That is the whole reason this
 * project has zero running cost.
 */
interface MfApi {

    @GET("mf/search")
    suspend fun search(@Query("q") query: String): List<SchemeSearchDto>

    /** Full NAV history, newest first. Can be several thousand entries. */
    @GET("mf/{schemeCode}")
    suspend fun scheme(@Path("schemeCode") schemeCode: Int): SchemeDetailDto

    /** Same shape, but `data` holds a single most-recent entry. */
    @GET("mf/{schemeCode}/latest")
    suspend fun latest(@Path("schemeCode") schemeCode: Int): SchemeDetailDto

    companion object {
        const val BASE_URL = "https://api.mfapi.in/"
    }
}
