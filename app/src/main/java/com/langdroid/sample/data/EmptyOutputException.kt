package com.langdroid.sample.data

class EmptyOutputException(override val message: String? = "The page content is empty!") :
    Exception(message)
