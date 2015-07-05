<?php
/*
 * Add reserve to database from POST HTTP request
 */

$servername = "db.dcs.aber.ac.uk";
$username = "admjof26";
$password = "sCcb6Kbw";

// connect to database
$conn = new mysqli($servername, $username, $password);
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
} 
$conn->select_db("csgp09_14_15");

if (isset($_POST['reserve_name'])) {
	$reserve_name = $conn->real_escape_string($_POST['reserve_name']);
	$user_name = $conn->real_escape_string($_POST['user_name']);
	$phone = $conn->real_escape_string($_POST['phone_number']);
	$email = $conn->real_escape_string($_POST['email']);
	$gridref = $conn->real_escape_string($_POST['grid_reference']);
	$desc = $conn->real_escape_string($_POST['description']);

	$sql = "INSERT INTO Reserves (
		Reserves.name_reserve, Reserves.name_user, Reserves.phone_number, Reserves.email, Reserves.grid_reference, Reserves.description
		) values ('$reserve_name', '$user_name', '$phone', '$email', '$gridref', '$desc')";
}

if ($conn->query($sql)) {
	echo $conn->insert_id;
}

?>