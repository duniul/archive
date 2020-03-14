<?php
header ('Content-Type: application/json; charset=utf-8', true);

// This file receives POST requests to store user GPS positions
// to the server.

// Include logger to enable logging.
require_once ('include/Logger.php');
$logger = new Logger();

// Include a database handler to use necessary functions.
require_once 'include/DatabaseHandler.php';
$databaseHandler = new DatabaseHandler();

// Initialize reponse array.
$response = array();
$response ["tag"] = "uploadRecording";

// Check if all necessary fields have been set.
if (isset($_POST['userID']) && $_POST['userID'] != '' && 
		isset($_POST['sessionID']) && $_POST['sessionID'] != '' && 
		isset($_POST['recordingID']) && $_POST['recordingID'] != '' && 
		isset($_POST['filename']) && $_POST['filename'] != '' && 
		isset($_POST['userFilename']) && $_POST['userFilename'] != '' && 
		isset($_POST['recordingStartDate']) && $_POST['recordingStartDate'] != '' && 
		isset($_POST['recordingEndDate']) && $_POST['recordingEndDate'] != '' && 
		isset($_POST['duration']) && $_POST['duration'] != '' && 
		isset($_POST['lastEdited']) && $_POST['lastEdited'] != '') {
            
    $postCopy = array();
	
	// Remove potential quotation marks from all received strings.
	foreach($_POST as $key => $value) {
        $postCopy[$key] = str_replace("\"", "", $value);
	}
	
	// Set the path for the directory the recording will be stored in.
	$uploaddir = "/a/oberon-home1/h11/baak5928/public_html/audio_app_api/recordings/" . $postCopy['userID'] . "/";
	
	// If the user directory doesn't exist, create it.
	if (!is_dir($uploaddir)) {
		mkdir($uploaddir);
	}
	
	// Set the internal server path and the public URL for the uploaded recording.
	$filePath = $uploaddir . $postCopy['filename'];
	$fileURL = "https://people.dsv.su.se/~baak5928/audio_app_api/recordings/" . $postCopy['userID'] . "/" . $postCopy['filename'];
	
	if (move_uploaded_file($_FILES['file']['tmp_name'], $filePath)) {
		// If the file was successfully uploaded and moved to the correct directory, store its info in the database.
		$storeInfoResult = $databaseHandler->storeRecordingInfo($postCopy['sessionID'], 
																$postCopy['recordingID'], 
																$fileURL, 
																$postCopy['userFilename'], 
																$postCopy['recordingStartDate'], 
																$postCopy['recordingEndDate'], 
																$postCopy['duration'], 
																$postCopy['lastEdited']);
		
		if ($storeInfoResult) {
			// If the info was stored successfully, respond with any values that may be needed for the local SQLite database.
			$response["error"] = false;
			$response["message"] = "Success! The recording was uploaded.";
			$response["recordingID"] = $storeInfoResult["recordingID"];
			$response["url"] = $storeInfoResult["url"];
			$response["uploadDate"] = $storeInfoResult["uploadDate"];
			
		} else {
			// If the info couldn't be stored, respond with an error.
			$response["error"] = true;
			$response["message"] = "Error! Recording was uploaded but but its information could not be stored in the database.";
			$response["recordingID"] = 0;
			$response["url"] =  "";
			$response["uploadDate"] = "";
			$logger->write("upload_recording.php: ERROR! Recording was uploaded but its information could not be stored in the database.");
		}
	
	} else {
		// If the file could not be uploaded, respond with an error.
		$response["error"] = true;
		$response["message"] = "Error! Recording could not be uploaded.";
		$response["recordingID"] = 0;
		$response["url"] =  "";
		$response["uploadDate"] = "";
		$logger->write("upload_recording.php: ERROR! Recording could not be uploaded. print_r of files: ");
		$logger->write(print_r($_FILES, true));
	}
	
	// Encode and return the response.
	echo json_encode ($response);
	
} else {
	// If a variable is not set or is missing, respond with an error.
	$response["error"] = true;
	$response["message"] = "Error: Necessary POST variables are missing, null or empty.";
	$logger->write("store_position.php: ERROR! Necessary POST variables were missing, null or empty in a request.");
	
	// Encode and return the response.
	echo json_encode ($response);
}
?>