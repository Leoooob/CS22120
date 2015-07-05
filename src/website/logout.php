<?php
session_save_path("../../tmp");
session_start();
session_destroy();
header("location: index.php");
?>