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
$response ["tag"] = "storePosition";

// Check if all necessary fields have been set.
if (isset($_POST['sessionID']) && $_POST['sessionID'] != '' && 
		isset($_POST['datetime']) && $_POST['datetime'] != '' && 
        isset($_POST['latitude']) && $_POST['latitude'] != '' &&
		isset($_POST['longitude']) && $_POST['longitude'] != '') {
	
	$postCopy = array();
	
	// Remove potential quotation marks from all received strings.
	foreach($_POST as $key => $value) {
        $postCopy[$key] = str_replace("\"", "", $value);
	}
	
	// Store position in database, returns true if successful and false if not.
	$storePositionResult = $databaseHandler->storePosition ($postCopy['sessionID'], 
															$postCopy['datetime'], 
                                                            $postCopy['latitude'], 
															$postCopy['longitude']);
	
	if ($storePositionResult) {
		$response["error"] = false;
		$response["message"] = "Success: GPS position was stored.";
	} else {
		$response["error"] = true;
		$response["message"] = "Error: GPS position could not be stored.";
		$logger->write("store_position.php: ERROR! GPS position could not be stored session ID: " . $postCopy['sessionID'] . ".");
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