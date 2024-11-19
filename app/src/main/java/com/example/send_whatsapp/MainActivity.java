package com.example.send_whatsapp;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private enum App {
        WHATSAPP,
        TELEGRAM
    }

    TextInputLayout inputLayout;
    TextInputEditText inputEditText;

    FloatingActionButton whatsappBtn;
    FloatingActionButton telegramBtn;
    FloatingActionButton clearBtn;
    FloatingActionButton pasteBtn;
    FloatingActionButton changeLanguageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLang();
        setContentView(R.layout.activity_main);

        inputLayout = findViewById(R.id.input_layout);
        inputEditText = findViewById(R.id.input_EditText);
        whatsappBtn = findViewById(R.id.whatsapp_btn);
        telegramBtn = findViewById(R.id.telegram_btn);
        clearBtn = findViewById(R.id.clear_btn);
        pasteBtn = findViewById(R.id.paste_btn);
        changeLanguageBtn = findViewById(R.id.change_language_btn);

        whatsappBtn.setOnClickListener(v -> goToUrl(App.WHATSAPP, inputEditText.getEditableText().toString().trim()));
        telegramBtn.setOnClickListener(v -> goToUrl(App.TELEGRAM, inputEditText.getEditableText().toString().trim()));

        clearBtn.setOnClickListener(v -> {
            inputEditText.setText("");
            inputLayout.clearFocus();
            inputLayout.setError(null);
        });

        pasteBtn.setOnClickListener(v -> inputEditText.setText(getFromClipboard()));

        changeLanguageBtn.setOnClickListener(v -> {
            String[] languages = new String[]{"English", "עברית"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setSingleChoiceItems(languages, -1, (dialog, index) -> {
                switch (index) {
                    case 0:
                        setLang("en");
                        break;
                    case 1:
                        setLang("iw");
                        break;
                }
                inputLayout.setHint(R.string.enter_phone_number);
                recreate();
                dialog.dismiss();
            });
            builder.create().show();
        });
    }

    private void goToUrl(App app, String phoneNumber) {
        if (phoneNumber.equals("")) {
            inputLayout.setError(getString(R.string.enter_phone_number));
            return;
        }
        if (phoneNumber.charAt(0) == '0')
            phoneNumber = phoneNumber.replaceFirst("0", "+972");
        else if (phoneNumber.startsWith("\u200E0")) {
            phoneNumber = phoneNumber.replaceFirst("\u200E0", "+972");
        }

        Uri uriUrl = null;

        switch (app) {
            case WHATSAPP:
                uriUrl = Uri.parse("https://wa.me/" + phoneNumber);
                break;
            case TELEGRAM:
                uriUrl = Uri.parse("https://t.me/" + phoneNumber);
                break;
            default:
                Toast.makeText(this, "app is not supported", Toast.LENGTH_SHORT).show();
                return;
        }

        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    private String getFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (
                clipboard.hasPrimaryClip() && // has data in the clipboard
                        clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN) // the data is plain text
        ) {
            //since the clipboard contains plain text.
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            // Gets the clipboard as text.
            return item.getText().toString();
        } else {
            inputLayout.setError(getString(R.string.you_cant_pate_this_data));
            inputLayout.clearFocus();
            return "";
        }
    }

    private void setLang(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("lang", lang);
        editor.apply();
    }

    private void loadLang() {
        SharedPreferences sp = getSharedPreferences("Settings", MODE_PRIVATE);
        setLang(sp.getString("lang", "en"));
    }
}
