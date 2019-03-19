package thomas.com.NoExpiry;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewExpiryDate;

    final static int TAKE_PICTURE = 1000;
    Button takePhotoButton;

    File outputFile;
    Uri outputUri;

    Bitmap capturedImage;
    FirebaseVisionImage visionImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        // change return icon
        setTitle("Add Food");

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        textViewExpiryDate = findViewById(R.id.edit_textView_expiryDate);
        textViewExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(R.id.edit_textView_expiryDate);
            }
        });

        //Getting reference to layout
        takePhotoButton = findViewById(R.id.cameraButton);

        //Create an output file
        outputFile = new File(
                this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "test.jpg");

        //Specify a target URI in which to store the image
        outputUri = FileProvider.getUriForFile(getApplicationContext(),
                BuildConfig.APPLICATION_ID + ".provider", outputFile);

        //Generate the intent
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        //Setup click listener for take photo button
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent, TAKE_PICTURE);
            }
        });




    }

    /** This method will be called after image capture by Camera */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_PICTURE) {
            //Check if the result includes a thumbnail Bitmap
            if (data != null) {
                if (data.hasExtra("data")) {
                    Bitmap thumbnail = data.getParcelableExtra("data");
                } else {
                    //If there is no thumbnail image data, then the image is stored in target output URI
                    setupBitmapImage();
                }
            }
        }
    }

    /** Extract and display text recognized by Firebase Vision
     *  Detected text is in multiple layer -> Full text, lines or individual text element */
    private void processTextRecognitionResult(FirebaseVisionText text) {
        StringBuilder fullText = new StringBuilder();
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            Log.d("TAG", "No text found");
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    fullText.append(elements.get(k).getText());
                    fullText.append(" ");
                }
            }
        }


        // my code
        ArrayList<String> results = new ArrayList<>();
        String cameraResult = fullText.toString();
        try {
            regexChecker("([A-Za-z]{2,3}\\s?\\d{1,2})?(\\d{8})?", cameraResult);
            //Set text to display
            Log.v("cameraResult", cameraResult);
        } catch (Exception e) {
            Log.v("cameraResult", cameraResult);
            Toast.makeText(FormActivity.this, "Sorry, I can't recognize it =(", Toast.LENGTH_SHORT).show();
        }
    }

    public void regexChecker(String theRegex, String stringCheck) {

        Pattern checkRegex = Pattern.compile(theRegex);
        Matcher regexMatcher = checkRegex.matcher(stringCheck);
        // check how many matches
        int counter = 0;
        ArrayList<String> list = new ArrayList<>();

        while (regexMatcher.find()) {
            if (regexMatcher.group().length() != 0) {
                list.add(regexMatcher.group().trim());
                counter++;
            }
        }
        Log.v("Result0123: ", list.get(0));
        toDate(list.get(0));
    }

    //convert "20200101" to "2020-01-01"
    public void toDate(String dateString) {
        String result = "";
        // check if only number
        if (dateString.matches("[0-9]+") && dateString.length() >= 8) {
            // convert directly
            result = dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6);
        } else {
            // assume it is April
            Pattern replace = Pattern.compile("[A-Za-z]+");
            Matcher regexMatcher = replace.matcher(dateString.trim());
            String thisYear = new SimpleDateFormat("yyyy").format(new Date());
            System.out.println( thisYear + regexMatcher.replaceAll("04"));
            dateString = thisYear + regexMatcher.replaceAll("04");
            result = dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6);
        }


        Log.v("FinalResult: ", result);
        textViewExpiryDate.setText(result);

        Toast.makeText(FormActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    /** Create Bitmap image from the output file */
    public void setupBitmapImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(outputFile.getPath(), options);

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        //Determine how much to scale down the image
        int scaleFactor = Math.min(imageWidth / 984, imageHeight / 900);

        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;

        // Decode the image file into a Bitmap sized to fill the View
        Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getPath(), options);

        //Rotate imageView for Portrait mode
        Matrix matrix = new Matrix();
        matrix.postRotate(0);

        capturedImage = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        visionImage = FirebaseVisionImage.fromBitmap(capturedImage);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        //Send image to text recognizer for detecting text
        textRecognizer.processImage(visionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processTextRecognitionResult(firebaseVisionText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FormActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Setting the data from DatePickerDialog
     *
     * @param id findViewById's id is passed in
     */
    public void setDate(final int id) {

        DatePickerDialog datePickerDialog;
        final Calendar calendar;
        int currentYear;
        int currentMonth;
        int currentDayOfMonth;

        // show the current date
        calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(FormActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                // set the date the user wants
                Calendar date = Calendar.getInstance();
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // create text for the date, DateFormat.FULL will show the date base on your language
                TextView textView = findViewById(id);
                textView.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(date.getTime()));
            }
        }, currentYear, currentMonth, currentDayOfMonth); // make the current date appears here
        datePickerDialog.show();
    }
//    // config the save button
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        MenuInflater menuInflater = getMenuInflater();
//        // use new_item_menu as the menu for this activity
//        menuInflater.inflate(R.menu.new_item_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    // what to do when save item is clicked

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.save_item:
//                saveFoodItem();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    // send all data to fire store
    private void saveFoodItem() {
        // get all data from layout
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        //int priority = textViewExpiryDate.getValue();
        String expiryDate = textViewExpiryDate.getText().toString();
        int exDate = Integer.parseInt(textViewExpiryDate.getText().toString().replaceAll("[\\s\\-()]", ""));

        // check empty inputs
        if (title.trim().isEmpty() || expiryDate.trim().isEmpty() ) {
            Toast.makeText(this, "Please inset a title or expiryDate", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference foodRef = FirebaseFirestore.getInstance()
                .collection("FoodCollection"); // FoodCollection is in the Fire base
        // pass to recycle adapter
        foodRef.add(new Food(title, description, expiryDate, exDate));
        Toast.makeText(this, "New Food added", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void saveButton(View view) {
        saveFoodItem();
    }
}

