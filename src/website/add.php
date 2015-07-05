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

if (isset($_POST['submit'])) {
  $n = $conn->real_escape_string($_POST["reserve_name"]);
  $u = $conn->real_escape_string($_POST["user_name"]);
  $p = $conn->real_escape_string($_POST["phone"]);
  $e = $conn->real_escape_string($_POST["email"]);
  $g = $conn->real_escape_string($_POST["gridref"]);
  $d = $conn->real_escape_string($_POST["desc"]);
  $sql1 = "INSERT INTO Reserves (
    Reserves.name_reserve, Reserves.name_user, Reserves.phone_number, Reserves.email, Reserves.grid_reference, Reserves.description
    ) values ('$n', '$u','$p','$e','$g','$d')";

  if ($conn->query($sql1)) {
    echo '<p>Reserve added successfully.</p>
      <p>Redirecting you back to the reserves list...</p>
      <script>setTimeout(function() {window.location.href = "index.php";}, 2000);</script>';
  } else {
    echo '<p>Error: ' . $conn->error . '</p>';
  }
} else {
  if ($_SESSION['admin_loggedin']):
?>
<br />
<form method="post" action="add.php" id="addreserve">
  <p><strong>Reserve details</strong></p>
  <p>
    <label for="reserve_name">Reserve name</label>
    <input type="text" id="reserve_name" name="reserve_name" required>
  </p>

  <p>
    <label for="gridref">OS grid reference</label>
    <input type="text" id="gridref" name="gridref" pattern="[a-zA-Z]{2}([0-9]{2})+" title="Enter a valid OS grid reference." required>
  </p>

  <p>
    <label for="desc">Description</label>
    <input type="text" id="desc" name="desc" required>
  </p>

  <p><strong>Recorder's details</strong></p>
  <p>
    <label for="user_name">Name</label>
    <input type="text" id="user_name" name="user_name" required>
  </p>

  <p>
    <label for="phone">Phone number</label>
    <input type="tel" pattern="[0-9]{11}" title="Please enter a valid 11-digit phone number" id="phone" name="phone" required>
  </p>

  <p>
    <label for="email">Email</label>
    <input type="email" id="email" name="email" required>
  </p>

  <p class="submitwrapper">
    <input type="submit" name="submit" value="Add reserve">
  </p>
</form>
<?php
  else:
    echo '<p>You do not have access to this page. Please log in if you are admin.</p>';
  endif;
}

require_once 'footer.php'
?>