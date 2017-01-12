package solutiontogo.de.audiocitytourguide;

/**
 * Created by shivaramak on 02/01/2017.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import solutiontogo.de.audiocitytourguide.utils.ValidationUtility;

/**
 *
 * Register Activity Class
 */
public class RegisterActivity extends Activity {
    // Progress Dialog Object
    ProgressDialog prgDialog;
    // Error Msg TextView Object
    TextView errorMsg;
    // First Name Edit View Object
    EditText etFirstName;
    // Last Name Edit View Object
    EditText etLastName;
    // Email Edit View Object
    EditText etEmail;
    // Phone Edit View Object
    EditText etPhone;
    // Password Edit View Object
    EditText etPassword;
    // Confirm Password Edit View Object
    EditText etConfirmPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        // Find Error Msg Text View control by ID
        errorMsg = (TextView)findViewById(R.id.register_error);

        etFirstName = (EditText)findViewById(R.id.registerFirstName);
        etLastName = (EditText)findViewById(R.id.registerLastName);

        etEmail = (EditText)findViewById(R.id.registerEmail);
        etPhone = (EditText)findViewById(R.id.registerPhone);

        etPassword = (EditText)findViewById(R.id.registerPassword);
        etConfirmPwd = (EditText)findViewById(R.id.registerConfirmPassword);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);
    }

    /**
     * Method gets triggered when Register button is clicked
     *
     * @param view
     */
    public void registerUser(View view){

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();

        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();

        String password = etPassword.getText().toString();
        String confirmPwd = etConfirmPwd.getText().toString();

        RequestParams params = new RequestParams();

        if(ValidationUtility.isNotNull(firstName) && ValidationUtility.isNotNull(lastName) && ValidationUtility.isNotNull(email)
                && ValidationUtility.isNotNull(phone) &&ValidationUtility.isNotNull(password) && ValidationUtility.isNotNull(confirmPwd)){
            // When Email is valid and Password matches
            if(ValidationUtility.validate(email) && password.equals(confirmPwd)) {
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("displayName", (firstName + " " + lastName));
                params.put("phoneNumber", phone);
                params.put("email", email);
                params.put("password", password);
                invokeWS(params);
            } else if(!password.equals(confirmPwd)) {
                Toast.makeText(getApplicationContext(), "Password not matching", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://192.168.2.2:9999/useraccount/register/doregister",params ,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String response = new String(responseBody);

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }


        });
    }

    /**
     * Method which navigates from Register Activity to Login Activity
     */
    public void navigatetoLoginActivity(View view){
        Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        // Clears History of Activity
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    /**
     * Set degault values for Edit View controls
     */
    public void setDefaultValues(){
        etFirstName.setText("");
        etEmail.setText("");
        etPassword.setText("");
    }

}