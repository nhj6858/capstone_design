package com.example.loginUI;

public class ListViewItem {

    private  String lectureStr;
    private  String resultStr;

    public void setLecture(String lecture){
        lectureStr = lecture;
    }
    public void setResult(String result){
        resultStr = result;
    }

    public String getLecture(){
        return  this.lectureStr;
    }
    public String getResult(){
        return this.resultStr;
    }
}
