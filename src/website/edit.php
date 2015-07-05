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
  $sql1 ="UPDATE Reserves SET Reserves.name_reserve='" . $n . "',
    Reserves.name_user='" . $u . "',
    Reserves.phone_number='" . $p . "',
    Reserves.email='".$e . "',
    Reserves.grid_reference='" . $g . "',
    Reserves.description='" . $d . "' 
    WHERE Reserves.Id=" . $_POST['reserveid'];
  if ($conn->query($sql1)) {
    echo '<p>Reserve updated successfully.</p>
      <p>Redirecting you back to the reserves list...</p>
      <script>setTimeout(function() {window.location.href = "index.php";}, 2000);</script>';
  } else {
    echo '<p>Error: ' . $conn->error . '</p>';
  }
} elseif (isset($_POST['reserveid'])) {
  if ($_SESSION['admin_loggedin']):
    $sql = "SELECT * FROM Reserves WHERE Reserves.Id=".$_POST['reserveid']."";
    $result = $conn->query($sql);
    $row = $result->fetch_assoc();
?>
<form method="post" action="edit.php" id="editreserve">
  <p><strong>Reserve details</strong></p>
  <p>
    <label for="reserve_name">Reserve name</label>
    <input type="text" id="reserve_name" name="reserve_name" value="<?php echo $row['name_reserve'] ?>" required>
  </p>

  <p>
    <label for="gridref">OS grid reference</label>
    <input type="text" id="gridref" name="gridref" value="<?php echo $row['grid_reference'] ?>" pattern="[a-zA-Z]{2}([0-9]{2})+" title="Enter a valid OS grid reference." required>
  </p>

  <p>
    <label for="desc">Description</label>
    <input type="text" id="desc" name="desc" value="<?php echo $row['description'] ?>" required>
  </p>

  <p><strong>Recorder's details</strong></p>
  <p>
    <label for="user_name">Name</label>
    <input type="text" id="user_name" name="user_name" value="<?php echo $row['name_user'] ?>" required>
  </p>

  <p>
    <label for="phone">Phone number</label>
    <input type="tel" pattern="[0-9]{11}" title="Please enter a valid 11-digit phone number" id="phone" name="phone" value="<?php echo $row['phone_number'] ?>" required>
  </p>

  <p>
    <label for="email">Email</label>
    <input type="email" id="email" name="email" value="<?php echo $row['email'] ?>" required>
  </p>

  <input type="hidden" name="reserveid" value="<?php echo $_POST['reserveid'] ?>">

  <p class="submitwrapper">
    <input type="submit" name="submit" value="Edit reserve">
  </p>
</form>
<?php
  else:
    echo '<p>You do not have access to this page. Please log in if you are admin.</p>';
  endif;
} else {
  echo '<p>Please select a reserve to edit from the <a href="index.php">reserve list</a>.</p>';
}

require_once 'footer.php'
?>