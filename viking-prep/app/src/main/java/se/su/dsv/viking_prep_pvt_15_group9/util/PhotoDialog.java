package se.su.dsv.viking_prep_pvt_15_group9.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import se.su.dsv.viking_prep_pvt_15_group9.R;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;

/**
 * Created by Daniel on 2015-06-02.
 */
public class PhotoDialog extends DialogFragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_PICK_FROM_GALLERY = 2;
    static final int ROTATE_LEFT = 1;
    static final int ROTATE_RIGHT = 2;
    static final int PICTURE_TYPE_PROFILE = 1;
    static final int PICTURE_TYPE_TIP = 2;

    private View dialogView;
    private ImageView chosenPhoto;
    private Button rotateLeftButton;
    private Button rotateRightButton;
    private Button mirrorButton;
    private Button takePhotoButton;
    private Button pickFromGalleryButton;
    private Bitmap picture;
    private File photoFile;
    private String mCurrentPhotoPath;
    private String uploadedImageUrl;
    private int pictureType;

    private SQLiteManager localDatabase;
    private ProgressDialog progressDialog;

    public interface PhotoDialogListener {
        void onPhotoDialogConfirmClicked(Bitmap image);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.dialog_photo, null);
        dialogBuilder.setView(dialogView);

        chosenPhoto = (ImageView) dialogView.findViewById(R.id.chosen_photo);
        rotateLeftButton = (Button) dialogView.findViewById(R.id.button_rotate_left);
        rotateLeftButton.setOnClickListener(this);
        rotateLeftButton.setClickable(false);
        rotateRightButton = (Button) dialogView.findViewById(R.id.button_rotate_right);
        rotateRightButton.setOnClickListener(this);
        rotateRightButton.setClickable(false);
        mirrorButton = (Button) dialogView.findViewById(R.id.button_mirror);
        mirrorButton.setOnClickListener(this);
        mirrorButton.setClickable(false);
        takePhotoButton = (Button) dialogView.findViewById(R.id.button_take_photo);
        takePhotoButton.setOnClickListener(this);
        pickFromGalleryButton = (Button) dialogView.findViewById(R.id.button_pick_from_gallery);
        pickFromGalleryButton.setOnClickListener(this);

        dialogBuilder.setPositiveButton("Bekräfta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                PhotoDialogListener targetFragment = (PhotoDialogListener) getTargetFragment();
                targetFragment.onPhotoDialogConfirmClicked(picture);
            }
        })
        .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                PhotoDialog.this.getDialog().cancel();
            }
        });

        // Lägger till en databashanterare.
        localDatabase = new SQLiteManager(getActivity());

        // Lägger till en progress dialog.
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        return dialogBuilder.create();
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.button_rotate_left:
                picture = rotateBitmap(picture, ROTATE_LEFT);
                chosenPhoto.setImageBitmap(picture);
                break;

            case R.id.button_rotate_right:
                picture = rotateBitmap(picture, ROTATE_RIGHT);
                chosenPhoto.setImageBitmap(picture);
                break;

            case R.id.button_mirror:
                picture = mirrorBitmap(picture);
                chosenPhoto.setImageBitmap(picture);
                break;

            case R.id.button_take_photo:
                dispatchTakePictureIntent();
                break;

            case R.id.button_pick_from_gallery:
                dispatchPickFromGalleryIntent();
                break;
        }
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                this.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void dispatchPickFromGalleryIntent() {
        Intent pickFromGalleryIntent = new Intent();

        pickFromGalleryIntent.setType("image/*");
        pickFromGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        this.startActivityForResult(Intent.createChooser(pickFromGalleryIntent, "Välj bild"), REQUEST_PICK_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            picture = getResizedBitmapFromPath(mCurrentPhotoPath, 150, 150);
            picture = cropBitmapToSquare(picture);
            chosenPhoto.setImageBitmap(picture);

            rotateLeftButton.setClickable(true);
            rotateRightButton.setClickable(true);
            mirrorButton.setClickable(true);
            photoFile = null;
            mCurrentPhotoPath = null;

        } else if (requestCode == REQUEST_PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            try {
                Bitmap pickedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                picture = getResizedBitmap(pickedImage, 300, 300);
                picture = cropBitmapToSquare(picture);
                chosenPhoto.setImageBitmap(picture);

                rotateLeftButton.setClickable(true);
                rotateRightButton.setClickable(true);
                mirrorButton.setClickable(true);
                photoFile = null;
                mCurrentPhotoPath = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "VIKING_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int maxHeight, int maxWidth) {
        Bitmap resizedBitmap;

        float originalWidth = bitmap.getWidth();
        float originalHeight = bitmap.getHeight();
        float aspectRatio = originalWidth / originalHeight;

        int newWidth = 0;
        int newHeight = 0;
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            if (originalWidth > originalHeight) {
                newWidth = maxWidth;
                newHeight = Math.round(newWidth / aspectRatio);
            } else {
                newHeight = maxHeight;
                newWidth = Math.round(newHeight * aspectRatio);
            }
        }

        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }

    private Bitmap getResizedBitmapFromPath(String filePath, int maxWidth, int maxHeight) {
        Bitmap resizedBitmap;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoWidth = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoWidth / maxWidth, photoHeight / maxHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        resizedBitmap = BitmapFactory.decodeFile(filePath, bmOptions);
        return resizedBitmap;
    }

    public static Bitmap cropBitmapToSquare(Bitmap bitmap){
        int originalWidth  = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int cropWidth = (originalWidth - originalHeight) / 2;
        int cropHeight = (originalHeight - originalWidth) / 2;
        int newWidth = 0;
        int newHeight = 0;

        if (originalHeight > originalWidth) {
            newWidth = originalWidth;
        } else {
            newWidth = originalHeight;
        }

        if (originalHeight > originalWidth) {
            newHeight = originalHeight - ( originalHeight - originalWidth);
        } else {
            newHeight = originalHeight;
        }

        if (cropWidth < 0) {
            cropWidth = 0;
        }

        if (cropHeight < 0) {
            cropHeight = 0;
        }

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, cropWidth, cropHeight, newWidth, newHeight);

        return croppedBitmap;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int direction) {
        Matrix matrix = new Matrix();

        if (direction == ROTATE_LEFT) {
            matrix.postRotate(-90);
        } else if (direction == ROTATE_RIGHT) {
            matrix.postRotate(90);
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }

    private Bitmap mirrorBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);

        Bitmap mirroredBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return mirroredBitmap;
    }
}
