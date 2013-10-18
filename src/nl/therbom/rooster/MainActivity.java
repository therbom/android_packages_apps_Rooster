package nl.therbom.rooster;

import java.util.Locale;
import java.util.regex.Pattern;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
//import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
    @SuppressLint("SetJavaScriptEnabled") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize webView, that outputs the timetable
        final WebView mOutput = (WebView)findViewById(R.id.output);
        // Enable Javascript, so interaction with the website works
        mOutput.getSettings().setJavaScriptEnabled(true);
        // Enable zoom controls
        mOutput.getSettings().setBuiltInZoomControls(true);
        // Hide zoom buttons, they are ugly
        mOutput.getSettings().setDisplayZoomControls(false);
        // Maximize zoom out options
        mOutput.getSettings().setUseWideViewPort(true);
        
        // Initialize the button
        final Button mButton = (Button)findViewById(R.id.button_input);
        // Initialize the text input
        final EditText mInput = (EditText)findViewById(R.id.input);
        
    	/* 
    	 * Define valid patterns
    	 */
    	// Pattern 1: leerlingnummer (5 digits)
    	final Pattern mPattern1 = Pattern.compile("^[0-9]{5}$");
    	// Pattern 2: docentencode (3 letters)
    	final Pattern mPattern2 = Pattern.compile("^[a-zA-Z][a-zA-Z][a-zA-Z]$");
    	// Pattern 3: class (digit-letter)
    	//            need to append 'D'
    	final Pattern mPattern3 = Pattern.compile("^[1-2][a-zA-Z]$");
    	// Pattern 4: class (letter-digit-letter) e.g. 'v3a'
    	//            need to append 'D'
    	final Pattern mPattern4 = Pattern.compile("^[v|V|h|H][3-6][a-zA-Z]$");
    	// Pattern 5: class (letter-letter-digit-letter)
    	final Pattern mPattern5 = Pattern.compile("^[d|D][v|V|h|H][3-6][a-zA-Z]$");
    	// Pattern 6: classroom (3 digits)
    	final Pattern mPattern6 = Pattern.compile("^[0-9][0-9][0-9]$");
    	// Pattern 7: classroom ('D'-digit (1-4))
    	final Pattern mPattern7 = Pattern.compile("^[D|d][1-4]$");
    	// Pattern 8: classroom ('med'-digit (1-4))
    	final Pattern mPattern8 = Pattern.compile("^med[1-4]$");
    	// Pattern 9: class (letter-digit-letter) e.g. 'd2a'
    	final Pattern mPattern9 = Pattern.compile("^[d|D][1-2][a-zA-Z]$");
    	/* END: Define valid patterns */
    	
    	// Use the Send button on IME to simulate a button click
    	mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    	    @Override
    	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    	        if (actionId == EditorInfo.IME_ACTION_SEND) {
    	            mButton.performClick();
    	        }
    	        return false;
    	    }
    	});

        // Wait until the button is pressed
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Don't do anything if there's no internet connection
            	if(isOnline()) {
            		// Retrieve input and store it in mCode
            		String mCode = mInput.getText().toString();

            		// Initialize the string that contains the correct URL
            		String mUrl = null;
            	
            		/* 
            		 * Check if the input matches a listed pattern.
            		 * If so, set the correct URL
            		 */
            		if(mPattern1.matcher(mCode).find()) {
            			mUrl = "http://www.damstede.net/rooster/infoweb/index.php?ref=2&id=";
            		} else if(mPattern2.matcher(mCode).find()) {
            			mUrl = "http://www.damstede.net/roosterdocenten/infoweb/index.php?ref=3&id=";           		
            		} else if(mPattern3.matcher(mCode).find() || mPattern4.matcher(mCode).find() || mPattern5.matcher(mCode).find() || mPattern9.matcher(mCode).find()) {
            			mUrl = "http://www.damstede.net/rooster/infoweb/index.php?ref=5&id=";
            		} else if(mPattern6.matcher(mCode).find() || mPattern7.matcher(mCode).find() || mPattern8.matcher(mCode).find()) {
            			mUrl = "http://www.damstede.net/roosterdocenten/infoweb/index.php?ref=4&id=";
            		}
            	
            		/*
            		 * Load the requested timetable in a correct way.
            		 * Also deal with unmatched inputs.
            		 */
            		if(!TextUtils.isEmpty(mUrl)) {
            			// Only load if it's matched
            			if(!mPattern8.matcher(mCode).find()) {
            				// 'Everything' should be capitilized...
            				if(mPattern3.matcher(mCode).find() || mPattern4.matcher(mCode).find()) {
            					// Add the 'D' if a class is requested and the user did not enter it himself
            					mOutput.loadUrl(mUrl + "D" + mCode.toUpperCase(Locale.ENGLISH) + "#rooster");
            				} else {
            					// No 'D' required; user already inserted it
            					mOutput.loadUrl(mUrl + mCode.toUpperCase(Locale.ENGLISH) + "#rooster");
            				}
            			} else {
            				// ...except the 'med' classrooms
            				mOutput.loadUrl(mUrl + mCode + "#rooster");
            			}
            		
            			// Hide keyboard after loading
            			InputMethodManager mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            			mIMM.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            		} else {
            			// Notify the user input isn't matched
            			Toast mToast = Toast.makeText(getApplicationContext(), "Invoer niet herkend", Toast.LENGTH_SHORT);
            			mToast.show();
            		}
            	} else {
        		Toast mToast = Toast.makeText(getApplicationContext(), "Geen werkende internetverbinding", Toast.LENGTH_SHORT);
        		mToast.show();
            	}
            }
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
   */
}
