package com.kbyai.idcardrecognition;

import android.graphics.Bitmap;

public class ResultItem {

    public String key;
    public String value1;
    public String value2;

    public String value3;
    public String field1;
    public String field2;

    public String field3;

    public ResultItem() {

    }

    public ResultItem(String key, String field1, String value1, String field2, String value2, String field3, String value3) {
        this.key = key;

        this.value1 = value1.replace("^", "\n");
        this.value2 = value2.replace("^", "\n");
        this.value3 = value3.replace("^", "\n");
        this.field1 = field1.replace("^", "\n");
        this.field2 = field2.replace("^", "\n");
        this.field3 = field3.replace("^", "\n");
    }
}
