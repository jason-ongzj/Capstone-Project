package com.example.android.capstone_project;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.search_toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.deleteText)
    ImageView deleteText;
    @BindView(R.id.editText)
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editText.getText().length() > 0){
                    deleteText.setVisibility(View.VISIBLE);
                } else deleteText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE){
                    Toast.makeText(SearchActivity.this, "Hello", Toast.LENGTH_SHORT).show();
                    InputMethodManager inm = (InputMethodManager) textView.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    editText.setCursorVisible(false);
                    return true;
                } else if (i == EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS) {
                    editText.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        });

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editText.setCursorVisible(true);
                return false;
            }
        });

    }
}
