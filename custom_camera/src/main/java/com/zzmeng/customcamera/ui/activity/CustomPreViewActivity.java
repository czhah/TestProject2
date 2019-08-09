package com.zzmeng.customcamera.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.dosmono.customcamera.util.BitmapUtils;
import com.zzmeng.customcamera.R;
import com.zzmeng.customcamera.ui.widget.cut.CustomCutImageView;

/**
 * 图片剪裁
 */
public class CustomPreViewActivity extends AppCompatActivity {

    public static final String PARAMS1 = "preview_path";
    public static final String PARAMS2 = "preview_cut";

    private CustomCutImageView mIvCut;
    private ImageView mIvPreview;

    private String path;
    private boolean isCut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_preview);

        mIvCut = findViewById(R.id.main_iv_cut);
        mIvPreview = findViewById(R.id.iv_preview);
        findViewById(R.id.main_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.main_btn_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCut) {
                    Bitmap image = mIvCut.getCroppedImage();
                    if (image != null) {
                        boolean isSuccess = BitmapUtils.INSTANCE.bitmap2Path(image, path);
                        image.recycle();
                        image = null;
                        if (isSuccess) {
                            //  保存成功
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    } else {
                        Toast.makeText(CustomPreViewActivity.this, "剪切失败, 请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        initData(savedInstanceState);
    }


    void initData(Bundle bundle) {
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra(PARAMS1);
            isCut = intent.getBooleanExtra(PARAMS2, false);
            if (isCut) {
                mIvCut.setVisibility(View.VISIBLE);
                mIvPreview.setVisibility(View.GONE);
                Glide.with(this).load(path).into(mIvCut);
            } else {
                Glide.with(this).load(path).into(mIvPreview);
            }
        } else {
            finish();
        }
    }
}
