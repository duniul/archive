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
$response ["tag"] = "updateListeningSession";

// Check if all necessary fields have been set.
if (isset($_POST['sessionID']) && $_POST['sessionID'] != '' && 
		isset($_POST['endTime']) && $_POST['endTime'] != '') {
	
	$postCopy = array();
	
	// Remove potential quotation marks from all received strings.
	foreach($_POST as $key => $value) {
        $postCopy[$key] = $value;
	}
	
	// Try to update the listening session in the database.
	$updateResult = $databaseHandler->updateListeningSession($postCopy['sessionID'], 
														   	 $postCopy['endTime']);
	
	
	if ($updateResult) {
		// If the listening session was stored successfully, respond with the user ID.
    	$response["error"] = false;
        $response["message"] = "Success: Listening session has been updated.";
    } else {
    	// If the listening session couldn't be stored, respond with an error.
        $response["error"] = true;
        $response["message"] = "Error: Listening session could not be updated.";
        $logger->write("update_listening_session.php: ERROR! Listening session could not be updated.");
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