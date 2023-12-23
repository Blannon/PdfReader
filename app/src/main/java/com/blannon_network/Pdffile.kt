package com.blannon_network

data class Pdffile(val filename: String, val downloadUrl: String){
    constructor(): this("", "")
}
