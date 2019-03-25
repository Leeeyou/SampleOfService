package com.leeeyou.sampleofservice.book;

import com.leeeyou.sampleofservice.book.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
