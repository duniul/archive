package se.su.dsv.viking_prep_pvt_15_group9;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import se.su.dsv.viking_prep_pvt_15_group9.helper.AppConfig;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.util.PhotoDialog;


//http://www.androidbegin.com/tutorial/android-parse-com-image-upload-tutorial/
public class CreateTipFragment extends Fragment implements View.OnClickListener,PhotoDialog.PhotoDialogListener {

    private String pickedObstacleName;
    private boolean clickedButtonOne;
    private TextView header;
    private String imageOneUrl;
    private String imageTwoUrl;
    private Bitmap imageOneBitmap;
    private Bitmap imageTwoBitmap;
    private EditText descriptionField;

    private String descriptionText;
    private ImageView tipImage1,tipImage2;

    private SQLiteManager localDatabase;
    private ProgressDialog progressDialog;
    private View rootView;
    private Button imageOneButton;
    private Button imageTwoButton;
    private Button sendButton;
    private PhotoDialog photoDialog;
    int imagesUploaded = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        localDatabase = new SQLiteManager(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        photoDialog = new PhotoDialog();

        ((MainActivity) getActivity()).setActionBarTitle("Skapa eget tips");

        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_create_tip, container, false);

        Bundle b = new Bundle();
        b = getArguments();
        pickedObstacleName = b.getString("chosenObstacle");
        header = (TextView)rootView.findViewById(R.id.addTip);
        header.setText(pickedObstacleName);
        descriptionField = (EditText) rootView.findViewById(R.id.tipTextField);
        tipImage1= (ImageView) rootView.findViewById(R.id.tip_picture_one);
        tipImage2= (ImageView) rootView.findViewById(R.id.tip_picture_two);
        imageOneButton = (Button) rootView.findViewById(R.id.button_edit_tip_picture_one);
        imageTwoButton = (Button) rootView.findViewById(R.id.button_edit_tip_picture_two);
        sendButton = (Button) rootView.findViewById(R.id.sendTipButton);
        clickedButtonOne = false;
        sendButton.setOnClickListener(this);
        imageOneButton.setOnClickListener(this);
        imageTwoButton.setOnClickListener(this);

        return rootView;
    }



    public void onClick(View v){
        if(v.getId() == R.id.button_edit_tip_picture_one){
            clickedButtonOne = true;
            photoDialog.setTargetFragment(this, 2);
            photoDialog.show(getFragmentManager(), "photoDialog");

        } else if(v.getId() == R.id.button_edit_tip_picture_two){
            clickedButtonOne = false;
            photoDialog.setTargetFragment(this, 2);
            photoDialog.show(getFragmentManager(), "photoDialog");

        } else if (v.getId()== R.id.sendTipButton){
            if (imageOneBitmap != null && imageTwoBitmap != null){
                progressDialog.setMessage("Sparar ditt tips..."); // Progressdialogen visas ända tills regiserTip är klart.
                progressDialog.show();
                descriptionText = String.valueOf(descriptionField.getText());
                uploadImage(imageOneBitmap);
                uploadImage(imageTwoBitmap); // När den är uppladdad kommer registerTip köras.

            } else {
                String errorMessage = "Du måste lägga till 2 bilder";
                Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onPhotoDialogConfirmClicked(Bitmap image) {
            if(clickedButtonOne == true){
                tipImage1.setImageBitmap(image);
                imageOneBitmap = image;
            }else{
                tipImage2.setImageBitmap(image);
                imageTwoBitmap = image;
            }
    }

    private void uploadImage(final Bitmap image) {

        // Convert it to byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Compress image to lower quality scale 1 - 100 and change to JPEG
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] imageByteArray = stream.toByteArray();

        // Create the ParseFile
        final ParseFile imageFile = new ParseFile("toughviking.jpeg", imageByteArray);

        // Create a New Class called "ImageUpload" in Parse
        final ParseObject imageUpload = new ParseObject("ImageUpload");

        // Create a column named "ImageName" and set the string
        imageUpload.put("ImageName", "Tough Viking");

        // Create a column named "ImageFile" and insert the image
        imageUpload.put("ImageFile", imageFile);

        // Överskuggar saveInBackground för att direkt kunna spara url:en.
        imageFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (imagesUploaded == 0) {
                    imageOneUrl=imageFile.getUrl();
                    imagesUploaded++;
                } else if (imagesUploaded == 1) {
                    imageTwoUrl=imageFile.getUrl();
                    imagesUploaded++;
                }

                if (imagesUploaded == 2) {
                    registerTip(localDatabase.getUserID(1), pickedObstacleName,descriptionText,imageOneUrl,imageTwoUrl);
                }
            }
        });

        // Create the class and the columns
        imageUpload.saveInBackground();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerTip(final int userID, final String pickedObstacle, final String description, final String imageOne,final String imageTwo ){

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("CreateTipActivity", "Svar från databasen: " + response.toString());
                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (error) {
                        // Om api:n returnerar ett felmeddelande så visas detta på rätt ställe.
                        String errorMsg = jsonObject.getString("error_msg");
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toastLoginError = Toast.makeText(context, errorMsg, duration);
                        toastLoginError.show();

                    } else {
                        // Visar en toast som bekräftar att kontot skapats.
                        Context context = getActivity();
                        int duration = Toast.LENGTH_LONG;
                        String registerToastText = "Ditt tips har lagts till!";
                        Toast toastLoginError = Toast.makeText(context, registerToastText, duration);
                        toastLoginError.show();

                        goBackToChosenObstacle(pickedObstacle);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getActivity();
                String errorMsg = error.getMessage();
                int duration = Toast.LENGTH_LONG;

                Log.e("CreateTipFragment", "Registration error: " + errorMsg);

                Toast toastRegistrationError = Toast.makeText(context, errorMsg, duration);
                toastRegistrationError.show();
                progressDialog.dismiss();
            }
        };

        // Skickar en StringRequest som via PHP kopplas till MySQL-databasen online.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.DB_API_URL, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "add_user_tip");
                params.put("user_id", Integer.toString(userID));
                params.put("obstacle", pickedObstacleName);
                params.put("tip_text", descriptionText);
                params.put("picture1_url", imageOneUrl);
                params.put("picture2_url", imageTwoUrl);
                return params;
            }
        };

        // Adding request to request queue
        AppRequestManager.getInstance().addToRequestQueue(strReq, "req_add_user_tip");
    }

    public void goBackToChosenObstacle(String obstacle) {
        ChosenObstacleFragment fragment = new ChosenObstacleFragment();
        Bundle b = new Bundle();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        b.putString("chosenObstacle", obstacle);
        fragment.setArguments(b);
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }

}
