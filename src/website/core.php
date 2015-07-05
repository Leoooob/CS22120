<?php
$servername = "db.dcs.aber.ac.uk";
$username = "admjof26";
$password = "sCcb6Kbw";

// Create connection
$conn = new mysqli($servername, $username, $password);

// Check connection
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
}
//select database
$conn->select_db("csgp09_14_15");
//selecting all reserve names from the database
$sql = "SELECT name_reserve FROM Reserves";
$result = $conn->query($sql);

//homepage
if (empty($_GET['reserve'])) {
?>
<h3>Viewing All Reserves</h3>
<p>Click on the name of a reserve to view all the species recorded there.</p>
<!-- displaying all the reserves -->
<form method="post" id="reserveselect" action="">
  <table class="t1">
    <thead>
      <tr>
        <th>Name of reserve</th>
        <th>OS grid reference</th>
        <th>Description</th>
<?php if ($_SESSION['admin_loggedin']) : ?>
        <th>Select</th>
<?php endif; ?>
      </tr>
    </thead>
    <tbody>
<?php
    $sql = "SELECT * FROM Reserves";
    $result = $conn->query($sql);
    if ($result->num_rows > 0) {
      while($row = $result->fetch_assoc()) {
          echo '
            <tr>
              <td>
                <label for="radio_' . $row["Id"] . '">
                  <span><a href="index.php?reserve=' . $row["Id"] . '">' . $row["name_reserve"] . '</a></span>
                </label>
              </td>
              <td>
                <label for="radio_' . $row["Id"] . '"><span>' . $row["grid_reference"].'</span></label>
              </td>
              <td>
                <label for="radio_' . $row["Id"] . '"><span>' . $row["description"].'</span></label>
              </td>';
          if ($_SESSION['admin_loggedin']) {
            echo '<td><input type="radio" id="radio_' . $row["Id"] . '" class="reserveradio" name="reserveid" value="' . $row["Id"] . '" /></td>';
          }
          echo '</tr>';
      }
    } else {
        echo "0 results";
    }
?>
    </tbody>
  </table>
<?php if ($_SESSION['admin_loggedin']) : ?>
  <div class="submitwrapper">
    <input type="submit" name="addreserve" value="Add reserve" onclick="add()">
    <input type="submit" name="editreserve" value="Edit reserve" onclick="return edit()">
    <input type="submit" name="deletereserve" value="Delete reserve" onclick="return del()">
  </div>
<?php endif; ?>
</form>
<script>
  //script submits the form depending on which button was clicked (add/edit/delete)
  form=document.getElementById("reserveselect");
  function add() {
    form.action="add.php";
    form.submit();
  }
  function edit() {
    if ($('.reserveradio').is(':checked')) {
      form.action = "edit.php";
      return true;
    } else {
      alert('Please select a reserve to edit.');
      return false;
    }
  } 
  function del() {
    if ($('.reserveradio').is(':checked')) {
      if (window.confirm("Are you sure you want to delete this reserve?")) {
        form.action="delete.php";
        return true;
      }
    } else {
      alert('Please select a reserve to delete.');
      return false;
    }
  }
</script>

<?php
} elseif (isset($_GET['reserve'])) {
  if (empty($_GET['id'])) { //when user clicks on a reserve from reserve list
    $sql = "SELECT * FROM Reserves";
    $GLOBALS['reserve']=$_GET['reserve'];
    $result = $conn->query($sql);
    if ($result->num_rows > 0) {
      while ($row = $result->fetch_assoc()) {
        if ($_GET['reserve'] == $row['Id']){
?>
  <div id="desc">
    <h3>Viewing - <?php echo $row['name_reserve'] ?></h3>
    <p>Click on the name of a species to view its details.</p>
  </div>
<?php
        }
      }
    } else {
      echo "0 results";
    }
?>
  <!-- sort species by latin name or date -->
  <form name="myform" method="post">
    Sort by:
    <select name="order" onchange="this.form.submit()">
      <option value="latin" <?php  //makes this option selected if user selected it previously
      if (isset($_POST['order'])) {
        if ($_POST['order'] == "latin") {
          echo "selected";
        }
      }
      ?>>Latin name</option>
      <option value="asc" <?php
      if (isset($_POST['order'])) {
        if ($_POST['order'] == "asc") {
          echo "selected";
        }
      }
      ?>>Date - earliest first</option>
      <option value="desc" <?php
      if (isset($_POST['order'])) {
        if ($_POST['order'] == "desc") {
          echo "selected";
        }
      }
      ?>>Date - latest first</option>
    </select>
  </form> 
  <table class="t1">
    <thead>
      <tr>
        <th>Latin name</th>
        <th>Common name</th>
        <th>Date &amp; time recorded</th>
      </tr>
    </thead>
      <input type="hidden" name="reserveid" value="<?php echo $_GET["reserve"]; ?>">
<?php
  //queries, sorts species either by latin name or date
  $sql = "SELECT * FROM Species INNER JOIN Species_Recordings ON Species_Recordings.species_id=Species.id JOIN Reserves ON 
    Species_Recordings.reserve_id=Reserves.id ORDER BY name_species";
  $sql1 = "SELECT * FROM Species INNER JOIN Species_Recordings ON Species_Recordings.species_id=Species.id JOIN Reserves ON 
    Species_Recordings.reserve_id=Reserves.id ORDER BY Species_Recordings.date ASC";
  $sql2 = "SELECT * FROM Species INNER JOIN Species_Recordings ON Species_Recordings.species_id=Species.id JOIN Reserves ON 
    Species_Recordings.reserve_id=Reserves.id ORDER BY Species_Recordings.date DESC";   
  if(isset($_POST['order'])) {
    if ($_POST['order'] == 'asc'){
     	$sql=$sql1;
    } else if ($_POST['order'] == 'desc'){
     	$sql=$sql2;
    }
  }
  $result = $conn->query($sql);
  if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
      if ($row["reserve_id"] == $_GET["reserve"]) {
        echo '
        <tr>
          <td><a href="index.php?reserve=' . $row["reserve_id"] . '&id=' . $row['species_id'] . '">' . $row["name_species"] . '</td>
          <td>' . $row["name_common"] . '</td>
          <td>' . $row['date'] . '
		 </td>';
        echo '</tr>';
      }
    }
  } else {
    echo "0 results";
  }
?>
  </table>
  <p><a href="index.php" class="backlink">&larr; Back to reserves list</a></p>
  
<?php
  } elseif (isset($_GET['id'])) { //when user clicks on a species from a reserve page
    $sql = "SELECT * FROM Species INNER JOIN Species_Recordings ON Species_Recordings.species_id=Species.id JOIN Reserves ON 
      Species_Recordings.reserve_id=Reserves.id ORDER BY name_species";
    $result = $conn->query($sql);
    if ($result->num_rows > 0) {
      while($row = $result->fetch_assoc()) {
        if (($row["reserve_id"] == $_GET["reserve"]) && ($row["species_id"] == $_GET['id'])) { 
?>
  <h3>Viewing Record - "<?php echo $row['name_species']; ?>"</h3>
  <p id="speciesdetails">
    <span class="detaillabel">Latin name:</span><br />
    <span class="detail"><?php echo $row['name_species']; ?></span><br />
    <span class="detaillabel">Common name:</span><br />
    <span class="detail"><?php echo $row['name_common']; ?></span><br />
    <span class="detaillabel">Authority:</span><br />
    <span class="detail"><?php echo $row['authority']; ?></span><br />
    <span class="detaillabel">Date & time recorded:</span><br />
    <span class="detail"><?php echo $row['date']; ?></span><br />
    <span class="detaillabel">Location:</span><br />
    <span class="detail">
      <a href="http://maps.google.com/?q=<?php echo $row['location_lat'] . "," . $row['location_lon']; ?>" target="_blank">
        <?php echo $row['location_lat'] . ", " . $row['location_lon']; ?>
      </a>
    </span><br />
    <span class="detaillabel">Abundance:</span><br />
    <span class="detail"><?php echo $row['abundance']; ?></span><br />
    <span class="detaillabel">Recorded by:</span><br />
    <span class="detail"><?php echo $row['name_user']; ?></span><br />
  <?php if (isset($row['description'])): ?>
    <span class="detaillabel">Description:</span><br />
    <span class="detail"><?php echo $row['description']; ?></span><br />
  <?php endif; ?>
  </p>
  <div id="speciesphotos">
    <?php if (isset($row['specimen_photo_url'])): ?>
    <p>
      <span class="photolabel">Specimen photo:</span><br />
      <a href="<?php echo $row['specimen_photo_url']; ?>">
        <img src="<?php echo $row['specimen_photo_url']; ?>" alt="Specimen photo">
      </a>
    </p>
    <?php endif; ?>
    <?php if (isset($row['general_photo_url'])): ?>
    <p>
      <span class="photolabel">General scene photo:</span><br />
      <a href="<?php echo $row['general_photo_url']; ?>">
        <img src="<?php echo $row['general_photo_url']; ?>" alt="Photo of the general scene at species location">
      </a>
    </p>
    <?php endif; ?>
  </div>
  <div class="clear"></div>
  <a href="index.php?reserve=<?php echo $_GET['reserve']; ?>" class="backlink">&larr; Back to reserve</a>
<?php
        }
      }
    } else {
      echo "0 results";
    }
  } else {
    echo "Page not found";
  }
}
?>