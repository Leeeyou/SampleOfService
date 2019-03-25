package com.leeeyou.sampleofservice.book;

import com.leeeyou.sampleofservice.book.Book;
import com.leeeyou.sampleofservice.book.IOnNewBookArrivedListener;

interface IBookManager {
     List<Book> getBookList();
     void addBook(in Book book);
     void registerListener(IOnNewBookArrivedListener listener);
     void unregisterListener(IOnNewBookArrivedListener listener);
}