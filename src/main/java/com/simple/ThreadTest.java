package com.simple;

public class ThreadTest {
  public  static ThreadLocal<String> threadLocal = new ThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return "test";
        }
    };



}
