<?php
require_once 'header.php';

$servername = "db.dcs.aber.ac.uk";
$username = "admjof26";
$password = "sCcb6Kbw";

// connect to database
$conn = new mysqli($servername, $username, $password);
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
} 
$conn->select_db("csgp09_14_15");

if (isset($_POST['reserveid'])) {
	$sql1 ="DELETE FROM Reserves WHERE Reserves.Id=".$_POST['reserveid']."";
	if ($conn->query($sql1)) {
		echo '<p>Reserve deleted successfully.</p>
			<p>Redirecting you back to the reserves list...</p>
			<script>setTimeout(function() {window.location.href = "index.php";}, 2000);</script>';
	} else {
		echo '<p>Error: ' . $conn->error . '</p>';
	}
} else {
	echo '<p>Please select a reserve to delete from the <a href="index.php">reserve list</a>.</p>';
}

require_once 'footer.php'
?>