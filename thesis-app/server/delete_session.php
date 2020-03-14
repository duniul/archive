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
$response ["tag"] = "deleteSession";

// Check if all necessary fields have been set.
if (isset($_POST['sessionID']) && $_POST['sessionID'] != '') {
	
	$postCopy = array();
	
	// Remove potential quotation marks from all received strings.
	$postCopy['sessionID'] = str_replace("\"", "", $_POST['sessionID']);

	
	// Check if the user has signed in before. If not, store the user in the database
    $result = $databaseHandler->deleteSession($postCopy['sessionID']);
    if ($result) {
    		// If the recording was deleted successfully, respond with a message.
	    	$response["error"] = false;
	    	$response["message"] = "Session was successfully deleted.";
    } else {
    	// If the recording couldn't be deleted, respond with an error.
    	$response["error"] = true;
    	$response["message"] = "Error: Failed to delete session " . $postCopy['sessionID'] . " .";
    	$logger->write("delete_session.php: ERROR! Failed to delete session " . $postCopy['sessionID'] . " .");
    }
	
	// Encode and return the response.
	echo json_encode ($response);
	
} else {
	// If a variable is not set or is missing, respond with an error.
	$response["error"] = true;
	$response["message"] = "Error: Necessary POST variables are missing, null or empty.";
	$logger->write("delete_session.php: ERROR! Necessary POST variables were missing, null or empty in a request.");
	
	// Encode and return the response.
	echo json_encode ($response);
}
?>