package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class RepeatingActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private TextView txtWord;
    private TextView txtDetail1;
    private TextView txtDetail2;
    private ImageView imgSpeaker;
    private TextToSpeech tts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeating_layout);

        mappings();

        //nhận dữ liệu từ intent và đổ lên activity
        Intent intent = getIntent();

        Word word = (Word) intent.getSerializableExtra("wordInfo");

        txtWord.setText(word.getWord());
        txtDetail2.setText(word.getDetail2());
        if(word.getDetail1().contains("@") || word.getDetail1().contains("/")) {
            txtDetail1.setText(word.getDetail1());
        }else {
            txtDetail1.setText(" ");
        }

        //hiện nút back về activity cũ, đặt lại tiêu đề cho activity
        if(getSupportActionBar()!= null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(word.getWord());
        }

        //khởi tạo texttospeech
        tts = new TextToSpeech(RepeatingActivity.this, (TextToSpeech.OnInitListener) this);
        //bắt sự kiện phát âm cho imgSpeaker
        imgSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(txtWord.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    //ánh xạ
    private void mappings(){
        txtWord = (TextView) findViewById(R.id.txtWordNotice);
        txtDetail1 = (TextView) findViewById(R.id.txtDetailNotice);
        txtDetail2 = (TextView) findViewById(R.id.txtDetail2Notice);
        imgSpeaker = (ImageView) findViewById(R.id.imgSpeakerNotice);
    }

    //sự kiện click nút quay về
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(tts != null){
            tts.stop(); //stop speak
            tts.shutdown(); //giải phóng tài nguyên đc sử dụng bởi TextToSpeech engine
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR){
            tts.setLanguage(Locale.US); //thiết lập ngôn ngữ
        }
    }
}
