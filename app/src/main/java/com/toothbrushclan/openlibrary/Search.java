package com.toothbrushclan.openlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class Search extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener  {

    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";

    TextView javaLabelObject;
    Button javaButtonObject;
    EditText javaTextBoxObject;

    ListView mainListView;
    JSONAdapter mJSONAdapter;
    ArrayList mNameList = new ArrayList();
    ShareActionProvider mShareActionProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        javaLabelObject = (TextView) findViewById(R.id.label);
        javaLabelObject.setText("Set in Java!");

        javaButtonObject = (Button) findViewById(R.id.button);
        javaButtonObject.setOnClickListener(this);

        javaTextBoxObject = (EditText) findViewById(R.id.textbox);
        javaTextBoxObject.clearFocus();
        javaTextBoxObject.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    javaTextBoxObject.clearFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(javaTextBoxObject.getWindowToken(), 0);
                    queryBooks(javaTextBoxObject.getText().toString());
                    return true;
                }
                return false;
            }
        });

        mainListView = (ListView) findViewById(R.id.main_listview);

        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());

        mainListView.setAdapter(mJSONAdapter);

        mainListView.setOnItemClickListener(this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        // Access the object responsible for
        // putting together the sharing submenu
        if (shareItem != null) {
            mShareActionProvider = (ShareActionProvider)shareItem.getActionProvider();
        }

        // Create an Intent to share your content
        setShareIntent("", "");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {


        // mNameList.add(javaTextBoxObject.getText().toString());
        // mArrayAdapter.notifyDataSetChanged();
        // javaTextBoxObject.setText("");
        // setShareIntent();
        javaTextBoxObject.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(javaTextBoxObject.getWindowToken(), 0);
        queryBooks(javaTextBoxObject.getText().toString());

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        JSONObject book = (JSONObject) mJSONAdapter.getItem(i);
        String bookTitle = "";
        String authorName = "";

        if (book.has("title")) {
            bookTitle = book.optString("title");
        }

        if (book.has("author_name")) {
            authorName = book.optJSONArray("author_name").optString(0);
        }

        setShareIntent(bookTitle, authorName);
    }

    private void setShareIntent(String title, String author) {


        if (mShareActionProvider != null) {

            // create an Intent with the contents of the TextView
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            if (title.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Not reading anything");
            } else {
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Reading " + title);
            }

            if (author.isEmpty()) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Please start reading something!");
            } else {
                if (title.isEmpty()) {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "A book which has no name by " + author);
                } else {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, title + " by " + author);
                }
            }

            // Make sure the provider knows
            // it should work with that Intent
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
    private void queryBooks(String searchString) {

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            // if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();
        setProgressBarIndeterminateVisibility(true);

        // Have the client get a JSONArray of data
        // and define how to respond
        client.get(QUERY_URL + urlString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        setProgressBarIndeterminateVisibility(false);
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                        // 8. For now, just log results
                        Log.d("omg android", jsonObject.toString());
                        mJSONAdapter.updateData(jsonObject.optJSONArray("docs"));
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable throwable, JSONObject error) {
                        setProgressBarIndeterminateVisibility(false);
                        Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();

                        // Log error message
                        // to help solve any problems
                        Log.e("omg android", statusCode + " " + throwable.getMessage());
                    }
                });
    }

}
