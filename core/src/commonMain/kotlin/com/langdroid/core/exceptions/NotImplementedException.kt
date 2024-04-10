package com.langdroid.core.exceptions

public class NotImplementedException(model: String) :
    Exception("This functionality is not implemented for model $model")
