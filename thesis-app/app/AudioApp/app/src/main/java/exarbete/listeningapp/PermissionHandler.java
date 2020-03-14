package exarbete.listeningapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * This class contains helper methods used when checking and/or requesting permissions at runtime.
 */
public class PermissionHandler {


    /**
     * Loops through an array of permissions and checks if the app has recieved permissions for them.
     *
     * @param activity    activity used for checkSelfPermission
     * @param permissions array of permissions to check
     * @return true if all permissions have been granted, false if one or more has been denied.
     */
    public static boolean checkPermissions(Activity activity, String[] permissions) {
        boolean allGranted = true;

        for (String permission : permissions) {
            int permissionState = ContextCompat.checkSelfPermission(activity, permission);
            if (permissionState == PackageManager.PERMISSION_DENIED) {
                allGranted = false;
            }
        }

        return allGranted;
    }

    /**
     * Verifies multiple grant results recieved from the onRequestPermissionsResult-method in
     * classes that handle permission requests.
     *
     * @param grantResults grant results from a permission request.
     * @return true if all are granted, otherwise false.
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
