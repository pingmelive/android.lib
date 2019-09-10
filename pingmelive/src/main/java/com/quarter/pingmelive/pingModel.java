package com.quarter.pingmelive;

public class pingModel {

    public int data_id = -1;
    public String data_device_info = "";
    public String data_error_info = "";
    public String data_error_trace = "";
    public String data_date_time = "";

    public int getData_id() {
        return data_id;
    }

    public void setData_id(int data_id) {
        this.data_id = data_id;
    }

    public String getData_device_info() {
        return data_device_info;
    }

    public void setData_device_info(String data_device_info) {
        this.data_device_info = data_device_info;
    }

    public String getData_error_info() {
        return data_error_info;
    }

    public void setData_error_info(String data_error_info) {
        this.data_error_info = data_error_info;
    }

    public String getData_error_trace() {
        return data_error_trace;
    }

    public void setData_error_trace(String data_error_trace) {
        this.data_error_trace = data_error_trace;
    }

    public String getData_date_time() {
        return data_date_time;
    }

    public void setData_date_time(String data_date_time) {
        this.data_date_time = data_date_time;
    }
}
