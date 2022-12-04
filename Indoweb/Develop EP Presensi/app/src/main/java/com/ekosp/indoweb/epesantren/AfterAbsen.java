package com.ekosp.indoweb.epesantren;

/**
 * Created by Eko Setyo Purnomo on 05-Apr-18.
 * Find me on https://ekosp.com
 * Or email me directly to ekosetyopurnomo@gmail.com
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.ekosp.indoweb.epesantren.helper.GlobalVar;
import com.ekosp.indoweb.epesantren.helper.SessionManager;


public class AfterAbsen extends Activity {

    private String TYPE;
    private SessionManager session;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_absen);

        session = new SessionManager(this);

        Intent intent = getIntent();
        if (intent != null) TYPE = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI);

        TextView txt_absen_remark = (TextView) findViewById(R.id.txt_absen_remark);
        ImageView ic_hdr = (ImageView) findViewById(R.id.ic_hdr);

        txt_absen_remark.setText("Absen "+TYPE+" Anda Berhasil Dikirim !");

//        if(GlobalVar.PARAM_TYPE_ABSENSI == "DATANG") {
//            ic_hdr.setImageResource(R.drawable.ic_a_datang);
//        }else {
//            ic_hdr.setImageResource(R.drawable.ic_a_pulang);
//        }

        back = findViewById(R.id.btn_bckhp);
        back.setOnClickListener(view -> backTohp());
    }

    public void backTohp (){
//        Intent intent = new Intent(this, Homepage.class);
//        startActivity(intent);
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

}