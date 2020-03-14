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
$response ["tag"] = "syncLocalDatabase";

// Check if all necessary fields have been set.
if (isset($_POST['userID']) && $_POST['userID'] > 0) {

	$postCopy = array();
	
	// Remove potential quotation marks from all received strings.
	$postCopy['userID'] = str_replace("\"", "", $_POST['userID']);
	
	$databaseResponse = $databaseHandler->getUserTables($postCopy['userID']);
	
	if ($databaseResponse) {
		// If the rows were fetched successfully, return them.
		$response["error"] = false;
		$response["message"] = "Success: Rows received from database.";
		$response["userID"] = $postCopy['userID'];
		$response["listeningSessionsRows"] = $databaseResponse["listeningSessionsRows"];
		$response["recordingsRows"] = $databaseResponse["recordingsRows"];
		$response["positionsRows"] = $databaseResponse["positionsRows"];
		
	} else {
		// If the rows could not be fetched, respond with an error.
		$response["error"] = false;
		$response["message"] = "Success: No rows found in database for user ID.";
		$response["userID"] = $postCopy['userID'];
		$response["listeningSessionsRows"] = array();
		$response["recordingsRows"] = array();
		$response["positionsRows"] = array();
	}
	
	// Encode and return the response.
	echo json_encode ($response);
	
} else {
	// If a variable is not set or is missing, respond with an error.
	$response["error"] = true;
	$response["message"] = "Error: Necessary POST variables are missing, null or empty.";
	$logger->write("store_position.php: ERROR! Necessary POST variables were missing, null or empty in a request.");
	
	// Encode and return the response.
	echo json_encode($response);
}
?>