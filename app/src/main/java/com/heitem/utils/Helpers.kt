package com.heitem.utils

import android.content.Context
import android.graphics.Bitmap
import com.heitem.data_localization.GooglePlace
import org.json.JSONObject
import java.util.*

object Helpers {

    fun parseGooglePlace(response: String): ArrayList<GooglePlace> {

        val temp = ArrayList<GooglePlace>()
        try {

            // make an jsonObject in order to parse the response
            val jsonObject = JSONObject(response)

            // make an jsonObject in order to parse the response
            if (jsonObject.has("results")) {

                val jsonArray = jsonObject.getJSONArray("results")

                for (i in 0 until jsonArray.length()) {
                    val poi = GooglePlace()
                    poi.name = jsonArray.getJSONObject(i).getString("name")
                    if (jsonArray.getJSONObject(i).has("vicinity")) {
                        poi.address = jsonArray.getJSONObject(i).getString("vicinity")
                    }
                    if (jsonArray.getJSONObject(i).has("rating")) {
                        poi.rating = jsonArray.getJSONObject(i).getDouble("rating").toFloat()
                    }
                    poi.latitude = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                    poi.longitude = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                    if (jsonArray.getJSONObject(i).has("photos")) {
                        val photoArray = jsonArray.getJSONObject(i).getJSONArray("photos")
                        for (j in 0 until photoArray.length()) {
                            if (photoArray.getJSONObject(j).has("photo_reference")) {
                                poi.photo_reference = photoArray.getJSONObject(j).getString("photo_reference")
                            }
                        }
                    }
                    if (jsonArray.getJSONObject(i).has("icon")) {
                        poi.icon = jsonArray.getJSONObject(i).getString("icon")
                    }
                    temp.add(poi)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //temp.add(new GooglePlace());
        }

        return temp
    }

    fun scaleDownBitmap(photo: Bitmap, newHeight: Int, context: Context): Bitmap {
        var photo = photo

        val densityMultiplier = context.resources.displayMetrics.density

        val h = (newHeight * densityMultiplier).toInt()
        val w = (h * photo.width / photo.height.toDouble()).toInt()

        photo = Bitmap.createScaledBitmap(photo, w, h, true)

        return photo
    }
}