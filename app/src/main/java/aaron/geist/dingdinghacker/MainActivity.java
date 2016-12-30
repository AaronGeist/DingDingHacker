package aaron.geist.dingdinghacker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.prefs = getSharedPreferences(Constants.MOD_PREFS, 1);


        Switch unReadSwitch = (Switch) findViewById(R.id.unReadSwitch);

        unReadSwitch.setChecked(prefs.getBoolean(PREFS.KEY_ENABLE_KEEP_UNREAD, false));

        unReadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                prefs.edit().putBoolean(PREFS.KEY_ENABLE_KEEP_UNREAD, isChecked).apply();
            }
        });
    }
}
