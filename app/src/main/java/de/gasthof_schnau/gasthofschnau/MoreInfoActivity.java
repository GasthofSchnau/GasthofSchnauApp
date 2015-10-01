package de.gasthof_schnau.gasthofschnau;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import java.util.List;

public class MoreInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);
        setTitle(getIntent().getStringExtra("title"));

        ((TextView) findViewById(R.id.moreInfo)).setText(Html.fromHtml(getIntent().getStringExtra("moreInfo")));
    }

}
