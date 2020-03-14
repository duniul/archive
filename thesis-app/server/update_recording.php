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
$response ["tag"] = "updateRecording";

// Check if all necessary fields have been set.
if (isset($_POST['recordingID']) && $_POST['recordingID'] != '' && 
		isset($_POST['newFilename']) && $_POST['newFilename'] != '' &&
		isset($_POST['lastEdited']) && $_POST['lastEdited'] != '') {
	
	$postCopy = array();
	
	// Remove potential quotation marks from all received strings.
	foreach($_POST as $key => $value) {
        $postCopy[$key] = $value;
	}
	
	// Try to update the listening session in the database.
	$updateResult = $databaseHandler->updateRecording($postCopy['recordingID'], 
											   	      $postCopy['newFilename'],
													  $postCopy['lastEdited']);
	
	
	if ($updateResult) {
		// If the recording was updated successfully, respond with the user ID.
    	$response["error"] = false;
        $response["message"] = "Success: Recording has been updated.";
    } else {
    	// If the recording couldn't be updated, respond with an error.
        $response["error"] = true;
        $response["message"] = "Error: Recording could not be updated.";
        $logger->write("update_recording.php: ERROR! Recording could not be updated.");
    }
	
	// Encode and return the response.
	echo json_encode($response);
	
} else {
	// If a variable is not set or is missing, respond with an error.
	$response["error"] = true;
	$response["message"] = "Error: Necessary POST variables are missing, null or empty.";
	$logger->write("store_position.php: ERROR! Necessary POST variables were missing, null or empty in a request.");
	
	// Encode and return the response.
	echo json_encode($response);
}
?>