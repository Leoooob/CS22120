<?php
session_save_path('../../tmp');
session_start();
$admin_password = "g9admin";
$wrong_password = false;

if (isset($_POST['login-submit'])) {
  if (strcmp($_POST['admin-pw'], $admin_password) == 0) {
    $_SESSION['admin_loggedin'] = true;
  } else {
    $wrong_password = true;
  }
}
?>
<!DOCTYPE html>
  <html>
    <head>
      <meta charset="utf-8">
      <title>RPSRview</title>
      <!-- Meta data. Holds the description and stuff -->
      <meta name="description" content="CS22100 Assignment - RPSRView">
      <meta name="author" content="John Friend (jof26), Punit Shah (pus1), Kamil Lewinski (kal27)">
      <!-- Orginal CSS made by boot strap. -->
      <link rel="stylesheet" href="style.css">
      <!-- Any small edits made -->
      <link rel="stylesheet" href="stylemore.css">
      <link rel="icon" href="favicon.ico">
    </head>

  <body>
    <header>
    <!-- Fixed navbar -->
      <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
        <?php if ($wrong_password): ?>
        <div class="wronglogin">
          <span>Incorrect admin password entered.</span>
        </div>
        <?php endif; ?>
        <div class="container">
          <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
              <span class="sr-only">Toggle navigation</span>
              <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="./index.php">RPSRview</a>
            <div id="navbar" class="collapse navbar-collapse">
              <ul class="nav navbar-nav">
                <li><a href="./index.php">Home</a></li>
                <li><a href="./about.php">About</a></li>
                <li id="admin">
                  <?php if (!$_SESSION['admin_loggedin']): ?>
                  <a id="login-trigger" href="#">
                    Admin login
                  </a>
                  <form id="loginform" action="index.php" method="post">
                    <input type="password" id="admin-pw" name="admin-pw" placeholder="Admin password">
                    <input type="submit" id="login-submit" name="login-submit" value="Log in">
                  </form>
                  <?php else: ?>
                  <a id="logout" href="logout.php">
                    Logout admin
                  </a>
                  <?php endif; ?>
                </li>
              </ul>
            </div><!--/#navbar-->
          </div><!--/.navbar-header-->
        </div>
      </nav>
    </header>

    <main class="row">