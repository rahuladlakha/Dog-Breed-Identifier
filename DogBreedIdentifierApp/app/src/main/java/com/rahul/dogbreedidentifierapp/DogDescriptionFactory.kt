package com.rahul.dogbreedidentifierapp

import android.content.Context
import java.io.*;
class DogDescriptionFactory {
    companion object {
        fun getDescription(dogname: String?, context: Context) : String?{
            val br = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.akc_data)))
            if (dogname == null) return null
            var line : String? = null
            while (true){
                line = br.readLine()
                if (line == null) return null
                var s = line.split("<<>>")
                if (s[0].trim().lowercase().equals(dogname.trim().lowercase())){
                    return s[1]
                }
            }
            return null
        }
    }
}