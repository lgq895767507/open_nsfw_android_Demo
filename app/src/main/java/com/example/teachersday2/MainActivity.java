package com.example.teachersday2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.zwy.nsfw.api.NSFWHelper;
import com.zwy.nsfw.core.NSFWConfig;
import com.zwy.nsfw.core.NsfwBean;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView imgIv;
    private EditText urlEt;
    private Button searchBtn, open_camera_btn;
    private NSFWHelper nsfwHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //请求权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initView();
        initListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == RESULT_OK) {
            init();
        }
    }

    private void initListener() {
        open_camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nsfwHelper == null) {
                    init();
                }
                try {
                    Random random = new Random();
                    String path = "img" + File.separator + getResources().getAssets().list("img")[random.nextInt(getResources().getAssets().list("img").length)];
                    Bitmap bitmap = BitmapFactory.decodeStream(getResources().getAssets().open(path));
                    result(nsfwHelper.scanBitmap(bitmap));
                    imgIv.setBackground(new BitmapDrawable(getResources(),bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Glide.with(v)
                        .load(urlEt.getText().toString())
                        .centerCrop()
                        .placeholder(R.mipmap.timg)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                BitmapDrawable d = (BitmapDrawable) resource;
                                final Bitmap bp = d.getBitmap();
                                if (nsfwHelper == null) {
                                    init();
                                }
                                if (bp != null){
                                    result(nsfwHelper.scanBitmap(bp));
                                }
                                return false;
                            }
                        })
                        .into(imgIv);
            }
        });
    }

    private void result(NsfwBean nsfwBean){
        if (nsfwBean.getNsfw() > 0.3) {
            Toast.makeText(getApplicationContext(), "图片涉黄!!黄似度:" + nsfwBean.getNsfw(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "合法分数:" + nsfwBean.getSfw() + ",黄似度:" + nsfwBean.getNsfw(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        imgIv = findViewById(R.id.img_iv);
        urlEt = findViewById(R.id.url_rt);
        searchBtn = findViewById(R.id.search_btn);
        open_camera_btn = findViewById(R.id.open_camera_btn);
    }

    private void init() {
        nsfwHelper = NSFWHelper.INSTANCE.init(new NSFWConfig(getAssets(), false));
    }
}
