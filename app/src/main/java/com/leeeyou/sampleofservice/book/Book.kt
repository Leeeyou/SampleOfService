package com.leeeyou.sampleofservice.book

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

class Book : Parcelable {

    var bookId: Int = 0
    var bookName: String? = null

    constructor() {

    }

    constructor(bookId: Int, bookName: String) {
        this.bookId = bookId
        this.bookName = bookName
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(bookId)
        out.writeString(bookName)
    }

    private constructor(`in`: Parcel) {
        bookId = `in`.readInt()
        bookName = `in`.readString()
    }

    override fun toString(): String {
        return String.format("[bookId:%s, bookName:%s]", bookId, bookName)
    }

    companion object {

        @SuppressLint("ParcelCreator")
        val CREATOR: Parcelable.Creator<Book> = object : Parcelable.Creator<Book> {
            override fun createFromParcel(`in`: Parcel): Book {
                return Book(`in`)
            }

            override fun newArray(size: Int): Array<Book?> {
                return arrayOfNulls(size)
            }
        }
    }

}
